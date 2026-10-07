package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import tareas.Validador;

/**
 * Formulario de una tarea nueva: fecha, nombre y descripción, y el botón
 * "Nueva". Valida los datos con {@link tareas.Validador} antes de enviarlos;
 * si no son válidos, muestra el motivo en rojo debajo y no envía nada.
 * No habla con el mainframe: entrega los datos a quien lo crea.
 */
public class PanelAlta extends JPanel {

    /** Lo que se hace con una tarea cuyos datos ya han pasado la validación. */
    @FunctionalInterface
    public interface AccionNueva {
        void nueva(String fecha, String nombre, String descripcion);
    }

    private static final Color COLOR_ERROR = new Color(0xB00020);

    private final AccionNueva accion;
    private final JTextField fecha = new JTextField(5);
    private final JTextField nombre = new JTextField(10);
    private final JTextField descripcion = new JTextField(12);
    private final JButton botonNueva = new JButton("Nueva");
    // Un espacio para que la línea ocupe su alto aunque no haya error.
    private final JLabel error = new JLabel(" ");

    public PanelAlta(AccionNueva accion) {
        super(new BorderLayout());
        this.accion = accion;

        nombre.setToolTipText("Vacío para una tarea general; con nombre, específica");
        fecha.setToolTipText("Día y mes, por ejemplo 1210");
        error.setForeground(COLOR_ERROR);
        // Mismo margen que deja el FlowLayout de los campos (5 px).
        error.setBorder(new EmptyBorder(0, 5, 5, 5));

        JPanel campos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        campos.add(new JLabel("Fecha (DDMM)"));
        campos.add(fecha);
        campos.add(new JLabel("Nombre"));
        campos.add(nombre);
        campos.add(new JLabel("Descripción"));
        campos.add(descripcion);
        campos.add(botonNueva);

        add(campos, BorderLayout.CENTER);
        add(error, BorderLayout.SOUTH);

        // El botón y la tecla Intro en cualquier campo hacen lo mismo.
        botonNueva.addActionListener(e -> pulsarNueva());
        for (JTextField campo : new JTextField[] {fecha, nombre, descripcion}) {
            campo.addActionListener(e -> pulsarNueva());
            campo.getDocument().addDocumentListener(new BorrarErrorAlEscribir());
        }
    }

    /** Vacía los campos y el error, y deja el cursor en la fecha. */
    public void limpiar() {
        fecha.setText("");
        nombre.setText("");
        descripcion.setText("");
        error.setText(" ");
        fecha.requestFocusInWindow();
    }

    /** Activa o desactiva campos y botón (se desactivan mientras se habla con el mainframe). */
    public void setActivo(boolean activo) {
        fecha.setEnabled(activo);
        nombre.setEnabled(activo);
        descripcion.setEnabled(activo);
        botonNueva.setEnabled(activo);
    }

    private void pulsarNueva() {
        // Los espacios de los extremos se quitan; los de en medio los rechaza el Validador.
        String f = fecha.getText().strip();
        String n = nombre.getText().strip();
        String d = descripcion.getText().strip();

        String problema = Validador.validar(f, n, d);
        if (problema != null) {
            error.setText(problema);
            return;
        }
        error.setText(" ");
        accion.nueva(f, n, d);
    }

    /** Al corregir cualquier campo, desaparece el error anterior. */
    private class BorrarErrorAlEscribir implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            error.setText(" ");
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            error.setText(" ");
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            error.setText(" ");
        }
    }
}
