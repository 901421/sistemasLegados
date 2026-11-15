import legado.ServicioLegado;
import servidor.ServidorWeb;

import java.io.File;
import java.net.BindException;
import java.nio.file.Path;

/**
 * Punto de entrada de la práctica: reserva el puerto del servidor web, arranca
 * la aplicación legada en DOSBox (a través del wrapper) y publica la web que la
 * encapsula.
 *
 * Uso: java -cp "out;lib/*" Main [PUERTO]   (8080 si no se indica)
 *
 * Las rutas son relativas a la carpeta del programa (SL-P3-02), desde la que lo
 * lanzan iniciar.bat y build.sh:
 * <ul>
 *   <li>Database-MSDOS: la aplicación legada, intacta (R5).</li>
 *   <li>dosbox/p3-dosbox.conf: nuestra configuración de DOSBox (fuera de Database).</li>
 *   <li>frontend: la página web.</li>
 * </ul>
 *
 * El orden importa (C16): primero se reserva el puerto. Si está ocupado, casi
 * seguro que el servidor ya está en marcha (por ejemplo, por un doble clic de
 * más): se avisa y se termina sin tocar DOSBox, para no cerrarle la aplicación
 * al servidor que funciona. Solo entonces se cierra el DOSBox que hubiera dejado
 * una ejecución anterior (VentanaDosBox.cerrarAnterior) y se lanza el nuevo.
 *
 * Al terminar (Ctrl+C, cerrar la ventana de la consola o un error al arrancar)
 * se deja de atender y se cierra DOSBox.
 */
public class Main {

    /** Puerto por defecto del servidor web. */
    public static final int PUERTO = 8080;

    /** Código de salida si el puerto está ocupado o no es válido. */
    public static final int SALIDA_PUERTO = 2;

    public static void main(String[] args) {
        int puerto = PUERTO;
        if (args.length > 0) {
            try {
                puerto = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                puerto = -1;
            }
            if (puerto < 1 || puerto > 65535) {
                System.out.println("El puerto debe ser un número del 1 al 65535 (se ha indicado \"" + args[0] + "\").");
                System.exit(SALIDA_PUERTO);
            }
        }

        ServidorWeb servidor;
        try {
            servidor = new ServidorWeb(puerto);
        } catch (BindException e) {
            System.out.println("El puerto " + puerto + " ya está en uso: probablemente el servidor ya está en marcha.");
            System.out.println("Ábrelo en http://localhost:" + puerto + " o cierra la otra ventana del servidor antes.");
            System.out.println("(También se puede arrancar con otro puerto, por ejemplo: lanzar.bat 9090.)");
            System.exit(SALIDA_PUERTO);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        ServicioLegado[] servicio = new ServicioLegado[1];
        // Se registra antes de lanzar DOSBox, por si algo falla al arrancar.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            servidor.parar();
            if (servicio[0] != null) servicio[0].cerrar();
            System.out.println("Servidor parado y DOSBox cerrado.");
        }, "cierre"));

        try {
            System.out.println("Arrancando DOSBox y cargando la aplicación legada...");
            servicio[0] = new ServicioLegado(new File("Database-MSDOS"), new File("dosbox/p3-dosbox.conf"));
            servidor.publicar(servicio[0], Path.of("frontend"));
            servidor.arrancar();

            System.out.println("¡Servidor listo y escuchando en http://localhost:" + servidor.puerto() + "!");
            for (String d : servidor.direcciones()) System.out.println("  Desde otro equipo de la red: " + d);
            System.out.println("Para apagar el servidor y DOSBox, cierra esta ventana o pulsa Ctrl+C.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1); // ejecuta el cierre: DOSBox no se queda abierto
        }
    }
}
