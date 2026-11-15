package legado;

import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static legado.PantallaLegado.*;

/**
 * El wrapper: hace cada funcionalidad de la web (F1, F2 y F3) usando la
 * aplicación legada como lo haría una persona, pulsando teclas con Teclado y
 * leyendo la pantalla con LectorPantalla, según los recorridos de
 * docs/mapa-pantallas.md. Cada operación accede a la aplicación en ese momento:
 * no se guarda ningún dato entre operaciones (R8).
 *
 * Después de cada tecla se espera a ver la pantalla siguiente (Espera), nunca
 * un tiempo fijo, y la aplicación se deja siempre en el MENÚ (D2).
 *
 * Al empezar cada operación (D2, D5):
 * <ul>
 *   <li>se guarda la ventana que tiene el foco (normalmente el navegador) para
 *       devolvérselo al terminar, también si la operación falla;</li>
 *   <li>se pone DOSBox delante (Robot solo escribe en la ventana con el foco y la
 *       captura necesita verla);</li>
 *   <li>se comprueba que se ve el MENÚ. Si DOSBox se ha cerrado o la aplicación se
 *       ha quedado en otra pantalla (por un fallo anterior), se cierra y se lanza
 *       de nuevo. Es seguro: la aplicación solo escribe en disco con la opción 2
 *       (GRABAR), que nunca se pulsa (R5).</li>
 * </ul>
 *
 * Las operaciones son synchronized: nunca hay dos tecleando a la vez en DOSBox.
 */
public class ServicioLegado {

    /** Tras pulsar una tecla, hasta que aparece la pantalla siguiente. */
    public static final long ESPERA_PANTALLA = 5_000;
    /** Tras pedir la ordenación por ANTIGUEDAD, lo más lento de la aplicación. */
    public static final long ESPERA_ORDENAR = 10_000;
    /** Al lanzar DOSBox, hasta que se ve el MENÚ (medido: 2,3 s). */
    public static final long ESPERA_ARRANQUE = 15_000;
    /**
     * Al empezar una operación, hasta ver el MENÚ. La operación anterior ya acabó
     * esperando el MENÚ, así que debe estar en la primera captura; si no está en
     * 1 s, algo ha fallado y se reinicia DOSBox (D2; revisable a 5 s en la prueba real).
     */
    public static final long ESPERA_MENU_INICIAL = 1_000;
    /** Tope de resultados de una búsqueda: no puede haber más que registros (763, R5: no cambian). */
    public static final int MAX_RESULTADOS = 763;
    /** Tope de páginas del listado: 763 registros / 18 por página = 43. */
    public static final int MAX_PAGINAS = 43;

    private final File carpetaApp;
    private final File conf;
    private final LectorPantalla lector;
    private VentanaDosBox dosbox;
    private Teclado teclado;

    /**
     * Lanza DOSBox con la aplicación legada y espera a ver el MENÚ.
     *
     * @param carpetaApp carpeta Database-MSDOS
     * @param conf       nuestro p3-dosbox.conf
     */
    public ServicioLegado(File carpetaApp, File conf) throws Exception {
        this.carpetaApp = carpetaApp;
        this.conf = conf;
        this.lector = LectorPantalla.cargar();
        lanzarDosBox();
    }

    /** F1: número de registros, con la opción 4 (información). */
    public synchronized int obtenerTotalRegistros() throws Exception {
        return operar(() -> {
            teclado.escribir("4");
            String[] info = esperar("la información (" + INFO + ")", p -> INFO.en(p) && INFO_PIE.en(p));
            int total = ParserPantalla.extraerTotalRegistros(info);
            teclado.enter();
            esperarMenu(ESPERA_PANTALLA);
            return total;
        });
    }

