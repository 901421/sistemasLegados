package pruebas;

import legado.Espera;
import legado.FuentePantalla;
import legado.Juego;
import legado.LectorPantalla;
import legado.PantallaLegado;
import legado.PantallaLegado.Marca;
import legado.ParserPantalla;
import legado.ServicioLegado;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pruebas automáticas del wrapper que no necesitan DOSBox ni Windows: se
 * ejecutan en unos segundos en cualquier equipo con Java.
 *
 * En lugar de la ventana de DOSBox usan las 68 capturas reales guardadas en
 * pruebas/conjunto (menú, información, las 43 páginas del listado y varias
 * búsquedas), hechas con pruebas.CapturarConjunto. Cada captura se lee con
 * LectorPantalla, igual que hace el wrapper, y se comprueba lo que sale.
 *
 * Para saber qué debería leerse, la prueba lee DATOS.DAT de la copia de la
 * aplicación (Database-MSDOS), solo para comparar y sin modificarlo. El wrapper
 * nunca lee ese fichero: todo lo que muestra la web sale de la pantalla de la
 * aplicación (R8).
 *
 * También comprueba que la aplicación legada sigue siendo byte a byte la del
 * .rar del profesor (R5), con los SHA-256 de docs/mapa-pantallas.md.
 *
 * Termina con "TODO BIEN" o con el número de fallos (y código de salida 1), como
 * pruebas.PruebaTeclado.
 *
 * Uso, desde SL-P3-02: java -cp "out:lib/*" pruebas.PruebasOffline [CARPETA_CAPTURAS] [CARPETA_APP]
 * (por defecto ../pruebas/conjunto y Database-MSDOS; en Windows, "out;lib/*").
 */
public class PruebasOffline {

    /** SHA-256 de la aplicación legada tal como viene en Database-MSDOS.rar (docs/mapa-pantallas.md, "Comprobación"). */
    private static final Map<String, String> ORIGINAL = new LinkedHashMap<>();
    static {
        ORIGINAL.put("Database/DATABASE.BAS", "393209f8a3dfe5dbdabe6b9126bde31d10d284ef39483f44a30d432cdd39f661");
        ORIGINAL.put("Database/DATOS.DAT", "ca37fc1ccbbc62eccdd545bbdb7846e0c43d511300613d2b7e157f809fc52c11");
        ORIGINAL.put("Database/gwbasic.bat", "de0ec019014604c7d48e7eab6e384b24476cd1b946356693a481cdb6300ab6d8");
        ORIGINAL.put("Database/GWBASIC.EXE", "7c83bec46ef04c3766c41959dd00b92ee7b206557a97bd67d53d11bd3fbe0b79");
        ORIGINAL.put("Database/GWBASIC.PIF", "d27381323bab22e19d5642c8577babcf310427052b7da598c4972df31b98e9a6");
        ORIGINAL.put("Database/INFORM.DAT", "67cc1f9bb83f0564e2a74bb464c2e31255825482926fa8fb6ec0a7b1a575e50c");
        ORIGINAL.put("database.bat", "b76eaabf4f0da32a9f310f7c95a992eb181f1d4253609d19529813c27d45746a");
    }

    private static int casos, fallos;

    public static void main(String[] args) throws Exception {
        File capturas = new File(args.length > 0 ? args[0] : "../pruebas/conjunto");
        File app = new File(args.length > 1 ? args[1] : "Database-MSDOS");

        List<String[]> datos = leerDatos(new File(app, "Database/DATOS.DAT"));
        Map<String, String[]> pantallas = leerCapturas(capturas);

        System.out.println("== Aplicación legada intacta (R5)");
        aplicacionIntacta(app);

        System.out.println("== Marcas de pantalla (PantallaLegado)");
        marcas(pantallas);

        System.out.println("== Número de registros (F1)");
        numeroDeRegistros(pantallas);

        System.out.println("== Búsqueda por nombre (F2)");
        busquedaPorNombre(pantallas, datos);

        System.out.println("== Listado y filtro por cinta (F3)");
        List<Juego> listado = listado(pantallas, datos);
        filtroPorCinta(listado, datos);

        System.out.println("== Esperas (Espera y condiciones de ServicioLegado)");
        esperas(pantallas, capturas);

        System.out.println("== Parámetros de la dirección (servidor.Parametros)");
        parametros();

        System.out.println("== JSON y validación (servidor.Json, servidor.Validacion)");
        jsonYValidacion();

        System.out.println("== API y ficheros de la web, con una aplicación falsa (servidor.ServidorWeb)");
        api(listado);

        System.out.println();
        System.out.println(casos + " casos: " + (fallos == 0 ? "TODO BIEN" : fallos + " FALLOS"));
        if (fallos > 0) System.exit(1);
    }

