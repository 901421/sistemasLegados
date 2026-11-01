package legado;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main {

    private static ServicioLegado servicio;

    public static void main(String[] args) {
        try {
            File carpetaApp = new File("Database-MSDOS");
            File conf = new File("dosbox/p3-dosbox.conf");

            System.out.println("Arrancando DOSBox y cargando la aplicación legada...");
            servicio = new ServicioLegado(carpetaApp, conf);

            // Servidor nativo de Java en el puerto 8080
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            // Endpoint /api/registros
            server.createContext("/api/registros", new HttpHandler() {
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
            });

            // Endpoint /api/nombre?q=DEFENDER
            server.createContext("/api/nombre", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if (manejarCorsOptions(exchange)) return;
                    try {
                        String query = exchange.getRequestURI().getQuery();
                        String q = obtenerParametro(query, "q");
                        if (q == null || q.isBlank()) {
                            responderJson(exchange, 400, "{\"error\":\"Falta el parametro q\"}");
                            return;
                        }
                        List<Juego> resultados = servicio.buscarPorNombre(q.trim());
                        responderJson(exchange, 200, convertirListaAJson(resultados));
                    } catch (Exception e) {
                        responderJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                    }
                }
            });

            // Endpoint /api/cinta?id=3
            server.createContext("/api/cinta", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if (manejarCorsOptions(exchange)) return;
                    try {
                        String query = exchange.getRequestURI().getQuery();
                        String id = obtenerParametro(query, "id");
                        if (id == null || id.isBlank()) {
                            responderJson(exchange, 400, "{\"error\":\"Falta el parametro id\"}");
                            return;
                        }
                        List<Juego> resultados = servicio.listarPorCinta(id.trim());
                        responderJson(exchange, 200, convertirListaAJson(resultados));
                    } catch (Exception e) {
                        responderJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                    }
                }
            });

            server.setExecutor(null);
            server.start();

            System.out.println("¡Servidor listo y escuchando en http://localhost:8080!");

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /** Extrae el valor de un parámetro de la URL (ej. ?q=DEFENDER) */
    private static String obtenerParametro(String query, String clave) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] par = param.split("=");
            if (par.length > 1 && par[0].equals(clave)) {
                return java.net.URLDecoder.decode(par[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    /** Convierte una lista de objetos Juego a un string en formato JSON */
    private static String convertirListaAJson(List<Juego> juegos) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < juegos.size(); i++) {
            json.append(juegos.get(i).toJson());
            if (i < juegos.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }
}