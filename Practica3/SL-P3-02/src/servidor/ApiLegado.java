package servidor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import legado.AplicacionLegada;
import legado.AplicacionOcupada;
import legado.PantallaInesperada;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Los tres puntos de la API que usa la página web. Cada uno valida lo que llega,
 * llama a la aplicación legada (a través del wrapper) y devuelve el resultado
 * en JSON:
 * <ul>
 *   <li>/api/registros: número de registros (F1).</li>
 *   <li>/api/nombre?q=...: datos de los programas con ese nombre (F2).</li>
 *   <li>/api/cinta?id=...: programas de una cinta, en orden (F3).</li>
 * </ul>
 *
 * Todo lo común está en un solo sitio, el método api() (D7): solo se acepta GET;
 * cada operación se registra en la consola (hora, dirección, código, duración); y
 * cada error se convierte en su código HTTP con un mensaje para el usuario:
 * <ul>
 *   <li>400 "entrada": lo escrito no se puede enviar a la aplicación (EntradaNoValida).</li>
 *   <li>503 "ocupado": la aplicación está atendiendo otras consultas y no ha
 *       llegado el turno a tiempo (AplicacionOcupada); la web lo muestra como aviso.</li>
 *   <li>503 "legado": la aplicación no ha respondido como se esperaba
 *       (PantallaInesperada) o otra ventana ha tomado el foco del equipo.</li>
 *   <li>500 "interno": cualquier otro fallo; el detalle va solo a la consola.</li>
 * </ul>
 *
 * No se envían cabeceras CORS: la página y la API las sirve el mismo servidor, y
 * así ninguna otra página puede usar la API (D7).
 */
public class ApiLegado {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AplicacionLegada legado;

    public ApiLegado(AplicacionLegada legado) {
        this.legado = legado;
    }

    /** /api/registros */
    public HttpHandler registros() {
        return api(peticion -> Json.registros(legado.obtenerTotalRegistros()));
    }

    /** /api/nombre?q=DEFENDER */
    public HttpHandler nombre() {
        return api(peticion -> Json.juegos(legado.buscarPorNombre(
                Validacion.nombre(peticion.parametro("q"), c -> legado.puedeEscribir((char) c)))));
    }

    /**
     * /api/pantalla: la última captura que ha hecho el wrapper, para verla en
     * directo mientras dura una consulta (D9). No espera turno ni toca DOSBox, y
     * no se registra en la consola (la web la pide varias veces por segundo).
     */
    public HttpHandler pantalla() {
        return api(peticion -> Json.enDirecto(legado.ultimaCaptura(), legado.operando()), false);
    }

    /** /api/cinta?id=A */
    public HttpHandler cinta() {
        return api(peticion -> Json.juegos(legado.listarPorCinta(Validacion.cinta(peticion.parametro("id")))));
    }

    // ---- Funcionamiento común ------------------------------------------------

    /** Lo que hace cada punto de la API con la petición: devuelve el JSON de la respuesta. */
    @FunctionalInterface
    private interface Accion {
        String responder(Peticion peticion) throws Exception;
    }

    /** Lo que una acción puede leer de la petición. */
    public record Peticion(String query) {
        /** Valor de un parámetro de la dirección, o null si no está (ver Parametros). */
        public String parametro(String clave) throws EntradaNoValida {
            return Parametros.obtener(query, clave);
        }
    }

    /** Envuelve una acción con lo común a toda la API: método, registro y errores. */
    private static HttpHandler api(Accion accion) {
        return api(accion, true);
    }

    /** Como api(accion), indicando si cada petición se escribe en el registro de la consola. */
    private static HttpHandler api(Accion accion, boolean registrar) {
        return exchange -> {
            long inicio = System.nanoTime();
            String direccion = exchange.getRequestURI().getRawPath()
                    + (exchange.getRequestURI().getRawQuery() == null ? "" : "?" + exchange.getRequestURI().getRawQuery());
            int codigo;
            String cuerpo;
            String detalle = "";
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Allow", "GET");
                codigo = 405;
                cuerpo = Json.error("entrada", "Solo se admiten peticiones GET.");
            } else {
                try {
                    cuerpo = accion.responder(new Peticion(exchange.getRequestURI().getRawQuery()));
                    codigo = 200;
                } catch (EntradaNoValida e) {
                    codigo = 400;
                    cuerpo = Json.error("entrada", e.getMessage());
                    detalle = e.getMessage();
                } catch (AplicacionOcupada e) {
                    codigo = 503;
                    cuerpo = Json.error("ocupado", e.getMessage());
                    detalle = e.getMessage();
                } catch (PantallaInesperada e) {
                    codigo = 503;
                    cuerpo = Json.error("legado", "La aplicación legada no ha mostrado lo esperado (" + e.esperado
                            + "). Si se ha quedado a medias, se reiniciará en la próxima consulta.");
                    detalle = e.getMessage();
                } catch (IllegalStateException e) {
                    // Teclado y VentanaDosBox: DOSBox ha perdido el foco o Windows no deja ponerlo delante.
                    codigo = 503;
                    cuerpo = Json.error("legado", "Otra ventana ha tomado el foco del equipo servidor durante la consulta. "
                            + "Inténtalo de nuevo sin usar el teclado ni el ratón de ese equipo.");
                    detalle = e.getMessage();
                } catch (Exception e) {
                    codigo = 500;
                    cuerpo = Json.error("interno", "Error interno del servidor. El detalle está en su consola.");
                    detalle = e.toString();
                    e.printStackTrace(System.out);
                }
            }
            responder(exchange, codigo, cuerpo);
            if (registrar || codigo != 200) System.out.printf("[%s] %s %s -> %d en %d ms%s%n", LocalTime.now().format(HORA),
                    exchange.getRequestMethod(), direccion, codigo, (System.nanoTime() - inicio) / 1_000_000,
                    detalle.isEmpty() ? "" : " (" + detalle + ")");
        };
    }

    /** Envía una respuesta HTTP en formato JSON. */
    private static void responder(HttpExchange exchange, int codigo, String cuerpo) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
