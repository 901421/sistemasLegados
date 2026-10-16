
package pruebas;

import mainframe.MainframeClient;
import mainframe.SesionMusic;

import java.io.Console;
import java.io.IOException;
import java.util.Arrays;

public class PruebaArranqueTareas {

    private static final String HOST = "155.210.152.51";
    private static final int PUERTO = 3270;

    public static void main(String[] args) {
        Console consola = System.console();

        if (consola == null) {
            System.err.println(
                    "Ejecuta esta prueba desde una terminal, no desde IntelliJ."
            );
            return;
        }

        String usuario = consola.readLine("Usuario MUSIC/SP: ");
        char[] clave = consola.readPassword("Contraseña MUSIC/SP: ");

        if (clave == null) {
            System.err.println("No se ha introducido ninguna contraseña.");
            return;
        }

        try (MainframeClient cliente = new MainframeClient()) {
            SesionMusic sesion = new SesionMusic(cliente);

            try {
                cliente.conectar(HOST, PUERTO);

                sesion.iniciar(usuario, new String(clave));
                System.out.println("Login correcto.");

                // Ejecutar tareas.c
                String pantalla = sesion.arrancarTareas();

                System.out.println("tareas.c iniciado correctamente.");
                System.out.println(pantalla);

                sesion.cerrar();

                System.out.println("Salida de tareas.c y MUSIC/SP cerrada.");

            } finally {
                // Si hay un error, comprobar dónde estamos antes de cerrar.
                try {
                    String pantallaActual = cliente.pantalla();

                    if (pantallaActual.contains("MENU PRINCIPAL")
                            && pantallaActual.contains("OPTION:")) {

                        System.out.println(
                                "Recuperación: intentando salir de tareas.c..."
                        );

                        sesion.cerrarTareas();
                    }

                    // Solo ejecutar off si la pantalla confirma el FSI.
                    pantallaActual = cliente.pantalla();

                    if (pantallaActual.contains("Command ===>")) {
                        sesion.cerrarDesdeFsi();

                        System.out.println(
                                "Recuperación: sesión MUSIC/SP cerrada."
                        );
                    }

                } catch (IOException e) {
                    System.err.println(
                            "No se pudo completar la recuperación: "
                                    + e.getMessage()
                    );
                }
            }

        } catch (IOException e) {
            System.err.println("Error durante la prueba: " + e.getMessage());

        } finally {
            Arrays.fill(clave, '\0');
        }
    }
}
