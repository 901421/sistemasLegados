package tareas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mainframe.MainframeClient;

/**
 * Operaciones de la aplicación legada: listar tareas y crear una nueva.
 * Cada operación empieza y termina en el menú principal de tareas.c.
 * Es lo único que usa la interfaz gráfica para llegar a los datos.
 */
public class GestorTareas {

    private final MainframeClient cliente;
    private final LectorListado lector;

    public GestorTareas(MainframeClient cliente) {
        this.cliente = cliente;
        this.lector = new LectorListado(new Validador());
    }

    /**
     * Tareas generales y específicas, ordenadas por número. Recorre todas
     * las páginas de cada listado ("More...").
     */
    public List<Tarea> listar() throws IOException {
        List<Tarea> todasLasTareas = new ArrayList<>();

        // 1. Desde el MENU PRINCIPAL de tareas.c, elegimos la opción de ver/listar tareas (opción "2")
        cliente.escribir("2");
        cliente.intro();

        boolean listadoCompleto = false;

        while (!listadoCompleto) {
            // 2. Esperamos a que la pantalla se dibuje y estabilice
            String pantallaTexto = cliente.esperarPantallaEstable(10_000);

            // 3. Extraemos las tareas presentes en la lectura actual de pantalla
            List<Tarea> tareasPaginaActual = lector.parsearPantalla(pantallaTexto);

            // 4. Guardamos/acumulamos las tareas obtenidas (evitando duplicados si en el scroll repite líneas)
            for (Tarea t : tareasPaginaActual) {
                if (!todasLasTareas.contains(t)) {
                    todasLasTareas.add(t);
                }
            }

            // 5. Comprobar únicamente la ÚLTIMA LÍNEA mediante la función de MainframeClient
            if (MainframeClient.hayMas(pantallaTexto)) {
                // Si la última línea es "More...", la pantalla está llena y requiere Intro
                cliente.intro();
            } else {
                // Si la última línea NO es "More...", ya hemos llegado al final del listado
                listadoCompleto = true;
            }
        }

        // 6. Al terminar de volcar todo el listado, enviamos un Intro final para
        // salir del listado y regresar al Menú Principal de tareas.c
        cliente.intro();
        cliente.esperarPantallaEstable(10_000);

        return todasLasTareas;
    }

    /**
     * Crea una tarea: general si el nombre está vacío, específica si no.
     */
    public void nueva(String fecha, String nombre, String descripcion) throws IOException {
        // 1. Entramos al menú de asignar tareas (Opción "1" desde el MENU PRINCIPAL)
        cliente.escribir("1");
        cliente.intro();
        cliente.esperarPantallaEstable(5_000);

        // 2. Enviamos la fecha y confirmamos con Intro
        cliente.escribir(fecha);
        cliente.intro();
        cliente.esperarPantallaEstable(5_000);

        // 3. Si tiene nombre, es una tarea Específica; si no, es General
        if (nombre != null && !nombre.isBlank()) {
            cliente.escribir(nombre);
            cliente.intro();
            cliente.esperarPantallaEstable(5_000);
        }

        // 4. Enviamos la descripción y confirmamos
        cliente.escribir(descripcion);
        cliente.intro();

        // 5. Esperamos a que la pantalla se estabilice tras el alta
        cliente.esperarPantallaEstable(10_000);
    }
}