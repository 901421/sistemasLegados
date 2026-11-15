package pruebas;

import net.sourceforge.tess4j.Tesseract1;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.UnaryOperator;

/**
 * Mide el OCR (Tess4J + spa.traineddata) sobre el conjunto de capturas reales
 * de CapturarConjunto, con distintas configuraciones, para la memoria (R24).
 *
 * El texto que DEBERÍA salir en cada captura se genera aquí reproduciendo los
 * LOCATE/PRINT de DATABASE.BAS con los datos de DATOS.DAT. Leer DATOS.DAT es
 * solo para esta medición offline: el wrapper nunca lo lee (R8).
 *
 * Uso (en Windows, la DLL de Tesseract es de Windows):
 *   java --enable-native-access=ALL-UNNAMED -cp "out;lib/*" pruebas.MedirOcr
 *        CARPETA_CONJUNTO DATOS.DAT CARPETA_TESSDATA FICHERO_RESULTADOS.md
 */
public class MedirOcr {

    private MedirOcr() {
    }

    // ---------------------------------------------------------------- pantallas esperadas

    /** Pantalla de texto de 80x25, escrita como hace GW-BASIC con LOCATE y PRINT. */
    static final class Pantalla {
        final char[][] celdas = new char[25][80];
        /** Filas que no se puntúan (su contenido no se puede saber de antemano). */
        final boolean[] ignorar = new boolean[25];

        Pantalla() {
            for (char[] fila : celdas) Arrays.fill(fila, ' ');
        }

        /** LOCATE fila, col : PRINT texto (fila y columna desde 1). */
        Pantalla en(int fila, int col, String texto) {
            for (int i = 0; i < texto.length() && col - 1 + i < 80; i++) {
                celdas[fila - 1][col - 1 + i] = texto.charAt(i);
            }
            return this;
        }

        /** Líneas no vacías que se puntúan, sin espacios sobrantes. */
        List<String> lineas() {
            List<String> r = new ArrayList<>();
            for (int f = 0; f < 25; f++) {
                if (ignorar[f]) continue;
                String l = normalizar(new String(celdas[f]));
                if (!l.isEmpty()) r.add(l);
            }
            return r;
        }
    }

    /** PRINT de un número en BASIC: espacio para el signo delante y un espacio detrás. */
    private static String num(int n) {
        return " " + n + " ";
    }

    record Registro(int numero, String nombre, String tipo, String cinta) {}

    static List<Registro> leerDatos(File datos) throws IOException {
        List<Registro> r = new ArrayList<>();
        for (String linea : Files.readAllLines(datos.toPath(), Charset.forName("IBM437"))) {
            // Formato de WRITE#: "NOMBRE","TIPO","CINTA" (sin comas ni comillas dentro).
            String[] campos = linea.split("\",\"");
            if (campos.length != 3) continue;
            r.add(new Registro(r.size() + 1, campos[0].substring(1), campos[1],
                    campos[2].substring(0, campos[2].length() - 1)));
        }
        return r;
    }

    private static Pantalla menu() {
        return new Pantalla().en(4, 32, "M E N U")
                .en(7, 25, "1 - INTRODUCIR DATOS").en(9, 25, "2 - GRABAR DATOS")
                .en(11, 25, "3 - ORDENAR DATOS").en(13, 25, "4 - INFORMACION BASE DE DATOS")
                .en(15, 25, "5 - IMPRIMIR DATOS SEGUN ORDEN").en(17, 25, "6 - LISTAR DATOS SEGUN ORDEN")
                .en(19, 25, "7 - BUSCAR DATOS").en(22, 25, "8 - ACABAR");
    }

    private static Pantalla informacion(int total) {
        Pantalla p = new Pantalla().en(2, 27, "INFORMACION BASE DE DATOS")
                .en(5, 1, "CONTIENE " + num(total) + " ARCHIVOS")
                .en(7, 1, "LA BASE DE DATOS ESTA ORDENADA SEGUN EL CAMPO 'ANTIGUEDAD'.")
                .en(24, 1, "PULSA ENTER           ?");
        p.ignorar[9] = true; // fila 10: MEMORIA DISPONIBLE = n BYTES (n cambia con el uso)
        return p;
    }

