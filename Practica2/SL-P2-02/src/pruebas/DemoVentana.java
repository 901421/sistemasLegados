package pruebas;

import javax.swing.SwingUtilities;

import gui.Estilo;
import gui.VentanaTareas;
import mainframe.MainframeClient;

/**
 * Abre la ventana "Tareas" con datos inventados, sin conectar al mainframe:
 * el cliente nunca se conecta, así que desconectar() no hace nada.
 */
public class DemoVentana {

    public static void main(String[] args) {
        // El mismo aspecto que el programa de verdad (Main).
        Estilo.aplicarAspecto();
        SwingUtilities.invokeLater(() -> {
            VentanaTareas ventana = new VentanaTareas(new MainframeClient(), new SesionFalsa(), new GestorFalso());
            ventana.setInfoConexion("grupo_02", "demo sin mainframe");
            ventana.setVisible(true);
        });
    }
}
