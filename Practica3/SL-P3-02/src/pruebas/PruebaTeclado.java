package pruebas;

import legado.Teclado;
import legado.VentanaDosBox;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Prueba real del apartado 3 (generación de la entrada): lanza DOSBox, entra
 * en la opción 7 (BUSCAR DATOS) y escribe con Robot nombres reales que, juntos,
 * tienen todos los caracteres que aparecen en los datos (letras, minúsculas,
 * dígitos, espacio y ' &amp; ! - .). Si un carácter saliera mal, la aplicación no
 * encontraría el programa y diría "NO HAY NINGUN PROGRAMA".
 *
 * Para saber en qué pantalla está, compara celdas de la pantalla con las
 * capturas de pruebas/pantallas (píxel a píxel, solo para sincronizar la
 * prueba; la lectura de verdad es del apartado 4). Guarda en la carpeta de
 * salida una captura con el nombre tecleado y otra con el resultado.
 *
 * Uso (en Windows, con -Dsun.java2d.uiScale=1 para que Robot capture en
 * píxeles reales):
 *   java --enable-native-access=ALL-UNNAMED -Dsun.java2d.uiScale=1 -cp "out;lib/*" pruebas.PruebaTeclado
 *        CARPETA_APP CONF CARPETA_PANTALLAS CARPETA_SALIDA
 */
public class PruebaTeclado {

    private PruebaTeclado() {
    }

    /** Nombre a buscar y registro que debe encontrar (calculado con los datos originales). */
    private record Caso(String nombre, int registro) {}

    private static final List<Caso> CASOS = List.of(
            new Caso("BC'S QUEST FOR TIRES", 414),
            new Caso("SOFT & CUDDLY", 762),
            new Caso("WHAM! THE MUSIC BOX", 424),
            new Caso("B.C. BILL", 197),
            new Caso("D-DAY", 171),
            new Caso("FRIDAY 13th", 241),
            new Caso("BARRY McGUIGAN'S BOXING", 236));

    private static VentanaDosBox dosbox;
    private static File salida;
    private static BufferedImage refMenu, refBuscar, refResultado, refNoHay;

    /** Ejecuta la prueba real: ver el uso en la descripción de la clase. */
    public static void main(String[] args) throws Exception {
        File app = new File(args[0]);
        File conf = new File(args[1]);
        File pantallas = new File(args[2]);
        salida = new File(args[3]);
        salida.mkdirs();
        refMenu = ImageIO.read(new File(pantallas, "menu.png"));
        refBuscar = ImageIO.read(new File(pantallas, "buscar-caracteres.png"));
        refResultado = ImageIO.read(new File(pantallas, "buscar-defender-1.png"));
        refNoHay = ImageIO.read(new File(pantallas, "buscar-no-hay-mas.png"));

        int fallos = 0;
        dosbox = VentanaDosBox.lanzar(app, conf, 15000);
        try {
            dosbox.traerAlFrente();
            System.out.println("Área de cliente: " + dosbox.areaCliente());
            Teclado teclado = new Teclado(dosbox);
            teclado.setTraza(true);
            esperar("MENU", refMenu, 4, 32, 38);

            for (int i = 0; i < CASOS.size(); i++) {
                Caso caso = CASOS.get(i);
                teclado.tecla('7');
                esperar("LOCALIZACION", refBuscar, 1, 30, 50);
                teclado.responder("N");
                esperar("DIME EL NOMBRE", refBuscar, 10, 1, 44);
                teclado.escribir(caso.nombre());
                dosbox.robot().delay(200);
                guardar(String.format("%02d-tecleado", i + 1));
                teclado.enter();

                // O sale el programa ("ES ESTE ? (S/N)" en la fila 7) o "NO HAY NINGUN PROGRAMA".
                String pantalla = esperarUna(refResultado, 7, 1, 15, refNoHay, 12, 1, 50);
                guardar(String.format("%02d-resultado", i + 1));
                if (pantalla.equals("resultado")) {
                    System.out.printf("OK     %-25s encontrado (se esperaba el registro %d; ver captura %02d)%n",
                            caso.nombre(), caso.registro(), i + 1);
                    teclado.responder("N"); // no es este: busca el siguiente (no hay más)
                    esperar("NO HAY", refNoHay, 12, 1, 50);
                } else {
                    System.out.printf("FALLO  %-25s la aplicación no lo encuentra%n", caso.nombre());
                    fallos++;
                }
                teclado.enter();                 // PULSA ENTER
                dosbox.robot().delay(300);       // "QUIERES BUSCAR ALGUNO MAS? S/N"
                teclado.responder("N");
                esperar("MENU", refMenu, 4, 32, 38);
            }
        } finally {
            dosbox.cerrar();
        }
        System.out.println(fallos == 0 ? "TODO BIEN" : fallos + " FALLOS");
        System.exit(fallos == 0 ? 0 : 1);
    }

    private static BufferedImage capturar() {
        if (!dosbox.estaDelante()) {
            throw new IllegalStateException("DOSBox no está delante: no se captura la pantalla");
        }
        return dosbox.robot().createScreenCapture(dosbox.areaCliente());
    }

    private static void guardar(String nombre) throws Exception {
        ImageIO.write(capturar(), "png", new File(salida, nombre + ".png"));
    }

    /** true si las celdas (fila, c1..c2) de las dos imágenes tienen los mismos píxeles encendidos. */
    private static boolean iguales(BufferedImage a, BufferedImage b, int fila, int c1, int c2) {
        for (int y = (fila - 1) * 16; y < fila * 16; y++) {
            for (int x = (c1 - 1) * 8; x < c2 * 8; x++) {
                if (encendido(a, x, y) != encendido(b, x, y)) return false;
            }
        }
        return true;
    }

    private static boolean encendido(BufferedImage img, int x, int y) {
        return ((img.getRGB(x, y) >> 16) & 0xFF) > 128;
    }

    private static void esperar(String nombre, BufferedImage ref, int fila, int c1, int c2) {
        long limite = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < limite) {
            if (iguales(capturar(), ref, fila, c1, c2)) return;
            dosbox.robot().delay(30);
        }
        throw new IllegalStateException("No aparece la pantalla " + nombre);
    }

    private static String esperarUna(BufferedImage refA, int fA, int a1, int a2,
                                     BufferedImage refB, int fB, int b1, int b2) {
        long limite = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < limite) {
            BufferedImage img = capturar();
            if (iguales(img, refA, fA, a1, a2)) return "resultado";
            if (iguales(img, refB, fB, b1, b2)) return "nohay";
            dosbox.robot().delay(30);
        }
        throw new IllegalStateException("No aparece ni el resultado ni NO HAY");
    }
}