    /** Los 6 ficheros de Database/ y database.bat deben tener los mismos bytes que en el .rar. */
    private static void aplicacionIntacta(File app) throws Exception {
        for (var e : ORIGINAL.entrySet()) {
            File f = new File(app, e.getKey());
            String hash = f.isFile() ? sha256(f) : "(no existe)";
            comprobar(e.getKey() + " igual que en el .rar", hash.equals(e.getValue()), "SHA-256 " + hash);
        }
        String[] enDatabase = new File(app, "Database").list();
        comprobar("Database/ tiene solo sus 6 ficheros", enDatabase != null && enDatabase.length == 6,
                "contiene " + (enDatabase == null ? "nada" : Arrays.toString(enDatabase)));
    }

    /**
     * Cada marca de PantallaLegado debe aparecer en las capturas de su pantalla
     * y en ninguna otra. ORDENAR, LISTAR_DESDE y BUSCAR_OTRO no tienen captura en
     * pruebas/conjunto: se comprobaron con capturas-1 (primera sesión) y se
     * vuelven a comprobar en la prueba real.
     */
    private static void marcas(Map<String, String[]> pantallas) {
        marcaSoloEn("MENU", PantallaLegado.MENU, pantallas, n -> n.equals("menu"));
        marcaSoloEn("INFO", PantallaLegado.INFO, pantallas, n -> n.equals("informacion"));
        marcaSoloEn("BUSCAR_ES_ESTE", PantallaLegado.BUSCAR_ES_ESTE, pantallas,
                n -> n.startsWith("buscar") && !n.equals("buscar-6-nohay"));
        marcaSoloEn("BUSCAR_NO_HAY", PantallaLegado.BUSCAR_NO_HAY, pantallas, n -> n.endsWith("-nohay"));
        marcaSoloEn("BUSCAR_NOMBRE", PantallaLegado.BUSCAR_NOMBRE, pantallas,
                n -> n.equals("buscar-6-nohay"));
        comprobar("BUSCAR_SABES_NUMERO en buscar-6-nohay", PantallaLegado.BUSCAR_SABES_NUMERO.en(pantallas.get("buscar-6-nohay")), "");
        marcaSoloEn("LISTADO_CABECERA", PantallaLegado.LISTADO_CABECERA, pantallas, n -> n.startsWith("listado"));
        marcaSoloEn("LISTADO_PIE", PantallaLegado.LISTADO_PIE, pantallas, n -> n.startsWith("listado"));
    }

    private static void marcaSoloEn(String nombre, Marca marca, Map<String, String[]> pantallas,
                                    java.util.function.Predicate<String> debeEstar) {
        List<String> mal = new ArrayList<>();
        for (var e : pantallas.entrySet()) {
            if (marca.en(e.getValue()) != debeEstar.test(e.getKey())) mal.add(e.getKey());
        }
        comprobar(nombre + " solo en sus pantallas", mal.isEmpty(), mal.isEmpty() ? "" : "falla en " + mal);
    }

    /** F1: la pantalla de información da 763; otra pantalla no puede dar un número (debe ser un error). */
    private static void numeroDeRegistros(Map<String, String[]> pantallas) throws Exception {
        int total = ParserPantalla.extraerTotalRegistros(pantallas.get("informacion"));
        comprobar("pantalla de información → 763", total == 763, "leído " + total);
        debeFallar("pantalla del MENÚ → error, no un número",
                () -> "leído " + ParserPantalla.extraerTotalRegistros(pantallas.get("menu")));
    }

    /**
     * F2: cada resultado de búsqueda ("Nº - NOMBRE   TIPO   CINTA:c", fila 5) debe
     * coincidir con el registro Nº de DATOS.DAT; una línea que no tiene ese formato
     * debe dar error, no un juego inventado.
     */
    private static void busquedaPorNombre(Map<String, String[]> pantallas, List<String[]> datos) throws Exception {
        int bien = 0, total = 0;
        for (var e : pantallas.entrySet()) {
            String[] lineas = e.getValue();
            if (!e.getKey().startsWith("buscar") || !String.join("\n", lineas).contains("ES ESTE")) continue;
            total++;
            Juego j = ParserPantalla.parsearResultadoBusqueda(lineas);
            String[] esperado = registro(datos, j.getNumero());
            boolean ok = esperado != null && j.getNombre().equals(esperado[0])
                    && j.getTipo().equals(esperado[1]) && j.getCinta().equals(esperado[2]);
            if (ok) bien++;
            else System.out.println("    " + e.getKey() + ": leído " + j.toString() + ", esperado " + Arrays.toString(esperado));
        }
        comprobar("resultados de búsqueda iguales a DATOS.DAT", total > 0 && bien == total, bien + "/" + total);
        String[] dosEspacios = pantallas.get("buscar-1-res1").clone();
        dosEspacios[4] = dosEspacios[4].replace("DEFENDER   ARCADE", "DEFENDER  ARCADE");
        debeFallar("resultado con 2 espacios en vez de 3 → error",
                () -> ParserPantalla.parsearResultadoBusqueda(dosEspacios).toString());
        // El MENÚ tiene líneas con guion ("1 - INTRODUCIR DATOS"), pero ninguna es un resultado.
        debeFallar("pantalla del MENÚ como resultado → error",
                () -> ParserPantalla.parsearResultadoBusqueda(pantallas.get("menu")).toString());
    }