    /** Página del listado (L6011-L6030) con orden ANTIGUEDAD: registro n en la posición n. */
    private static Pantalla pagina(List<Registro> datos, int numPagina) {
        Pantalla p = new Pantalla().en(3, 1,
                "      Nº    NOMBRE                           TIPO              CINTA    REGISTRO");
        int primero = (numPagina - 1) * 18 + 1;
        for (int y = 5, n = primero; y < 23 && n <= datos.size(); y++, n++) {
            Registro r = datos.get(n - 1);
            String nStr = " " + n;
            p.en(y, 9 - nStr.length(), nStr).en(y, 13, r.nombre()).en(y, 46, r.tipo()).en(y, 66, r.cinta());
            String ord = String.valueOf(n);
            p.en(y, 77 - ord.length(), " " + ord);
        }
        return p.en(25, 1, "PULSA SPACE PARA CONTINUAR U OTRA TECLA PARA ACABAR");
    }

    /*
     * INPUT "texto";V (con punto y coma) imprime además "? " tras el texto;
     * INPUT "texto",V (con coma) no. "ES ESTE ? (S/N)  " (L7062) usa punto y coma.
     */

    /** Línea del resultado de la búsqueda (L7060), desde la columna 5. */
    private static String resultado(Registro r) {
        return num(r.numero()) + " - " + r.nombre() + "   " + r.tipo() + "   CINTA:" + r.cinta();
    }

    /** Las pantallas esperadas del conjunto, por nombre de fichero. */
    static Map<String, Pantalla> esperadas(List<Registro> datos) {
        Map<String, Pantalla> m = new LinkedHashMap<>();
        m.put("menu", menu());
        m.put("informacion", informacion(datos.size()));
        int paginas = (datos.size() + 17) / 18;
        for (int k = 1; k <= paginas; k++) m.put(String.format("listado-pag%02d", k), pagina(datos, k));
        List<String> busquedas = CapturarConjunto.BUSQUEDAS;
        for (int b = 0; b < busquedas.size(); b++) {
            String texto = busquedas.get(b);
            List<Registro> encontrados = datos.stream().filter(r -> r.nombre().contains(texto)).toList();
            for (int i = 0; i < encontrados.size(); i++) {
                m.put(String.format("buscar-%d-res%d", b + 1, i + 1),
                        new Pantalla().en(5, 5, resultado(encontrados.get(i))).en(7, 1, "ES ESTE ? (S/N)  ? "));
            }
            Pantalla noHay;
            if (encontrados.isEmpty()) {
                noHay = new Pantalla().en(1, 30, "LOCALIZACION DE DATOS")
                        .en(5, 1, "SABES EL NUMERO DE REGISTRO QUE QUIERES BUSCAR? (S/N) N")
                        .en(10, 1, "DIME EL NOMBRE DEL PROGRAMA QUE QUIERES VER     " + texto);
            } else {
                noHay = new Pantalla().en(5, 5, resultado(encontrados.getLast())).en(7, 1, "ES ESTE ? (S/N)  ? N");
            }
            noHay.en(12, 1, "NO HAY NINGUN PROGRAMA CON ESE NOMBRE; PULSA ENTER").en(13, 1, "?");
            m.put(String.format("buscar-%d-nohay", b + 1), noHay);
        }
        return m;
    }

    // ---------------------------------------------------------------- comparación

    /** Quita el cursor (_) del final, los espacios de los extremos y junta los espacios seguidos. */
    static String normalizar(String s) {
        String t = s.strip();
        while (t.endsWith("_")) t = t.substring(0, t.length() - 1).strip();
        return t.replaceAll("\\s+", " ");
    }

    private static List<String> lineasOcr(String texto) {
        List<String> r = new ArrayList<>();
        for (String l : texto.split("\\R")) {
            String n = normalizar(l);
            if (!n.isEmpty()) r.add(n);
        }
        return r;
    }

