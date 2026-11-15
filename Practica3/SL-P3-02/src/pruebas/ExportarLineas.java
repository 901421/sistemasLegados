package pruebas;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

/**
 * Prepara los datos para entrenar Tesseract (tesstrain) con las capturas reales:
 * corta cada línea de texto de las pantallas de ENTRENAMIENTO (menú y páginas
 * 1-30 del listado) en una imagen (texto negro sobre blanco, escalada x2, con
 * margen) y escribe al lado su texto exacto (.gt.txt), generado como en MedirOcr.
 *
 * Las demás pantallas (páginas 31-43, información y búsquedas) no se exportan:
 * quedan para medir el modelo entrenado con pantallas que no ha visto.
 *
 * Uso: java -cp "out;lib/*" pruebas.ExportarLineas CARPETA_CONJUNTO DATOS.DAT CARPETA_SALIDA
 */
public class ExportarLineas {

    private ExportarLineas() {
    }

    static final int ESCALA = 2;
    private static final int MARGEN = 8;

    /** true si la pantalla se usa para entrenar; el resto es para medir. */
    static boolean deEntrenamiento(String nombre) {
        if (nombre.equals("menu")) return true;
        if (nombre.startsWith("listado-pag")) return Integer.parseInt(nombre.substring(11)) <= 30;
        return false;
    }

    /** Ejecuta la herramienta: el uso está en la descripción de la clase. */
    public static void main(String[] args) throws Exception {
        File conjunto = new File(args[0]);
        Map<String, MedirOcr.Pantalla> esperadas = MedirOcr.esperadas(MedirOcr.leerDatos(new File(args[1])));
        File salida = new File(args[2]);
        salida.mkdirs();
        int lineas = 0;
        for (var e : esperadas.entrySet()) {
            if (!deEntrenamiento(e.getKey())) continue;
            BufferedImage img = ImageIO.read(new File(conjunto, e.getKey() + ".png"));
            MedirOcr.Pantalla p = e.getValue();
            for (int f = 0; f < 25; f++) {
                String fila = new String(p.celdas[f]);
                String texto = MedirOcr.normalizar(fila);
                if (p.ignorar[f] || texto.isEmpty()) continue;
                int c1 = fila.indexOf(fila.strip().charAt(0));
                int c2 = fila.stripTrailing().length(); // columna siguiente al último carácter
                Files.writeString(new File(salida, String.format("%s-f%02d.gt.txt", e.getKey(), f + 1)).toPath(),
                        texto + "\n", StandardCharsets.UTF_8);
                ImageIO.write(linea(img, f, c1, c2), "png",
                        new File(salida, String.format("%s-f%02d.png", e.getKey(), f + 1)));
                lineas++;
            }
        }
        System.out.println("Líneas exportadas: " + lineas);
    }

    /** Recorte de la fila f (desde 0), columnas c1..c2-1, en negro sobre blanco, escalado y con margen. */
    private static BufferedImage linea(BufferedImage img, int f, int c1, int c2) {
        int ancho = (c2 - c1) * 8 * ESCALA + 2 * MARGEN, alto = 16 * ESCALA + 2 * MARGEN;
        BufferedImage r = new BufferedImage(ancho, alto, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int sx = c1 * 8 + (x - MARGEN) / ESCALA, sy = f * 16 + (y - MARGEN) / ESCALA;
                boolean dentro = x >= MARGEN && y >= MARGEN && x < ancho - MARGEN && y < alto - MARGEN;
                boolean tinta = dentro && ((img.getRGB(sx, sy) >> 16) & 0xFF) > 128;
                r.setRGB(x, y, tinta ? 0x000000 : 0xFFFFFF);
            }
        }
        return r;
    }
}