    /** F3: las 43 páginas del listado, en orden, deben dar los 763 registros exactos. */
    private static List<Juego> listado(Map<String, String[]> pantallas, List<String[]> datos) throws Exception {
        List<Juego> todos = new ArrayList<>();
        for (int pagina = 1; pagina <= 43; pagina++) {
            todos.addAll(ParserPantalla.parsearTablaListado(pantallas.get(String.format("listado-pag%02d", pagina))));
        }
        int distintos = 0;
        for (int i = 0; i < todos.size() && i < datos.size(); i++) {
            Juego j = todos.get(i);
            String[] e = datos.get(i);
            String n = String.valueOf(i + 1);
            if (!(j.getNumero().equals(n) && j.getRegistro().equals(n) && j.getNombre().equals(e[0])
                    && j.getTipo().equals(e[1]) && j.getCinta().equals(e[2]))) {
                if (distintos++ < 5) System.out.println("    fila " + n + ": leído " + j.toString() + ", esperado " + Arrays.toString(e));
            }
        }
        comprobar("listado completo = DATOS.DAT", todos.size() == datos.size() && distintos == 0,
                todos.size() + " filas leídas, " + distintos + " distintas");
        debeFallar("pantalla del MENÚ como página del listado → error",
                () -> ParserPantalla.parsearTablaListado(pantallas.get("menu")).size() + " filas leídas");
        boolean paginasBien = true;
        for (int pagina = 1; pagina <= 43; pagina++) {
            int filas = ParserPantalla.parsearTablaListado(pantallas.get(String.format("listado-pag%02d", pagina))).size();
            if (filas != (pagina < 43 ? 18 : 7)) paginasBien = false;
        }
        comprobar("páginas 1-42 con 18 programas y la 43 con 7", paginasBien, "");

        // Filas alteradas a propósito (en una copia en memoria de la página 1).
        String[] hueco = pantallas.get("listado-pag01").clone();
        hueco[6] = " ".repeat(80);
        debeFallar("fila vacía en medio de una página → error",
                () -> ParserPantalla.parsearTablaListado(hueco).size() + " filas leídas");
        String[] letras = pantallas.get("listado-pag01").clone();
        letras[4] = "      X1" + letras[4].substring(8);
        debeFallar("Nº con letras → error", () -> ParserPantalla.parsearTablaListado(letras).size() + " filas leídas");
        return todos;
    }

    /**
     * Cada identificador de cinta debe dar exactamente los programas que lo tienen
     * como parte de su campo cinta (A-45 entra en A y en 45; 1 no coge 10), en el
     * orden de registro.
     */
    private static void filtroPorCinta(List<Juego> listado, List<String[]> datos) {
        Set<String> ids = new TreeSet<>();
        for (String[] d : datos) ids.addAll(Arrays.asList(d[2].split("-")));
        int existentes = ids.size();
        ids.addAll(List.of("45", "99"));
        int distintos = 0;
        for (String id : ids) {
            List<String> esperado = new ArrayList<>();
            for (int i = 0; i < datos.size(); i++) {
                if (Arrays.asList(datos.get(i)[2].split("-")).contains(id)) esperado.add(String.valueOf(i + 1));
            }
            List<String> obtenido = new ArrayList<>();
            for (Juego j : ParserPantalla.filtrarPorCinta(listado, id)) obtenido.add(j.getNumero());
            if (!esperado.equals(obtenido)) {
                distintos++;
                System.out.println("    cinta " + id + ": " + obtenido + ", esperado " + esperado);
            }
        }
        comprobar("filtro de las " + existentes + " cintas existentes, 45 y 99", distintos == 0,
                distintos + " identificadores con otro resultado");
        comprobar("cinta 1 no coge 10 ni 13-26", ParserPantalla.filtrarPorCinta(listado, "1").stream()
                .allMatch(j -> Arrays.asList(j.getCinta().split("-")).contains("1")), "");
        comprobar("cinta 45 (no existe) → ninguno", ParserPantalla.filtrarPorCinta(listado, "45").isEmpty(), "");
    }

