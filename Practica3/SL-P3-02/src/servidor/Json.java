package servidor;

import legado.Juego;

import java.util.List;

/**
 * Construcción de las respuestas JSON de la API.
 *
 * JSON es el formato de texto con el que el navegador y el servidor se pasan
 * los datos, por ejemplo [{"nombre":"DEFENDER","cinta":"C"}]. Se escribe a mano
 * para no añadir librerías, y todo texto pasa por texto(), que lo escapa: así una
 * comilla o un salto de línea en un nombre o en un mensaje de error no rompen la
 * respuesta (D7).
 */
public final class Json {

    private Json() {
    }

    /**
     * Un texto como cadena JSON, entre comillas y con los caracteres especiales
     * escapados (comillas, barra invertida y caracteres de control). null da null.
     */
    public static String texto(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder(s.length() + 2).append('"');
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }

    /** Respuesta de F1: {"totalRegistros":763,"pantalla":[...]}. */
    public static String registros(legado.Resultado<Integer> r) {
        return "{\"totalRegistros\":" + r.datos() + ",\"pantalla\":" + pantalla(r.pantalla()) + "}";
    }

    /** Respuesta de F2 y F3: {"juegos":[...],"pantalla":[...]}. */
    public static String juegos(legado.Resultado<List<Juego>> r) {
        return "{\"juegos\":" + juegos(r.datos()) + ",\"pantalla\":" + pantalla(r.pantalla()) + "}";
    }

    /** Las líneas de la pantalla de la aplicación, como lista de cadenas (o null si no hay). */
    public static String pantalla(String[] lineas) {
        if (lineas == null) return "null";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lineas.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(texto(lineas[i]));
        }
        return sb.append("]").toString();
    }

    /**
     * La lista de programas. El registro solo se incluye si la aplicación lo ha
     * mostrado (en la búsqueda no aparece).
     */
    public static String juegos(List<Juego> juegos) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < juegos.size(); i++) {
            Juego j = juegos.get(i);
            if (i > 0) json.append(",");
            json.append("{\"numero\":").append(texto(j.getNumero()))
                    .append(",\"nombre\":").append(texto(j.getNombre()))
                    .append(",\"tipo\":").append(texto(j.getTipo()))
                    .append(",\"cinta\":").append(texto(j.getCinta()));
            if (!j.getRegistro().isEmpty()) json.append(",\"registro\":").append(texto(j.getRegistro()));
            json.append("}");
        }
        return json.append("]").toString();
    }

    /**
     * Respuesta de error: {"error":"mensaje para el usuario","tipo":"entrada"}.
     *
     * @param tipo entrada (400), ocupado (503), legado (503) o interno (500)
     */
    public static String error(String tipo, String mensaje) {
        return "{\"error\":" + texto(mensaje) + ",\"tipo\":" + texto(tipo) + "}";
    }
}
