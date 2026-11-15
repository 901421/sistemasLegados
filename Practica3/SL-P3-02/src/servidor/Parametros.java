package servidor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Lectura de los parámetros de la dirección de una petición, la parte que va
 * después del "?" (por ejemplo, en /api/nombre?q=DEFENDER, el parámetro q vale
 * DEFENDER).
 *
 * El navegador codifica los caracteres especiales (un espacio es %20 o +, un
 * apóstrofo es %27); aquí se decodifican para obtener el texto que escribió el
 * usuario. Cada parámetro se parte en el primer "=": el resto es el valor, aunque
 * lleve otros "=" (q=A=B vale "A=B").
 */
public final class Parametros {

    private Parametros() {
    }

    /**
     * Valor decodificado de un parámetro.
     *
     * @param query parte de la dirección tras el "?", o null si no hay
     * @param clave nombre del parámetro
     * @return el valor, o null si no aparece o está vacío
     * @throws EntradaNoValida si el valor está mal codificado (un "%" suelto) o el
     *                         parámetro aparece más de una vez
     */
    public static String obtener(String query, String clave) throws EntradaNoValida {
        if (query == null) return null;
        String valor = null;
        boolean visto = false;
        for (String param : query.split("&")) {
            String[] par = param.split("=", 2);
            if (!par[0].equals(clave)) continue;
            if (visto) throw new EntradaNoValida("El parámetro \"" + clave + "\" aparece más de una vez en la dirección.");
            visto = true;
            if (par.length == 2 && !par[1].isEmpty()) {
                try {
                    valor = URLDecoder.decode(par[1], StandardCharsets.UTF_8);
                } catch (IllegalArgumentException e) {
                    throw new EntradaNoValida("La dirección de la consulta no es válida (el parámetro \"" + clave
                            + "\" está mal codificado).");
                }
            }
        }
        return valor;
    }
}
