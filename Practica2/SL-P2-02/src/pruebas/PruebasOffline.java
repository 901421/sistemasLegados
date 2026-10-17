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
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import gui.ModeloTablaTareas;
import gui.PanelAlta;
import mainframe.MainframeClient;
import mainframe.Pantalla;
import tareas.GestorTareas;
import tareas.LectorListado;
import tareas.Tarea;
import tareas.Validador;

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
        probarPantallas();
        probarPaginasListado();
        probarPreguntasAlta();

        System.out.println(fallos == 0 ? "TODO BIEN" : "FALLOS: " + fallos);
        System.exit(fallos == 0 ? 0 : 1);
    }

    /** Tabla vacía, una tarea general y una específica, fechas y aviso al cambiar los datos. */
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
        comprobar("tabla: fecha 1210 se ve como 12 oct", modelo.getValueAt(0, 2).toString().equals("12 oct"));
        comprobar("tabla: general sin nombre", modelo.getValueAt(0, 3).equals(""));
        comprobar("tabla: tipo específica", modelo.getValueAt(1, 1).equals("Específica"));
        comprobar("tabla: descripción con tilde", modelo.getValueAt(1, 4).equals("corrección"));
        comprobar("tabla: contar generales", modelo.contar(Tarea.Tipo.GENERAL) == 1);
        comprobar("tabla: contar todas", modelo.contar(null) == 2);

        // tareas.c no valida la fecha: lo que no es DDMM se enseña tal cual y va al final.
        ModeloTablaTareas.Fecha mala = new ModeloTablaTareas.Fecha("ab12");
        ModeloTablaTareas.Fecha imposible = new ModeloTablaTareas.Fecha("3213");
        ModeloTablaTareas.Fecha enero = new ModeloTablaTareas.Fecha("3101");
        ModeloTablaTareas.Fecha febrero = new ModeloTablaTareas.Fecha("0102");
        comprobar("tabla: fecha con letras, tal cual", mala.toString().equals("ab12"));
        comprobar("tabla: fecha imposible, tal cual", imposible.toString().equals("3213"));
        comprobar("tabla: 0102 se ve como 1 feb", febrero.toString().equals("1 feb"));
        comprobar("tabla: ordena por mes antes que por día", enero.compareTo(febrero) < 0);
        comprobar("tabla: fechas no válidas al final", febrero.compareTo(mala) < 0 && mala.compareTo(enero) > 0);

        modelo.setTareas(List.of());
        comprobar("tabla: vuelve a quedar vacía", modelo.getRowCount() == 0);
    }

    /**
     * Tipo, fecha, campos, limpiar y activar/desactivar. El botón "Nueva"
     * pasa por el Validador, así que su prueba se añade cuando esté hecho.
     */
    static void probarPanelAlta() {
        PanelAlta panel = new PanelAlta((f, n, d) -> { });
        List<JTextField> campos = buscar(panel, JTextField.class);
        // Los desplegables llevan dentro un botón de flecha: se busca el de texto "Nueva".
        List<JButton> botones = buscar(panel, JButton.class).stream()
                .filter(b -> "Nueva".equals(b.getText())).toList();
        List<JRadioButton> tipos = buscar(panel, JRadioButton.class);
        @SuppressWarnings("rawtypes")
        List<JComboBox> fecha = buscar(panel, JComboBox.class);
        comprobar("alta: nombre y descripción", campos.size() == 2);
        comprobar("alta: botón Nueva", botones.size() == 1);
        comprobar("alta: tipos General y Específica", tipos.size() == 2 && tipos.get(0).isSelected());
        comprobar("alta: día y mes", fecha.size() == 2);

        JTextField nombre = campos.get(0);
        comprobar("alta: en una general el nombre está desactivado", !nombre.isEnabled());
        tipos.get(1).doClick();
        comprobar("alta: en una específica el nombre se activa", nombre.isEnabled());

        fecha.get(1).setSelectedIndex(1);
        fecha.get(0).setSelectedItem(29);
        comprobar("alta: 29 de febrero se envía como 2902", panel.fecha().equals("2902"));
        fecha.get(0).setSelectedItem(31);
        fecha.get(1).setSelectedIndex(0);
        fecha.get(0).setSelectedItem(31);
        fecha.get(1).setSelectedIndex(3);
        comprobar("alta: del 31 de enero a abril queda el 30", panel.fecha().equals("3004"));
        comprobar("alta: abril tiene 30 días", fecha.get(0).getItemCount() == 30);

        campos.forEach(c -> c.setText("x"));
        panel.limpiar();
        comprobar("alta: limpiar vacía los campos", campos.stream().allMatch(c -> c.getText().isEmpty()));

        panel.setActivo(false);
        comprobar("alta: desactivar campos y botón",
                campos.stream().noneMatch(Component::isEnabled) && !botones.get(0).isEnabled()
                        && tipos.stream().noneMatch(Component::isEnabled));
        panel.setActivo(true);
        comprobar("alta: volver a activar",
                campos.stream().allMatch(Component::isEnabled) && botones.get(0).isEnabled());
        tipos.get(0).doClick();
        comprobar("alta: volver a general desactiva el nombre", !nombre.isEnabled());
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

    /** Pantalla.identificar con las pantallas reales de img/a1 e img/a2. */
    static void probarPantallas() throws IOException {
        comprobarPantalla("a1/01-bienvenida", Pantalla.BIENVENIDA);
        comprobarPantalla("a1/02-login", Pantalla.LOGIN);
        comprobarPantalla("a1/03-sesion-en-uso", Pantalla.SESION_EN_USO);
        comprobarPantalla("a1/04-menu-fsi", Pantalla.MENU_FSI);
        comprobarPantalla("a1/05-help", Pantalla.MENU_FSI);
        comprobarPantalla("a2/00-tasks-c-no-existe", Pantalla.DESCONOCIDA);
        comprobarPantalla("a2/01-tareas-arranque", Pantalla.MENU_PRINCIPAL);
        comprobarPantalla("a2/02-view-tasks", Pantalla.MENU_VIEW);
        comprobarPantalla("a2/05-assign-tasks", Pantalla.MENU_ASSIGN);
        comprobarPantalla("a2/14-specific-assigned", Pantalla.MENU_ASSIGN);
        comprobarPantalla("a2/12-listado-general", Pantalla.MENU_VIEW);
        comprobarPantalla("a2/31-listado-largo-p2", Pantalla.MENU_VIEW);
        comprobarPantalla("a2/21-hercules-bloqueado", Pantalla.HERCULES);
        comprobarPantalla("a2/06-assign-general", Pantalla.FORMULARIO);
        comprobarPantalla("a2/07-assign-general-fecha", Pantalla.FORMULARIO);
        comprobarPantalla("a2/09-assign-general-descripcion", Pantalla.FORMULARIO);
        comprobarPantalla("a2/13-assign-specific", Pantalla.FORMULARIO);
        comprobar("pantalla: texto vacío", Pantalla.identificar("") == Pantalla.DESCONOCIDA);
    }

    private static void comprobarPantalla(String nombre, Pantalla esperada) throws IOException {
        comprobar("pantalla: " + nombre + " es " + esperada, Pantalla.identificar(pantalla(nombre)) == esperada);
    }

    /**
     * Páginas reales de los listados (img/a2): solo cuenta lo que hay tras el
     * eco de la opción en la primera página, y el listado acaba en TOTAL TASK.
     */
    static void probarPaginasListado() throws IOException {
        LectorListado lector = new LectorListado();

        comprobar("listado: enseña también las tareas con fecha imposible (22 de 22)",
                lector.parsearPantalla(pantalla("a2/31-listado-largo-p2")).size() == 22);

        GestorTareas.Pagina vacia = GestorTareas.leerPagina(pantalla("a2/03-view-general"), "1", true, lector);
        comprobar("listado: vacío, sin tareas", vacia.tareas().isEmpty());
        comprobar("listado: vacío, termina", vacia.completa());

        GestorTareas.Pagina general = GestorTareas.leerPagina(pantalla("a2/12-listado-general"), "1", true, lector);
        comprobar("listado: 3 generales", general.tareas().size() == 3);
        comprobar("listado: la primera es la TASK 0", general.tareas().get(0).numero() == 0);
        comprobar("listado: TOTAL TASK antes de More... termina", general.completa());

        GestorTareas.Pagina p1 = GestorTareas.leerPagina(pantalla("a2/31-listado-largo-p1"), "1", true, lector);
        comprobar("listado largo: la página 1 no termina", !p1.completa());
        comprobar("listado largo: la página 1 acaba en la TASK 26",
                p1.tareas().get(p1.tareas().size() - 1).numero() == 26);
        GestorTareas.Pagina p2 = GestorTareas.leerPagina(pantalla("a2/31-listado-largo-p2"), "1", false, lector);
        comprobar("listado largo: la página 2 empieza en la TASK 27", p2.tareas().get(0).numero() == 27);
        comprobar("listado largo: la página 2 tiene 22 tareas", p2.tareas().size() == 22);
        comprobar("listado largo: la página 2 termina", p2.completa());

        GestorTareas.Pagina especifica = GestorTareas.leerPagina(pantalla("a2/15-listado-specific"), "2", false, lector);
        comprobar("listado: 1 específica", especifica.tareas().size() == 1
                && especifica.tareas().get(0).tipo() == Tarea.Tipo.ESPECIFICA);

        comprobar("listado: sin el eco de la opción, aún no hay página",
                GestorTareas.leerPagina(pantalla("a2/02-view-tasks"), "1", true, lector) == null);
    }

    /**
     * Pregunta a la que espera tareas.c en las pantallas reales del alta, y
     * que nueva() no envía nada si los datos no son válidos.
     */
    static void probarPreguntasAlta() throws IOException {
        comprobar("alta: tras elegir general pide la fecha",
                "ENTER DATE(DDMM):".equals(GestorTareas.pregunta(pantalla("a2/06-assign-general"))));
        comprobar("alta: tras elegir específica pide la fecha",
                "ENTER DATE(DDMM):".equals(GestorTareas.pregunta(pantalla("a2/13-assign-specific"))));
        comprobar("alta: con More... aún no hay pregunta",
                GestorTareas.pregunta(pantalla("a2/07-assign-general-fecha")) == null);
        comprobar("alta: la pregunta quedó en la página anterior",
                "".equals(GestorTareas.pregunta(pantalla("a2/08-more-siguiente-pagina"))));
        comprobar("alta: tras el alta vuelve a pedir opción",
                "OPTION:".equals(GestorTareas.pregunta(pantalla("a2/14-specific-assigned"))));
        comprobar("alta: el listado no espera respuesta en la página 1",
                GestorTareas.pregunta(pantalla("a2/31-listado-largo-p1")) == null);

        comprobar("validar: descripción con espacios",
                "La descripción no puede contener espacios.".equals(Validador.validar("1210", "", "hola mundo")));
        comprobar("validar: descripción de una palabra", Validador.validar("1210", "", "hola") == null);

        // Sin conexión: si nueva() intentara enviar algo, el error sería otro.
        GestorTareas gestor = new GestorTareas(new MainframeClient());
        try {
            gestor.nueva("3102", "", "examen");
            comprobar("alta: fecha imposible no se envía", false);
        } catch (IOException e) {
            comprobar("alta: fecha imposible no se envía", e.getMessage().startsWith("Febrero"));
        }
    }

    /** Anota un fallo si la condición no se cumple. */
    static void comprobar(String descripcion, boolean condicion) {
        if (!condicion) {
            fallos++;
        }
        System.out.println((condicion ? "ok    " : "FALLO ") + descripcion);
    }
}
