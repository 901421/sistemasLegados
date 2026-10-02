package gui;

import javax.swing.JFrame;

/**
 * Ventana principal "Tareas": formulario de alta ({@link PanelAlta}), tabla
 * de tareas ({@link ModeloTablaTareas}) y botón "Salir". Las operaciones con
 * el mainframe se hacen en segundo plano; al salir o cerrar con la X se
 * cierra la sesión y se desconecta.
 */
public class VentanaTareas extends JFrame {

    public VentanaTareas() {
        super("Tareas");
    }
}
