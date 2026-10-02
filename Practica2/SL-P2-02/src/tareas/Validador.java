package tareas;

/**
 * Comprobaciones de una tarea nueva antes de enviarla al mainframe
 * (decisión D2): fecha DDMM real, sin espacios y longitudes máximas.
 */
public final class Validador {

    /** Descripción de una tarea general. */
    public static final int MAX_DESCRIPCION_GENERAL = 12;

    /** Nombre y descripción de una tarea específica, sumados. */
    public static final int MAX_NOMBRE_MAS_DESCRIPCION = 16;

    private Validador() {
    }

    /**
     * @param nombre vacío para una tarea general
     * @return el mensaje de error para mostrar al usuario, o null si los datos son válidos
     */
    public static String validar(String fecha, String nombre, String descripcion) {
        throw new UnsupportedOperationException("Pendiente");
    }
}
