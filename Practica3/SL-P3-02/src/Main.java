import legado.ServicioLegado;
import servidor.ServidorWeb;

import java.io.File;
import java.nio.file.Path;

/**
 * Punto de entrada de la práctica: arranca la aplicación legada en DOSBox (a
 * través del wrapper) y después el servidor web que la encapsula.
 *
 * Las rutas son relativas a la carpeta del programa (SL-P3-02), desde la que lo
 * lanzan iniciar.bat y build.sh:
 * <ul>
 *   <li>Database-MSDOS: la aplicación legada, intacta (R5).</li>
 *   <li>dosbox/p3-dosbox.conf: nuestra configuración de DOSBox (fuera de Database).</li>
 *   <li>frontend: la página web.</li>
 * </ul>
 */
public class Main {

    public static void main(String[] args) {
        try {
            File carpetaApp = new File("Database-MSDOS");
            File conf = new File("dosbox/p3-dosbox.conf");

            System.out.println("Arrancando DOSBox y cargando la aplicación legada...");
            ServicioLegado servicio = new ServicioLegado(carpetaApp, conf);

            // Servidor nativo de Java en el puerto 8080
            ServidorWeb servidor = new ServidorWeb(8080, servicio, Path.of("frontend"));
            servidor.arrancar();

            System.out.println("¡Servidor listo y escuchando en http://localhost:8080!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
