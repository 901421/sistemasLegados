package mainframe;

import java.io.IOException;

/**
 * Sesión de MUSIC/SP sobre una conexión ya abierta: login, arranque de la
 * aplicación legada y cierre ordenado. No conecta ni desconecta; de eso se
 * encarga {@link MainframeClient}.
 */
public class SesionMusic {

    private final MainframeClient cliente;

    public SesionMusic(MainframeClient cliente) {
        this.cliente = cliente;
    }

    /**
     * Va de la bienvenida al menú FSI: pasa los "More...", escribe usuario y
     * clave y, si la sesión anterior quedó abierta, contesta OK.
     *
     * @throws IOException si el usuario o la clave no son válidos, o la pantalla no es la esperada
     */
    public void iniciar(String usuario, String clave) throws IOException {
        throw new UnsupportedOperationException("Pendiente");
    }

    /** Desde el menú FSI, arranca tareas.c y espera a su menú principal. */
    public void arrancarTareas() throws IOException {
        throw new UnsupportedOperationException("Pendiente");
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
