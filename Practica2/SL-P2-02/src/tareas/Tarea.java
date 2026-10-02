package tareas;

/**
 * Una tarea tal como la muestra el listado de tareas.c:
 * {@code TASK <numero>: <GENERAL|SPECIFIC> <fecha> <nombre> <descripcion>}.
 *
 * @param numero      número que le da tareas.c (común a los dos tipos)
 * @param tipo        general o específica
 * @param fecha       texto tal cual lo guarda tareas.c (no valida el formato DDMM)
 * @param nombre      vacío en las tareas generales (el listado muestra "-----")
 * @param descripcion una sola palabra
 */
public record Tarea(int numero, Tipo tipo, String fecha, String nombre, String descripcion) {

    public enum Tipo { GENERAL, ESPECIFICA }
}