    /**
     * Espera.hasta sin DOSBox, con pantallas guardadas: sigue en cuanto la
     * pantalla cumple la condición, da error con lo que había al agotarse el
     * tiempo, y una captura ilegible cuenta como "todavía no". También se prueban
     * las condiciones con que ServicioLegado espera cada resultado y cada página.
     */
    private static void esperas(Map<String, String[]> pantallas, File capturas) throws Exception {
        String[] menu = pantallas.get("menu"), info = pantallas.get("informacion");
        java.util.function.Predicate<String[]> esInfo = PantallaLegado.INFO::en;

        for (long max : new long[]{ServicioLegado.ESPERA_PANTALLA, ServicioLegado.ESPERA_ORDENAR}) {
            long t0 = System.nanoTime();
            String error = "";
            try {
                Espera.hasta(() -> menu, "la información", esInfo, max);
            } catch (legado.PantallaInesperada e) {
                error = e.getMessage();
            }
            long ms = (System.nanoTime() - t0) / 1_000_000;
            comprobar("pantalla que no cambia → error a los " + max / 1000 + " s",
                    Math.abs(ms - max) <= 100 && error.contains("M E N U"), ms + " ms; " + error);
        }

        long inicio = System.nanoTime();
        FuentePantalla cambia = () -> System.nanoTime() - inicio < 200_000_000L ? menu : info;
        Espera.hasta(cambia, "la información", esInfo, ServicioLegado.ESPERA_PANTALLA);
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        comprobar("pantalla que cambia a los 200 ms → sigue a los ~200 ms", ms >= 200 && ms <= 300, ms + " ms");

        // Página a medio dibujar, como en la prueba real del 2026-11-15: el pie ya está y la
        // fila 19 se va escribiendo (crece cada 10 ms); a los 100 ms está la página entera.
        // Supone que, mientras la aplicación escribe, la pantalla cambia en menos de
        // Espera.ESTABLE_MS: lo confirma la prueba real (F3 repetido).
        String[] entera = pantallas.get("listado-pag01");
        String[][] estados = new String[10][];
        for (int j = 0; j < 10; j++) {
            estados[j] = entera.clone();
            estados[j][18] = (entera[18].substring(0, 10 + j * 7) + " ".repeat(80)).substring(0, 80);
            for (int f = 19; f <= 21; f++) estados[j][f] = " ".repeat(80);
        }
        long t1 = System.nanoTime();
        FuentePantalla dibujando = () -> {
            int j = (int) ((System.nanoTime() - t1) / 10_000_000L);
            return j < 10 ? estados[j] : entera;
        };
        String[] leida = Espera.hasta(dibujando, "la página 1", p -> ServicioLegado.paginaQueEmpiezaEn(p, 1),
                ServicioLegado.ESPERA_PANTALLA);
        comprobar("página a medio dibujar → se espera a la página entera", Arrays.equals(leida, entera),
                "fila 19 leída: \"" + leida[18].strip() + "\"");

        // Tres capturas ilegibles (una casilla alterada en una copia en memoria) y luego el MENÚ.
        LectorPantalla lector = LectorPantalla.cargar();
        BufferedImage rota = ImageIO.read(new File(capturas, "menu.png"));
        for (int y = 0; y < 16; y++) rota.setRGB(3, y, 0xAAAAAA);
        int[] llamadas = {0};
        FuentePantalla ilegible = () -> ++llamadas[0] <= 3 ? lector.leer(rota) : menu;
        String[] r = Espera.hasta(ilegible, "el MENÚ", PantallaLegado.MENU::en, ServicioLegado.ESPERA_PANTALLA);
        comprobar("captura ilegible → se vuelve a capturar", PantallaLegado.MENU.en(r) && llamadas[0] > 3,
                llamadas[0] + " capturas (3 ilegibles y las del MENÚ quieto)");

        // Condiciones de F2: resultado nuevo, el mismo resultado otra vez, "NO HAY".
        comprobar("F2: primer resultado (Nº 50) es nuevo", ServicioLegado.resultadoNuevo(pantallas.get("buscar-1-res1"), 0), "");
        comprobar("F2: segundo resultado (Nº 323) es nuevo tras el 50",
                ServicioLegado.resultadoNuevo(pantallas.get("buscar-1-res2"), 50), "");
        comprobar("F2: el mismo resultado otra vez no es nuevo",
                !ServicioLegado.resultadoNuevo(pantallas.get("buscar-1-res1"), 50), "");
        boolean ningunNoHay = true;
        for (var e : pantallas.entrySet()) {
            if (e.getKey().endsWith("-nohay") && ServicioLegado.resultadoNuevo(e.getValue(), 0)) ningunNoHay = false;
        }
        comprobar("F2: \"NO HAY\" (con el último resultado visible) nunca cuenta como resultado nuevo", ningunNoHay, "");

        // Condiciones de F3: cada página empieza en 1, 19, 37...; el MENÚ no es una página.
        boolean paginas = true;
        for (int pagina = 1; pagina <= 43; pagina++) {
            String[] p = pantallas.get(String.format("listado-pag%02d", pagina));
            int primero = (pagina - 1) * 18 + 1;
            if (!ServicioLegado.paginaQueEmpiezaEn(p, primero) || ServicioLegado.paginaQueEmpiezaEn(p, primero - 18)) paginas = false;
        }
        comprobar("F3: cada página empieza en su Nº (1, 19, 37…) y no en el de la anterior", paginas, "");
        comprobar("F3: el MENÚ no es una página", !ServicioLegado.paginaQueEmpiezaEn(menu, 1), "");
    }

