package tareas;

import java.io.IOException;
import java.util.List;

import mainframe.MainframeClient;

/**
 * Operaciones de la aplicación legada: listar tareas y crear una nueva.
 * Cada operación empieza y termina en el menú principal de tareas.c.
 * Es lo único que usa la interfaz gráfica para llegar a los datos.
 */
public class GestorTareas {

    private final MainframeClient cliente;

    public GestorTareas(MainframeClient cliente) {
        this.cliente = cliente;
    }

    /**
     * Tareas generales y específicas, ordenadas por número. Recorre todas
     * las páginas de cada listado ("More...").
     */
    public List<Tarea> listar() throws IOException {
        throw new UnsupportedOperationException("Pendiente");
    }

    /**
     * Crea una tarea: general si el nombre está vacío, específica si no.
     * Los datos deben haber pasado antes por {@link Validador#validar}.
     *
     * @throws IOException si tareas.c no confirma el alta
     */
    public void nueva(String fecha, String nombre, String descripcion) throws IOException {
        throw new UnsupportedOperationException("Pendiente");
    }
}
