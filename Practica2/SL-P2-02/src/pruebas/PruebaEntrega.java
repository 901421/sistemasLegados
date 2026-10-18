package pruebas;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

/**
 * Prueba automática de la entrega CONTRA EL MAINFRAME: arranca Main con el
 * ws3270 de la entrega y maneja la GUI por código (sin ratón ni teclado):
 * conectar, alta vacía, alta general con tilde, alta específica, Actualizar y
 * cierre con la X. Nunca corta la conexión: siempre sale por la X de la
 * ventana, que hace el cierre ordenado. Avisar antes de lanzarla.
 *
 * Uso (desde la carpeta de la entrega, en Windows; la traza va a la salida de errores):
 *   java\bin\java.exe -cp "programa\SL-P2-02.jar;<clases de pruebas>" pruebas.PruebaEntrega ws3270\ws3270.exe 2> prueba.log
 */
public class PruebaEntrega {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static int fallos = 0;

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                log("FIN del programa (System.exit de la GUI). Fallos de la prueba: " + fallos)));

        try {
            prueba(args[0]);
        } catch (Exception e) {
            fallo("Excepción en la prueba: " + e);
            Window t = ventana("gui.VentanaTareas");
            if (t != null) {
                log("Cierra con la X tras el fallo");
                SwingUtilities.invokeLater(() -> t.dispatchEvent(new WindowEvent(t, WindowEvent.WINDOW_CLOSING)));
            }
        }
    }

    private static void prueba(String ws3270) throws Exception {
        log("Arranca Main con ws3270 = " + ws3270);
        // Main está en el paquete por defecto: no se puede importar desde aquí.
        Class.forName("Main").getMethod("main", String[].class).invoke(null, (Object) new String[] {ws3270});

        Window conexion = esperarVentana("gui.VentanaConexion", 20);
        log("Ventana de conexión: '" + titulo(conexion) + "'");
        pulsar(conexion, "Conectar");

        // Conectado = aparece la ventana de tareas; si sale un diálogo, es un error.
        esperar("ventana de tareas o diálogo de error", 120,
                () -> ventana("gui.VentanaTareas") != null || dialogo() != null);
        if (dialogo() != null) {
            fallo("Error al conectar: " + textoDialogo(dialogo()));
            log("Se deja la ventana abierta para verla; ciérrala con la X.");
            return;
        }
        Window tareas = ventana("gui.VentanaTareas");
        log("Ventana de tareas: '" + titulo(tareas) + "'");
        esperarLibre(tareas, "listado inicial");
        List<String> antes = filas(tareas);
        log("Listado inicial: " + antes.size() + " tareas");
        antes.forEach(f -> log("    " + f));

        // 1. Validación: descripción vacía. No debe mandarse nada al mainframe.
        rellenar(tareas, "General", 18, 10, "", "");
        pulsar(tareas, "Nueva");
        Thread.sleep(500);
        String error = errorAlta(tareas);
        comprobar("descripción vacía da error sin conectar", !error.isBlank() && estaLibre(tareas),
                "error='" + error + "'");

        // 2. Alta general con tilde (EBCDIC).
        alta(tareas, "General", 18, 10, "", "Revisión");
        // 3. Alta específica.
        alta(tareas, "Específica", 19, 10, "Imad", "AutoA3");

        // 4. Actualizar.
        pulsar(tareas, "Actualizar");
        esperarLibre(tareas, "Actualizar");
        List<String> despues = filas(tareas);
        log("Listado final: " + despues.size() + " tareas");
        despues.forEach(f -> log("    " + f));
        comprobar("Actualizar mantiene las tareas", despues.size() == antes.size() + 2,
                antes.size() + " + 2 = " + despues.size());

        // 5. Cierre con la X (mismo camino que Salir).
        log("Cierra con la X");
        enEdt(() -> { tareas.dispatchEvent(new WindowEvent(tareas, WindowEvent.WINDOW_CLOSING)); return null; });
        // Main termina con System.exit cuando ha desconectado; el gancho anota el fin.
        Thread.sleep(120_000);
        fallo("La GUI no ha terminado 2 min después de la X");
    }

    private static void alta(Window w, String tipo, int dia, int mes, String nombre, String desc) throws Exception {
        int antes = filas(w).size();
        rellenar(w, tipo, dia, mes, nombre, desc);
        pulsar(w, "Nueva");
        esperar("que empiece el alta", 5, () -> !estaLibre(w) || !errorSeguro(w).isBlank());
        esperarLibre(w, "alta " + tipo);
        String error = errorAlta(w);
        List<String> ahora = filas(w);
        String buscada = String.format("%02d%02d", dia, mes);
        boolean esta = ahora.stream().anyMatch(f -> f.contains(desc) && (nombre.isEmpty() || f.contains(nombre)));
        comprobar("alta " + tipo + " " + buscada + " " + nombre + " " + desc + " aparece en el listado",
                error.isBlank() && esta && ahora.size() == antes + 1,
                "filas " + antes + " -> " + ahora.size() + ", error='" + error + "'");
        if (dialogo() != null) {
            fallo("Diálogo de error: " + textoDialogo(dialogo()));
        }
    }

    // ---------------------------------------------------------------- GUI

    private static void rellenar(Window w, String tipo, int dia, int mes, String nombre, String desc) throws Exception {
        enEdt(() -> {
            Container panel = (Container) buscar(w, c -> c.getClass().getName().equals("gui.PanelAlta")).get(0);
            for (Component c : buscar(panel, c -> c instanceof JRadioButton)) {
                if (((JRadioButton) c).getText().equals(tipo)) ((JRadioButton) c).doClick();
            }
            List<Component> combos = buscar(panel, c -> c instanceof JComboBox);
            ((JComboBox<?>) combos.get(1)).setSelectedIndex(mes - 1);
            ((JComboBox<?>) combos.get(0)).setSelectedItem(dia);
            List<Component> campos = buscar(panel, c -> c instanceof JTextField);
            ((JTextField) campos.get(0)).setText(nombre);
            ((JTextField) campos.get(1)).setText(desc);
            return null;
        });
        log("Formulario: " + tipo + " " + dia + "/" + mes + " nombre='" + nombre + "' desc='" + desc + "'");
    }

    private static String errorAlta(Window w) throws Exception {
        return enEdt(() -> {
            Container panel = (Container) buscar(w, c -> c.getClass().getName().equals("gui.PanelAlta")).get(0);
            for (Component c : buscar(panel, c -> c instanceof JLabel)) {
                if (gui.Estilo.ERROR.equals(c.getForeground())) return ((JLabel) c).getText().trim();
            }
            return "";
        });
    }

    private static String errorSeguro(Window w) {
        try {
            return errorAlta(w);
        } catch (Exception e) {
            return "";
        }
    }

    /** Libre = el botón Actualizar está activo (se desactiva mientras se habla con el mainframe). */
    private static boolean estaLibre(Window w) {
        try {
            return enEdt(() -> boton(w, "Actualizar").isEnabled());
        } catch (Exception e) {
            return false;
        }
    }

    private static void esperarLibre(Window w, String que) throws Exception {
        long t0 = System.currentTimeMillis();
        esperar(que, 120, () -> estaLibre(w) || dialogo() != null);
        if (dialogo() != null) {
            fallo("Diálogo durante " + que + ": " + textoDialogo(dialogo()));
            enEdt(() -> { dialogo().dispose(); return null; });
        }
        log(que + ": " + (System.currentTimeMillis() - t0) + " ms");
    }

    private static List<String> filas(Window w) throws Exception {
        return enEdt(() -> {
            JTable tabla = (JTable) buscar(w, c -> c instanceof JTable).get(0);
            TableModel m = tabla.getModel();
            List<String> filas = new ArrayList<>();
            for (int f = 0; f < m.getRowCount(); f++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < m.getColumnCount(); c++) {
                    sb.append(c == 0 ? "" : " | ").append(m.getValueAt(f, c));
                }
                filas.add(sb.toString());
            }
            return filas;
        });
    }

    private static void pulsar(Window w, String texto) throws Exception {
        log("Pulsa '" + texto + "'");
        enEdt(() -> { boton(w, texto).doClick(); return null; });
    }

    private static AbstractButton boton(Window w, String texto) {
        for (Component c : buscar(w, c -> c instanceof JButton)) {
            if (texto.equals(((JButton) c).getText())) return (AbstractButton) c;
        }
        throw new IllegalStateException("No hay botón '" + texto + "'");
    }

    private static Window ventana(String clase) {
        for (Window w : Window.getWindows()) {
            if (w.isShowing() && w.getClass().getName().equals(clase)) return w;
        }
        return null;
    }

    private static Window esperarVentana(String clase, int segundos) throws Exception {
        esperar(clase, segundos, () -> ventana(clase) != null);
        return ventana(clase);
    }

    private static JDialog dialogo() {
        for (Window w : Window.getWindows()) {
            if (w.isShowing() && w instanceof JDialog) return (JDialog) w;
        }
        return null;
    }

    private static String textoDialogo(JDialog d) {
        StringBuilder sb = new StringBuilder("[" + d.getTitle() + "] ");
        for (Component c : buscar(d, c -> c instanceof JOptionPane)) {
            sb.append(((JOptionPane) c).getMessage());
        }
        return sb.toString().replace('\n', ' ');
    }

    private static String titulo(Window w) {
        return w instanceof java.awt.Frame ? ((java.awt.Frame) w).getTitle() : w.getName();
    }

    private static List<Component> buscar(Component raiz, java.util.function.Predicate<Component> p) {
        List<Component> r = new ArrayList<>();
        if (p.test(raiz)) r.add(raiz);
        if (raiz instanceof Container) {
            for (Component c : ((Container) raiz).getComponents()) r.addAll(buscar(c, p));
        }
        return r;
    }

    // ---------------------------------------------------------------- utilidades

    private static <T> T enEdt(Supplier<T> s) throws Exception {
        AtomicReference<T> r = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> r.set(s.get()));
        return r.get();
    }

    private static void esperar(String que, int segundos, BooleanSupplier cond) throws Exception {
        long fin = System.currentTimeMillis() + segundos * 1000L;
        while (!cond.getAsBoolean()) {
            if (System.currentTimeMillis() > fin) throw new IllegalStateException("Tiempo agotado esperando " + que);
            Thread.sleep(100);
        }
    }

    private static void comprobar(String que, boolean bien, String detalle) {
        if (bien) {
            log("OK    " + que + " (" + detalle + ")");
        } else {
            fallo(que + " (" + detalle + ")");
        }
    }

    private static void fallo(String que) {
        fallos++;
        log("FALLO " + que);
    }

    private static void log(String m) {
        System.err.println(LocalTime.now().format(HORA) + " [prueba] " + m);
    }
}
