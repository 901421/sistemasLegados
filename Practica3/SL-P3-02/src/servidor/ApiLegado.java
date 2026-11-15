package servidor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import legado.Juego;
import legado.ServicioLegado;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Los tres puntos de la API que usa la página web. Cada uno llama al wrapper
 * (ServicioLegado), que hace la operación en la aplicación legada, y devuelve
 * el resultado en JSON:
 * <ul>
 *   <li>/api/registros: número de registros (F1).</li>
 *   <li>/api/nombre?q=...: datos de los programas con ese nombre (F2).</li>
 *   <li>/api/cinta?id=...: programas de una cinta, en orden (F3).</li>
 * </ul>
 */
public class ApiLegado {

    private final ServicioLegado servicio;

    public ApiLegado(ServicioLegado servicio) {
        this.servicio = servicio;
    }

    /** Endpoint /api/registros */
    public HttpHandler registros() {
        return new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (manejarCorsOptions(exchange)) return;
                try {
                    int total = servicio.obtenerTotalRegistros();
                    String json = "{\"totalRegistros\":" + total + "}";
                    responderJson(exchange, 200, json);
                } catch (Exception e) {
                    responderJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            }
        };
    }

    /** Endpoint /api/nombre?q=DEFENDER */
    public HttpHandler nombre() {
        return new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (manejarCorsOptions(exchange)) return;
                try {
                    String query = exchange.getRequestURI().getQuery();
                    String q = Parametros.obtener(query, "q");
                    if (q == null || q.isBlank()) {
                        responderJson(exchange, 400, "{\"error\":\"Falta el parametro q\"}");
                        return;
                    }
                    List<Juego> resultados = servicio.buscarPorNombre(q.trim());
                    responderJson(exchange, 200, Json.lista(resultados));
                } catch (Exception e) {
                    responderJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            }
        };
    }

    /** Endpoint /api/cinta?id=3 */
    public HttpHandler cinta() {
        return new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (manejarCorsOptions(exchange)) return;
                try {
                    String query = exchange.getRequestURI().getQuery();
                    String id = Parametros.obtener(query, "id");
                    if (id == null || id.isBlank()) {
                        responderJson(exchange, 400, "{\"error\":\"Falta el parametro id\"}");
                        return;
                    }

                    id = id.trim().toUpperCase();

                    if (!id.matches("(?:[A-Z]|[0-9]+)")) {
                        responderJson(exchange, 400,
                                "{\"error\":\"El identificador debe ser una letra o un numero\"}");
                        return;
                    }

                    List<Juego> resultados = servicio.listarPorCinta(id);
                    responderJson(exchange, 200, Json.lista(resultados));
                } catch (Exception e) {
                    responderJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            }
        };
    }

    /* Solución para peticiones desde web */
    private static boolean manejarCorsOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    /** Envía una respuesta HTTP en formato JSON */
    private static void responderJson(HttpExchange exchange, int codigo, String respuesta) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
