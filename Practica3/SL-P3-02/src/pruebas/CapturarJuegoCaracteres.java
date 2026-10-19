package pruebas;

import legado.Teclado;
import legado.VentanaDosBox;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Captura real para completar las plantillas de la fuente: en la opción 7
 * (BUSCAR DATOS) escribe en el campo del nombre todos los caracteres
 * imprimibles, cada uno seguido de un espacio (el espacio también saca los
 * caracteres de las teclas muertas, como ^ o `), y captura la pantalla ANTES
 * de pulsar Enter. Después borra la línea con Esc, busca un nombre que no
 * existe y vuelve al menú. No se modifica nada.
 *
 * Deja juego-caracteres.png y juego-caracteres.txt (lo que se ha tecleado).
 *
 * Uso (en Windows):
 *   java --enable-native-access=ALL-UNNAMED -Dsun.java2d.uiScale=1 -cp "out;lib/*" pruebas.CapturarJuegoCaracteres
 *        CARPETA_APP CONF CARPETA_PANTALLAS CARPETA_SALIDA
 */
public class CapturarJuegoCaracteres {

    static final String JUEGO;

    static {
        StringBuilder sb = new StringBuilder();
        for (char c = '!'; c <= '~'; c++) sb.append(c);
        sb.append("ñÑºª¿¡ç");
        JUEGO = sb.toString();
    }

    public static void main(String[] args) throws Exception {
        File app = new File(args[0]);
        File conf = new File(args[1]);
        File pantallas = new File(args[2]);
        File salida = new File(args[3]);
        salida.mkdirs();
        BufferedImage refMenu = ImageIO.read(new File(pantallas, "menu.png"));
        BufferedImage refBuscar = ImageIO.read(new File(pantallas, "buscar-caracteres.png"));
        BufferedImage refNoHay = ImageIO.read(new File(pantallas, "buscar-no-hay-mas.png"));

        VentanaDosBox dosbox = VentanaDosBox.lanzar(app, conf, 15000);
        try {
            dosbox.traerAlFrente();
            Teclado teclado = new Teclado(dosbox);
            esperar(dosbox, "MENU", refMenu, 4, 32, 38);
            teclado.tecla('7');
            esperar(dosbox, "LOCALIZACION", refBuscar, 1, 30, 50);
            teclado.responder("N");
            esperar(dosbox, "DIME EL NOMBRE", refBuscar, 10, 1, 44);

            StringBuilder tecleado = new StringBuilder();
            for (char c : JUEGO.toCharArray()) {
                try {
                    teclado.tecla(c);
                    teclado.espacio();
                    tecleado.append(c).append(' ');
                } catch (IllegalArgumentException e) {
                    System.out.println("Sin tecla: " + e.getMessage());
                }
            }
            dosbox.robot().delay(300);
            ImageIO.write(capturar(dosbox), "png", new File(salida, "juego-caracteres.png"));
            Files.writeString(new File(salida, "juego-caracteres.txt").toPath(), tecleado.toString(), StandardCharsets.UTF_8);
            System.out.println("Tecleado: " + tecleado);

            dosbox.robot().keyPress(java.awt.event.KeyEvent.VK_ESCAPE); // borra la línea del INPUT
            dosbox.robot().keyRelease(java.awt.event.KeyEvent.VK_ESCAPE);
            dosbox.robot().delay(200);
            teclado.responder("ZZZZ");
            esperar(dosbox, "NO HAY", refNoHay, 12, 1, 50);
            teclado.enter();
            dosbox.robot().delay(300);
            teclado.responder("N");
            esperar(dosbox, "MENU", refMenu, 4, 32, 38);
        } finally {
            dosbox.cerrar();
        }
    }

    private static BufferedImage capturar(VentanaDosBox dosbox) {
        if (!dosbox.estaDelante()) throw new IllegalStateException("DOSBox no está delante: no se captura la pantalla");
        return dosbox.robot().createScreenCapture(dosbox.areaCliente());
    }

    private static void esperar(VentanaDosBox dosbox, String nombre, BufferedImage ref, int fila, int c1, int c2) {
        long limite = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < limite) {
            BufferedImage img = capturar(dosbox);
            boolean iguales = true;
            for (int y = (fila - 1) * 16; y < fila * 16 && iguales; y++) {
                for (int x = (c1 - 1) * 8; x < c2 * 8; x++) {
                    if ((((img.getRGB(x, y) >> 16) & 0xFF) > 128) != (((ref.getRGB(x, y) >> 16) & 0xFF) > 128)) {
                        iguales = false;
                        break;
                    }
                }
            }
            if (iguales) return;
            dosbox.robot().delay(30);
        }
        throw new IllegalStateException("No aparece la pantalla " + nombre);
    }
}
