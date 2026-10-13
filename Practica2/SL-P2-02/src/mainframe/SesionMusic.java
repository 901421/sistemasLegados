package mainframe;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Sesión de MUSIC/SP sobre una conexión ya abierta: login, arranque de la
 * aplicación legada y cierre ordenado. No conecta ni desconecta; de eso se
 * encarga {@link MainframeClient}.
 */
public class SesionMusic {

    private static final long ESPERA_MS = 15_000;
    private static final long PAUSA_MS = 300;

    private final MainframeClient cliente;

    public SesionMusic(MainframeClient cliente) {
        this.cliente = cliente;
    }

    private boolean esBienvenida(String pantalla) {
        return pantalla.contains(
                "Multi-User System for Interactive Computing / System Product"
        );
    }

    private boolean esLogin(String pantalla) {
        return pantalla.contains("MUSIC Userid:")
                && pantalla.contains("Password:");
    }

    private boolean esInformacionSesion(String pantalla) {
        return pantalla.contains("Userid last signed on")
                && pantalla.contains("Press ENTER to continue");
    }

    private boolean esFsi(String pantalla) {
        return pantalla.contains("Command ===>");
    }

    private String esperarFsi() throws IOException {
        long limite = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(ESPERA_MS);

        boolean informacionContinuada = false;

        while (System.nanoTime() < limite) {
            String pantalla = cliente.pantalla();

            if (MainframeClient.sesionEnUso(pantalla)) {
                throw new IOException(
                        "MUSIC/SP indica que el usuario ya tiene una sesión en uso."
                );
            }

            if (esFsi(pantalla)) {
                return pantalla;
            }

            // Pantalla informativa posterior a login
            if (esInformacionSesion(pantalla)) {

                if (!informacionContinuada) {
                    cliente.intro();
                    informacionContinuada = true;
                }

                // Si sigue visible, esperar a que cambie
                pausar();
                continue;
            }

            // Otra pantalla: esperar a que aparezca el FSI
            pausar();
        }

        throw new IOException(
                "No se ha alcanzado el menú FSI tras el inicio de sesión."
        );
    }

    private String esperarBienvenida() throws IOException {
        long limite = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(ESPERA_MS);

        while (System.nanoTime() < limite) {
            String pantalla = cliente.pantalla();

            if (esBienvenida(pantalla)) {
                return pantalla;
            }

            pausar();
        }

        throw new IOException(
                "No se ha confirmado el regreso a la bienvenida de MUSIC/SP."
        );
    }

    private void pausar() throws IOException {
        try {
            Thread.sleep(PAUSA_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Espera interrumpida.", e);
        }
    }

    /**
     * Va de la bienvenida al menú FSI: pasa los "More...", escribe usuario y
     * clave y, si la sesión anterior quedó abierta, contesta OK.
     *
     * @throws IOException si el usuario o la clave no son válidos, o la pantalla no es la esperada
     */
    public void iniciar(String usuario, String clave) throws IOException {
        String pantalla = cliente.pantalla();

        // Avanzar hasta el login
        if (esBienvenida(pantalla)) {
            cliente.intro();
            pantalla = cliente.esperarPantallaEstable(10_000);
        }

        // Comprobar que estamos en el login
        if (!esLogin(pantalla)) {
            throw new IOException(
                    "No se ha encontrado la pantalla de login."
            );
        }

        // Introducir usuario y avanzar
        cliente.escribir(usuario);
        cliente.intro();

        // Introducir contraseña y confirmar
        cliente.escribir(clave);
        cliente.intro();

        // Esperar respuesta
        esperarFsi();
    }

    /** Desde el menú FSI, arranca tareas.c y espera a su menú principal. */
    public void arrancarTareas() throws IOException {
        throw new UnsupportedOperationException("Pendiente");
    }

    /**
     * Cierra la sesión lógica cuando se está en FSI.
     * No desconecta el emulador.
     */
    public void cerrarDesdeFsi() throws IOException {
        String pantalla = cliente.pantalla();

        if (!esFsi(pantalla)) {
            throw new IOException(
                    "No estamos en FSI: no se enviará off."
            );
        }

        cliente.escribir("off");
        cliente.intro();

        esperarBienvenida();
    }

    /**
     * Sale de tareas.c (opción 3) si está abierto y cierra la sesión con
     * "off". Deja la conexión abierta: después hay que llamar a
     * {@link MainframeClient#desconectar()}.
     */
    public void cerrar() throws IOException {
        throw new UnsupportedOperationException("Pendiente");
    }
}
