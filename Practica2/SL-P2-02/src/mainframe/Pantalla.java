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
     *
     * @param texto El texto de la pantalla devuelto por el emulador.
     * @return El tipo de Pantalla detectado.
     */
    public static Pantalla identificar(String texto) {
        if (texto == null || texto.isBlank()) {
            return DESCONOCIDA;
        }

        if (texto.contains("Userid is in use")) {
            return SESION_EN_USO;
        }

        if (texto.contains("MUSIC Userid:") && texto.contains("Password:")) {
            return LOGIN;
        }

        if (texto.contains("Command ===>")) {
            return MENU_FSI;
        }

        if (texto.contains("Multi-User System for Interactive Computing / System Product")) {
            return BIENVENIDA;
        }

        // 2. Pantallas de la aplicación tareas.c (escritura acumulativa hacia abajo)
        // Buscamos la última aparición (lastIndexOf) de los textos característicos de cada menú
        int posPrincipal = texto.lastIndexOf("MENU PRINCIPAL");
        int posAssign = texto.lastIndexOf("ASSIGN TASKS");
        int posView = texto.lastIndexOf("VIEW TASKS");

        // Evaluamos cuál de las opciones de tareas.c aparece más abajo en la pantalla
        int maxPos = Math.max(posPrincipal, Math.max(posAssign, posView));

        if (maxPos != -1) {
            if (maxPos == posPrincipal) {
                return MENU_PRINCIPAL;
            } else if (maxPos == posAssign) {
                return MENU_ASSIGN;
            } else if (maxPos == posView) {
                return MENU_VIEW;
            }
        }

        return HERCULES;
    }
}