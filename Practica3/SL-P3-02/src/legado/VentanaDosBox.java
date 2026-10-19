package legado;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HKL;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * La ventana de DOSBox donde corre la aplicación legada.
 *
 * Java no sabe nada de las ventanas de otros programas, así que se usa JNA:
 * una librería que permite llamar desde Java a funciones de Windows (user32.dll)
 * como si fueran métodos normales. Con ella se busca la ventana de DOSBox, se
 * trae al frente y se mide su área de dibujo (el "área de cliente": la ventana
 * sin el borde ni la barra de título).
 *
 * DOSBox se lanza igual que lo hace database.bat, pero con nuestro .conf
 * (fuera de la carpeta Database, que no se toca).
 */
public class VentanaDosBox {

    /** Funciones de Windows que jna-platform no trae ya declaradas. */
    private interface User32Extra extends StdCallLibrary {
        User32Extra INSTANCIA = Native.load("user32", User32Extra.class);
        boolean ClientToScreen(HWND ventana, POINT punto);
        boolean IsIconic(HWND ventana);
    }

    /** dwmapi.dll: el gestor de ventanas de Windows (lo que dibuja las esquinas redondeadas). */
    private interface Dwmapi extends StdCallLibrary {
        Dwmapi INSTANCIA = Native.load("dwmapi", Dwmapi.class);
        int DwmSetWindowAttribute(HWND ventana, int atributo, IntByReference valor, int tamano);
    }

    private static final int DWMWA_WINDOW_CORNER_PREFERENCE = 33;
    private static final int DWMWCP_DONOTROUND = 1;
    private static final int SW_RESTORE = 9;

    private final Process proceso;
    private final HWND ventana;
    private final Robot robot;

    private VentanaDosBox(Process proceso, HWND ventana, Robot robot) {
        this.proceso = proceso;
        this.ventana = ventana;
        this.robot = robot;
    }

    /**
     * Lanza DOSBox con la aplicación legada y espera a que aparezca su ventana.
     *
     * @param carpetaApp carpeta Database-MSDOS (la que contiene database.bat)
     * @param conf       nuestro p3-dosbox.conf
     */
    public static VentanaDosBox lanzar(File carpetaApp, File conf, long esperaMaxMs)
            throws IOException, InterruptedException {
        File dosbox = new File(carpetaApp, "DOSBox-0.74/DOSBox.exe");
        // Mismos argumentos que database.bat, más -conf con nuestra configuración.
        ProcessBuilder pb = new ProcessBuilder(dosbox.getAbsolutePath(),
                "Database\\gwbasic.bat", "-noconsole", "-conf", conf.getAbsolutePath());
        pb.directory(carpetaApp);
        Process proceso = pb.start();

        long limite = System.currentTimeMillis() + esperaMaxMs;
        HWND encontrada = null;
        while (encontrada == null && System.currentTimeMillis() < limite) {
            Thread.sleep(100);
            if (!proceso.isAlive()) {
                throw new IOException("DOSBox se ha cerrado al arrancar (código " + proceso.exitValue() + ")");
            }
            encontrada = buscarVentana((int) proceso.pid());
            // La ventana existe antes de que GW-BASIC arranque: se espera a su título.
            if (encontrada != null && !titulo(encontrada).contains("GWBASIC")) {
                encontrada = null;
            }
        }
        if (encontrada == null) {
            proceso.destroy();
            throw new IOException("No aparece la ventana de DOSBox con GWBASIC en " + esperaMaxMs + " ms");
        }
        quitarEsquinasRedondeadas(encontrada);
        try {
            return new VentanaDosBox(proceso, encontrada, new Robot());
        } catch (java.awt.AWTException e) {
            proceso.destroy();
            throw new IOException("No se puede crear java.awt.Robot", e);
        }
    }

