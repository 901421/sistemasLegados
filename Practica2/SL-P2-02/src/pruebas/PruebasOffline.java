package pruebas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTextField;

import gui.ModeloTablaTareas;
import gui.PanelAlta;
import tareas.Tarea;

/**
 * Pruebas que no se conectan al mainframe: usan las pantallas guardadas en
 * img/a1 e img/a2 (ficheros .txt). Se ejecuta desde la carpeta SL-P2-02 y
 * termina con código distinto de 0 si alguna comprobación falla.
 */
public class PruebasOffline {

    private static final Path PANTALLAS = Path.of("..", "img");

    private static int fallos = 0;

    public static void main(String[] args) throws IOException {
        // Cada prueba nueva se añade aquí.
        probarModeloTabla();
        probarPanelAlta();

        System.out.println(fallos == 0 ? "TODO BIEN" : "FALLOS: " + fallos);
        System.exit(fallos == 0 ? 0 : 1);
    }

    /** Tabla vacía, una tarea general y una específica, y aviso al cambiar los datos. */
    static void probarModeloTabla() {
        ModeloTablaTareas modelo = new ModeloTablaTareas();
        comprobar("tabla: empieza vacía", modelo.getRowCount() == 0);
        comprobar("tabla: 5 columnas", modelo.getColumnCount() == 5);
        comprobar("tabla: cabecera Descripción", modelo.getColumnName(4).equals("Descripción"));

        int[] avisos = {0};
        modelo.addTableModelListener(e -> avisos[0]++);
        modelo.setTareas(List.of(
                new Tarea(1, Tarea.Tipo.GENERAL, "1210", "", "examen"),
                new Tarea(2, Tarea.Tipo.ESPECIFICA, "1510", "Paula", "corrección")));
        comprobar("tabla: avisa a la tabla al cambiar los datos", avisos[0] == 1);
        comprobar("tabla: 2 filas", modelo.getRowCount() == 2);
        comprobar("tabla: Nº es un número", modelo.getValueAt(0, 0).equals(1));
        comprobar("tabla: tipo general", modelo.getValueAt(0, 1).equals("General"));
        comprobar("tabla: general sin nombre", modelo.getValueAt(0, 3).equals(""));
        comprobar("tabla: tipo específica", modelo.getValueAt(1, 1).equals("Específica"));
        comprobar("tabla: descripción con tilde", modelo.getValueAt(1, 4).equals("corrección"));

        modelo.setTareas(List.of());
        comprobar("tabla: vuelve a quedar vacía", modelo.getRowCount() == 0);
    }

    /**
     * Campos, limpiar y activar/desactivar. El botón "Nueva" pasa por el
     * Validador, así que su prueba se añade cuando el Validador esté hecho.
     */
    static void probarPanelAlta() {
        PanelAlta panel = new PanelAlta((f, n, d) -> { });
        List<JTextField> campos = buscar(panel, JTextField.class);
        List<JButton> botones = buscar(panel, JButton.class);
        comprobar("alta: 3 campos", campos.size() == 3);
        comprobar("alta: botón Nueva", botones.size() == 1 && botones.get(0).getText().equals("Nueva"));

        campos.forEach(c -> c.setText("x"));
        panel.limpiar();
        comprobar("alta: limpiar vacía los campos", campos.stream().allMatch(c -> c.getText().isEmpty()));

        panel.setActivo(false);
        comprobar("alta: desactivar campos y botón",
                campos.stream().noneMatch(Component::isEnabled) && !botones.get(0).isEnabled());
        panel.setActivo(true);
        comprobar("alta: volver a activar",
                campos.stream().allMatch(Component::isEnabled) && botones.get(0).isEnabled());
    }

    /** Todos los componentes de un tipo dentro de un contenedor, en orden. */
    static <T> List<T> buscar(Container contenedor, Class<T> tipo) {
        List<T> encontrados = new ArrayList<>();
        for (Component c : contenedor.getComponents()) {
            if (tipo.isInstance(c)) {
                encontrados.add(tipo.cast(c));
            }
            if (c instanceof Container hijo) {
                encontrados.addAll(buscar(hijo, tipo));
            }
        }
        return encontrados;
    }

    /** Lee una pantalla guardada, por ejemplo {@code pantalla("a2/12-listado-general")}. */
    static String pantalla(String nombre) throws IOException {
        return Files.readString(PANTALLAS.resolve(nombre + ".txt"), StandardCharsets.UTF_8);
    }

    /** Anota un fallo si la condición no se cumple. */
    static void comprobar(String descripcion, boolean condicion) {
        if (!condicion) {
            fallos++;
        }
        System.out.println((condicion ? "ok    " : "FALLO ") + descripcion);
    }
}