    private static int distancia(String a, String b) {
        int[] prev = new int[b.length() + 1], cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int coste = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + coste);
            }
            int[] t = prev; prev = cur; cur = t;
        }
        return prev[b.length()];
    }

    /** Sustituciones carácter a carácter (esperado→leído) entre dos líneas de igual longitud. */
    private static void confusiones(String esperada, String leida, Map<String, Integer> cuenta) {
        if (esperada.length() != leida.length()) return;
        for (int i = 0; i < esperada.length(); i++) {
            if (esperada.charAt(i) != leida.charAt(i)) {
                cuenta.merge("'" + esperada.charAt(i) + "'→'" + leida.charAt(i) + "'", 1, Integer::sum);
            }
        }
    }

    // ---------------------------------------------------------------- configuraciones

    /** idioma: fichero .traineddata; oem: motor de Tesseract (3 = el que tenga el fichero, 1 = LSTM). */
    private record Config(String nombre, String preproceso, UnaryOperator<BufferedImage> imagen,
                          int psm, boolean listaBlanca, boolean sinDiccionario, String idioma, int oem) {
        Config(String nombre, String preproceso, UnaryOperator<BufferedImage> imagen,
               int psm, boolean listaBlanca, boolean sinDiccionario) {
            this(nombre, preproceso, imagen, psm, listaBlanca, sinDiccionario, "spa", 3);
        }
    }

    private static BufferedImage escalar(BufferedImage img, int factor) {
        if (factor == 1) return img;
        BufferedImage r = new BufferedImage(img.getWidth() * factor, img.getHeight() * factor, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < r.getHeight(); y++)
            for (int x = 0; x < r.getWidth(); x++) r.setRGB(x, y, img.getRGB(x / factor, y / factor));
        return r;
    }

    /** Texto negro sobre fondo blanco, con el gris original (85,85,85 tras invertir). */
    private static BufferedImage invertir(BufferedImage img) {
        BufferedImage r = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++) r.setRGB(x, y, ~img.getRGB(x, y) & 0xFFFFFF);
        return r;
    }

    /** Blanco y negro puros: texto negro, fondo blanco. */
    private static BufferedImage binarizar(BufferedImage img) {
        BufferedImage r = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                r.setRGB(x, y, ((img.getRGB(x, y) >> 16) & 0xFF) > 128 ? 0x000000 : 0xFFFFFF);
        return r;
    }

    private static UnaryOperator<BufferedImage> preproceso(String nombre) {
        return switch (nombre) {
            case "tal cual" -> i -> i;
            case "invertida" -> MedirOcr::invertir;
            case "binarizada" -> MedirOcr::binarizar;
            case "binarizada x2" -> i -> escalar(binarizar(i), 2);
            case "binarizada x3" -> i -> escalar(binarizar(i), 3);
            case "binarizada x4" -> i -> escalar(binarizar(i), 4);
            case "tal cual x2" -> i -> escalar(i, 2);
            case "tal cual x3" -> i -> escalar(i, 3);
            default -> throw new IllegalArgumentException(nombre);
        };
    }

    private record Resultado(Config config, int caracteres, int errores, int lineas, int lineasBien,
                             int registros, int registrosBien, long ms,
                             Map<String, Integer> confusiones, List<String[]> ejemplos) {
        double precision() { return 100.0 * (caracteres - errores) / caracteres; }
        double pctLineas() { return 100.0 * lineasBien / lineas; }
        double pctRegistros() { return 100.0 * registrosBien / registros; }
    }

    private static Resultado medir(Config c, Map<String, BufferedImage> capturas, Map<String, Pantalla> esperadas,
                                   File tessdata, String listaBlanca) throws Exception {
        Tesseract1 ocr = new Tesseract1();
        ocr.setDatapath(tessdata.getAbsolutePath());
        ocr.setLanguage(c.idioma());
        ocr.setOcrEngineMode(c.oem());
        ocr.setPageSegMode(c.psm());
        ocr.setVariable("user_defined_dpi", "96");
        if (c.listaBlanca()) ocr.setVariable("tessedit_char_whitelist", listaBlanca);
        if (c.sinDiccionario()) ocr.setConfigs(List.of("sin-diccionario"));

        int caracteres = 0, errores = 0, lineas = 0, lineasBien = 0, registros = 0, registrosBien = 0;
        Map<String, Integer> conf = new HashMap<>();
        List<String[]> ejemplos = new ArrayList<>();
        long inicio = System.currentTimeMillis();
        for (var e : esperadas.entrySet()) {
            BufferedImage img = capturas.get(e.getKey());
            if (img == null) throw new IOException("Falta la captura " + e.getKey() + ".png");
            List<String> esperado = e.getValue().lineas();
            List<String> leido = lineasOcr(ocr.doOCR(c.imagen().apply(img)));

            String textoEsperado = String.join("\n", esperado);
            caracteres += textoEsperado.length();
            errores += distancia(textoEsperado, String.join("\n", leido));

            List<String> pendientes = new ArrayList<>(leido);
            for (String l : esperado) {
                lineas++;
                if (pendientes.remove(l)) lineasBien++;
            }
            if (e.getKey().startsWith("listado")) {
                // Filas de datos: las que empiezan por el número de orden.
                for (String l : esperado) {
                    if (!Character.isDigit(l.charAt(0))) continue;
                    registros++;
                    if (leido.contains(l)) registrosBien++;
                }
            }
            if (esperado.size() == leido.size()) {
                for (int i = 0; i < esperado.size(); i++) {
                    confusiones(esperado.get(i), leido.get(i), conf);
                    if (!esperado.get(i).equals(leido.get(i)) && ejemplos.size() < 6) {
                        ejemplos.add(new String[]{e.getKey(), esperado.get(i), leido.get(i)});
                    }
                }
            }
        }
        return new Resultado(c, caracteres, errores, lineas, lineasBien, registros, registrosBien,
                System.currentTimeMillis() - inicio, conf, ejemplos);
    }

    // ---------------------------------------------------------------- programa

    /** Ejecuta la herramienta: el uso está en la descripción de la clase. */
    public static void main(String[] args) throws Exception {
        File conjunto = new File(args[0]);
        List<Registro> datos = leerDatos(new File(args[1]));
        File tessdata = new File(args[2]);
        File informe = new File(args[3]);
        // Quinto argumento opcional: modelo entrenado (p. ej. "dosbox") que se mide solo con las
        // pantallas que no se usaron para entrenarlo (ver ExportarLineas).
        String modelo = args.length > 4 ? args[4] : null;

        // Config de Tesseract para desactivar los diccionarios (solo se puede al arrancar).
        File configs = new File(tessdata, "configs");
        configs.mkdirs();
        Files.writeString(new File(configs, "sin-diccionario").toPath(),
                "load_system_dawg F\nload_freq_dawg F\n", StandardCharsets.US_ASCII);

        Map<String, Pantalla> esperadas = esperadas(datos);
        if (modelo != null) esperadas.keySet().removeIf(ExportarLineas::deEntrenamiento);
        Map<String, BufferedImage> capturas = new HashMap<>();
        for (String nombre : esperadas.keySet()) {
            capturas.put(nombre, ImageIO.read(new File(conjunto, nombre + ".png")));
        }
        // Lista blanca: todos los caracteres que puede mostrar la aplicación en estas pantallas.
        TreeSet<Character> juego = new TreeSet<>();
        for (Pantalla p : esperadas.values()) for (String l : p.lineas()) for (char ch : l.toCharArray()) juego.add(ch);
        StringBuilder lb = new StringBuilder();
        for (char ch : juego) if (ch != ' ') lb.append(ch);
        String listaBlanca = lb.toString();
        System.out.println(esperadas.size() + " pantallas, " + datos.size() + " registros; lista blanca: " + listaBlanca);

        List<Resultado> todos = new ArrayList<>();
        if (modelo != null) {
            String pre = "binarizada x2";
            todos.add(ejecutar(new Config("spa de la asignatura, " + pre + ", PSM 4 + sin diccionario", pre,
                    preproceso(pre), 4, false, true), capturas, esperadas, tessdata, listaBlanca));
            for (int psm : new int[]{6, 4}) {
                todos.add(ejecutar(new Config("e. " + modelo + " entrenado (LSTM), " + pre + ", PSM " + psm, pre,
                        preproceso(pre), psm, false, false, modelo, 1), capturas, esperadas, tessdata, listaBlanca));
            }
            escribirInforme(informe, todos, esperadas.size(), datos.size(), listaBlanca);
            System.out.println("Informe: " + informe);
            return;
        }
        // a) Como en el ejemplo del guion: captura tal cual, segmentación por defecto (PSM 3).
        todos.add(ejecutar(new Config("a. guion (tal cual, PSM 3)", "tal cual", preproceso("tal cual"), 3, false, false),
                capturas, esperadas, tessdata, listaBlanca));
        // b) Preprocesado de la imagen, con PSM 6 (un bloque de texto uniforme).
        List<Resultado> etapaB = new ArrayList<>();
        for (String pre : List.of("tal cual", "invertida", "binarizada", "binarizada x2", "binarizada x3",
                "binarizada x4", "tal cual x2", "tal cual x3")) {
            etapaB.add(ejecutar(new Config("b. " + pre + ", PSM 6", pre, preproceso(pre), 6, false, false),
                    capturas, esperadas, tessdata, listaBlanca));
        }
        todos.addAll(etapaB);
        Resultado mejorB = mejor(etapaB);
        String pre = mejorB.config().preproceso();
        // c) Modos de segmentación con el mejor preprocesado.
        List<Resultado> etapaC = new ArrayList<>(List.of(mejorB));
        for (int psm : new int[]{3, 4}) {
            Resultado r = ejecutar(new Config("c. " + pre + ", PSM " + psm, pre, preproceso(pre), psm, false, false),
                    capturas, esperadas, tessdata, listaBlanca);
            todos.add(r);
            etapaC.add(r);
        }
        Resultado mejorC = mejor(etapaC);
        int psm = mejorC.config().psm();
        // d) Lista blanca y diccionario desactivado.
        String base = pre + ", PSM " + psm;
        todos.add(ejecutar(new Config("d. " + base + " + lista blanca", pre, preproceso(pre), psm, true, false),
                capturas, esperadas, tessdata, listaBlanca));
        todos.add(ejecutar(new Config("d. " + base + " + sin diccionario", pre, preproceso(pre), psm, false, true),
                capturas, esperadas, tessdata, listaBlanca));
        todos.add(ejecutar(new Config("d. " + base + " + las dos", pre, preproceso(pre), psm, true, true),
                capturas, esperadas, tessdata, listaBlanca));

        escribirInforme(informe, todos, esperadas.size(), datos.size(), listaBlanca);
        System.out.println("Informe: " + informe);
    }

    private static Resultado ejecutar(Config c, Map<String, BufferedImage> capturas, Map<String, Pantalla> esperadas,
                                      File tessdata, String listaBlanca) throws Exception {
        Resultado r = medir(c, capturas, esperadas, tessdata, listaBlanca);
        System.out.printf("%-48s caracteres %6.2f %%  líneas %6.2f %%  registros %6.2f %%  (%d s)%n",
                c.nombre(), r.precision(), r.pctLineas(), r.pctRegistros(), r.ms() / 1000);
        return r;
    }

    /** La mejor: más registros exactos y, a igualdad, más precisión por carácter. */
    private static Resultado mejor(List<Resultado> rs) {
        Resultado m = rs.getFirst();
        for (Resultado r : rs) {
            if (r.registrosBien() > m.registrosBien()
                    || (r.registrosBien() == m.registrosBien() && r.errores() < m.errores())) m = r;
        }
        return m;
    }

    private static void escribirInforme(File f, List<Resultado> rs, int pantallas, int registros, String listaBlanca)
            throws IOException {
        try (PrintWriter w = new PrintWriter(f, StandardCharsets.UTF_8)) {
            w.println("# Resultados del OCR (Tess4J 5.20.0, Tesseract 5.5.3)");
            w.println();
            w.printf("Conjunto: %d capturas reales de DOSBox (menú, información, las 43 páginas del listado y 6 búsquedas). "
                    + "Texto esperado generado con los LOCATE/PRINT de DATABASE.BAS y los %d registros de DATOS.DAT "
                    + "(solo para medir). Generado por `pruebas.MedirOcr`.%n%n", pantallas, registros);
            w.println("- **Caracteres**: precisión por carácter (1 − distancia de edición / longitud esperada), espacios seguidos contados como uno.");
            w.println("- **Líneas**: % de líneas esperadas que aparecen exactas.");
            w.println("- **Registros**: % de filas del listado (Nº, NOMBRE, TIPO, CINTA y REGISTRO) leídas sin ningún error; es lo que necesita el wrapper.");
            w.println();
            w.println("| Configuración | Caracteres | Líneas | Registros | Tiempo |");
            w.println("|---|---|---|---|---|");
            for (Resultado r : rs) {
                w.printf("| %s | %.2f %% | %.2f %% (%d/%d) | %.2f %% (%d/%d) | %d s |%n", r.config().nombre(),
                        r.precision(), r.pctLineas(), r.lineasBien(), r.lineas(),
                        r.pctRegistros(), r.registrosBien(), r.registros(), r.ms() / 1000);
            }
            w.println();
            w.println("Lista blanca usada: `" + listaBlanca + "`");
            Resultado m = mejor(rs);
            for (Resultado r : List.of(rs.getFirst(), m)) {
                w.println();
                w.println("## " + (r == m ? "Mejor configuración: " : "Configuración del guion: ") + r.config().nombre());
                w.println();
                w.println("Confusiones más frecuentes (esperado→leído, en líneas de igual longitud):");
                w.println();
                r.confusiones().entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).limit(12)
                        .forEach(e -> w.println("- `" + e.getKey() + "`: " + e.getValue()));
                w.println();
                w.println("| Captura | Esperado | Leído |");
                w.println("|---|---|---|");
                for (String[] ej : r.ejemplos()) {
                    w.println("| " + ej[0] + " | `" + ej[1] + "` | `" + ej[2] + "` |");
                }
            }
        }
    }
}
