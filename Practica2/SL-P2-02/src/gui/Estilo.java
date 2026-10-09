package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Aspecto común de las ventanas: colores, letras, márgenes e icono. Así
 * todas se ven igual y se cambia en un solo sitio.
 */
public final class Estilo {

    /** Color principal (cabeceras e icono). */
    public static final Color ACENTO = new Color(0x0F6CBD);
    public static final Color ERROR = new Color(0xB00020);
    public static final Color EXITO = new Color(0x107C10);
    public static final Color TEXTO_SECUNDARIO = new Color(0x5F6368);
    /** Fondo de las filas pares de la tabla. */
    public static final Color FILA_ALTERNA = new Color(0xF3F6FA);

    /** Margen exterior de las ventanas y separación entre bloques, en píxeles. */
    public static final int MARGEN = 12;

    private Estilo() {
    }

    /**
     * Aspecto del sistema (en Windows, el de Windows). Si no se puede, se
     * queda el de Java, que funciona igual. Hay que llamarlo antes de crear
     * ninguna ventana.
     */
    public static void aplicarAspecto() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            System.err.println("Se usa el aspecto de Java: " + e.getMessage());
        }
    }

    /** Texto grande y en negrita para el título de una ventana. */
    static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        Font base = etiqueta.getFont();
        etiqueta.setFont(base.deriveFont(Font.BOLD, base.getSize2D() + 7));
        return etiqueta;
    }

    /** Texto gris para la línea que va debajo del título. */
    static JLabel subtitulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(TEXTO_SECUNDARIO);
        return etiqueta;
    }

    /** Icono de la ventana: una lista con una marca, dibujada aquí para no depender de ficheros. */
    static Image icono() {
        int lado = 64;
        BufferedImage imagen = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(ACENTO);
        g.fillRoundRect(2, 2, lado - 4, lado - 4, 16, 16);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Marca de "hecho" a la izquierda y tres renglones a la derecha.
        g.drawPolyline(new int[] {12, 19, 30}, new int[] {33, 41, 24}, 3);
        g.drawLine(36, 20, 52, 20);
        g.drawLine(36, 32, 52, 32);
        g.drawLine(36, 44, 52, 44);
        g.dispose();
        return imagen;
    }
}
