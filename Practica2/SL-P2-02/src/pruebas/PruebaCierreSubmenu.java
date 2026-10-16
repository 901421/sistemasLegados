
package pruebas;

import mainframe.MainframeClient;
import mainframe.Pantalla;
import mainframe.SesionMusic;

import java.io.Console;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class PruebaCierreSubmenu {

    private static final String HOST = "155.210.152.51";
    private static final int PUERTO = 3270;
    private static final long ESPERA_MS = 15_000;

    public static void main(String[] args) throws Exception {

        if (args.length != 1
                || (!args[0].equals("1") && !args[0].equals("2"))) {
            System.out.println(
                    "Uso: pruebas.PruebaCierreSubmenu 1|2"
            );
            return;
        }

        String opcion = args[0];
        Pantalla esperado = opcion.equals("1")
                ? Pantalla.MENU_ASSIGN
                : Pantalla.MENU_VIEW;

        Scanner scanner = new Scanner(System.in);

        System.out.print("Usuario MUSIC/SP: ");
        String usuario = scanner.nextLine();

        String clave;
        Console consola = System.console();

        if (consola != null) {
            char[] caracteres = consola.readPassword(
                    "Contraseña MUSIC/SP: "
            );
            if (caracteres == null) {
                throw new IOException("No se ha introducido contraseña.");
            }
            clave = new String(caracteres);
            java.util.Arrays.fill(caracteres, '\0');
        } else {
            System.out.print("Contraseña MUSIC/SP: ");
            clave = scanner.nextLine();
        }

        try (MainframeClient cliente = new MainframeClient()) {

            SesionMusic sesion = new SesionMusic(cliente);

            try {
                System.out.println("1. Conectando...");
                cliente.conectar(HOST, PUERTO);

                System.out.println("2. Iniciando sesión...");
                sesion.iniciar(usuario, clave);

                System.out.println("3. Arrancando tareas.c...");
                sesion.arrancarTareas();

                System.out.println("4. Abriendo submenú...");
                cliente.escribir(opcion);
                cliente.intro();

                esperarPantalla(cliente, esperado);

                System.out.println("5. Cerrando sesión...");
                sesion.cerrar();

                comprobarPantalla(
                        cliente, Pantalla.BIENVENIDA
                );

                System.out.println(
                        "PRUEBA SUPERADA: cierre completo desde "
                                + esperado
                );

            } catch (IOException | RuntimeException e) {
                System.err.println(
                        "ERROR DURANTE LA PRUEBA: " + e.getMessage()
                );

                // Recuperación conservadora: solo desde
                // estados que cerrar() sabe gestionar.
                try {
                    Pantalla actual = Pantalla.identificar(
                            cliente.pantalla()
                    );

                    if (actual == Pantalla.MENU_FSI
                            || actual == Pantalla.MENU_PRINCIPAL
                            || actual == Pantalla.MENU_ASSIGN
                            || actual == Pantalla.MENU_VIEW) {

                        System.out.println(
                                "Intentando cierre lógico seguro..."
                        );
                        sesion.cerrar();

                    } else {
                        System.err.println(
                                "No se enviarán comandos de salida "
                                        + "desde: " + actual
                        );
                    }

                } catch (Exception recuperacion) {
                    System.err.println(
                            "No se pudo confirmar el cierre lógico: "
                                    + recuperacion.getMessage()
                    );
                }

                throw e;
            }
        }

        System.out.println("Conexión TN3270 finalizada.");
    }

    private static void esperarPantalla(
            MainframeClient cliente, Pantalla esperada
    ) throws IOException {

        long limite = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(ESPERA_MS);

        while (System.nanoTime() < limite) {
            Pantalla actual = Pantalla.identificar(
                    cliente.pantalla()
            );

            if (actual == esperada) {
                return;
            }

            if (actual == Pantalla.HERCULES
                    || actual == Pantalla.SESION_EN_USO) {
                throw new IOException(
                        "Pantalla inesperada: " + actual
                );
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(
                        "Espera interrumpida.", e
                );
            }
        }

        throw new IOException(
                "No apareció la pantalla: " + esperada
        );
    }

    private static void comprobarPantalla(
            MainframeClient cliente, Pantalla esperada
    ) throws IOException {

        Pantalla actual = Pantalla.identificar(
                cliente.pantalla()
        );

        if (actual != esperada) {
            throw new IOException(
                    "Esperada " + esperada
                            + ", obtenida " + actual
            );
        }
    }
}