    /** Recorre las ventanas de Windows y devuelve la ventana visible del proceso con ese PID. */
    private static HWND buscarVentana(int pid) {
        HWND[] resultado = new HWND[1];
        User32.INSTANCE.EnumWindows((hwnd, datos) -> {
            IntByReference pidVentana = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hwnd, pidVentana);
            if (pidVentana.getValue() == pid && User32.INSTANCE.IsWindowVisible(hwnd)) {
                resultado[0] = hwnd;
                return false; // deja de recorrer
            }
            return true;
        }, Pointer.NULL);
        return resultado[0];
    }

    private static String titulo(HWND hwnd) {
        char[] texto = new char[256];
        int n = User32.INSTANCE.GetWindowText(hwnd, texto, texto.length);
        return new String(texto, 0, n);
    }

    /**
     * Windows 11 redondea las esquinas de las ventanas y pisa los píxeles de las
     * esquinas inferiores del área de cliente (la primera celda de la fila 25).
     * Se le pide que no lo haga para esta ventana. En Windows 10 no existe el
     * atributo y la llamada falla sin consecuencias.
     */
    private static void quitarEsquinasRedondeadas(HWND hwnd) {
        try {
            Dwmapi.INSTANCIA.DwmSetWindowAttribute(hwnd, DWMWA_WINDOW_CORNER_PREFERENCE,
                    new IntByReference(DWMWCP_DONOTROUND), 4);
        } catch (UnsatisfiedLinkError e) {
            // Sin dwmapi.dll no hay esquinas redondeadas que quitar.
        }
    }

    /** true si DOSBox es la ventana que tiene el foco (la que recibe el teclado). */
    public boolean estaDelante() {
        return ventana.equals(User32.INSTANCE.GetForegroundWindow());
    }

    /** La ventana que tiene ahora el foco, para devolvérselo después con {@link #devolverFoco}. */
    public static HWND ventanaConFoco() {
        return User32.INSTANCE.GetForegroundWindow();
    }

    /**
     * Pone DOSBox delante y con el foco. Windows solo deja cambiar el foco al
     * programa que ha recibido la última pulsación del usuario; por eso, si el
     * primer intento falla, Robot pulsa y suelta Mayúsculas (una tecla que no
     * hace nada por sí sola) y se vuelve a intentar. Si aun así no lo consigue,
     * se enlaza un momento con el hilo de la ventana que tiene el foco.
     *
     * @throws IllegalStateException si DOSBox no queda delante
     */
    public void traerAlFrente() {
        if (User32Extra.INSTANCIA.IsIconic(ventana)) {
            User32.INSTANCE.ShowWindow(ventana, SW_RESTORE);
        }
        if (intentarFoco()) return;

        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.keyRelease(KeyEvent.VK_SHIFT);
        if (intentarFoco()) return;

        HWND conFoco = User32.INSTANCE.GetForegroundWindow();
        int hiloFoco = User32.INSTANCE.GetWindowThreadProcessId(conFoco, null);
        int hiloPropio = com.sun.jna.platform.win32.Kernel32.INSTANCE.GetCurrentThreadId();
        User32.INSTANCE.AttachThreadInput(new DWORD(hiloPropio), new DWORD(hiloFoco), true);
        try {
            if (intentarFoco()) return;
        } finally {
            User32.INSTANCE.AttachThreadInput(new DWORD(hiloPropio), new DWORD(hiloFoco), false);
        }
        throw new IllegalStateException("Windows no deja poner DOSBox delante");
    }

    private boolean intentarFoco() {
        User32.INSTANCE.BringWindowToTop(ventana);
        User32.INSTANCE.SetForegroundWindow(ventana);
        for (int i = 0; i < 10; i++) {
            if (estaDelante()) return true;
            robot.delay(30);
        }
        return false;
    }

    /** Título de la ventana que tiene ahora el foco (para los mensajes de error). */
    public static String tituloVentanaConFoco() {
        HWND conFoco = User32.INSTANCE.GetForegroundWindow();
        return conFoco == null ? "(ninguna)" : titulo(conFoco);
    }

    /** Devuelve el foco a la ventana que lo tenía antes (normalmente el navegador). */
    public void devolverFoco(HWND anterior) {
        if (anterior != null && !anterior.equals(ventana)) {
            User32.INSTANCE.SetForegroundWindow(anterior);
        }
    }

    /** Área de cliente de DOSBox en coordenadas de pantalla (píxeles físicos). */
    public Rectangle areaCliente() {
        RECT r = new RECT();
        User32.INSTANCE.GetClientRect(ventana, r);
        POINT origen = new POINT(0, 0);
        User32Extra.INSTANCIA.ClientToScreen(ventana, origen);
        return new Rectangle(origen.x, origen.y, r.right - r.left, r.bottom - r.top);
    }

    /** Distribución de teclado del hilo de DOSBox (la que traduce las teclas a caracteres). */
    public HKL distribucionTeclado() {
        int hilo = User32.INSTANCE.GetWindowThreadProcessId(ventana, null);
        return User32.INSTANCE.GetKeyboardLayout(hilo);
    }

    public Robot robot() {
        return robot;
    }

    public boolean sigueAbierta() {
        return proceso.isAlive();
    }

    /** Cierra DOSBox. La aplicación no graba nada al cerrarse, así que Database no cambia. */
    public void cerrar() {
        proceso.destroy();
    }
}
