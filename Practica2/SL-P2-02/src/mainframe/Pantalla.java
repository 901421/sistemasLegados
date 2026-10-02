package mainframe;

/**
 * Pantallas del mainframe que el programa sabe reconocer por su texto
 * (ver docs/mapa-pantallas.md).
 */
public enum Pantalla {

    /** Consola de Hercules: el mainframe está bloqueado. */
    HERCULES,
    /** Bienvenida de MUSIC/SP, antes del login. */
    BIENVENIDA,
    /** Petición de usuario y clave ("MUSIC Userid:"). */
    LOGIN,
    /** "Userid is in use": la sesión anterior no se cerró con off. */
    SESION_EN_USO,
    /** Menú FSI, con la línea "Command ===>". */
    MENU_FSI,
    /** tareas.c: "MENU PRINCIPAL". */
    MENU_PRINCIPAL,
    /** tareas.c: menú "ASSIGN TASKS". */
    MENU_ASSIGN,
    /** tareas.c: menú "VIEW TASKS". */
    MENU_VIEW,
    /** Ninguna de las anteriores. */
    DESCONOCIDA;

    /**
     * Indica a qué pantalla corresponde un texto devuelto por
     * {@link MainframeClient#pantalla()}. tareas.c escribe hacia abajo sin
     * limpiar, así que cuenta el último menú que aparece.
     */
    public static Pantalla identificar(String texto) {
        throw new UnsupportedOperationException("Pendiente");
    }
}
