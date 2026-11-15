package legado;

import java.util.function.Predicate;

/**
 * Espera a que la aplicación legada muestre una pantalla concreta, en lugar de
 * esperar un tiempo fijo (D2).
 *
 * Después de pulsar una tecla, DOSBox tarda en dibujar la pantalla siguiente un
 * tiempo que no se conoce de antemano (16-83 ms medidos con cycles=max, más si el
 * equipo va cargado). Con una pausa fija, o se espera de más o se lee la pantalla
 * anterior. Aquí se captura una y otra vez hasta que la pantalla cumple la
 * condición (por ejemplo, que se vea la marca del MENÚ) y se sigue en ese mismo
 * momento. El tiempo máximo solo cuenta si algo falla: entonces se para con un
 * error que dice qué se esperaba y qué había.
 */
public final class Espera {

    /** Pausa entre dos capturas: cada captura y su lectura llevan unos 20 ms más. */
    private static final int PAUSA_MS = 10;

    private Espera() {
    }

    /**
     * Captura hasta que la pantalla cumple la condición.
     *
     * Una captura con un dibujo desconocido (CeldaDesconocida, típica si DOSBox
     * está a medio redibujar) cuenta como "todavía no" y se vuelve a capturar.
     *
     * @param fuente      de dónde sale la pantalla
     * @param descripcion qué se espera, para el mensaje de error
     * @param condicion   cuándo es la pantalla buena
     * @param maxMs       tiempo máximo, en milisegundos
     * @return las 25 líneas de la primera pantalla que cumple la condición
     * @throws PantallaInesperada si se agota el tiempo; dice qué había en pantalla
     */
    public static String[] hasta(FuentePantalla fuente, String descripcion, Predicate<String[]> condicion, long maxMs)
            throws Exception {
        long limite = System.nanoTime() + maxMs * 1_000_000L;
        String[] ultima = null;
        Exception ultimoError = null;
        while (true) {
            try {
                String[] pantalla = fuente.capturar();
                if (condicion.test(pantalla)) return pantalla;
                ultima = pantalla;
            } catch (LectorPantalla.CeldaDesconocida e) {
                ultimoError = e;
            }
            if (System.nanoTime() >= limite) break;
            Thread.sleep(PAUSA_MS);
        }
        String habia = ultima != null ? resumen(ultima)
                : "(ninguna captura legible: " + ultimoError.getMessage() + ")";
        String segundos = maxMs % 1000 == 0 ? String.valueOf(maxMs / 1000) : String.valueOf(maxMs / 1000.0).replace('.', ',');
        throw new PantallaInesperada(descripcion + " en " + segundos + " s", habia);
    }

    /** Las primeras líneas con texto de la pantalla, separadas por " | ", para los mensajes de error. */
    static String resumen(String[] pantalla) {
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (String l : pantalla) {
            if (l.isBlank()) continue;
            if (n++ == 3) {
                sb.append(" | …");
                break;
            }
            if (sb.length() > 0) sb.append(" | ");
            sb.append(l.strip());
        }
        return n == 0 ? "(pantalla vacía)" : sb.toString();
    }
}
