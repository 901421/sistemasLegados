package servidor;

import com.sun.net.httpserver.HttpServer;
import legado.AplicacionLegada;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * El servidor web de la práctica, hecho con com.sun.net.httpserver.HttpServer,
 * el servidor HTTP mínimo que trae el JDK (no hace falta instalar nada).
 *
 * Escucha en todas las interfaces de red del equipo, así que se puede entrar
 * desde otro ordenador o desde el móvil con http://IP-del-equipo:puerto (R7).
 * Cada dirección se asocia a quien la atiende: /api/... a ApiLegado y el resto
 * a FicherosEstaticos, que sirve la página.
 *
 * Atiende con varios hilos (D5): mientras una operación usa la aplicación legada
 * (el listado completo tarda unos segundos), la página, el CSS y el JS se siguen
 * sirviendo al momento. Las operaciones sobre la aplicación esperan su turno en
 * el wrapper (Turno), de una en una.
 */
public final class ServidorWeb {

    /** Hilos que atienden peticiones a la vez (la aplicación se usa de una en una igualmente). */
    private static final int HILOS = 8;

    private final HttpServer server;
    private final ExecutorService hilos = Executors.newFixedThreadPool(HILOS);

    /**
     * Reserva el puerto, sin atender todavía. Se hace antes de lanzar DOSBox: si
     * el puerto está ocupado (normalmente, porque el servidor ya está en marcha), el
     * programa avisa y termina sin tocar ningún DOSBox (C16).
     *
     * @param puerto puerto TCP (8080 por defecto; 0 = uno libre, para las pruebas)
     * @throws java.net.BindException si el puerto ya está en uso
     */
    public ServidorWeb(int puerto) throws IOException {
        server = HttpServer.create(new InetSocketAddress(puerto), 0);
        server.setExecutor(hilos);
    }

    /** Reserva el puerto y publica la web y la API (para las pruebas). */
    public ServidorWeb(int puerto, AplicacionLegada legado, Path frontend) throws IOException {
        this(puerto);
        publicar(legado, frontend);
    }

    /**
     * Asocia cada dirección a quien la atiende: la API a la aplicación legada y el
     * resto a los ficheros de la web.
     *
     * @param legado   la aplicación legada (el wrapper)
     * @param frontend carpeta con la página web
     */
    public void publicar(AplicacionLegada legado, Path frontend) {
        ApiLegado api = new ApiLegado(legado);
        server.createContext("/api/registros", api.registros());
        server.createContext("/api/nombre", api.nombre());
        server.createContext("/api/cinta", api.cinta());
        server.createContext("/api/pantalla", api.pantalla());

        // Servir el frontend desde la carpeta frontend/
        server.createContext("/", new FicherosEstaticos(frontend));
    }

    /** Empieza a atender peticiones (después de publicar la web y la API). */
    public void arrancar() {
        server.start();
    }

    /** Deja de atender peticiones. */
    public void parar() {
        server.stop(0);
        hilos.shutdownNow();
    }

    /**
     * Las direcciones con las que se puede entrar desde otro equipo de la red
     * (las IPv4 útiles de las interfaces activas), para mostrarlas al arrancar (R7).
     */
    public java.util.List<String> direcciones() {
        java.util.List<String> lista = new java.util.ArrayList<>();
        try {
            for (var red : java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces())) {
                if (!red.isUp() || red.isLoopback()) continue;
                for (var ip : java.util.Collections.list(red.getInetAddresses())) {
                    if (direccionUtil(red.getDisplayName() + " " + red.getName(), ip)) {
                        lista.add("http://" + ip.getHostAddress() + ":" + puerto());
                    }
                }
            }
        } catch (java.net.SocketException e) {
            // Sin acceso a las interfaces: solo se muestra localhost.
        }
        return lista;
    }

    /** Palabras que indican un adaptador virtual (de WSL, Hyper-V o máquinas virtuales). */
    private static final String[] VIRTUALES = {"virtual", "hyper-v", "vethernet", "wsl", "vmware", "virtualbox"};

    /**
     * true si por esa dirección puede entrar otro equipo de la red: IPv4, no de
     * autoconfiguración (169.254.x.x, que Windows pone a los adaptadores sin red) y
     * de un adaptador que no es virtual (C17).
     *
     * @param adaptador nombre y descripción del adaptador de red
     */
    public static boolean direccionUtil(String adaptador, java.net.InetAddress ip) {
        if (!(ip instanceof java.net.Inet4Address) || ip.isLinkLocalAddress() || ip.isLoopbackAddress()) return false;
        String nombre = adaptador.toLowerCase();
        for (String v : VIRTUALES) {
            if (nombre.contains(v)) return false;
        }
        return true;
    }

    /** Puerto en el que escucha (útil si se pidió el 0). */
    public int puerto() {
        return server.getAddress().getPort();
    }
}
