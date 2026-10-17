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
    /** tareas.c: en mitad de un alta, pide la fecha, el nombre o la descripción ("ENTER ..."). */
    FORMULARIO,
    /** Ninguna de las anteriores. */
    DESCONOCIDA;

    /**
     * Indica a qué pantalla corresponde un texto devuelto por
     * {@link MainframeClient#pantalla()}. tareas.c escribe hacia abajo sin
     * limpiar, así que cuenta el último menú que aparece.
     */
    public static Pantalla identificar(String texto) {
        if (texto == null || texto.isBlank()) {
            return DESCONOCIDA;
        }

        // 1. Pantallas globales de sistema / inicio de sesión
        if (MainframeClient.esHercules(texto)) {
            return HERCULES;
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

        String[] lineas = texto.split("\\r?\\n");

        if (enFormulario(lineas)) {
            return FORMULARIO;
        }

        // Recorremos las líneas comenzando desde el final del buffer de pantalla
        for (int i = lineas.length - 1; i >= 0; i--) {
            String linea = lineas[i].trim();

            // Si detectamos la línea de opciones de un submenú:
            if (linea.contains("1. GENERAL TASK") && linea.contains("2.SPECIFIC TASK") && linea.contains("3. MAIN MENU")) {
                // Comprobamos la línea JUSTO ENCIMA
                if (i > 0) {
                    String lineaTitulo = lineas[i - 1].trim();

                    if (lineaTitulo.contains("VIEW TASKS")) {
                        return MENU_VIEW;
                    }
                    if (lineaTitulo.contains("ASSIGN TASKS")) {
                        return MENU_ASSIGN;
                    }
                }
            }

            if (linea.equals("MENU PRINCIPAL")) {
                return MENU_PRINCIPAL;
            }
        }

        return DESCONOCIDA;
    }

    /**
     * Si lo último que ha escrito tareas.c (encima de la línea "------T---")
     * es una pregunta "ENTER ...:", sola o seguida del "?" que espera respuesta.
     */
    private static boolean enFormulario(String[] lineas) {
        String ultima = null;
        String penultima = null;
        for (String linea : lineas) {
            if (linea.startsWith("------T")) {
                break;
            }
            if (!linea.isBlank()) {
                penultima = ultima;
                ultima = linea.trim();
            }
        }
        if (ultima == null) {
            return false;
        }
        if (ultima.equals("?")) {
            return penultima != null && esPreguntaDeAlta(penultima);
        }
        return esPreguntaDeAlta(ultima);
    }

    private static boolean esPreguntaDeAlta(String linea) {
        return linea.contains("ENTER ") && linea.endsWith(":");
    }
}
