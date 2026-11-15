package legado;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HKL;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Genera la entrada de la aplicación legada pulsando teclas con java.awt.Robot,
 * como indica el guion.
 *
 * Robot no escribe caracteres sino que pulsa teclas (como un dedo sobre el
 * teclado): keyPress(VK_A) pulsa la tecla A. El carácter que sale de cada tecla
 * depende de la distribución del teclado: en el español, el apóstrofo está a la
 * derecha del 0 y "&amp;" es Mayúsculas+6. Para no depender de una tabla escrita a
 * mano, se pregunta a Windows (VkKeyScanEx) qué tecla y qué modificadores
 * producen cada carácter en la distribución de DOSBox.
 *
 * Antes de cada pulsación se comprueba que DOSBox sigue delante. Si alguien ha
 * puesto otra ventana delante, se recupera el foco (VentanaDosBox.asegurarDelante,
 * con un tope por operación) y se vuelve a comprobar justo antes de pulsar: nunca
 * se escribe en otra ventana.
 */
public class Teclado {

    /** Pausa tras cada pulsación, para que DOSBox procese las teclas de una en una. */
    private static final int PAUSA_MS = 20;

    private final VentanaDosBox dosbox;
    private final Robot robot;
    private final HKL distribucion;
    /** Si es true, se escribe en la consola cada tecla que se pulsa (para diagnosticar). */
    private boolean traza;

    /** Teclado sobre esa ventana de DOSBox, con la distribución de teclado que usa DOSBox. */
    public Teclado(VentanaDosBox dosbox) {
        this.dosbox = dosbox;
        this.robot = dosbox.robot();
        this.distribucion = dosbox.distribucionTeclado();
    }

    /** Si es true, se escribe en la consola cada tecla que se pulsa (para diagnosticar). */
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

    /** Pulsa Enter (fin de la respuesta a un INPUT de BASIC). */
    public void enter() {
        pulsar(KeyEvent.VK_ENTER, List.of(), "Enter");
    }

    /** Pulsa la barra espaciadora (página siguiente del listado). */
    public void espacio() {
        pulsar(KeyEvent.VK_SPACE, List.of(), "espacio");
    }

    /**
     * true si el carácter c se puede escribir en DOSBox con Robot. No se puede si:
     * el teclado no tiene tecla para él; necesita AltGr (Robot lo simula con
     * Ctrl+Alt y DOSBox no lo toma como AltGr: # @ ] | ~ en el teclado español);
     * su tecla es una tecla muerta (^ `); o Java no tiene un código de tecla que
     * Robot acepte (ñ, º, ç...). Comprobado con pruebas.CapturarJuegoCaracteres.
     */
    public boolean puedeEscribir(char c) {
        return motivoNoEscribible(c) == null;
    }

    /** Por qué no se puede escribir c, o null si se puede. */
    private String motivoNoEscribible(char c) {
        short codigo = User32.INSTANCE.VkKeyScanExW(c, distribucion);
        if (codigo == -1) return "el teclado no tiene ninguna tecla para él";
        int vkWindows = codigo & 0xFF;
        int modificadores = (codigo >> 8) & 0xFF;
        if ((modificadores & 6) != 0) return "necesita AltGr";
        if (vkWindows >= 0xBA) {
            int mapa = User32.INSTANCE.MapVirtualKeyEx(vkWindows, MAPVK_VK_TO_CHAR, distribucion);
            if ((mapa & 0x80000000) != 0) return "está en una tecla muerta";
            if (teclaJava(vkWindows, c, distribucion) > 0xFFFF) return "Robot no acepta su tecla";
        }
        return null;
    }

    /**
     * Pulsa la tecla (con Mayúsculas si hace falta) que produce el carácter c.
     *
     * @throws IllegalArgumentException sin pulsar nada si c no se puede escribir
     */
    public void tecla(char c) {
        String motivo = motivoNoEscribible(c);
        if (motivo != null) {
            throw new IllegalArgumentException("No se puede escribir '" + c + "': " + motivo);
        }
        short codigo = User32.INSTANCE.VkKeyScanExW(c, distribucion);
        int vkWindows = codigo & 0xFF;          // byte bajo: la tecla
        int modificadores = (codigo >> 8) & 0xFF; // byte alto: 1 Mayús (2 Ctrl y 4 Alt ya descartados)
        List<Integer> mods = (modificadores & 1) != 0 ? List.of(KeyEvent.VK_SHIFT) : List.of();
        int java = teclaJava(vkWindows, c, distribucion);
        pulsar(java, mods, String.format("'%c' (Windows 0x%02X, Java %s, modificadores %d)",
                c, vkWindows, KeyEvent.getKeyText(java), modificadores));
    }

    /**
     * Pulsa una tecla con sus modificadores: primero se bajan los modificadores,
     * luego la tecla, y se suelta todo en orden inverso. Los modificadores se
     * sueltan siempre, aunque Robot rechace la tecla, para no dejar Mayúsculas
     * pulsada en Windows.
     */
    private void pulsar(int teclaJava, List<Integer> modificadores, String descripcion) {
        dosbox.asegurarDelante(); // si alguien le ha quitado el foco, se recupera (con tope)
        if (!dosbox.estaDelante()) {
            throw new IllegalStateException("DOSBox ha dejado de estar delante antes de pulsar " + descripcion
                    + "; el foco lo tiene \"" + VentanaDosBox.tituloVentanaConFoco()
                    + "\": no se escribe en otra ventana");
        }
        if (traza) System.out.println("  tecla " + descripcion);
        int pulsados = 0;
        try {
            for (int m : modificadores) {
                robot.keyPress(m);
                pulsados++;
            }
            robot.keyPress(teclaJava);
            robot.keyRelease(teclaJava);
        } finally {
            for (int i = pulsados - 1; i >= 0; i--) robot.keyRelease(modificadores.get(i));
        }
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
