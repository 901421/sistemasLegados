package pruebas;

import legado.Teclado;
import legado.VentanaDosBox;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Captura real del conjunto de pantallas con el que se mide el OCR (apartado 4):
 * menú, información, las 43 páginas del listado y varias búsquedas (todas sus
 * pantallas de resultado y la de "NO HAY"). Solo usa opciones de lectura (4, 6
 * y 7 respondiendo siempre N): Database no cambia.
 *
 * Como PruebaTeclado, se sincroniza comparando celdas con las capturas de
 * pruebas/pantallas.
 *
 * Uso (en Windows):
 *   java --enable-native-access=ALL-UNNAMED -Dsun.java2d.uiScale=1 -cp "out;lib/*" pruebas.CapturarConjunto
 *        CARPETA_APP CONF CARPETA_PANTALLAS CARPETA_SALIDA
 */
public class CapturarConjunto {

    private CapturarConjunto() {
    }

    /** Textos de búsqueda: varias coincidencias, una sola, minúsculas y ninguna. */
    static final List<String> BUSQUEDAS = List.of(
            "DEFENDER", "KNIGHT", "SPY", "WORLD", "BARRY McGUIGAN'S", "friday");

    private static VentanaDosBox dosbox;
    private static File salida;
    private static int guardadas;

    /** Ejecuta la herramienta: el uso está en la descripción de la clase. */
    public static void main(String[] args) throws Exception {
        File app = new File(args[0]);
        File conf = new File(args[1]);
        File pantallas = new File(args[2]);
        salida = new File(args[3]);
        salida.mkdirs();
        BufferedImage refMenu = ImageIO.read(new File(pantallas, "menu.png"));
        BufferedImage refInfo = ImageIO.read(new File(pantallas, "informacion.png"));
        BufferedImage refPagina = ImageIO.read(new File(pantallas, "listado-pag01.png"));
        BufferedImage refBuscar = ImageIO.read(new File(pantallas, "buscar-caracteres.png"));
        BufferedImage refResultado = ImageIO.read(new File(pantallas, "buscar-defender-1.png"));
        BufferedImage refNoHay = ImageIO.read(new File(pantallas, "buscar-no-hay-mas.png"));

        dosbox = VentanaDosBox.lanzar(app, conf, 15000);
        try {
            dosbox.traerAlFrente();
            Teclado teclado = new Teclado(dosbox);
            esperar("MENU", refMenu, 4, 32, 38, null);
            guardar("menu");

            teclado.tecla('4');
            esperar("INFORMACION", refInfo, 2, 27, 51, null);
            guardar("informacion");
            teclado.enter();
            esperar("MENU", refMenu, 4, 32, 38, null);

            // Listado completo: Enter = desde el principio; espacio = página siguiente.
            teclado.tecla('6');
            dosbox.robot().delay(300);
            teclado.enter();
            String anterior = esperar("página 1", refPagina, 25, 2, 51, null);
            guardar("listado-pag01");
            for (int pagina = 2; pagina <= 43; pagina++) {
                teclado.espacio();
                anterior = esperar("página " + pagina, refPagina, 25, 2, 51, anterior);
                guardar(String.format("listado-pag%02d", pagina));
            }
            teclado.espacio(); // en la última página, cualquier tecla vuelve al menú
            esperar("MENU", refMenu, 4, 32, 38, null);

            for (int b = 0; b < BUSQUEDAS.size(); b++) {
                String texto = BUSQUEDAS.get(b);
                String prefijo = String.format("buscar-%d", b + 1);
                teclado.tecla('7');
                esperar("LOCALIZACION", refBuscar, 1, 30, 50, null);
                teclado.responder("N");
                esperar("DIME EL NOMBRE", refBuscar, 10, 1, 44, null);
                teclado.responder(texto);
                String filaAnterior = null;
                for (int r = 1; ; r++) {
                    String hay = esperarUna(refResultado, refNoHay, filaAnterior);
                    if (hay == null) {
                        guardar(prefijo + "-nohay");
                        System.out.printf("%-18s %d resultados%n", texto, r - 1);
                        break;
                    }
                    filaAnterior = hay;
                    guardar(String.format("%s-res%d", prefijo, r));
                    teclado.responder("N");
                }
                teclado.enter();
                dosbox.robot().delay(300); // "QUIERES BUSCAR ALGUNO MAS? S/N"
                teclado.responder("N");
                esperar("MENU", refMenu, 4, 32, 38, null);
            }
        } finally {
            dosbox.cerrar();
        }
        System.out.println("Capturas guardadas: " + guardadas);
    }

    private static BufferedImage capturar() {
        if (!dosbox.estaDelante()) {
            throw new IllegalStateException("DOSBox no está delante: no se captura la pantalla");
        }
        return dosbox.robot().createScreenCapture(dosbox.areaCliente());
    }

    private static void guardar(String nombre) throws Exception {
        ImageIO.write(capturar(), "png", new File(salida, nombre + ".png"));
        guardadas++;
    }

    /** Píxeles encendidos de las celdas (fila, c1..c2) como texto de 0 y 1. */
    private static String celdas(BufferedImage img, int fila, int c1, int c2) {
        StringBuilder sb = new StringBuilder();
        for (int y = (fila - 1) * 16; y < fila * 16; y++) {
            for (int x = (c1 - 1) * 8; x < c2 * 8; x++) {
                sb.append(((img.getRGB(x, y) >> 16) & 0xFF) > 128 ? '1' : '0');
            }
        }
        return sb.toString();
    }

    /**
     * Espera a que las celdas coincidan con la referencia. Si se da la fila 5
     * de la pantalla anterior, además espera a que la fila 5 haya cambiado (para
     * no confundir la página nueva con la anterior). Devuelve la fila 5 actual.
     */
    private static String esperar(String nombre, BufferedImage ref, int fila, int c1, int c2, String fila5Anterior) {
        String marca = celdas(ref, fila, c1, c2);
        long limite = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < limite) {
            BufferedImage img = capturar();
            if (celdas(img, fila, c1, c2).equals(marca)) {
                String fila5 = celdas(img, 5, 1, 80);
                if (fila5Anterior == null || !fila5.equals(fila5Anterior)) return fila5;
            }
            dosbox.robot().delay(30);
        }
        throw new IllegalStateException("No aparece la pantalla " + nombre);
    }

    /**
     * Tras una búsqueda: espera a un resultado nuevo ("ES ESTE ?" en la fila 7
     * con una fila 5 distinta de la anterior) o a "NO HAY" en la fila 12.
     * Devuelve la fila 5 del resultado, o null si es "NO HAY".
     */
    private static String esperarUna(BufferedImage refResultado, BufferedImage refNoHay, String fila5Anterior) {
        String marcaResultado = celdas(refResultado, 7, 1, 15);
        String marcaNoHay = celdas(refNoHay, 12, 1, 50);
        long limite = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < limite) {
            BufferedImage img = capturar();
            if (celdas(img, 12, 1, 50).equals(marcaNoHay)) return null;
            if (celdas(img, 7, 1, 15).equals(marcaResultado)) {
                String fila5 = celdas(img, 5, 1, 80);
                if (!fila5.equals(fila5Anterior)) return fila5;
            }
            dosbox.robot().delay(30);
        }
        throw new IllegalStateException("No aparece ni un resultado nuevo ni NO HAY");
    }
}
