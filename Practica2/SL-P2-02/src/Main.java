import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import gui.VentanaConexion;
import mainframe.MainframeClient;

/**
 * Arranque del programa: abre la ventana de conexión.
 *
 * Uso: java Main [ruta de ws3270]. Sin argumento, se usa la propiedad
 * -Dws3270.ruta o, si no está, la ruta de la instalación de wc3270.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0) {
            System.setProperty(MainframeClient.PROPIEDAD_RUTA, args[0]);
        }

        // Aspecto del sistema (en Windows, el de Windows). Si no se puede, se
        // queda el de Java, que funciona igual.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
            System.err.println("Se usa el aspecto de Java: " + e.getMessage());
        }

        // Las ventanas se crean en el hilo de la interfaz, como pide Swing.
        SwingUtilities.invokeLater(() -> new VentanaConexion().setVisible(true));
    }
}
