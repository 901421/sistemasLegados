package pruebas;

import legado.LectorPantalla;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

/**
 * Comprueba LectorPantalla (reconocimiento por plantillas) con el conjunto de
 * capturas reales: compara cada fila leída, carácter a carácter y con sus
 * espacios, con la fila esperada (generada como en MedirOcr). Da el resultado
 * aparte para las pantallas que NO se usaron para generar las plantillas.
 *
 * Uso: java -cp "out:lib/*" pruebas.VerificarPlantillas CARPETA_CONJUNTO DATOS.DAT
 */
public class VerificarPlantillas {

    private VerificarPlantillas() {
    }

    /** Ejecuta la comprobación: el uso está en la descripción de la clase. */
    public static void main(String[] args) throws Exception {
        File conjunto = new File(args[0]);
        Map<String, MedirOcr.Pantalla> esperadas = MedirOcr.esperadas(MedirOcr.leerDatos(new File(args[1])));
        LectorPantalla lector = LectorPantalla.cargar();

        int[] celdas = new int[2], celdasBien = new int[2], pantallas = new int[2], pantallasBien = new int[2];
        long inicio = System.nanoTime();
        for (var e : esperadas.entrySet()) {
            int g = ExportarLineas.deEntrenamiento(e.getKey()) ? 0 : 1; // 0 entrenamiento, 1 no vistas
            BufferedImage img = ImageIO.read(new File(conjunto, e.getKey() + ".png"));
            MedirOcr.Pantalla p = e.getValue();
            pantallas[g]++;
            String[] leidas;
            try {
                leidas = lector.leer(img);
            } catch (LectorPantalla.CeldaDesconocida ex) {
                System.out.println("FALLO " + e.getKey() + ": " + ex.getMessage());
                continue;
            }
            boolean bien = true;
            for (int f = 0; f < 25; f++) {
                if (p.ignorar[f]) continue;
                String esperada = new String(p.celdas[f]);
                for (int c = 0; c < 80; c++) {
                    celdas[g]++;
                    if (esperada.charAt(c) == leidas[f].charAt(c)) celdasBien[g]++;
                }
                if (!esperada.equals(leidas[f])) {
                    bien = false;
                    System.out.printf("DIFERENTE %s fila %d%n  esperada: [%s]%n  leída:    [%s]%n",
                            e.getKey(), f + 1, esperada, leidas[f]);
                }
            }
            if (bien) pantallasBien[g]++;
        }
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        String[] nombres = {"pantallas de las plantillas", "pantallas no vistas"};
        for (int g = 0; g < 2; g++) {
            System.out.printf("%-28s %d/%d pantallas exactas, %d/%d celdas (%.2f %%)%n", nombres[g],
                    pantallasBien[g], pantallas[g], celdasBien[g], celdas[g], 100.0 * celdasBien[g] / celdas[g]);
        }
        System.out.println("Tiempo total: " + ms + " ms");
        boolean todo = pantallasBien[0] == pantallas[0] && pantallasBien[1] == pantallas[1];
        System.out.println(todo ? "TODO BIEN" : "HAY FALLOS");
        System.exit(todo ? 0 : 1);
    }
}
