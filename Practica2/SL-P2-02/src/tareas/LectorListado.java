package tareas;

import java.util.List;

/**
 * Extrae las tareas del texto de una pantalla de listado. Solo analiza
 * texto: no habla con el mainframe, así que se prueba con pantallas guardadas.
 */
public final class LectorListado {

    private LectorListado() {
    }

    /** Tareas de las líneas "TASK n: ..." de una pantalla, en el orden en que aparecen. */
    public static List<Tarea> tareas(String pantalla) {
        throw new UnsupportedOperationException("Pendiente");
    }

    /** Indica si la pantalla contiene el final del listado ("TOTAL TASK"). */
    public static boolean finListado(String pantalla) {
        throw new UnsupportedOperationException("Pendiente");
    }
}
