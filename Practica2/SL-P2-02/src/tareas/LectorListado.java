package tareas;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LectorListado {

    // Regex para detectar tareas generales: TASK <num>: GENERAL <fecha> ----- <descripcion>
    private static final Pattern PATTERN_GENERAL = Pattern.compile(
            "TASK\\s+(\\d+):\\s+GENERAL\\s+(\\d{4})\\s+-----\\s+(.+)"
    );

    // Regex para detectar tareas específicas: TASK <num>: SPECIFIC <fecha> <nombre> <descripcion>
    private static final Pattern PATTERN_SPECIFIC = Pattern.compile(
            "TASK\\s+(\\d+):\\s+SPECIFIC\\s+(\\d{4})\\s+(\\S+)\\s+(.+)"
    );

    private final Validador validator;

    public LectorListado(Validador validator) {
        this.validator = validator;
    }

    /**
     * Parsea la pantalla actual y extrae la lista de tareas válidas.
     */
    public List<Tarea> parsearPantalla(String pantallaTexto) {
        List<Tarea> tareasEncontradas = new ArrayList<>();
        if (pantallaTexto == null || pantallaTexto.isBlank()) {
            return tareasEncontradas;
        }

        // Dividimos la pantalla en líneas
        String[] lineas = pantallaTexto.split("\\r?\\n");

        for (String linea : lineas) {
            String lineaLimpia = linea.trim();

            if (lineaLimpia.startsWith("TASK")) {
                Tarea tarea = procesarLineaTarea(lineaLimpia);
                if (tarea != null) {
                    // Comprobamos que los datos sean válidos
                    boolean esValida = (validator == null) ||
                            (Validador.validar(tarea.fecha(), tarea.nombre(), tarea.descripcion()) == null);

                    if (esValida) {
                        tareasEncontradas.add(tarea);
                    }
                }
            }
        }

        return tareasEncontradas;
    }

    private Tarea procesarLineaTarea(String linea) {
        // 1. Intentar parsear como Tarea General
        Matcher matcherGen = PATTERN_GENERAL.matcher(linea);
        if (matcherGen.find()) {
            int id = Integer.parseInt(matcherGen.group(1));
            String fecha = matcherGen.group(2);
            String descripcion = matcherGen.group(3).trim();

            // En tareas generales el nombre va vacío ("") según la especificación
            return new Tarea(id, Tarea.Tipo.GENERAL, fecha, "", descripcion);
        }

        // 2. Intentar parsear como Tarea Específica
        Matcher matcherSpec = PATTERN_SPECIFIC.matcher(linea);
        if (matcherSpec.find()) {
            int id = Integer.parseInt(matcherSpec.group(1));
            String fecha = matcherSpec.group(2);
            String nombre = matcherSpec.group(3).trim();
            String descripcion = matcherSpec.group(4).trim();

            return new Tarea(id, Tarea.Tipo.ESPECIFICA, fecha, nombre, descripcion);
        }

        return null;
    }
}