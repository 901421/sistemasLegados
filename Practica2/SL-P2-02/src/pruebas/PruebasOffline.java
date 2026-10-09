package pruebas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import tareas.LectorListado;
import tareas.Tarea;
import tareas.Validador;

/**
 * Pruebas que no se conectan al mainframe: usan las pantallas guardadas en
 * img/a1 e img/a2 (ficheros .txt). Se ejecuta desde la carpeta SL-P2-02 y
 * termina con código distinto de 0 si alguna comprobación falla.
 */
public class PruebasOffline {

    private static final Path PANTALLAS = Path.of("img");
    private static int fallos = 0;

    public static void main(String[] args) throws IOException {
        Validador validator = new Validador();
        LectorListado lector = new LectorListado(validator);

        // --- PRUEBA 1: Detección de caracteres especiales (tildes y 'ñ') ---
        String txtTildes = pantalla("a2/23-listado-tildes");
        List<Tarea> tareasTildes = lector.parsearPantalla(txtTildes);
        comprobar("Parseo de tildes/ñ encuentra tareas", !tareasTildes.isEmpty());
        if (!tareasTildes.isEmpty()) {
            comprobar("Soporta descripción con tilde ('Camión')",
                    tareasTildes.stream().anyMatch(t -> t.descripcion().contains("Camión")));
            comprobar("Soporta descripción con 'ñ' ('Año')",
                    tareasTildes.stream().anyMatch(t -> t.descripcion().contains("Año")));
        }

        // --- PRUEBA 2: Control de paginación ("More...") ---
        String txtPaginacion = pantalla("a2/31-listado-largo-p1");
        comprobar("Detecta indicador 'More...' correctamente", lector.hayMasPaginas(txtPaginacion));

        // --- PRUEBA 3: Control de fin de listado ---
        String txtFin = pantalla("a2/31-listado-largo-p2");
        comprobar("Detecta fin de listado / TOTAL TASK", lector.esFinDeListado(txtFin));
        comprobar("No detecta 'More...' cuando ya ha finalizado", !lector.hayMasPaginas(txtFin));

        // --- PRUEBA 4: Nombres en Tareas Específicas ---
        String txtEspacios = pantalla("a2/30-limite-31-caracteres");
        List<Tarea> tareasEspecificas = lector.parsearPantalla(txtEspacios);
        comprobar("Detecta nombres asignados ('Ana')",
                tareasEspecificas.stream().anyMatch(t -> "Ana".equals(t.nombre())));

        // Resumen final
        System.out.println("\n------------------------------------");
        System.out.println(fallos == 0 ? "RESULTADO: TODO BIEN" : "RESULTADO: FALLOS ENCONTRADOS -> " + fallos);
        System.exit(fallos == 0 ? 0 : 1);
    }

    /** Lee una pantalla guardada, por ejemplo {@code pantalla("a2/12-listado-general")}. */
    static String pantalla(String nombre) throws IOException {
        return Files.readString(PANTALLAS.resolve(nombre + ".txt"), StandardCharsets.UTF_8);
    }

    /** Anota un fallo si la condición no se cumple. */
    static void comprobar(String descripcion, boolean condicion) {
        if (!condicion) {
            fallos++;
        }
        System.out.println((condicion ? "ok    " : "FALLO ") + descripcion);
    }
}