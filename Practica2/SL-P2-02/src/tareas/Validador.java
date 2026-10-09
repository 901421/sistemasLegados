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

    public Validador() {
    }

    /**
     * @param fecha formato DDMM
     * @param nombre vacío para una tarea general
     * @param descripcion texto de la tarea
     * @return el mensaje de error para mostrar al usuario, o null si los datos son válidos
     */
    public static String validar(String fecha, String nombre, String descripcion) {
        // 1. Validar Fecha (debe ser formato DDMM con valores de día y mes válidos)
        String errorFecha = validarFecha(fecha);
        if (errorFecha != null) {
            return errorFecha;
        }

        // 2. Normalizar nulos
        String nom = (nombre == null) ? "" : nombre.trim();
        String desc = (descripcion == null) ? "" : descripcion.trim();

        if (desc.isEmpty()) {
            return "La descripción no puede estar vacía.";
        }

        // 3. Validar que no contengan espacios
        if (nom.contains(" ")) {
            return "El nombre no puede contener espacios.";
        }

        // 4. Validar longitudes máximas según el tipo
        if (nom.isEmpty()) {
            // Tarea General
            if (desc.length() > MAX_DESCRIPCION_GENERAL) {
                return "La descripción general no puede superar los " + MAX_DESCRIPCION_GENERAL + " caracteres.";
            }
        } else {
            // Tarea Específica
            if ((nom.length() + desc.length()) > MAX_NOMBRE_MAS_DESCRIPCION) {
                return "La suma de nombre y descripción no puede superar los " + MAX_NOMBRE_MAS_DESCRIPCION + " caracteres.";
            }
        }

        return null; // Todo correcto
    }

    private static String validarFecha(String fecha) {
        if (fecha == null || !fecha.matches("\\d{4}")) {
            return "La fecha debe tener exactamente 4 dígitos (DDMM).";
        }

        int dia = Integer.parseInt(fecha.substring(0, 2));
        int mes = Integer.parseInt(fecha.substring(2, 4));

        if (mes < 1 || mes > 12) {
            return "El mes de la fecha debe estar entre 01 y 12.";
        }

        if (dia < 1 || dia > 31) {
            return "El día de la fecha debe estar entre 01 y 31.";
        }

        // Validación simplificada de días máximos según el mes
        if ((mes == 4 || mes == 6 || mes == 9 || mes == 11) && dia > 30) {
            return "El mes indicado no tiene más de 30 días.";
        }
        if (mes == 2 && dia > 29) {
            return "Febrero no tiene más de 29 días.";
        }

        return null;
    }
}