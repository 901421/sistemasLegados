import mainframe.MainframeClient;

/**
 * Prueba de la conexión: conecta con el mainframe, muestra la primera
 * pantalla y desconecta. No hace login.
 *
 * Uso: java Main [ruta de ws3270]
 */
public class Main {

    private static final String HOST = "155.210.152.51";
    private static final int PORT = 3270;

    public static void main(String[] args) {

        MainframeClient cliente = args.length > 0
                ? new MainframeClient(args[0])
                : new MainframeClient();

        // Al salir del try, con o sin error, se llama a close(), que
        // desconecta respetando el tiempo mínimo conectado.
        try (cliente) {
            String pantalla = cliente.conectar(HOST, PORT);

            System.out.println(pantalla);

            if (MainframeClient.hayMas(pantalla)) {
                System.out.println("[La pantalla espera un Intro para continuar]");
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }
}
