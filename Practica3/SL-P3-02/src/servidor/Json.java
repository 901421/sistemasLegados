package servidor;

import legado.Juego;

import java.util.List;

/**
 * Construcción de las respuestas JSON de la API.
 *
 * JSON es el formato de texto con el que el navegador y el servidor se pasan
 * los datos, por ejemplo [{"nombre":"DEFENDER","cinta":"C"}]. Se escribe a mano
 * para no añadir librerías: cada Juego sabe convertirse con Juego.toJson().
 */
public final class Json {

    private Json() {
    }

    /** Convierte una lista de objetos Juego a un string en formato JSON. */
    public static String lista(List<Juego> juegos) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < juegos.size(); i++) {
            json.append(juegos.get(i).toJson());
            if (i < juegos.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }
}
