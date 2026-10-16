package pruebas;

import mainframe.MainframeClient;
import mainframe.SesionMusic;

import java.io.Console;
import java.util.Arrays;

public class PruebaLogin {

    private static final String HOST = "155.210.152.51";
    private static final int PORT = 3270;

    public static void main(String[] args) throws Exception {

        // Debe ejecutarse en la terminal
        Console consola = System.console();

        if (consola == null) {
            throw new IllegalStateException(
                    "Ejecuta esta prueba desde una terminal."
            );
        }

        String usuario = consola.readLine("Usuario MUSIC/SP: ");
        char[] clave = consola.readPassword("Contraseña MUSIC/SP: ");

        if (usuario == null || clave == null) {
            throw new IllegalStateException("Entrada cancelada.");
        }

        try {
            try (MainframeClient cliente = new MainframeClient()) {

                cliente.conectar(HOST, PORT);

                SesionMusic sesion = new SesionMusic(cliente);

                try {
                    sesion.iniciar(usuario, new String(clave));
                    System.out.println("Login correcto.");
                } finally {
                    // Solo intentamos off si observamos realmente el FSI.
                    String pantalla = cliente.pantalla();

                    if (pantalla.contains("Command ===>")) {
                        sesion.cerrar();
                        System.out.println("Sesión MUSIC/SP cerrada.");
                    }
                }
            }
        } finally {
            Arrays.fill(clave, '\0');
        }
    }
}