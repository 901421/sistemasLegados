package legado;

import java.util.ArrayList;
import java.util.List;

public class ParserPantalla {

    /* Extrae el número de la fila 5 */
    public static int extraerTotalRegistros(String[] lineas) {
        if (lineas != null && lineas.length > 4) {
            String num = lineas[4].replaceAll("[^0-9]", "");
            if (!num.isEmpty()) {
                return Integer.parseInt(num);
            }
        }
        return 0;
    }

    /*  Extrae los campos de la fila 5 en la Opción 7 (Buscar por nombre) */
    public static Juego parsearLineaBusqueda(String linea) {
        if (linea == null || linea.isBlank() || !linea.contains("-")) return null;

        try {
            // Ejemplo: " 50  -  DEFENDER   ARCADE   CINTA:C"
            String[] partesGuion = linea.split("-", 2);
            String numero = partesGuion[0].trim();

            String resto = partesGuion[1].trim();
            String cinta = "";
            if (resto.contains("CINTA:")) {
                String[] partesCinta = resto.split("CINTA:");
                cinta = partesCinta[1].trim();
                resto = partesCinta[0].trim();
            }

            // Separar Nombre y Tipo por espacios múltiples
            String[] datos = resto.split("\\s{2,}");
            String nombre = datos.length > 0 ? datos[0].trim() : "";
            String tipo = datos.length > 1 ? datos[1].trim() : "";

            return new Juego(numero, nombre, tipo, cinta, "");
        } catch (Exception e) {
            return new Juego("", linea.trim(), "", "", "");
        }
    }

    /* Extrae las filas del listado recortando por posiciones de caracteres */
    public static List<Juego> parsearTablaListado(String[] lineas) {
        List<Juego> lista = new ArrayList<>();
        if (lineas == null) return lista;

        for (int i = 4; i <= 21 && i < lineas.length; i++) {
            String l = lineas[i];
            if (l.isBlank() || l.contains("PULSA SPACE")) continue;

            if (l.length() >= 60) {
                String num = recortar(l, 0, 8);
                String nombre = recortar(l, 12, 45);
                String tipo = recortar(l, 45, 65);
                String cinta = recortar(l, 65, 70);
                String reg = recortar(l, 70, 80);

                if (!nombre.isEmpty()) {
                    lista.add(new Juego(num, nombre, tipo, cinta, reg));
                }
            }
        }
        return lista;
    }

    /* Filtra la lista asegurando que la cinta coincida de verdad */
    public static List<Juego> filtrarPorCinta(List<Juego> juegos, String idCinta) {
        List<Juego> filtrados = new ArrayList<>();
        if (juegos == null || idCinta == null) return filtrados;

        for (Juego j : juegos) {
            // Comprobamos si la cinta es exactamente igual
            if (esMismaCinta(j.getCinta(), idCinta)) {
                filtrados.add(j);
            }
        }
        return filtrados;
    }

    private static boolean esMismaCinta(String cintaJuego, String idBuscado) {
        if (cintaJuego.equalsIgnoreCase(idBuscado)) return true;
        // Si el juego está en varias cintas tipo "A-45" o "B-E", comprobar las partes
        String[] partes = cintaJuego.split("-");
        for (String p : partes) {
            if (p.trim().equalsIgnoreCase(idBuscado)) return true;
        }
        return false;
    }

    private static String recortar(String texto, int inicio, int fin) {
        if (inicio >= texto.length()) return "";
        int finReal = Math.min(fin, texto.length());
        return texto.substring(inicio, finReal).trim();
    }
}