    /**
     * Lectura de ?q= e ?id=: se parte en el primer "=", se decodifica, y una
     * codificación inválida o un parámetro repetido son un error de entrada (D3).
     */
    private static void parametros() {
        String[][] bien = {
                {"q=DEFENDER", "DEFENDER"}, {"x=1&q=BC%27S", "BC'S"}, {"q=A=B", "A=B"}, {"q=a+b", "a b"},
                {"q=", null}, {null, null}, {"x=1", null}};
        for (String[] c : bien) {
            String obtenido;
            try {
                obtenido = servidor.Parametros.obtener(c[0], "q");
            } catch (Exception e) {
                obtenido = "lanza " + e.getClass().getSimpleName();
            }
            comprobar("q de \"" + c[0] + "\" → " + (c[1] == null ? "ninguno" : "\"" + c[1] + "\""),
                    java.util.Objects.equals(obtenido, c[1]), "obtenido " + obtenido);
        }
        for (String q : new String[]{"q=%", "q=%G1", "q=A&q=B"}) {
            debeFallarCon("q de \"" + q + "\" → error de entrada", servidor.EntradaNoValida.class,
                    () -> servidor.Parametros.obtener(q, "q"));
        }
    }

    /** Cadena JSON válida (comillas, escapes permitidos y ningún carácter de control suelto). */
    private static final String CADENA_JSON = "\"(?:[^\"\\\\\\x00-\\x1f]|\\\\[\"\\\\/bfnrt]|\\\\u[0-9a-fA-F]{4})*\"";
    private static final Pattern ERROR_JSON = Pattern.compile(
            "\\{\"error\":(" + CADENA_JSON + "),\"tipo\":\"(entrada|ocupado|legado|interno)\"\\}");
    private static final String JUEGO_JSON = "\\{\"numero\":" + CADENA_JSON + ",\"nombre\":" + CADENA_JSON
            + ",\"tipo\":" + CADENA_JSON + ",\"cinta\":" + CADENA_JSON + "(?:,\"registro\":" + CADENA_JSON + ")?\\}";
    private static final Pattern LISTA_JSON = Pattern.compile("\\[(?:" + JUEGO_JSON + "(?:," + JUEGO_JSON + ")*)?\\]");

