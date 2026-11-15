package legado;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
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

    /**
     * kernel32.dll: los "Job Objects" de Windows, que agrupan procesos. Un job con
     * KILL_ON_JOB_CLOSE cierra sus procesos cuando se cierra el último handle del
     * job; ese handle solo lo tiene este programa, así que, termine Java como
     * termine (la X de la consola, Ctrl+C, el Administrador de tareas o un fallo),
     * Windows cierra DOSBox (C11).
     */
    private interface Kernel32Extra extends StdCallLibrary {
        Kernel32Extra INSTANCIA = Native.load("kernel32", Kernel32Extra.class);
        HANDLE CreateJobObjectW(Pointer atributos, WString nombre);
        boolean SetInformationJobObject(HANDLE job, int clase, Pointer informacion, int tamano);
        boolean AssignProcessToJobObject(HANDLE job, HANDLE proceso);
    }

    private static final int JOB_OBJECT_EXTENDED_LIMIT_INFORMATION = 9;
    /** sizeof(JOBOBJECT_EXTENDED_LIMIT_INFORMATION) en Windows de 64 bits. */
    private static final int TAMANO_LIMITES_JOB = 144;
    /** Posición de LimitFlags dentro de la estructura (tras dos LARGE_INTEGER). */
    private static final int POSICION_LIMIT_FLAGS = 16;
    private static final int JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE = 0x2000;
    private static final int PROCESS_TERMINATE = 0x0001, PROCESS_SET_QUOTA = 0x0100;

    /** El job de los DOSBox lanzados por el programa (se crea una vez y no se cierra nunca a mano). */
    private static HANDLE job;

    /**
     * Mete el proceso de DOSBox en el job que lo cerrará al terminar Java. Si no se
     * puede (Java de 32 bits o Windows lo rechaza), se avisa y se sigue: quedan el
     * gancho de cierre y el fichero dosbox.pid para el siguiente arranque.
     */
    private static void cerrarConJava(long pid) {
        try {
            if (Native.POINTER_SIZE != 8) {
                System.out.println("Aviso: Java de 32 bits; DOSBox no se cerrará solo si Java termina de golpe.");
                return;
            }
            synchronized (VentanaDosBox.class) {
                if (job == null) {
                    HANDLE nuevo = Kernel32Extra.INSTANCIA.CreateJobObjectW(Pointer.NULL, null);
                    Memory limites = new Memory(TAMANO_LIMITES_JOB);
                    limites.clear();
                    limites.setInt(POSICION_LIMIT_FLAGS, JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE);
                    if (nuevo == null || !Kernel32Extra.INSTANCIA.SetInformationJobObject(nuevo,
                            JOB_OBJECT_EXTENDED_LIMIT_INFORMATION, limites, TAMANO_LIMITES_JOB)) {
                        System.out.println("Aviso: Windows no deja crear el job (error " + Native.getLastError() + ").");
                        return;
                    }
                    job = nuevo;
                }
            }
            HANDLE proceso = Kernel32.INSTANCE.OpenProcess(PROCESS_TERMINATE | PROCESS_SET_QUOTA, false, (int) pid);
            boolean ok = proceso != null && Kernel32Extra.INSTANCIA.AssignProcessToJobObject(job, proceso);
            if (!ok) System.out.println("Aviso: DOSBox no se ha podido asociar al job (error " + Native.getLastError() + ").");
            if (proceso != null) Kernel32.INSTANCE.CloseHandle(proceso);
        } catch (UnsatisfiedLinkError e) {
            System.out.println("Aviso: sin Job Objects (" + e.getMessage() + ").");
        }
    }

    private static final int DWMWA_WINDOW_CORNER_PREFERENCE = 33;
    private static final int DWMWCP_DONOTROUND = 1;
    private static final int SW_RESTORE = 9;

    private final Process proceso;
    private final HWND ventana;
    private final Robot robot;
    private final File pid;

    /** Veces que se puede recuperar el foco en una misma operación antes de abandonarla. */
    public static final int MAX_RECUPERACIONES = 3;
    /** Recuperaciones del foco en la operación en curso (ver asegurarDelante). */
    private int recuperaciones;

    private VentanaDosBox(Process proceso, HWND ventana, Robot robot, File pid) {
        this.proceso = proceso;
        this.ventana = ventana;
        this.robot = robot;
        this.pid = pid;
    }

    /**
     * Fichero con el número de proceso del DOSBox que lanzó el programa (junto a
     * nuestro .conf, fuera de Database). Sirve para cerrarlo si el programa
     * terminó sin poder hacerlo (por ejemplo, si se mató Java a la fuerza).
     */
    public static File ficheroPid(File conf) {
        return new File(conf.getAbsoluteFile().getParentFile(), "dosbox.pid");
    }

    /**
     * Cierra el DOSBox que dejó abierto una ejecución anterior del programa, si lo
     * hay. Solo se cierra el proceso cuyo número está en el fichero y solo si es un
     * DOSBox.exe: nunca otro programa ni otro DOSBox que tenga abierto el usuario.
     *
     * @return qué se ha hecho, para la consola, o null si no había nada que cerrar
     */
    public static String cerrarAnterior(File ficheroPid) {
        if (!ficheroPid.isFile()) return null;
        String texto;
        try {
            texto = java.nio.file.Files.readString(ficheroPid.toPath()).strip();
        } catch (IOException e) {
            texto = "";
        }
        ficheroPid.delete();
        long numero;
        try {
            numero = Long.parseLong(texto);
        } catch (NumberFormatException e) {
            return null;
        }
        var proceso = ProcessHandle.of(numero);
        if (proceso.isEmpty() || !proceso.get().isAlive()) return null;
        String programa = proceso.get().info().command().orElse("");
        if (!programa.toLowerCase().endsWith("dosbox.exe")) return null;
        proceso.get().destroy();
        try {
            proceso.get().onExit().get(3, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            proceso.get().destroyForcibly();
        }
        return "Cerrado el DOSBox que dejó abierto una ejecución anterior (proceso " + numero + ").";
    }

    /**
     * Lanza DOSBox con la aplicación legada y espera a que aparezca su ventana.
     *
     * @param carpetaApp carpeta Database-MSDOS (la que contiene database.bat)
     * @param conf       nuestro p3-dosbox.conf
     */
    public static VentanaDosBox lanzar(File carpetaApp, File conf, long esperaMaxMs)
            throws IOException, InterruptedException {
        File pid = ficheroPid(conf);
        String anterior = cerrarAnterior(pid);
        if (anterior != null) System.out.println(anterior);
        File dosbox = new File(carpetaApp, "DOSBox-0.74/DOSBox.exe");
        // Mismos argumentos que database.bat, más -conf con nuestra configuración.
        ProcessBuilder pb = new ProcessBuilder(dosbox.getAbsolutePath(),
                "Database\\gwbasic.bat", "-noconsole", "-conf", conf.getAbsolutePath());
        pb.directory(carpetaApp);
        Process proceso = pb.start();
        cerrarConJava(proceso.pid());
        java.nio.file.Files.writeString(pid.toPath(), String.valueOf(proceso.pid()));

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
            pid.delete();
            throw new IOException("No aparece la ventana de DOSBox con GWBASIC en " + esperaMaxMs + " ms");
        }
        quitarEsquinasRedondeadas(encontrada);
        try {
            return new VentanaDosBox(proceso, encontrada, new Robot(), pid);
        } catch (java.awt.AWTException e) {
            proceso.destroy();
            pid.delete();
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

    /** Empieza la cuenta de recuperaciones del foco para una operación nueva. */
    public void empezarOperacion() {
        recuperaciones = 0;
    }

    /**
     * Comprueba que DOSBox sigue delante y, si no, lo vuelve a poner delante. Pasa
     * cuando alguien usa el equipo servidor durante una operación (por ejemplo, un
     * clic en su navegador mientras otro cliente consulta): en lugar de abandonar la
     * operación, se recupera el foco, como mucho MAX_RECUPERACIONES veces por
     * operación, para no pelearse sin fin con quien está usando el equipo.
     *
     * @throws IllegalStateException si se ha superado el tope o Windows no deja ponerlo delante
     */
    public void asegurarDelante() {
        if (estaDelante()) return;
        String quien = tituloVentanaConFoco();
        if (++recuperaciones > MAX_RECUPERACIONES) {
            throw new IllegalStateException("DOSBox ha perdido el foco más de " + MAX_RECUPERACIONES
                    + " veces en la misma operación (ahora lo tiene \"" + quien + "\")");
        }
        System.out.println("DOSBox ha perdido el foco (lo tiene \"" + quien + "\"): se vuelve a poner delante ("
                + recuperaciones + "/" + MAX_RECUPERACIONES + ").");
        traerAlFrente();
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

    /** El Robot con el que se pulsan las teclas y se captura la pantalla. */
    public Robot robot() {
        return robot;
    }

    /** true si el proceso de DOSBox sigue en marcha. */
    public boolean sigueAbierta() {
        return proceso.isAlive();
    }

    /** Cierra DOSBox. La aplicación no graba nada al cerrarse, así que Database no cambia. */
    public void cerrar() {
        proceso.destroy();
        pid.delete();
    }
}
