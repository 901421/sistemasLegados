package servidor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sirve los ficheros de la página web (HTML, CSS, JavaScript, fuentes e imágenes) desde la carpeta
 * frontend/, para que el navegador cargue la página desde el mismo servidor
 * que responde a la API.
 *
 * Solo entrega ficheros que estén dentro de frontend/: una dirección como
 * /../src/... se resuelve y, si sale de la carpeta, se responde 404.
 */
public class FicherosEstaticos implements HttpHandler {

    private final Path raiz;

    /** @param carpeta carpeta de la web (frontend) */
    public FicherosEstaticos(Path carpeta) {
        this.raiz = carpeta.toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Solo permitimos peticiones GET
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "GET");
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        // Ruta solicitada
        String ruta = exchange.getRequestURI().getPath();

        if (ruta.equals("/")) {
            ruta = "/index.html";
        }

        // Localizar el archivo solicitado
        Path archivo = raiz.resolve(ruta.substring(1)).normalize();

        // Evitar acceso a archivos fuera del frontend
        if (!archivo.startsWith(raiz)
                || !Files.isRegularFile(archivo)) {

            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        // Determinar el tipo de contenido
        String contentType;

        if (ruta.endsWith(".html")) {
            contentType = "text/html; charset=UTF-8";
        } else if (ruta.endsWith(".css")) {
            contentType = "text/css; charset=UTF-8";
        } else if (ruta.endsWith(".js")) {
            contentType = "application/javascript; charset=UTF-8";
        } else if (ruta.endsWith(".woff2")) {
            contentType = "font/woff2";
        } else if (ruta.endsWith(".woff")) {
            contentType = "font/woff";
        } else if (ruta.endsWith(".png")) {
            contentType = "image/png";
        } else if (ruta.endsWith(".svg")) {
            contentType = "image/svg+xml";
        } else {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        // Leer el archivo
        byte[] contenido = Files.readAllBytes(archivo);

        // Enviar respuesta HTTP
        exchange.getResponseHeaders().set(
                "Content-Type", contentType
        );

        exchange.sendResponseHeaders(200, contenido.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(contenido);
        }
    }
}
