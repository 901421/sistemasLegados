package gui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.Month;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import tareas.Validador;

/**
 * Formulario de una tarea nueva: tipo (general o específica), fecha (día y
 * mes), nombre (solo en las específicas) y descripción, y el botón "Nueva".
 * Un contador avisa del límite de caracteres de tareas.c mientras se
 * escribe. Valida los datos con {@link tareas.Validador} antes de
 * enviarlos; si no son válidos, muestra el motivo en rojo y no envía nada.
 * No habla con el mainframe: entrega los datos a quien lo crea.
 */
public class PanelAlta extends JPanel {

    /** Lo que se hace con una tarea cuyos datos ya han pasado la validación. */
    @FunctionalInterface
    public interface AccionNueva {
        /**
         * @param fecha  en el formato de tareas.c, DDMM
         * @param nombre vacío en una tarea general
         */
        void nueva(String fecha, String nombre, String descripcion);
    }

    private static final String[] NOMBRES_MESES = {
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};

    private final AccionNueva accion;

    private final JRadioButton general = new JRadioButton("General", true);
    private final JRadioButton especifica = new JRadioButton("Específica");
    private final JComboBox<Integer> dia = new JComboBox<>();
    private final JComboBox<String> mes = new JComboBox<>(NOMBRES_MESES);
    private final JTextField nombre = new JTextField(14);
    private final JTextField descripcion = new JTextField(14);
    private final JLabel contador = new JLabel();
    private final JButton botonNueva = new JButton("Nueva");
    // Un espacio para que la línea ocupe su alto aunque no haya error.
    private final JLabel error = new JLabel(" ");

    private boolean activo = true;

