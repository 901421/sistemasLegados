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
    private static final long ESPERA_ARRANQUE_MS = 30_000;
    private static final long PAUSA_MS = 300;
    /** Dato de relleno para terminar un alta a medias: vale como fecha DDMM y como palabra. */
    private static final String RELLENO = "0101";

    private final MainframeClient cliente;

    public SesionMusic(MainframeClient cliente) {
        this.cliente = cliente;
    }

    private void pausar() throws IOException {
        try {
            Thread.sleep(PAUSA_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Espera interrumpida.", e);
        }
    }

    private String esperarPantalla(Pantalla esperada, long maxMs)
        throws IOException {

        long limite = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(maxMs);

        while (System.nanoTime() < limite) {
            String texto = cliente.pantalla();
            Pantalla actual = Pantalla.identificar(texto);

            if (actual == esperada) {
                return texto;
            }

            if (actual == Pantalla.SESION_EN_USO) {
                throw new IOException(
                        "El usuario ya tiene una sesión activa en MUSIC/SP."
                );
            }

            if (actual == Pantalla.HERCULES) {
                throw new IOException(
                        "El mainframe muestra la consola de Hercules."
                );
            }

            pausar();
        }

        throw new IOException(
                "Tiempo de espera agotado. Pantalla esperada: " + esperada
        );
    }

    /**
     * Va de la bienvenida al menú FSI: pasa los "More..." y escribe usuario y
     * clave (la clave no sale en el registro).
     *
     * @throws IOException si el usuario o la clave no son válidos, la sesión
     *                     anterior sigue abierta ("Userid is in use") o la
     *                     pantalla no es la esperada
     */
    public void iniciar(String usuario, String clave) throws IOException {
        String texto = cliente.pantalla();
        Pantalla actual = Pantalla.identificar(texto);

        // Avanzar hasta el login
        if (actual == Pantalla.BIENVENIDA) {
            cliente.intro();
            esperarPantalla(Pantalla.LOGIN, ESPERA_MS);
            actual = Pantalla.LOGIN;
        }

        // Comprobar que estamos en el login
        if (actual != Pantalla.LOGIN) {
            throw new IOException(
                    "No se ha encontrado la pantalla de login."
            );
        }

        // Introducir usuario y avanzar
        cliente.escribir(usuario);
        cliente.intro();

        // Introducir contraseña y confirmar
        cliente.escribirClave(clave);
        cliente.intro();

        // Esperar respuesta
        esperarFsiTrasLogin();
    }

    /**
     * Gestiona la pantalla informativa posterior al login.
     * Envía un único Intro cuando aparece el mensaje de continuación.
     */
    private void esperarFsiTrasLogin() throws IOException {
        long limite = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(ESPERA_MS);

        boolean continuacionEnviada = false;

        while (System.nanoTime() < limite) {
            String texto = cliente.pantalla();
            Pantalla actual = Pantalla.identificar(texto);

            if (actual == Pantalla.MENU_FSI) {
                return;
            }

            if (actual == Pantalla.SESION_EN_USO) {
                throw new IOException(
                        "El usuario ya tiene una sesión activa en MUSIC/SP."
                );
            }

            if (actual == Pantalla.HERCULES) {
                throw new IOException(
                        "El mainframe muestra la consola de Hercules."
                );
            }

            // Continuación específica del login.
            if (!continuacionEnviada
                    && texto.contains("Userid last signed on")
                    && texto.contains("Press ENTER to continue")) {

                cliente.intro();
                continuacionEnviada = true;
            }

            pausar();
        }

        throw new IOException(
                "No se ha alcanzado el FSI tras el inicio de sesión."
        );
    }


    /**
     * Desde el menú FSI, arranca tareas.c y espera a su menú principal.
     *
     * @return pantalla del menú principal de tareas.c
     * @throws IOException si no estamos en el FSI o no aparece el menú
     */
    public String arrancarTareas() throws IOException {
        if (Pantalla.identificar(cliente.pantalla())
                != Pantalla.MENU_FSI) {

            throw new IOException(
                    "No se puede ejecutar tareas.c: no estamos en el FSI."
            );
        }

        cliente.escribir("tareas.c");
        cliente.intro();

        return esperarPantalla(
                Pantalla.MENU_PRINCIPAL,
                ESPERA_ARRANQUE_MS
        );
    }

    /**
     * Regresa al menú principal desde ASSIGN TASKS o VIEW TASKS.
     */
    public void volverAlMenuPrincipal() throws IOException {
        Pantalla actual = Pantalla.identificar(cliente.pantalla());

        if (actual != Pantalla.MENU_ASSIGN
                && actual != Pantalla.MENU_VIEW) {

            throw new IOException(
                    "No estamos en un submenú desde el que se pueda regresar."
            );
        }

        cliente.escribir("3");
        cliente.intro();

        esperarPantalla(Pantalla.MENU_PRINCIPAL, ESPERA_MS);
    }

    /**
     * Sale de tareas.c desde su menú principal y regresa al FSI.
     */
    public void cerrarTareas() throws IOException {
        if (Pantalla.identificar(cliente.pantalla())
                != Pantalla.MENU_PRINCIPAL) {

            throw new IOException(
                    "No se puede cerrar tareas.c: "
                            + "no estamos en el menú principal."
            );
        }

        cliente.escribir("3");
        cliente.intro();

        long limite = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(ESPERA_MS);

        boolean continuacionEnviada = false;

        while (System.nanoTime() < limite) {
            String texto = cliente.pantalla();
            Pantalla actual = Pantalla.identificar(texto);

            if (actual == Pantalla.MENU_FSI) {
                return;
            }

            if (actual == Pantalla.HERCULES) {
                throw new IOException(
                        "El mainframe muestra la consola de Hercules."
                );
            }

            // Pantalla de continuación al salir
            if (!continuacionEnviada
                    && (texto.contains("Press enter to continue")
                    || MainframeClient.hayMas(texto))) {

                cliente.intro();
                continuacionEnviada = true;
            }

            pausar();
        }

        throw new IOException(
                "No se ha conseguido regresar al FSI al salir de tareas.c."
        );
    }

    /**
     * Cierra la sesión lógica cuando se está en FSI.
     * No desconecta el emulador.
     */
    public void cerrarDesdeFsi() throws IOException {
        if (Pantalla.identificar(cliente.pantalla())
                != Pantalla.MENU_FSI) {

            throw new IOException(
                    "No estamos en el FSI: no se enviará off."
            );
        }

        cliente.escribir("off");
        cliente.intro();

        esperarPantalla(Pantalla.BIENVENIDA, ESPERA_MS);
    }

    /**
     * Termina un alta que se quedó a medias contestando cada pregunta con un
     * dato de relleno válido (sirve como fecha, nombre o descripción), hasta
     * volver a ASSIGN TASKS. La tarea de relleno se pierde al salir de
     * tareas.c, que no guarda nada (AMB-8).
     */
    private void terminarFormulario() throws IOException {
        String texto = cliente.esperarPantallaEstable(ESPERA_MS);
        // Una tarea específica tiene 3 preguntas; con margen por los "More...".
        for (int i = 0; i < 6; i++) {
            Pantalla actual = Pantalla.identificar(texto);
            if (actual == Pantalla.MENU_ASSIGN) {
                return;
            }
            if (MainframeClient.hayMas(texto)) {
                cliente.intro();
            } else if (actual == Pantalla.FORMULARIO) {
                cliente.escribir(RELLENO);
                cliente.intro();
            } else {
                throw new IOException("No se puede terminar el alta a medias desde: " + actual);
            }
            texto = esperarCambio(texto);
        }
        throw new IOException("No se ha conseguido terminar el alta a medias.");
    }

    /** Espera a que la pantalla sea distinta de la anterior y esté estable. */
    private String esperarCambio(String anterior) throws IOException {
        long limite = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(ESPERA_MS);
        while (System.nanoTime() < limite) {
            String texto = cliente.esperarPantallaEstable(ESPERA_MS);
            if (!texto.equals(anterior)) {
                return texto;
            }
        }
        throw new IOException("El mainframe no ha respondido a tiempo.");
    }

    /**
     * Sale de tareas.c (opción 3) si está abierto y cierra la sesión con
     * "off". Deja la conexión abierta: después hay que llamar a
     * {@link MainframeClient#desconectar()}.
     *
     * Si un alta se quedó a medias, la termina con datos de relleno antes de
     * salir. No intenta cerrar desde pantallas desconocidas, ya que no existe
     * una ruta de salida confirmada.
     */
    public void cerrar() throws IOException {
        Pantalla actual = Pantalla.identificar(cliente.pantalla());

        switch (actual) {
            case BIENVENIDA:
                return;

            case MENU_FSI:
                cerrarDesdeFsi();
                return;

            case MENU_PRINCIPAL:
                cerrarTareas();
                cerrarDesdeFsi();
                return;

            case FORMULARIO:
                terminarFormulario();
                volverAlMenuPrincipal();
                cerrarTareas();
                cerrarDesdeFsi();
                return;

            case MENU_ASSIGN:
            case MENU_VIEW:
                volverAlMenuPrincipal();
                cerrarTareas();
                cerrarDesdeFsi();
                return;

            default:
                throw new IOException(
                        "No se puede cerrar automáticamente desde "
                                + "la pantalla actual: " + actual
                );
        }
    }
}
