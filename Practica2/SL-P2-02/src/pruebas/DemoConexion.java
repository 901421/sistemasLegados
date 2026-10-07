package pruebas;

import javax.swing.SwingUtilities;

import gui.VentanaConexion;

/**
 * Abre la ventana de conexión con una conexión simulada ({@link ConectorFalso}):
 * no se conecta al mainframe. Con los datos por defecto entra y abre la
 * ventana "Tareas" con datos inventados.
 */
public class DemoConexion {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaConexion(new ConectorFalso()).setVisible(true));
    }
}
