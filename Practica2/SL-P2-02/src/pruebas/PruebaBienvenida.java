package pruebas;

import mainframe.MainframeClient;

public class PruebaBienvenida {

    private static final String HOST = "155.210.152.51";
    private static final int PORT = 3270;

    public static void main(String[] args) {

        try (MainframeClient cliente = new MainframeClient()) {

            // Conectar y obtener la bienvenida
            String pantalla = cliente.conectar(HOST, PORT);

            System.out.println("=== PANTALLA INICIAL ===");
            System.out.println(pantalla);

            // Avanzar por las pantallas de bienvenida
            int paginas = 0;
            final int MAX_PAGINAS = 10;

            while (MainframeClient.hayMas(pantalla)
                    && paginas < MAX_PAGINAS) {

                cliente.intro();

                pantalla = cliente.esperarPantallaEstable(10_000);
                paginas++;

                System.out.println("=== PANTALLA " + paginas + " ===");
                System.out.println(pantalla);
            }

            if (MainframeClient.hayMas(pantalla)) {
                throw new IllegalStateException(
                        "Se ha superado el máximo de páginas de bienvenida."
                );
            }

            // No introducir credenciales todavía
            System.out.println("=== FIN DE LA PRUEBA ===");

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }
}