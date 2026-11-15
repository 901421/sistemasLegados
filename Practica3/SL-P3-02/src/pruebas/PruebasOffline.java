package pruebas;

import legado.Juego;
import legado.LectorPantalla;
import legado.PantallaLegado;
import legado.PantallaLegado.Marca;
import legado.ParserPantalla;

import javax.imageio.ImageIO;
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
            else System.out.println("    " + e.getKey() + ": leído " + j.toJson() + ", esperado " + Arrays.toString(esperado));
        }
        comprobar("resultados de búsqueda iguales a DATOS.DAT", total > 0 && bien == total, bien + "/" + total);
        String[] dosEspacios = pantallas.get("buscar-1-res1").clone();
        dosEspacios[4] = dosEspacios[4].replace("DEFENDER   ARCADE", "DEFENDER  ARCADE");
        debeFallar("resultado con 2 espacios en vez de 3 → error",
                () -> ParserPantalla.parsearResultadoBusqueda(dosEspacios).toJson());
        // El MENÚ tiene líneas con guion ("1 - INTRODUCIR DATOS"), pero ninguna es un resultado.
        debeFallar("pantalla del MENÚ como resultado → error",
                () -> ParserPantalla.parsearResultadoBusqueda(pantallas.get("menu")).toJson());
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
                if (distintos++ < 5) System.out.println("    fila " + n + ": leído " + j.toJson() + ", esperado " + Arrays.toString(e));
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