    /**
     * F2: datos de los programas cuyo nombre contiene el texto (opción 7). La
     * aplicación busca por subcadena y distingue mayúsculas; se responde N a cada
     * "ES ESTE ?" para ver todas las coincidencias, en el orden de la aplicación.
     *
     * @param nombre texto a buscar, ya validado (sin comas ni comillas y escribible)
     */
    public synchronized List<Juego> buscarPorNombre(String nombre) throws Exception {
        return operar(() -> {
            List<Juego> resultados = new ArrayList<>();
            teclado.escribir("7");
            esperar(BUSCAR_SABES_NUMERO.toString(), BUSCAR_SABES_NUMERO::en);
            teclado.responder("N");
            esperar(BUSCAR_NOMBRE.toString(), BUSCAR_NOMBRE::en);
            teclado.responder(nombre);

            int ultimo = 0;
            while (true) {
                final int anterior = ultimo;
                // Un resultado nuevo (fila 7 sin responder y Nº mayor que el anterior) o
                // "NO HAY NINGUN": esta pantalla deja visible el último resultado.
                String[] p = esperar("un resultado con Nº mayor que " + anterior + " o " + BUSCAR_NO_HAY,
                        q -> BUSCAR_NO_HAY.en(q) || resultadoNuevo(q, anterior));
                if (BUSCAR_NO_HAY.en(p)) break;
                Juego juego = ParserPantalla.parsearResultadoBusqueda(p);
                resultados.add(juego);
                if (resultados.size() > MAX_RESULTADOS) {
                    throw new PantallaInesperada("como mucho " + MAX_RESULTADOS + " resultados", juego.toJson());
                }
                ultimo = Integer.parseInt(juego.getNumero());
                teclado.responder("N"); // nunca S: llevaría a "QUIERES ALTERAR SUS DATOS?" (R5)
            }
            teclado.enter(); // "PULSA ENTER"
            esperar(BUSCAR_OTRO.toString(), BUSCAR_OTRO::en);
            teclado.responder("N");
            esperarMenu(ESPERA_PANTALLA);
            return resultados;
        });
    }

    /**
     * F3: programas de una cinta en el orden en que están grabados. La aplicación
     * no guarda la posición en la cinta: se usa el orden de registro (AMB-5). Se
     * ordena por ANTIGUEDAD (opción 3), se recorre el listado completo desde el
     * primero (opción 6) y se filtra por cada parte del campo cinta (A-45 → A y 45).
     *
     * @param idCinta una letra o un número, ya validado
     */
    public synchronized List<Juego> listarPorCinta(String idCinta) throws Exception {
        return operar(() -> {
            teclado.escribir("3");
            esperar(ORDENAR.toString(), ORDENAR::en);
            teclado.responder("4"); // ANTIGUEDAD: vuelve al menú sin preguntar más
            esperarMenu(ESPERA_ORDENAR);

            teclado.escribir("6");
            esperar(LISTAR_DESDE.toString(), LISTAR_DESDE::en);
            teclado.enter(); // desde el principio

            List<Juego> todos = new ArrayList<>();
            int filasUltimaPagina = 0;
            for (int pagina = 1; ; pagina++) {
                final int primero = todos.size() + 1;
                String[] p = esperar("la página " + pagina + " del listado (empieza en el Nº " + primero + ") o el MENÚ",
                        q -> MENU.en(q) || paginaQueEmpiezaEn(q, primero));
                if (MENU.en(p)) {
                    // Solo se vuelve al MENÚ tras la última página, la única con menos de 18
                    // programas (la 43 tiene 7). Si no, se ha perdido alguna página.
                    if (pagina == 1 || filasUltimaPagina == LISTADO_FILAS_POR_PAGINA) {
                        throw new PantallaInesperada("otra página del listado tras el Nº " + todos.size(), Espera.resumen(p));
                    }
                    break;
                }
                if (pagina > MAX_PAGINAS) {
                    throw new PantallaInesperada("como mucho " + MAX_PAGINAS + " páginas", Espera.resumen(p));
                }
                List<Juego> filas = ParserPantalla.parsearTablaListado(p);
                for (int i = 0; i < filas.size(); i++) {
                    String esperado = String.valueOf(primero + i);
                    if (!filas.get(i).getNumero().equals(esperado)) {
                        throw new PantallaInesperada("el Nº " + esperado + " en la página " + pagina, filas.get(i).toJson());
                    }
                }
                todos.addAll(filas);
                filasUltimaPagina = filas.size();
                teclado.espacio(); // en la última página, vuelve al MENÚ
            }
            return ParserPantalla.filtrarPorCinta(todos, idCinta);
        });
    }

