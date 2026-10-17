package tareas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import mainframe.MainframeClient;
import mainframe.Pantalla;

/**
 * Operaciones de la aplicación legada: listar tareas y crear una nueva.
 * Cada operación empieza y termina en el menú principal de tareas.c.
 * Es lo único que usa la interfaz gráfica para llegar a los datos.
 */
public class GestorTareas {

    private static final long ESPERA_MS = 10_000;

    private final MainframeClient cliente;
    private final LectorListado lector;

    public GestorTareas(MainframeClient cliente) {
        this.cliente = cliente;
        this.lector = new LectorListado();
    }

    /**
     * Tareas generales y específicas, ordenadas por número. Desde el menú
     * principal entra en VIEW TASKS, pide cada listado (1 y 2) recorriendo
     * todas sus páginas ("More...") hasta "TOTAL TASK" y vuelve con 3.
     *
     * @throws IOException si tareas.c no muestra la pantalla esperada a tiempo
     */
    public List<Tarea> listar() throws IOException {
        cliente.escribir("2");
        cliente.intro();
        esperarMenu(Pantalla.MENU_VIEW);

        List<Tarea> tareas = new ArrayList<>();
        tareas.addAll(listarTipo("1"));
        tareas.addAll(listarTipo("2"));

        cliente.escribir("3");
        cliente.intro();
        esperarMenu(Pantalla.MENU_PRINCIPAL);

        tareas.sort(Comparator.comparingInt(Tarea::numero));
        return tareas;
    }

    /** Desde VIEW TASKS, pide un listado (1 generales, 2 específicas) y vuelve a VIEW TASKS. */
    private List<Tarea> listarTipo(String opcion) throws IOException {
        String anterior = cliente.pantalla();
        cliente.escribir(opcion);
        cliente.intro();

        List<Tarea> tareas = new ArrayList<>();
        boolean primera = true;
        while (true) {
            String texto = esperarCambio(anterior);
            Pagina pagina = leerPagina(texto, opcion, primera, lector);
            if (pagina == null) {
                // Todavía no ha salido el eco de la opción: se vuelve a leer.
                anterior = texto;
                continue;
            }
            tareas.addAll(pagina.tareas());
            if (pagina.completa()) {
                break;
            }
            if (!MainframeClient.hayMas(texto)) {
                throw new IOException("El listado de tareas no ha terminado como se esperaba.");
            }
            anterior = texto;
            primera = false;
            cliente.intro();
        }
        esperarMenu(Pantalla.MENU_VIEW);
        return tareas;
    }

    /**
     * Lo que aporta una página del listado: sus tareas y si en ella aparece
     * "TOTAL TASK" (fin del listado).
     */
    public record Pagina(List<Tarea> tareas, boolean completa) {
    }

    /**
     * Saca de una pantalla las tareas de un listado. tareas.c escribe hacia
     * abajo sin borrar, así que en la primera página solo cuenta lo que hay
     * después del eco de la opción ("?" y debajo la opción); las páginas
     * siguientes empiezan limpias. Se lee hasta "TOTAL TASK".
     *
     * @return la página, o null si en la primera aún no se ve el eco de la opción
     */
    public static Pagina leerPagina(String pantalla, String opcion, boolean primera, LectorListado lector) {
        String[] lineas = pantalla.split("\\r?\\n");
        int inicio = 0;
        if (primera) {
            inicio = -1;
            for (int i = lineas.length - 1; i > 0; i--) {
                if (lineas[i].trim().equals(opcion) && lineas[i - 1].trim().equals("?")) {
                    inicio = i + 1;
                    break;
                }
            }
            if (inicio < 0) {
                return null;
            }
        }
        StringBuilder trozo = new StringBuilder();
        for (int i = inicio; i < lineas.length; i++) {
            if (lineas[i].trim().equals("TOTAL TASK")) {
                return new Pagina(lector.parsearPantalla(trozo.toString()), true);
            }
            trozo.append(lineas[i]).append('\n');
        }
        return new Pagina(lector.parsearPantalla(trozo.toString()), false);
    }

    /** Espera a que la pantalla sea distinta de la anterior y esté estable. */
    private String esperarCambio(String anterior) throws IOException {
        long limite = System.nanoTime() + ESPERA_MS * 1_000_000;
        while (System.nanoTime() < limite) {
            String texto = cliente.esperarPantallaEstable(ESPERA_MS);
            if (!texto.equals(anterior)) {
                return texto;
            }
        }
        throw new IOException("tareas.c no ha respondido a tiempo.");
    }

