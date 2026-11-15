package servidor;

import com.sun.net.httpserver.HttpServer;
import legado.AplicacionLegada;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

/**
 * El servidor web de la práctica, hecho con com.sun.net.httpserver.HttpServer,
 * el servidor HTTP mínimo que trae el JDK (no hace falta instalar nada).
 *
 * Escucha en todas las interfaces de red del equipo, así que se puede entrar
 * desde otro ordenador o desde el móvil con http://IP-del-equipo:puerto (R7).
 * Cada dirección se asocia a quien la atiende: /api/... a ApiLegado y el resto
 * a FicherosEstaticos, que sirve la página.
 */
public class ServidorWeb {

    private final HttpServer server;

    /**
     * @param puerto   puerto TCP (8080; 0 = uno libre, para las pruebas)
     * @param legado   la aplicación legada (el wrapper)
     * @param frontend carpeta con la página web
     */
    public ServidorWeb(int puerto, AplicacionLegada legado, Path frontend) throws IOException {
        server = HttpServer.create(new InetSocketAddress(puerto), 0);

        ApiLegado api = new ApiLegado(legado);
        server.createContext("/api/registros", api.registros());
        server.createContext("/api/nombre", api.nombre());
        server.createContext("/api/cinta", api.cinta());

        // Servir el frontend desde la carpeta frontend/
        server.createContext("/", new FicherosEstaticos(frontend));

        server.setExecutor(null);
    }

    public void arrancar() {
        server.start();
    }

    /** Deja de atender peticiones. */
    public void parar() {
        server.stop(0);
    }

    /** Puerto en el que escucha (útil si se pidió el 0). */
    public int puerto() {
        return server.getAddress().getPort();
    }
}