    public PanelAlta(AccionNueva accion) {
        super(new GridBagLayout());
        this.accion = accion;
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Nueva tarea"),
                BorderFactory.createEmptyBorder(4, 8, 6, 8)));

        ButtonGroup tipos = new ButtonGroup();
        tipos.add(general);
        tipos.add(especifica);
        JPanel filaTipo = filaSinMargen(general, especifica);

        // Fecha de hoy por defecto; los días se ajustan al mes elegido.
        LocalDate hoy = LocalDate.now();
        mes.setSelectedIndex(hoy.getMonthValue() - 1);
        rellenarDias(hoy.getDayOfMonth());
        mes.addActionListener(e -> rellenarDias((Integer) dia.getSelectedItem()));
        // El aspecto de Windows calcula el ancho con una cifra de menos y el día sale
        // como "..."; con el ancho de tres cifras se ve entero.
        dia.setPrototypeDisplayValue(888);
        dia.setMaximumRowCount(16);
        mes.setMaximumRowCount(12);
        JPanel filaFecha = filaSinMargen(dia, mes);

        nombre.setToolTipText("Solo en las tareas específicas. Una palabra, sin espacios");
        descripcion.setToolTipText("Una palabra, sin espacios");
        contador.setForeground(Estilo.TEXTO_SECUNDARIO);
        error.setForeground(Estilo.ERROR);

        botonNueva.setMnemonic(KeyEvent.VK_N);
        botonNueva.setToolTipText("Crear la tarea (Alt+N)");

        anadirFila(0, "Tipo", filaTipo, null);
        anadirFila(1, "Fecha", filaFecha, null);
        anadirFila(2, "Nombre", nombre, null);
        anadirFila(3, "Descripción", descripcion, contador);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 4;
        c.gridx = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(6, 4, 0, 4);
        add(error, c);
        c.gridx = 3;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        add(botonNueva, c);

        general.addActionListener(e -> cambiarTipo());
        especifica.addActionListener(e -> cambiarTipo());
        botonNueva.addActionListener(e -> pulsarNueva());
        // Intro en un campo de texto equivale a pulsar "Nueva".
        for (JTextField campo : new JTextField[] {nombre, descripcion}) {
            campo.addActionListener(e -> pulsarNueva());
            campo.getDocument().addDocumentListener(new AlEscribir());
        }
        cambiarTipo();
    }

    /** Vacía nombre, descripción y error, y deja el cursor donde se sigue escribiendo. Tipo y fecha se mantienen. */
    public void limpiar() {
        nombre.setText("");
        descripcion.setText("");
        error.setText(" ");
        empezarAEscribir();
    }

    /** Pone el cursor en el primer campo que hay que escribir. */
    public void empezarAEscribir() {
        (especifica.isSelected() ? nombre : descripcion).requestFocusInWindow();
    }

    /** Activa o desactiva todo el formulario (se desactiva mientras se habla con el mainframe). */
    public void setActivo(boolean activo) {
        this.activo = activo;
        for (JComponent c : new JComponent[] {general, especifica, dia, mes, descripcion, botonNueva}) {
            c.setEnabled(activo);
        }
        nombre.setEnabled(activo && especifica.isSelected());
    }

    /** Fecha elegida en el formato de tareas.c: DDMM. */
    public String fecha() {
        return String.format("%02d%02d", (Integer) dia.getSelectedItem(), mes.getSelectedIndex() + 1);
    }

    private void anadirFila(int fila, String etiqueta, JComponent campo, JComponent extra) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(3, 4, 3, 4);
        c.anchor = GridBagConstraints.LINE_END;
        add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        add(campo, c);
        if (extra != null) {
            c.gridx = 2;
            add(extra, c);
        }
    }

    private static JPanel filaSinMargen(JComponent... componentes) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        // El primer componente, pegado a la izquierda como los campos de texto.
        fila.setBorder(BorderFactory.createEmptyBorder(0, -6, 0, 0));
        for (JComponent c : componentes) {
            fila.add(c);
        }
        return fila;
    }

    /** Pone en el desplegable los días del mes elegido y conserva el día si existe. */
    private void rellenarDias(Integer diaElegido) {
        // Sin año: febrero admite el 29.
        int dias = Month.of(mes.getSelectedIndex() + 1).maxLength();
        dia.removeAllItems();
        for (int d = 1; d <= dias; d++) {
            dia.addItem(d);
        }
        int elegido = diaElegido == null ? 1 : Math.min(diaElegido, dias);
        dia.setSelectedItem(elegido);
    }

    private void cambiarTipo() {
        nombre.setEnabled(activo && especifica.isSelected());
        actualizarContador();
        error.setText(" ");
    }

    /**
     * Caracteres usados frente al máximo de tareas.c: la descripción en una
     * general; nombre y descripción juntos en una específica.
     */
    private void actualizarContador() {
        boolean esGeneral = general.isSelected();
        int usados = descripcion.getText().strip().length()
                + (esGeneral ? 0 : nombre.getText().strip().length());
        int maximo = esGeneral ? Validador.MAX_DESCRIPCION_GENERAL : Validador.MAX_NOMBRE_MAS_DESCRIPCION;
        contador.setText(esGeneral ? usados + "/" + maximo : "nombre + descripción: " + usados + "/" + maximo);
        contador.setForeground(usados > maximo ? Estilo.ERROR : Estilo.TEXTO_SECUNDARIO);
    }

    private void pulsarNueva() {
        // Los espacios de los extremos se quitan; los de en medio los rechaza el Validador.
        String f = fecha();
        String n = general.isSelected() ? "" : nombre.getText().strip();
        String d = descripcion.getText().strip();

        // Con el nombre vacío, GestorTareas.nueva la crearía como general.
        if (especifica.isSelected() && n.isEmpty()) {
            error.setText("Una tarea específica necesita un nombre.");
            nombre.requestFocusInWindow();
            return;
        }
        String problema = Validador.validar(f, n, d);
        if (problema != null) {
            error.setText(problema);
            return;
        }
        error.setText(" ");
        accion.nueva(f, n, d);
    }

    /** Al escribir: se actualiza el contador y desaparece el error anterior. */
    private class AlEscribir implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            cambio();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            cambio();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            cambio();
        }

        private void cambio() {
            actualizarContador();
            error.setText(" ");
        }
    }
}
