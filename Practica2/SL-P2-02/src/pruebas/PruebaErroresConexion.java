
package pruebas;

import mainframe.MainframeClient;
import mainframe.SesionMusic;

import java.io.IOException;

public class PruebaErroresConexion {

    public static void main(String[] args) throws Exception {

        // 1. Desconectar sin haberse conectado.
        try (MainframeClient cliente = new MainframeClient()) {
            cliente.desconectar();
            cliente.desconectar();
            System.out.println("OK: desconexión repetida.");
        }

        // 2. Enviar una orden sin conexión.
        try (MainframeClient cliente = new MainframeClient()) {
            try {
                cliente.intro();
                throw new AssertionError(
                        "Se ha permitido enviar Intro sin conexión."
                );
            } catch (IOException e) {
                System.out.println(
                        "OK: no permite enviar órdenes sin conexión."
                );
            }
        }

        // 3. Leer pantalla sin conexión.
        try (MainframeClient cliente = new MainframeClient()) {
            try {
                cliente.pantalla();
                throw new AssertionError(
                        "Se ha permitido leer sin conexión."
                );
            } catch (IOException e) {
                System.out.println(
                        "OK: no permite leer sin conexión."
                );
            }
        }

        // 4. Arrancar tareas.c sin conexión.
        try (MainframeClient cliente = new MainframeClient()) {
            SesionMusic sesion = new SesionMusic(cliente);

            try {
                sesion.arrancarTareas();
                throw new AssertionError(
                        "Se ha permitido arrancar sin conexión."
                );
            } catch (IOException e) {
                System.out.println(
                        "OK: no permite arrancar sin conexión."
                );
            }
        }

        // 5. Cerrar una sesión inexistente.
        try (MainframeClient cliente = new MainframeClient()) {
            SesionMusic sesion = new SesionMusic(cliente);

            try {
                sesion.cerrar();
                throw new AssertionError(
                        "Se ha permitido cerrar una sesión inexistente."
                );
            } catch (IOException e) {
                System.out.println(
                        "OK: detecta que no existe conexión."
                );
            }
        }

        System.out.println("TODAS LAS PRUEBAS SUPERADAS");
    }
}
