package pruebas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tareas.GestorTareas;
import tareas.Tarea;

/**
 * Gestor de tareas sin mainframe, para probar la interfaz: guarda las tareas
 * en memoria y tarda un segundo en cada operación, como si esperase al
 * mainframe. Una descripción "error" simula un fallo del alta.
 */
public class GestorFalso extends GestorTareas {

    private static final long RETARDO_MS = 1000;

    private final List<Tarea> tareas = new ArrayList<>(List.of(
            new Tarea(1, Tarea.Tipo.GENERAL, "1210", "", "examen"),
            new Tarea(2, Tarea.Tipo.ESPECIFICA, "1510", "Paula", "memoria"),
            new Tarea(3, Tarea.Tipo.GENERAL, "1910", "", "entrega"),
            new Tarea(4, Tarea.Tipo.ESPECIFICA, "0910", "Ainhoa", "login"),
            new Tarea(5, Tarea.Tipo.ESPECIFICA, "1310", "Imad", "integración"),
            new Tarea(6, Tarea.Tipo.GENERAL, "0111", "", "revisión")));

    public GestorFalso() {
        super(null);
    }

    @Override
    public synchronized List<Tarea> listar() throws IOException {
        esperar();
        return List.copyOf(tareas);
    }

    @Override
    public synchronized void nueva(String fecha, String nombre, String descripcion) throws IOException {
        esperar();
        if (descripcion.equals("error")) {
            throw new IOException("Fallo simulado: tareas.c no ha confirmado el alta.");
        }
        Tarea.Tipo tipo = nombre.isEmpty() ? Tarea.Tipo.GENERAL : Tarea.Tipo.ESPECIFICA;
        tareas.add(new Tarea(tareas.size() + 1, tipo, fecha, nombre, descripcion));
    }

    private static void esperar() throws IOException {
        try {
            Thread.sleep(RETARDO_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Operación interrumpida.");
        }
    }
}