    /** Lee una cadena JSON (con sus escapes) y devuelve el texto original. */
    private static String leerCadena(String json) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < json.length() - 1; i++) {
            char c = json.charAt(i);
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            char e = json.charAt(++i);
            switch (e) {
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 't' -> sb.append('\t');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'u' -> {
                    sb.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                    i += 4;
                }
                default -> sb.append(e);
            }
        }
        return sb.toString();
    }

    /** Json.texto escapa todo lo necesario (se comprueba volviendo a leerlo) y la validación aplica las reglas de D3. */
    private static void jsonYValidacion() {
        String raro = "BC'S \"Q\" \\ barra\nsalto\ttab\u0001 ñ";
        String json = servidor.Json.texto(raro);
        comprobar("Json.texto: comillas, barra, salto, tabulador y control → cadena JSON válida e igual al leerla",
                json.matches(CADENA_JSON) && leerCadena(json).equals(raro), json);
        comprobar("Json.texto(null) → null", servidor.Json.texto(null).equals("null"), "");
        comprobar("Json.error → objeto válido", ERROR_JSON.matcher(servidor.Json.error("legado", raro)).matches(),
                servidor.Json.error("legado", raro));

        java.util.function.IntPredicate escribible = c -> PROHIBIDOS.indexOf(c) < 0;
        String[][] nombres = {{"DEFENDER", "DEFENDER"}, {"  DEFENDER  ", "DEFENDER"}, {"friday", "friday"},
                {"BC'S Q-?", "BC'S Q-?"}, {"A=B", "A=B"}, {"X".repeat(30), "X".repeat(30)}};
        for (String[] c : nombres) {
            String obtenido;
            try {
                obtenido = servidor.Validacion.nombre(c[0], escribible);
            } catch (Exception e) {
                obtenido = "error: " + e.getMessage();
            }
            comprobar("nombre \"" + c[0] + "\" → \"" + c[1] + "\"", c[1].equals(obtenido), obtenido);
        }
        for (String n : new String[]{null, "", "   ", "X".repeat(31), "A,B", "A\"B", "ESPAÑA", "#1", "a@b"}) {
            debeFallarCon("nombre " + (n == null ? "null" : "\"" + n + "\"") + " → error de entrada",
                    servidor.EntradaNoValida.class, () -> servidor.Validacion.nombre(n, escribible));
        }
        for (String[] c : new String[][]{{"A", "A"}, {" a ", "A"}, {"12", "12"}, {"0", "0"}}) {
            String obtenido;
            try {
                obtenido = servidor.Validacion.cinta(c[0]);
            } catch (Exception e) {
                obtenido = "error: " + e.getMessage();
            }
            comprobar("cinta \"" + c[0] + "\" → \"" + c[1] + "\"", c[1].equals(obtenido), obtenido);
        }
        for (String id : new String[]{null, "", "A-45", "AB", "1A", "Ñ", "-"}) {
            debeFallarCon("cinta " + (id == null ? "null" : "\"" + id + "\"") + " → error de entrada",
                    servidor.EntradaNoValida.class, () -> servidor.Validacion.cinta(id));
        }
    }

    /**
     * Caracteres que Robot no puede escribir en DOSBox con el teclado español,
     * según pruebas.CapturarJuegoCaracteres (apartado 4). La aplicación falsa los
     * usa en lugar de Teclado.puedeEscribir, que necesita DOSBox abierto.
     */
    private static final String PROHIBIDOS = "[\\^`{}ñÑºªç#@]|~";

    /** Una aplicación legada falsa: respuestas fijas y registro de lo que se le pide. */
    private static class AplicacionFalsa implements legado.AplicacionLegada {
        final List<String> llamadas = new ArrayList<>();
        final List<Juego> listado;
        Exception fallo;

        AplicacionFalsa(List<Juego> listado) {
            this.listado = listado;
        }

        private void quizaFalla() throws Exception {
            if (fallo != null) {
                Exception e = fallo;
                fallo = null;
                throw e;
            }
        }

        @Override
        public int obtenerTotalRegistros() throws Exception {
            llamadas.add("registros");
            quizaFalla();
            return 763;
        }

        @Override
        public List<Juego> buscarPorNombre(String nombre) throws Exception {
            llamadas.add("nombre:" + nombre);
            quizaFalla();
            return List.of(new Juego("50", "DEFENDER", "ARCADE", "C", ""), new Juego("323", "DEFENDER", "ARCADE", "V", ""));
        }

        @Override
        public List<Juego> listarPorCinta(String idCinta) throws Exception {
            llamadas.add("cinta:" + idCinta);
            quizaFalla();
            return ParserPantalla.filtrarPorCinta(listado, idCinta);
        }

        @Override
        public boolean puedeEscribir(char c) {
            return PROHIBIDOS.indexOf(c) < 0;
        }
    }

    /** Una respuesta HTTP: código, cabeceras y cuerpo. */
    private record Respuesta(int codigo, java.net.http.HttpHeaders cabeceras, String cuerpo) {
    }

    /**
     * Petición HTTP escrita a mano por un socket, para poder enviar direcciones mal
     * formadas (como ?q=%) que el cliente HTTP de Java se niega a enviar.
     */
    private static Respuesta pedirCrudo(int puerto, String ruta) throws Exception {
        try (java.net.Socket socket = new java.net.Socket("127.0.0.1", puerto)) {
            socket.getOutputStream().write(("GET " + ruta + " HTTP/1.1\r\nHost: 127.0.0.1\r\nConnection: close\r\n\r\n")
                    .getBytes(StandardCharsets.US_ASCII));
            String todo = new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int codigo = Integer.parseInt(todo.substring(9, 12));
            int fin = todo.indexOf("\r\n\r\n");
            return new Respuesta(codigo, null, fin < 0 ? "" : todo.substring(fin + 4));
        }
    }

    private static Respuesta pedir(java.net.http.HttpClient cliente, int puerto, String metodo, String ruta) throws Exception {
        var peticion = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://127.0.0.1:" + puerto + ruta))
                .method(metodo, java.net.http.HttpRequest.BodyPublishers.noBody()).build();
        var r = cliente.send(peticion, java.net.http.HttpResponse.BodyHandlers.ofString());
        return new Respuesta(r.statusCode(), r.headers(), r.body());
    }

    /**
     * El servidor web completo (rutas, API y ficheros) en un puerto libre, con la
     * aplicación falsa: códigos, formato JSON, que la entrada no válida no llega a
     * la aplicación, que no hay cabeceras CORS y que no se sale de frontend/.
     */
    private static void api(List<Juego> listado) throws Exception {
        AplicacionFalsa falsa = new AplicacionFalsa(listado);
        servidor.ServidorWeb web = new servidor.ServidorWeb(0, falsa, java.nio.file.Path.of("frontend"));
        web.arrancar();
        var cliente = java.net.http.HttpClient.newHttpClient();
        int puerto = web.puerto();
        java.io.PrintStream consola = System.out;
        try {
            Respuesta r = pedir(cliente, puerto, "GET", "/api/registros");
            comprobar("GET /api/registros → 200 {\"totalRegistros\":763}", r.codigo == 200
                    && r.cuerpo.equals("{\"totalRegistros\":763}")
                    && r.cabeceras.firstValue("Content-Type").orElse("").startsWith("application/json"), r.codigo + " " + r.cuerpo);
            comprobar("sin cabeceras CORS", r.cabeceras.firstValue("Access-Control-Allow-Origin").isEmpty(), "");

            falsa.llamadas.clear();
            r = pedir(cliente, puerto, "GET", "/api/nombre?q=%20DEFENDER%20");
            comprobar("GET /api/nombre?q=%20DEFENDER%20 → 200, lista válida, la aplicación recibe \"DEFENDER\"",
                    r.codigo == 200 && LISTA_JSON.matcher(r.cuerpo).matches() && falsa.llamadas.equals(List.of("nombre:DEFENDER"))
                            && !r.cuerpo.contains("registro"), r.codigo + " " + r.cuerpo + " " + falsa.llamadas);

            r = pedir(cliente, puerto, "GET", "/api/cinta?id=a");
            comprobar("GET /api/cinta?id=a → 200, 18 programas con registro, la aplicación recibe \"A\"",
                    r.codigo == 200 && LISTA_JSON.matcher(r.cuerpo).matches() && r.cuerpo.split("\"numero\"").length == 19
                            && r.cuerpo.contains("\"registro\":\"1\"") && falsa.llamadas.getLast().equals("cinta:A"),
                    r.codigo + " " + falsa.llamadas.getLast());

            // Dirección mal formada: la rechaza con 400 el propio HttpServer del JDK, antes de
            // llegar a la API (con su página HTML, no con nuestro JSON). El navegador nunca la
            // envía: la web codifica el "%" como %25.
            falsa.llamadas.clear();
            r = pedirCrudo(puerto, "/api/nombre?q=%");
            comprobar("GET /api/nombre?q=% (mal formada) → 400 del JDK sin tocar la aplicación",
                    r.codigo == 400 && falsa.llamadas.isEmpty(), r.codigo + " " + r.cuerpo);

            String[] malas = {"/api/nombre", "/api/nombre?q=", "/api/nombre?q=A%2CB", "/api/nombre?q=A%22B",
                    "/api/nombre?q=ESPA%C3%91A", "/api/nombre?q=%23", "/api/nombre?q=A&q=B",
                    "/api/nombre?q=" + "X".repeat(31), "/api/cinta", "/api/cinta?id=A-45", "/api/cinta?id=AB"};
            for (String ruta : malas) {
                falsa.llamadas.clear();
                r = pedirCrudo(puerto, ruta);
                Matcher m = ERROR_JSON.matcher(r.cuerpo);
                comprobar("GET " + ruta + " → 400 \"entrada\" sin tocar la aplicación", r.codigo == 400 && m.matches()
                        && m.group(2).equals("entrada") && falsa.llamadas.isEmpty(),
                        r.codigo + " " + (m.matches() ? leerCadena(m.group(1)) : r.cuerpo));
            }

            Object[][] fallos = {
                    {new legado.PantallaInesperada("el MENÚ en 5 s", "algo"), 503, "legado"},
                    {new IllegalStateException("DOSBox ha dejado de estar delante"), 503, "legado"},
                    {new RuntimeException("fallo \"raro\"\ncon salto"), 500, "interno"}};
            for (Object[] f : fallos) {
                falsa.fallo = (Exception) f[0];
                System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream())); // la traza del 500
                r = pedir(cliente, puerto, "GET", "/api/registros");
                System.setOut(consola);
                Matcher m = ERROR_JSON.matcher(r.cuerpo);
                comprobar("la aplicación lanza " + f[0].getClass().getSimpleName() + " → " + f[1] + " \"" + f[2] + "\"",
                        r.codigo == (int) f[1] && m.matches() && m.group(2).equals(f[2]),
                        r.codigo + " " + (m.matches() ? leerCadena(m.group(1)) : r.cuerpo));
            }

            for (String metodo : new String[]{"POST", "OPTIONS"}) {
                falsa.llamadas.clear();
                r = pedir(cliente, puerto, metodo, "/api/registros");
                comprobar(metodo + " /api/registros → 405 sin tocar la aplicación", r.codigo == 405 && falsa.llamadas.isEmpty(),
                        r.codigo + " Allow: " + r.cabeceras.firstValue("Allow").orElse(""));
            }

            String[][] ficheros = {{"/", "200"}, {"/index.html", "200"}, {"/styles/main.css", "200"}, {"/js/app.js", "200"},
                    {"/styles/main.scss", "404"}, {"/../src/Main.java", "404"}, {"/%2e%2e/build.bat", "404"},
                    {"/..%2fbuild.bat", "404"}, {"/favicon.ico", "404"}};
            for (String[] f : ficheros) {
                var peticion = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://127.0.0.1:" + puerto + f[0])).build();
                int codigo = cliente.send(peticion, java.net.http.HttpResponse.BodyHandlers.discarding()).statusCode();
                comprobar("fichero " + f[0] + " → " + f[1], String.valueOf(codigo).equals(f[1]), String.valueOf(codigo));
            }
        } finally {
            System.setOut(consola);
            web.parar();
        }
    }

    /** Como debeFallar, pero la excepción tiene que ser de ese tipo. */
    private static void debeFallarCon(String caso, Class<? extends Exception> tipo, java.util.concurrent.Callable<String> accion) {
        try {
            comprobar(caso, false, "no da error: " + accion.call());
        } catch (Exception e) {
            comprobar(caso, tipo.isInstance(e), e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    // ---- Utilidades ----------------------------------------------------------

    private static void comprobar(String caso, boolean ok, String detalle) {
        casos++;
        if (!ok) fallos++;
        System.out.println((ok ? "  bien   " : "  FALLO  ") + caso + (detalle.isEmpty() ? "" : "  (" + detalle + ")"));
    }

    /** El caso está bien si la acción lanza una excepción; si devuelve algo, es un dato inventado. */
    private static void debeFallar(String caso, java.util.concurrent.Callable<String> accion) {
        try {
            String resultado = accion.call();
            comprobar(caso, false, "no da error: " + resultado);
        } catch (Exception e) {
            comprobar(caso, true, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /** Lee todas las capturas de la carpeta, por nombre sin ".png". */
    private static Map<String, String[]> leerCapturas(File carpeta) throws Exception {
        File[] pngs = carpeta.listFiles((d, n) -> n.endsWith(".png"));
        if (pngs == null || pngs.length == 0) throw new IOException("No hay capturas en " + carpeta.getAbsolutePath());
        Arrays.sort(pngs);
        LectorPantalla lector = LectorPantalla.cargar();
        Map<String, String[]> pantallas = new LinkedHashMap<>();
        for (File f : pngs) pantallas.put(f.getName().replace(".png", ""), lector.leer(ImageIO.read(f)));
        return pantallas;
    }

    /**
     * Registros de DATOS.DAT, en orden: una línea por programa con tres campos
     * entre comillas (nombre, tipo y cinta). El registro N es la línea N.
     */
    private static List<String[]> leerDatos(File datos) throws IOException {
        Pattern campo = Pattern.compile("\"([^\"]*)\"");
        List<String[]> registros = new ArrayList<>();
        for (String linea : Files.readAllLines(datos.toPath(), StandardCharsets.ISO_8859_1)) {
            Matcher m = campo.matcher(linea);
            List<String> campos = new ArrayList<>();
            while (m.find()) campos.add(m.group(1));
            if (campos.size() == 3) registros.add(campos.toArray(new String[0]));
        }
        return registros;
    }

    private static String[] registro(List<String[]> datos, String numero) {
        try {
            int n = Integer.parseInt(numero);
            return n >= 1 && n <= datos.size() ? datos.get(n - 1) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String sha256(File f) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(f.toPath())));
    }
}
