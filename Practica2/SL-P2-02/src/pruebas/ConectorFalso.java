package pruebas;

import java.io.IOException;

import gui.VentanaConexion;
import mainframe.MainframeClient;

/**
 * Conexión simulada, para probar la ventana de conexión sin mainframe. Tarda
 * un segundo y falla según los datos: host "hercules" (mainframe bloqueado),
 * host "sinred" (no se puede conectar) o una clave distinta de "secreto6".
 * Si todo va bien, devuelve una sesión con {@link GestorFalso}.
 */
public class ConectorFalso implements VentanaConexion.Conector {

    @Override
    public VentanaConexion.Conexion conectar(String host, int puerto, String usuario, String clave)
            throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Conexión interrumpida.");
        }
        if (host.equals("hercules")) {
            throw new IOException("El mainframe está bloqueado (pantalla de Hercules): "
                    + "no acepta sesiones hasta que el profesor lo reinicie.");
        }
        if (host.equals("sinred")) {
            throw new IOException("No se ha podido conectar con " + host + ":" + puerto
                    + ". Comprueba la red y los datos.");
        }
        if (!clave.equals("secreto6")) {
            throw new IOException("Usuario o clave incorrectos.");
        }
        // El cliente nunca se conecta: desconectar() no hace nada.
        return new VentanaConexion.Conexion(new MainframeClient(), new SesionFalsa(), new GestorFalso());
    }
}