    /**
     * Espera a que tareas.c muestre un menú, pasando los "More..." que
     * salgan por el camino (en un menú no se pierde nada al pasarlos).
     */
    private void esperarMenu(Pantalla menu) throws IOException {
        long limite = System.nanoTime() + ESPERA_MS * 1_000_000;
        String texto = cliente.esperarPantallaEstable(ESPERA_MS);
        while (System.nanoTime() < limite) {
            if (MainframeClient.hayMas(texto)) {
                cliente.intro();
                // Sin esperar al cambio, la página vieja haría pulsar Intro otra vez.
                texto = esperarCambio(texto);
                continue;
            }
            if (Pantalla.identificar(texto) == menu) {
                return;
            }
            texto = cliente.esperarPantallaEstable(ESPERA_MS);
        }
        throw new IOException("tareas.c no ha vuelto al menú esperado (" + menu + ").");
    }

    /**
     * Crea una tarea: general si el nombre está vacío, específica si no.
     * Desde el menú principal entra en ASSIGN TASKS, elige el tipo, contesta
     * cada pregunta solo cuando aparece y vuelve con 3.
     *
     * @throws IOException si los datos no pasan el {@link Validador} (no se
     *                     envía nada) o tareas.c no muestra la pantalla esperada
     */
    public void nueva(String fecha, String nombre, String descripcion) throws IOException {
        String error = Validador.validar(fecha, nombre, descripcion);
        if (error != null) {
            throw new IOException(error);
        }
        boolean especifica = nombre != null && !nombre.isBlank();

        cliente.escribir("1");
        cliente.intro();
        esperarMenu(Pantalla.MENU_ASSIGN);

        cliente.escribir(especifica ? "2" : "1");
        cliente.intro();
        esperarPregunta("ENTER DATE(DDMM):");
        cliente.escribir(fecha);
        cliente.intro();
        if (especifica) {
            esperarPregunta("ENTER NAME:");
            cliente.escribir(nombre.trim());
            cliente.intro();
        }
        esperarPregunta("ENTER DESCRIPTION:");
        cliente.escribir(descripcion.trim());
        cliente.intro();

        String texto = esperarPregunta("OPTION:");
        if (!texto.contains("TASK ASSIGNED") || Pantalla.identificar(texto) != Pantalla.MENU_ASSIGN) {
            throw new IOException("tareas.c no ha confirmado el alta de la tarea.");
        }
        cliente.escribir("3");
        cliente.intro();
        esperarMenu(Pantalla.MENU_PRINCIPAL);
    }

    /**
     * Pregunta a la que espera tareas.c: la línea que hay encima del último
     * "?" de la zona de salida, si después de él no se ha escrito nada.
     *
     * @return la pregunta; "" si el "?" es lo primero de la página (la pregunta
     *         quedó en la anterior, antes del "More..."); null si no espera nada
     */
    public static String pregunta(String pantalla) {
        List<String> salida = lineasDeSalida(pantalla);
        if (salida.isEmpty() || !salida.get(salida.size() - 1).equals("?")) {
            return null;
        }
        return salida.size() == 1 ? "" : salida.get(salida.size() - 2);
    }

    /** Líneas no vacías de la zona de salida (encima de la línea "------T---"), sin espacios a los lados. */
    private static List<String> lineasDeSalida(String pantalla) {
        List<String> salida = new ArrayList<>();
        for (String linea : pantalla.split("\\r?\\n")) {
            if (linea.startsWith("------T")) {
                break;
            }
            if (!linea.isBlank()) {
                salida.add(linea.trim());
            }
        }
        return salida;
    }

    /**
     * Espera a que tareas.c haga una pregunta concreta, pasando los "More..."
     * (la pregunta puede quedar al final de una página y el "?" en la siguiente).
     *
     * @return la pantalla con la pregunta
     */
    private String esperarPregunta(String esperada) throws IOException {
        long limite = System.nanoTime() + ESPERA_MS * 1_000_000;
        String ultimaLinea = "";
        String texto = cliente.esperarPantallaEstable(ESPERA_MS);
        while (System.nanoTime() < limite) {
            if (MainframeClient.hayMas(texto)) {
                List<String> salida = lineasDeSalida(texto);
                ultimaLinea = salida.isEmpty() ? "" : salida.get(salida.size() - 1);
                cliente.intro();
                // Sin esperar al cambio, la página vieja haría pulsar Intro otra vez.
                texto = esperarCambio(texto);
                continue;
            }
            String actual = pregunta(texto);
            if ("".equals(actual)) {
                actual = ultimaLinea;
            }
            if (actual != null && actual.endsWith(esperada)) {
                return texto;
            }
            texto = cliente.esperarPantallaEstable(ESPERA_MS);
        }
        throw new IOException("tareas.c no ha preguntado " + esperada);
    }
}