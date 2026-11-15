package legado;

/**
 * Dónde está cada cosa en las pantallas de la aplicación legada: las marcas que
 * dicen en qué pantalla está la aplicación y las filas y columnas de los datos.
 *
 * Todo sale de docs/mapa-pantallas.md (que cita las líneas de DATABASE.BAS,
 * L&lt;nº&gt;) y está comprobado con las capturas reales de pruebas/conjunto y de
 * la primera sesión (capturas-1). Las filas y columnas se cuentan como en el
 * mapa, desde 1 (fila 1-25, columna 1-80); la conversión a los índices de Java,
 * que empiezan en 0, se hace solo aquí.
 *
 * Si la aplicación mostrara algo en otro sitio, se corrige en esta clase y en el
 * mapa, y PruebasOffline lo comprueba con las capturas.
 */
public final class PantallaLegado {

    private PantallaLegado() {
    }

    /**
     * Un texto fijo que la aplicación escribe siempre en la misma posición y que
     * identifica una pantalla (por ejemplo, "M E N U" en la fila 4, columna 32).
     */
    public record Marca(int fila, int columna, String texto) {

        /** true si la pantalla tiene este texto en esta posición. */
        public boolean en(String[] pantalla) {
            return linea(pantalla, fila).startsWith(texto, columna - 1);
        }

        @Override
        public String toString() {
            return "\"" + texto + "\" en la fila " + fila + ", columna " + columna;
        }
    }

    /** Un campo de ancho fijo de una fila: de la columna desde a la hasta, ambas incluidas. */
    public record Columna(int desde, int hasta) {

        /** El texto del campo, sin los espacios de relleno. */
        public String de(String linea) {
            if (linea.length() < hasta) return "";
            return linea.substring(desde - 1, hasta).trim();
        }
    }

    /** La fila f (desde 1) de la pantalla, o "" si no existe. */
    public static String linea(String[] pantalla, int fila) {
        return pantalla != null && fila >= 1 && fila <= pantalla.length ? pantalla[fila - 1] : "";
    }

    // ---- Menú principal (L100-L180) -----------------------------------------

    /** El menú principal: la aplicación espera una tecla del 1 al 8 (INKEY$). */
    public static final Marca MENU = new Marca(4, 32, "M E N U");

    // ---- Opción 4: información (L4000-L4200) → F1 ---------------------------

    /** Fila del número de registros: "CONTIENE  763  ARCHIVOS". */
    public static final int INFO_FILA_TOTAL = 5;
    /** La pantalla de información: "CONTIENE" al principio de la fila 5. */
    public static final Marca INFO = new Marca(INFO_FILA_TOTAL, 1, "CONTIENE");
    /** "PULSA ENTER           ?" en la fila 24: lo último que se dibuja; se pulsa Enter para volver. */
    public static final Marca INFO_PIE = new Marca(24, 1, "PULSA ENTER");

    // ---- Opción 7: búsqueda (L7000-L7090) → F2 ------------------------------

    /** "SABES EL NUMERO DE REGISTRO QUE QUIERES BUSCAR? (S/N)": se responde N. */
    public static final Marca BUSCAR_SABES_NUMERO = new Marca(5, 1, "SABES EL NUMERO");
    /** "DIME EL NOMBRE DEL PROGRAMA QUE QUIERES VER": se escribe el nombre. */
    public static final Marca BUSCAR_NOMBRE = new Marca(10, 1, "DIME EL NOMBRE");
    /** Fila de cada resultado: "     50  - DEFENDER   ARCADE   CINTA:C" (L7060). */
    public static final int BUSCAR_FILA_RESULTADO = 5;
    /** "ES ESTE ? (S/N)" bajo cada resultado: se responde siempre N (S lleva a alterar los datos). */
    public static final Marca BUSCAR_ES_ESTE = new Marca(7, 1, "ES ESTE ?");
    /**
     * La fila 7 tal como queda cuando acaba de llegar un resultado y la aplicación
     * espera la respuesta. Cuando ya se ha contestado, queda "... ?  N" (y así se ve
     * también bajo "NO HAY NINGUN"). Comprobado en las 22 capturas de búsqueda.
     */
    public static final String BUSCAR_ES_ESTE_SIN_RESPONDER = "ES ESTE ? (S/N)  ?";

    /** true si la fila 7 es "ES ESTE ? (S/N)  ?" sin responder: hay un resultado nuevo completo. */
    public static boolean esperaRespuestaEsEste(String[] pantalla) {
        return linea(pantalla, BUSCAR_ES_ESTE.fila()).stripTrailing().equals(BUSCAR_ES_ESTE_SIN_RESPONDER);
    }
    /**
     * "NO HAY NINGUN PROGRAMA CON ESE NOMBRE; PULSA ENTER": no hay (más)
     * coincidencias. Ojo: la aplicación no borra la pantalla antes, así que siguen
     * visibles el último resultado (fila 5) y su "ES ESTE ?" (fila 7).
     */
    public static final Marca BUSCAR_NO_HAY = new Marca(12, 1, "NO HAY NINGUN PROGRAMA");
    /** "QUIERES BUSCAR ALGUNO MAS? S/N": se responde N para volver al menú. */
    public static final Marca BUSCAR_OTRO = new Marca(10, 10, "QUIERES BUSCAR ALGUNO MAS");

    // ---- Opción 3: ordenación (L3000-L3300) → paso previo de F3 -------------

    /** "¿ SEGUN QUE CAMPO QUIERES ORDENAR LOS DATOS ?": se responde 4 (ANTIGUEDAD). */
    public static final Marca ORDENAR = new Marca(21, 17, "SEGUN QUE CAMPO");

    // ---- Opción 6: listado (L6000-L6060) → F3 -------------------------------

    /** "INTRODUCE EL DATO DEL PRIMER PROGRAMA A VISUALIZAR": Enter para empezar por el primero. */
    public static final Marca LISTAR_DESDE = new Marca(7, 1, "INTRODUCE EL DATO");
    /** Cabecera de cada página del listado: "Nº    NOMBRE    TIPO    CINTA    REGISTRO". */
    public static final Marca LISTADO_CABECERA = new Marca(3, 7, "Nº");
    /** Pie de cada página: "PULSA SPACE PARA CONTINUAR U OTRA TECLA PARA ACABAR". */
    public static final Marca LISTADO_PIE = new Marca(25, 1, "PULSA SPACE");
    /** Filas de los programas en cada página (18 por página; la última, la 43, tiene 7). */
    public static final int LISTADO_PRIMERA_FILA = 5, LISTADO_ULTIMA_FILA = 22;
    /** 18 programas por página. */
    public static final int LISTADO_FILAS_POR_PAGINA = LISTADO_ULTIMA_FILA - LISTADO_PRIMERA_FILA + 1;
    /** Columnas de cada campo del listado (L6020): el Nº y el registro van alineados a la derecha. */
    public static final Columna LISTADO_NUMERO = new Columna(1, 8);
    /** NOMBRE: empieza en la columna 13, hasta 30 caracteres. */
    public static final Columna LISTADO_NOMBRE = new Columna(13, 45);
    /** TIPO: empieza en la columna 46, hasta 15 caracteres. */
    public static final Columna LISTADO_TIPO = new Columna(46, 65);
    /** CINTA: empieza en la columna 66, hasta 5 caracteres (como "B-E" o "13-26"). */
    public static final Columna LISTADO_CINTA = new Columna(66, 70);
    /** REGISTRO: alineado a la derecha, termina en la columna 77. */
    public static final Columna LISTADO_REGISTRO = new Columna(71, 80);
}
