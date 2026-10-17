package mainframe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Conexión con el mainframe a través de ws3270, lanzado como proceso hijo.
 *
 * Las órdenes se escriben en la entrada estándar de ws3270 y la respuesta se
 * lee de su salida estándar: líneas "data: ..." con el contenido, una línea
 * de estado y, al final, "ok" o "error".
 *
 * Toda desconexión pasa por {@link #desconectar()}, que garantiza el tiempo
 * mínimo conectado que exige el enunciado (nota i).
 */
public class MainframeClient implements AutoCloseable {

    /** Propiedad de sistema para indicar otra ruta: -Dws3270.ruta=... */
    public static final String PROPIEDAD_RUTA = "ws3270.ruta";

    private static final String RUTA_WINDOWS = "C:\\Program Files\\wc3270\\ws3270.exe";
    private static final String RUTA_WSL = "/mnt/c/Program Files/wc3270/ws3270.exe";

    /** El enunciado exige al menos 1 s entre conexión y desconexión; se deja margen. */
    private static final long MIN_CONEXION_MS = 1500;

    /** Tiempo máximo de espera de la respuesta a una orden. */
    private static final long MAX_RESPUESTA_MS = 40_000;

    /** Tiempo máximo por orden durante el cierre, para no alargarlo si ws3270 no responde. */
    private static final long MAX_RESPUESTA_CIERRE_MS = 5_000;

    /** Pausa entre dos lecturas de pantalla mientras se espera a que se estabilice. */
    private static final long PAUSA_LECTURA_MS = 400;

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final String rutaWs3270;

    private Process proceso;
    private BufferedWriter entrada;

    /**
     * Líneas que va escribiendo ws3270, leídas por un hilo aparte. Así la
     * espera de una respuesta puede tener tiempo máximo (leer directamente
     * del proceso bloquea sin límite). Un valor vacío indica fin de la salida.
     */
    private final BlockingQueue<Optional<String>> salida = new LinkedBlockingQueue<>();

    /** Instante de la conexión, en nanosegundos; 0 si no se ha conectado. */
    private long instanteConexion = 0;

    /** Cierre de emergencia si el programa termina sin desconectar (Ctrl+C, error no capturado). */
    private Thread cierreAlTerminar;

    /** Usa la ruta de la propiedad {@value #PROPIEDAD_RUTA} o, si no está, la de la instalación de wc3270. */
    public MainframeClient() {
        this(rutaPorDefecto());
    }

    public MainframeClient(String rutaWs3270) {
        this.rutaWs3270 = rutaWs3270;
    }

    private static String rutaPorDefecto() {
        String ruta = System.getProperty(PROPIEDAD_RUTA);
        if (ruta != null && !ruta.isBlank()) {
            return ruta;
        }
        // Desde WSL, el disco C: de Windows está montado en /mnt/c.
        return new File(RUTA_WSL).isFile() ? RUTA_WSL : RUTA_WINDOWS;
    }

    // ------------------------------------------------------------------
    // Conexión y desconexión
    // ------------------------------------------------------------------

    /**
     * Arranca ws3270, conecta y espera la primera pantalla.
     *
     * @return el texto de la primera pantalla
     * @throws IOException si no se puede conectar o el mainframe está bloqueado (pantalla de Hercules)
     */
    public synchronized String conectar(String host, int puerto) throws IOException {
        if (proceso != null) {
            throw new IOException("Ya hay una conexión abierta.");
        }

        // -model 4: pantalla de 43x80, la misma del mapa de pantallas.
        // -localcp 65001: ws3270 lee y escribe en UTF-8, igual que este programa.
        ProcessBuilder pb = new ProcessBuilder(rutaWs3270, "-model", "4", "-localcp", "65001");
        pb.redirectErrorStream(true);
        proceso = pb.start();

        entrada = new BufferedWriter(
                new OutputStreamWriter(proceso.getOutputStream(), StandardCharsets.UTF_8));
        arrancarLector(new BufferedReader(
                new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8)));

        cierreAlTerminar = new Thread(this::desconectar, "cierre-ws3270");
        Runtime.getRuntime().addShutdownHook(cierreAlTerminar);

        // Se anota antes de Connect por si falla a medias, y otra vez al
        // quedar establecida, que es desde cuando cuenta el mínimo.
        instanteConexion = System.nanoTime();
        ejecutar("Connect(" + host + ":" + puerto + ")", MAX_RESPUESTA_MS);
        instanteConexion = System.nanoTime();
        registrar("CONECTADO a " + host + ":" + puerto);

        String pantalla = esperarPantallaEstable(20_000);
        if (esHercules(pantalla)) {
            throw new IOException("El mainframe está bloqueado (pantalla de Hercules): "
                    + "no acepta sesiones hasta que el profesor lo reinicie.");
        }
        return pantalla;
    }

    /**
     * Único punto de desconexión. Espera a que se cumpla el tiempo mínimo
     * desde la conexión, desconecta y cierra ws3270. Se puede llamar varias
     * veces y desde cualquier camino: cierre normal, error o fin del programa.
     */
    public synchronized void desconectar() {
        if (proceso == null) {
            return;
        }

        try {
            // Respuestas atrasadas de una orden anterior que agotó su tiempo.
            salida.clear();

            if (instanteConexion != 0) {
                esperarMinimoConectado();
                ejecutar("Disconnect()", MAX_RESPUESTA_CIERRE_MS);
                registrar("DESCONECTADO tras " + milisConectado() + " ms");
            }
            ejecutar("Quit()", MAX_RESPUESTA_CIERRE_MS);
        } catch (IOException e) {
            // Tras Quit ws3270 puede cerrar sin contestar; no es un fallo.
            registrar("Cierre de ws3270: " + e.getMessage());
        } finally {
            try {
                if (!proceso.waitFor(3, TimeUnit.SECONDS)) {
                    // No ha respondido; el mínimo ya se ha esperado arriba.
                    proceso.destroy();
                }
            } catch (InterruptedException e) {
                proceso.destroy();
                Thread.currentThread().interrupt();
            }
            proceso = null;
            entrada = null;
            instanteConexion = 0;
            quitarCierreAlTerminar();
        }
    }

    /** Permite usar la clase en un try-with-resources. */
    @Override
    public void close() {
        desconectar();
    }

    /** Espera, sin poder ser interrumpida, hasta cumplir el mínimo conectado. */
    private void esperarMinimoConectado() {
        boolean interrumpido = false;
        long falta;
        while ((falta = MIN_CONEXION_MS - milisConectado()) > 0) {
            try {
                Thread.sleep(falta);
            } catch (InterruptedException e) {
                interrumpido = true;
            }
        }
        if (interrumpido) {
            Thread.currentThread().interrupt();
        }
    }

    private long milisConectado() {
        return (System.nanoTime() - instanteConexion) / 1_000_000;
    }

    private void quitarCierreAlTerminar() {
        try {
            if (cierreAlTerminar != null && Thread.currentThread() != cierreAlTerminar) {
                Runtime.getRuntime().removeShutdownHook(cierreAlTerminar);
            }
        } catch (IllegalStateException e) {
            // El programa ya está terminando: no hace falta quitarlo.
        }
        cierreAlTerminar = null;
    }

    // ------------------------------------------------------------------
    // Órdenes
    // ------------------------------------------------------------------

    /**
     * Envía una orden de ws3270 y devuelve las líneas de datos de la respuesta.
     *
     * @throws IOException si ws3270 contesta "error", no contesta a tiempo o se ha cerrado
     */
    public synchronized List<String> enviar(String orden) throws IOException {
        return ejecutar(orden, MAX_RESPUESTA_MS);
    }

    /** Escribe un texto en la posición del cursor. */
    public void escribir(String texto) throws IOException {
        String escapado = texto.replace("\\", "\\\\").replace("\"", "\\\"");
        enviar("String(\"" + escapado + "\")");
    }

    /**
     * Escribe una clave en la posición del cursor. Se envía igual que
     * {@link #escribir}, pero en el registro y en los errores sale como "****".
     */
    public synchronized void escribirClave(String clave) throws IOException {
        String escapado = clave.replace("\\", "\\\\").replace("\"", "\\\"");
        ejecutar("String(\"" + escapado + "\")", "String(\"****\")", MAX_RESPUESTA_MS);
    }

    /** Pulsa Intro. */
    public void intro() throws IOException {
        enviar("Enter()");
    }

    /** Devuelve el texto de la pantalla actual, una línea por fila. */
    public String pantalla() throws IOException {
        return String.join("\n", enviar("Ascii()"));
    }

    /**
     * Espera a que el mainframe termine de dibujar la pantalla: repite la
     * lectura hasta obtener dos iguales, con texto y sin el estado "Working".
     * Las esperas propias de ws3270 (Wait) vuelven antes de tiempo.
     *
     * @return la pantalla estable
     * @throws IOException si no se estabiliza en maxMs milisegundos
     */
    public String esperarPantallaEstable(long maxMs) throws IOException {
        long limite = System.nanoTime() + maxMs * 1_000_000;
        String anterior = null;

        while (System.nanoTime() < limite) {
            String actual = pantalla();
            if (actual.equals(anterior) && !actual.isBlank() && !actual.contains("Working")) {
                return actual;
            }
            anterior = actual;
            try {
                Thread.sleep(PAUSA_LECTURA_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Espera de pantalla interrumpida.");
            }
        }
        throw new IOException("La pantalla no se ha estabilizado en " + maxMs + " ms.");
    }

    /**
     * Espera hasta que la pantalla contenga un texto determinado.
     *
     * @throws IOException si el texto no aparece dentro del tiempo indicado
     */
    public String esperarTexto(String texto, long maxMs) throws IOException {
        long limite = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(maxMs);

        while (System.nanoTime() < limite) {
            String actual = pantalla();

            if (actual.contains(texto)) {
                return actual;
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Espera interrumpida", e);
            }
        }

        throw new IOException(
                "No apareció el texto esperado: " + texto
        );
    }

    // ------------------------------------------------------------------
    // Reconocimiento de pantallas
    // ------------------------------------------------------------------

    /** Mainframe bloqueado: aparece la consola de Hercules en lugar de MUSIC/SP. */
    public static boolean esHercules(String pantalla) {
        return pantalla.contains("Hercules");
    }

    /** La sesión anterior no se cerró con "off"; hay que contestar OK. */
    public static boolean sesionEnUso(String pantalla) {
        return pantalla.contains("Userid is in use");
    }

    /** La pantalla está llena y espera un Intro para continuar ("More..." en la última fila). */
    public static boolean hayMas(String pantalla) {
        return pantalla.stripTrailing().endsWith("More...");
    }

    // ------------------------------------------------------------------
    // Protocolo con ws3270
    // ------------------------------------------------------------------

    private List<String> ejecutar(String orden, long maxMs) throws IOException {
        return ejecutar(orden, orden, maxMs);
    }

    /**
     * @param visible la orden tal como sale en el registro y en los errores
     *                (distinta de la real cuando lleva una clave)
     */
    private List<String> ejecutar(String orden, String visible, long maxMs) throws IOException {
        if (proceso == null) {
            throw new IOException("No hay conexión con el mainframe.");
        }
        registrar(">> " + visible);
        entrada.write(orden);
        entrada.newLine();
        entrada.flush();

        List<String> datos = new ArrayList<>();
        long limite = System.nanoTime() + maxMs * 1_000_000;

        while (true) {
            Optional<String> leida;
            try {
                leida = salida.poll(limite - System.nanoTime(), TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Espera de respuesta interrumpida: " + visible);
            }
            if (leida == null) {
                throw new IOException("ws3270 no ha respondido a " + visible + " en " + maxMs + " ms.");
            }
            if (leida.isEmpty()) {
                throw new IOException("ws3270 se ha cerrado inesperadamente.");
            }

            String linea = leida.get();
            if (linea.equals("ok")) {
                return datos;
            }
            if (linea.equals("error")) {
                throw new IOException("ws3270 rechazó " + visible + ": " + String.join(" ", datos).trim());
            }
            if (linea.startsWith("data:")) {
                // Formato "data: <texto>"; el texto conserva sus espacios.
                datos.add(linea.length() > 6 ? linea.substring(6) : "");
            }
            // El resto es la línea de estado de ws3270: no se usa.
        }
    }

    /** Hilo que pasa a la cola cada línea que escribe ws3270. */
    private void arrancarLector(BufferedReader lector) {
        salida.clear();
        Thread hilo = new Thread(() -> {
            try {
                String linea;
                while ((linea = lector.readLine()) != null) {
                    salida.add(Optional.of(linea));
                }
            } catch (IOException e) {
                // Se trata igual que el fin de la salida.
            }
            salida.add(Optional.empty());
        }, "lector-ws3270");
        hilo.setDaemon(true);
        hilo.start();
    }

    /** Traza con la hora, para poder comprobar el tiempo entre conexión y desconexión. */
    private static void registrar(String mensaje) {
        System.err.println(LocalTime.now().format(HORA) + " " + mensaje);
    }
}
