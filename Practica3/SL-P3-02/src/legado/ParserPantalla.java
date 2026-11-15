package legado;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static legado.PantallaLegado.*;

/**
 * Convierte el texto de una pantalla de la aplicación legada (las 25 líneas que
 * devuelve LectorPantalla) en datos: el número de registros, un resultado de la
 * búsqueda o los programas de una página del listado.
 *
 * Cada método comprueba que la pantalla tiene exactamente el formato que
 * imprime DATABASE.BAS (docs/mapa-pantallas.md). Si no lo tiene, lanza
 * PantallaInesperada con la fila leída, en lugar de devolver un dato
 * aproximado: así la web nunca muestra algo que la aplicación no ha dicho (R20).
 */
public class ParserPantalla {

    private ParserPantalla() {
    }

    /** "CONTIENE  763  ARCHIVOS" (L4000-L4200; PRINT numérico, con sus espacios). */
    private static final Pattern TOTAL = Pattern.compile("CONTIENE +(\\d+) +ARCHIVOS");

    /**
     * "     50  - DEFENDER   ARCADE   CINTA:C" (L7060: N;" - ";N$;"   ";T$;"   CINTA:";C$).
     * PRINT de un número deja un espacio delante y otro detrás, de ahí los dos
     * espacios antes del guion. Nombre y tipo van separados por 3 espacios
     * exactos; ningún nombre de los datos tiene 2 espacios seguidos.
     */
    private static final Pattern RESULTADO = Pattern.compile(" +(\\d+)  - (.+?)   (.+?)   CINTA:(\\S+)");

    /**
     * Número de registros de la pantalla de información (opción 4).
     *
     * @throws PantallaInesperada si la fila 5 no es "CONTIENE n ARCHIVOS"
     */
    public static int extraerTotalRegistros(String[] pantalla) throws PantallaInesperada {
        String fila = linea(pantalla, INFO_FILA_TOTAL).stripTrailing();
        Matcher m = TOTAL.matcher(fila);
        if (!m.matches()) throw new PantallaInesperada("\"CONTIENE n ARCHIVOS\" en la fila " + INFO_FILA_TOTAL, fila);
        return Integer.parseInt(m.group(1));
    }

    /**
     * El resultado que muestra la búsqueda (opción 7) en la fila 5. La búsqueda
     * no muestra el registro, así que ese campo queda vacío.
     *
     * @throws PantallaInesperada si la fila 5 no tiene el formato de un resultado
     */
    public static Juego parsearResultadoBusqueda(String[] pantalla) throws PantallaInesperada {
        String fila = linea(pantalla, BUSCAR_FILA_RESULTADO).stripTrailing();
        Matcher m = RESULTADO.matcher(fila);
        if (!m.matches()) {
            throw new PantallaInesperada("\"Nº  - NOMBRE   TIPO   CINTA:c\" en la fila " + BUSCAR_FILA_RESULTADO, fila);
        }
        return new Juego(m.group(1), m.group(2), m.group(3), m.group(4), "");
    }

    /**
     * Los programas de una página del listado (opción 6), en el orden en que
     * aparecen. Las filas vacías solo pueden ir al final (la última página tiene 7).
     *
     * @throws PantallaInesperada si la pantalla no es una página del listado
     *                            (sin cabecera o sin pie) o alguna fila no tiene
     *                            Nº y registro numéricos y nombre, tipo y cinta
     */
    public static List<Juego> parsearTablaListado(String[] pantalla) throws PantallaInesperada {
        if (!LISTADO_CABECERA.en(pantalla)) {
            throw new PantallaInesperada("la cabecera del listado (" + LISTADO_CABECERA + ")", linea(pantalla, 3).stripTrailing());
        }
        if (!LISTADO_PIE.en(pantalla)) {
            throw new PantallaInesperada("el pie del listado (" + LISTADO_PIE + ")", linea(pantalla, 25).stripTrailing());
        }
        List<Juego> lista = new ArrayList<>();
        boolean finDeDatos = false;
        for (int f = LISTADO_PRIMERA_FILA; f <= LISTADO_ULTIMA_FILA; f++) {
            String l = linea(pantalla, f);
            if (l.isBlank()) {
                finDeDatos = true;
                continue;
            }
            String num = LISTADO_NUMERO.de(l);
            String nombre = LISTADO_NOMBRE.de(l);
            String tipo = LISTADO_TIPO.de(l);
            String cinta = LISTADO_CINTA.de(l);
            String reg = LISTADO_REGISTRO.de(l);
            if (finDeDatos || !num.matches("\\d+") || !reg.matches("\\d+")
                    || nombre.isEmpty() || tipo.isEmpty() || cinta.isEmpty()) {
                throw new PantallaInesperada("una fila del listado (Nº, NOMBRE, TIPO, CINTA, REGISTRO) en la fila " + f,
                        l.stripTrailing());
            }
            lista.add(new Juego(num, nombre, tipo, cinta, reg));
        }
        return lista;
    }

    /**
     * Los programas de la lista que están en esa cinta, en el mismo orden. Se
     * compara cada parte del campo cinta: "A-45" está en la A y en la 45, y la 1
     * no coge la 10 ni la 13-26 (R11, R12).
     */
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
}