    /** Cierra DOSBox (al apagar el servidor). La aplicación no graba nada al cerrarse. */
    public synchronized void cerrar() {
        if (dosbox != null) dosbox.cerrar();
    }

    // ---- Funcionamiento común ------------------------------------------------

    /** Una operación sobre la aplicación: lo que hay que hacer, ya en el MENÚ. */
    @FunctionalInterface
    private interface Operacion<T> {
        T hacer() throws Exception;
    }

    /**
     * Prepara la aplicación (DOSBox delante y en el MENÚ, reiniciándola si hace
     * falta), hace la operación y devuelve el foco a la ventana que lo tenía.
     */
    private <T> T operar(Operacion<T> operacion) throws Exception {
        HWND anterior = VentanaDosBox.ventanaConFoco();
        try {
            avisarSiBloqMayus();
            prepararMenu();
            return operacion.hacer();
        } finally {
            if (dosbox != null) dosbox.devolverFoco(anterior);
        }
    }

    /** DOSBox delante y con el MENÚ visible; si no se consigue, DOSBox se lanza de nuevo. */
    private void prepararMenu() throws Exception {
        if (!dosbox.sigueAbierta()) {
            System.out.println("DOSBox se ha cerrado: se lanza de nuevo.");
            lanzarDosBox();
            return;
        }
        dosbox.traerAlFrente();
        try {
            esperarMenu(ESPERA_MENU_INICIAL);
        } catch (PantallaInesperada e) {
            System.out.println("La aplicación no está en el MENÚ (" + e.encontrado + "): se reinicia DOSBox.");
            dosbox.cerrar();
            lanzarDosBox();
        }
    }

    /** Lanza DOSBox con la aplicación y espera a ver el MENÚ. */
    private void lanzarDosBox() throws Exception {
        dosbox = VentanaDosBox.lanzar(carpetaApp, conf, ESPERA_ARRANQUE);
        teclado = new Teclado(dosbox);
        dosbox.traerAlFrente();
        esperarMenu(ESPERA_ARRANQUE);
    }

    private void esperarMenu(long maxMs) throws Exception {
        Espera.hasta(this::capturar, "el MENÚ (" + MENU + ")", MENU::en, maxMs);
    }

    private String[] esperar(String descripcion, Predicate<String[]> condicion) throws Exception {
        return Espera.hasta(this::capturar, descripcion, condicion, ESPERA_PANTALLA);
    }

    /** Una captura del área de cliente de DOSBox, leída con las plantillas. */
    private String[] capturar() throws Exception {
        return lector.leer(dosbox.robot().createScreenCapture(dosbox.areaCliente()));
    }

    /** Un resultado de búsqueda completo, esperando respuesta y posterior al anterior (público para PruebasOffline). */
    public static boolean resultadoNuevo(String[] pantalla, int anterior) {
        if (!esperaRespuestaEsEste(pantalla)) return false;
        try {
            return Integer.parseInt(ParserPantalla.parsearResultadoBusqueda(pantalla).getNumero()) > anterior;
        } catch (PantallaInesperada e) {
            return false;
        }
    }

    /** Una página del listado ya dibujada (cabecera y pie) cuyo primer programa es el Nº indicado (público para PruebasOffline). */
    public static boolean paginaQueEmpiezaEn(String[] pantalla, int primero) {
        return LISTADO_CABECERA.en(pantalla) && LISTADO_PIE.en(pantalla)
                && LISTADO_NUMERO.de(linea(pantalla, LISTADO_PRIMERA_FILA)).equals(String.valueOf(primero));
    }

    /**
     * Robot escribe las mayúsculas con Mayúsculas + letra; con Bloq Mayús activado
     * podrían salir al revés (D6). Por ahora solo se avisa en la consola: la prueba
     * real dirá si DOSBox se ve afectado.
     */
    private static void avisarSiBloqMayus() {
        try {
            if (Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK)) {
                System.out.println("Aviso: Bloq Mayús está activado en este equipo.");
            }
        } catch (UnsupportedOperationException e) {
            // El sistema no deja consultarlo: no se avisa.
        }
    }
}
