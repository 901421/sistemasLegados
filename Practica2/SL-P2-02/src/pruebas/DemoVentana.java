package pruebas;

import javax.swing.SwingUtilities;

import gui.VentanaTareas;
import mainframe.MainframeClient;

/**
 * Abre la ventana "Tareas" con datos inventados, sin conectar al mainframe:
 * el cliente nunca se conecta, así que desconectar() no hace nada.
 */
public class DemoVentana {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new VentanaTareas(new MainframeClient(), new SesionFalsa(), new GestorFalso())
                        .setVisible(true));
    }
}
