package legado;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Lee el texto de una captura de la pantalla de DOSBox reconociendo cada
 * carácter por su dibujo (reconocimiento por plantillas).
 *
 * En modo texto, DOSBox dibuja 25 filas de 80 caracteres y cada carácter es
 * siempre el mismo dibujo de 8x16 píxeles en su celda de la cuadrícula. Por
 * eso basta con partir la imagen en celdas, convertir cada celda en una huella
 * (128 píxeles encendidos o apagados) y buscarla en la tabla de plantillas
 * fuente-dosbox.txt, sacada de capturas reales de la propia aplicación.
 *
 * Si una celda no está en la tabla, no se inventa nada: se lanza
 * {@link CeldaDesconocida} con su posición y su huella.
 */
public class LectorPantalla {

    public static final int FILAS = 25, COLUMNAS = 80, ANCHO_CELDA = 8, ALTO_CELDA = 16;

    /** Una celda cuyo dibujo no está en la tabla de plantillas. */
    public static class CeldaDesconocida extends Exception {
        private static final long serialVersionUID = 1L;
        public final int fila, columna;
        public final String huella;

        CeldaDesconocida(int fila, int columna, String huella) {
            super(String.format("Carácter desconocido en la fila %d, columna %d (huella %s)", fila, columna, huella));
            this.fila = fila;
            this.columna = columna;
            this.huella = huella;
        }
    }

    private final Map<String, Character> plantillas;

    private LectorPantalla(Map<String, Character> plantillas) {
        this.plantillas = plantillas;
    }

    /** Carga la tabla fuente-dosbox.txt, que va dentro del programa junto a esta clase. */
    public static LectorPantalla cargar() throws IOException {
        try (InputStream in = LectorPantalla.class.getResourceAsStream("fuente-dosbox.txt")) {
            if (in == null) throw new IOException("No se encuentra fuente-dosbox.txt");
            Map<String, Character> tabla = new HashMap<>();
            BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            for (String linea; (linea = r.readLine()) != null; ) {
                if (linea.isBlank() || linea.startsWith("#")) continue;
                String[] partes = linea.trim().split(" ");
                tabla.put(partes[0], (char) Integer.parseInt(partes[1].substring(2), 16));
            }
            return new LectorPantalla(tabla);
        }
    }

    /** Huella de la celda (fila, columna), desde 0: 16 bytes en hexadecimal, uno por fila de píxeles. */
    public static String huella(BufferedImage img, int fila, int columna) {
        StringBuilder sb = new StringBuilder(32);
        for (int y = 0; y < ALTO_CELDA; y++) {
            int fila8 = 0;
            for (int x = 0; x < ANCHO_CELDA; x++) {
                int rgb = img.getRGB(columna * ANCHO_CELDA + x, fila * ALTO_CELDA + y);
                boolean encendido = ((rgb >> 16) & 0xFF) > 128; // texto gris (170) sobre negro (0)
                fila8 = (fila8 << 1) | (encendido ? 1 : 0);
            }
            sb.append(String.format("%02x", fila8));
        }
        return sb.toString();
    }

    /**
     * Mientras la aplicación espera una respuesta (INPUT), el cursor parpadea:
     * una raya en la fila 15 de píxeles de su celda (huella ...ff00). Como
     * parpadea, unas capturas la tienen y otras no. Se quita para buscar el
     * carácter que hay debajo (normalmente un espacio).
     */
    private static String sinCursor(String huella) {
        return huella.substring(0, 28) + "00" + huella.substring(30);
    }

    /**
     * Devuelve las 25 líneas de 80 caracteres de la pantalla.
     *
     * @throws IllegalArgumentException si la imagen no mide 640x400 (ventana escalada)
     * @throws CeldaDesconocida          si algún dibujo no está en la tabla
     */
    public String[] leer(BufferedImage img) throws CeldaDesconocida {
        if (img.getWidth() != COLUMNAS * ANCHO_CELDA || img.getHeight() != FILAS * ALTO_CELDA) {
            throw new IllegalArgumentException("La pantalla mide " + img.getWidth() + "x" + img.getHeight()
                    + " y debería medir 640x400 (¿DOSBox escalado?)");
        }
        String[] lineas = new String[FILAS];
        for (int f = 0; f < FILAS; f++) {
            char[] linea = new char[COLUMNAS];
            for (int c = 0; c < COLUMNAS; c++) {
                String h = huella(img, f, c);
                Character ch = plantillas.get(h);
                if (ch == null) ch = plantillas.get(sinCursor(h));
                if (ch == null) throw new CeldaDesconocida(f + 1, c + 1, h);
                linea[c] = ch;
            }
            lineas[f] = new String(linea);
        }
        return lineas;
    }
}
