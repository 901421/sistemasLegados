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
 * usuario.
 */
public final class Parametros {

    private Parametros() {
    }

    /**
     * Extrae el valor de un parámetro de la URL (ej. ?q=DEFENDER).
     *
     * @param query parte de la dirección tras el "?", o null si no hay
     * @param clave nombre del parámetro
     * @return el valor decodificado, o null si no aparece o está vacío
     */
    public static String obtener(String query, String clave) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] par = param.split("=");
            if (par.length > 1 && par[0].equals(clave)) {
                return URLDecoder.decode(par[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
