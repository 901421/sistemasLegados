package legado;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HKL;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera la entrada de la aplicación legada pulsando teclas con java.awt.Robot,
 * como indica el guion.
 *
 * Robot no escribe caracteres sino que pulsa teclas (como un dedo sobre el
 * teclado): keyPress(VK_A) pulsa la tecla A. El carácter que sale de cada tecla
 * depende de la distribución del teclado: en el español, el apóstrofo está a la
 * derecha del 0 y "&" es Mayúsculas+6. Para no depender de una tabla escrita a
 * mano, se pregunta a Windows (VkKeyScanEx) qué tecla y qué modificadores
 * producen cada carácter en la distribución de DOSBox.
 *
 * Antes de cada pulsación se comprueba que DOSBox sigue delante: si el usuario
 * cambia de ventana, se para con un error en lugar de escribir en otra ventana.
 */
public class Teclado {

    /** Pausa tras cada pulsación, para que DOSBox procese las teclas de una en una. */
    private static final int PAUSA_MS = 20;

    private final VentanaDosBox dosbox;
    private final Robot robot;
    private final HKL distribucion;
    /** Si es true, se escribe en la consola cada tecla que se pulsa (para diagnosticar). */
    private boolean traza;

    public Teclado(VentanaDosBox dosbox) {
        this.dosbox = dosbox;
        this.robot = dosbox.robot();
        this.distribucion = dosbox.distribucionTeclado();
    }

    public void setTraza(boolean traza) {
        this.traza = traza;
    }

    /** Escribe un texto carácter a carácter (sin pulsar Enter). */
    public void escribir(String texto) {
        for (char c : texto.toCharArray()) {
            tecla(c);
        }
    }

    /** Escribe un texto y pulsa Enter (respuesta a un INPUT de BASIC). */
    public void responder(String texto) {
        escribir(texto);
        enter();
    }

    public void enter() {
        pulsar(KeyEvent.VK_ENTER, List.of(), "Enter");
    }

    public void espacio() {
        pulsar(KeyEvent.VK_SPACE, List.of(), "espacio");
    }

    /** Pulsa la tecla (con sus modificadores) que produce el carácter c. */
    public void tecla(char c) {
        short codigo = User32.INSTANCE.VkKeyScanExW(c, distribucion);
        if (codigo == -1) {
            throw new IllegalArgumentException("El teclado no tiene ninguna tecla para '" + c + "'");
        }
        int vkWindows = codigo & 0xFF;          // byte bajo: la tecla
        int modificadores = (codigo >> 8) & 0xFF; // byte alto: 1 Mayús, 2 Ctrl, 4 Alt
        List<Integer> mods = new ArrayList<>();
        if ((modificadores & 1) != 0) mods.add(KeyEvent.VK_SHIFT);
        if ((modificadores & 2) != 0) mods.add(KeyEvent.VK_CONTROL);
        if ((modificadores & 4) != 0) mods.add(KeyEvent.VK_ALT);
        int java = teclaJava(vkWindows, c, distribucion);
        pulsar(java, mods, String.format("'%c' (Windows 0x%02X, Java %s, modificadores %d)",
                c, vkWindows, KeyEvent.getKeyText(java), modificadores));
    }

    /**
     * Pulsa una tecla con sus modificadores: primero se bajan los modificadores,
     * luego la tecla, y se suelta todo en orden inverso.
     */
    private void pulsar(int teclaJava, List<Integer> modificadores, String descripcion) {
        if (!dosbox.estaDelante()) {
            throw new IllegalStateException("DOSBox ha dejado de estar delante antes de pulsar " + descripcion
                    + "; el foco lo tiene \"" + VentanaDosBox.tituloVentanaConFoco()
                    + "\": no se escribe en otra ventana");
        }
        if (traza) System.out.println("  tecla " + descripcion);
        for (int m : modificadores) robot.keyPress(m);
        robot.keyPress(teclaJava);
        robot.keyRelease(teclaJava);
        for (int i = modificadores.size() - 1; i >= 0; i--) robot.keyRelease(modificadores.get(i));
        robot.delay(PAUSA_MS);
    }

    /** MapVirtualKeyEx: de código de tecla al carácter que produce sin modificadores. */
    private static final int MAPVK_VK_TO_CHAR = 2;

    /**
     * Traduce el código de tecla de Windows al código de KeyEvent que acepta
     * Robot. Letras, dígitos y espacio tienen el mismo número en los dos.
     *
     * Las teclas de signos (VK_OEM_*) no tienen un código Java fijo: Java en
     * Windows le da a cada una el código del carácter que produce en la
     * distribución activa (en el teclado español, la tecla VK_OEM_4 da "'" y
     * para Java es VK_QUOTE; en el inglés da "[" y es VK_OPEN_BRACKET). Se hace
     * lo mismo: se pregunta a Windows qué carácter da la tecla sin modificadores
     * y se pide a Java el código de ese carácter.
     */
    private static int teclaJava(int vkWindows, char c, HKL distribucion) {
        if ((vkWindows >= 'A' && vkWindows <= 'Z') || (vkWindows >= '0' && vkWindows <= '9') || vkWindows == 0x20) {
            return vkWindows;
        }
        int caracterTecla = User32.INSTANCE.MapVirtualKeyEx(vkWindows, MAPVK_VK_TO_CHAR, distribucion) & 0xFFFF;
        int codigo = caracterTecla == 0 ? KeyEvent.VK_UNDEFINED
                : KeyEvent.getExtendedKeyCodeForChar(Character.toLowerCase((char) caracterTecla));
        if (codigo == KeyEvent.VK_UNDEFINED) {
            throw new IllegalArgumentException(String.format(
                    "Tecla de Windows 0x%02X (para '%c') sin equivalente en Robot", vkWindows, c));
        }
        return codigo;
    }
}
