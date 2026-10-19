package pruebas;

import legado.LectorPantalla;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Genera la tabla de plantillas de la fuente de DOSBox (fuente-dosbox.txt) a
 * partir de las capturas de ENTRENAMIENTO (menú y páginas 1-30 del listado) y
 * de su texto esperado (como en MedirOcr): para cada celda de 8x16 se anota qué
 * carácter hay. Comprueba que cada dibujo corresponde siempre al mismo carácter
 * y que cada carácter tiene un único dibujo; si no, se para.
 *
 * Además usa la captura de CapturarJuegoCaracteres (todos los caracteres que
 * se pueden teclear, escritos en la opción 7 a partir de la fila 10, columna 49).
 *
 * Uso: java -cp "out:lib/*" pruebas.GenerarPlantillas CARPETA_CONJUNTO DATOS.DAT CARPETA_JUEGO FICHERO_SALIDA
 */
public class GenerarPlantillas {

    public static void main(String[] args) throws Exception {
        File conjunto = new File(args[0]);
        var datos = MedirOcr.leerDatos(new File(args[1]));
        Map<String, MedirOcr.Pantalla> esperadas = MedirOcr.esperadas(datos);

        Map<String, Character> dibujoACaracter = new TreeMap<>();
        Map<Character, Set<String>> caracterADibujos = new TreeMap<>();
        // Pantallas de entrenamiento y la del juego de caracteres.
        Map<File, MedirOcr.Pantalla> fuentes = new java.util.LinkedHashMap<>();
        for (var e : esperadas.entrySet()) {
            if (ExportarLineas.deEntrenamiento(e.getKey())) fuentes.put(new File(conjunto, e.getKey() + ".png"), e.getValue());
        }
        File juego = new File(args[2]);
        fuentes.put(new File(juego, "juego-caracteres.png"), pantallaJuego(new File(juego, "juego-caracteres.txt")));

        int celdas = 0, conflictos = 0;
        for (var e : fuentes.entrySet()) {
            BufferedImage img = ImageIO.read(e.getKey());
            MedirOcr.Pantalla p = e.getValue();
            for (int f = 0; f < 25; f++) {
                if (p.ignorar[f]) continue;
                for (int c = 0; c < 80; c++) {
                    char esperado = p.celdas[f][c];
                    String dibujo = LectorPantalla.huella(img, f, c);
                    celdas++;
                    Character previo = dibujoACaracter.putIfAbsent(dibujo, esperado);
                    if (previo != null && previo != esperado) {
                        conflictos++;
                        System.out.printf("CONFLICTO %s fila %d col %d: el dibujo ya era '%c' y aquí se espera '%c'%n",
                                e.getKey().getName(), f + 1, c + 1, previo, esperado);
                    }
                    caracterADibujos.computeIfAbsent(esperado, k -> new HashSet<>()).add(dibujo);
                }
            }
        }
        for (var e : caracterADibujos.entrySet()) {
            if (e.getValue().size() > 1) {
                conflictos++;
                System.out.printf("CONFLICTO: '%c' aparece con %d dibujos distintos%n", e.getKey(), e.getValue().size());
            }
        }
        // Todos los caracteres de los datos (nombres, tipos y cintas) deben tener plantilla.
        Set<Character> faltan = new TreeSet<>();
        for (var r : datos) {
            for (char ch : (r.nombre() + r.tipo() + r.cinta()).toCharArray()) {
                if (!caracterADibujos.containsKey(ch)) faltan.add(ch);
            }
        }
        System.out.printf("%d celdas, %d dibujos distintos, %d caracteres; faltan de los datos: %s%n",
                celdas, dibujoACaracter.size(), caracterADibujos.size(), faltan);
        if (conflictos > 0 || !faltan.isEmpty()) {
            System.out.println("NO se escribe la tabla");
            System.exit(1);
        }
        try (PrintWriter w = new PrintWriter(new File(args[3]), StandardCharsets.UTF_8)) {
            w.println("# Plantillas de la fuente de texto de DOSBox (celdas de 8x16 píxeles).");
            w.println("# Cada línea: 16 bytes en hexadecimal (una fila de 8 píxeles por byte, bit alto = izquierda)");
            w.println("# y el código Unicode del carácter. Generado por pruebas.GenerarPlantillas.");
            for (var e : dibujoACaracter.entrySet()) {
                w.printf("%s U+%04X%n", e.getKey(), (int) e.getValue());
            }
        }
        System.out.println("Tabla escrita: " + args[3]);
    }

    /**
     * Pantalla esperada de la captura del juego de caracteres: la pregunta de la
     * opción 7 y, desde la fila 10 columna 49, lo tecleado (con salto de línea en
     * la columna 80). En la captura real, # @ ] | ~ (AltGr) no salieron en
     * pantalla: se quitan y queda solo el espacio que los seguía.
     */
    private static MedirOcr.Pantalla pantallaJuego(File tecleado) throws java.io.IOException {
        String texto = java.nio.file.Files.readString(tecleado.toPath(), StandardCharsets.UTF_8)
                .replaceAll("[#@\\]|~]", "");
        MedirOcr.Pantalla p = new MedirOcr.Pantalla()
                .en(1, 30, "LOCALIZACION DE DATOS")
                .en(5, 1, "SABES EL NUMERO DE REGISTRO QUE QUIERES BUSCAR? (S/N) N")
                .en(10, 1, "DIME EL NOMBRE DEL PROGRAMA QUE QUIERES VER     ");
        int fila = 10, col = 49;
        for (char ch : texto.toCharArray()) {
            p.en(fila, col, String.valueOf(ch));
            if (++col > 80) { col = 1; fila++; }
        }
        return p;
    }
}
