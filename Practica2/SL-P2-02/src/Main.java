import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {

    private static final String WS3270 = "C:\\Program Files\\wc3270\\ws3270.exe";

    private static final String HOST = "155.210.152.51";
    private static final int PORT = 3270;

    private static final String USER = "grupo_02";
    private static final String PASSWORD = "secreto6";

    // El enunciado exige al menos 1 s entre conexión y desconexión,
    // también si hay un error. Dejamos margen: 1,5 s.
    private static final long MIN_CONEXION_MS = 1500;

    // Instante en que se envió Connect (0 = todavía no se ha conectado).
    private static long instanteConexion = 0;

    public static void main(String[] args) {

        Process process = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;

        try {
            System.out.println("========================================");
            System.out.println("   PRUEBA JAVA + WS3270 + MAINFRAME");
            System.out.println("========================================");

            // --------------------------------------------------
            // 1. Arrancar ws3270
            // --------------------------------------------------

            System.out.println("\n[1] Arrancando ws3270...");

            // -localcp 65001: ws3270 usa UTF-8 en su entrada y salida,
            // igual que los lectores de abajo (si no, usa cp1252 y las
            // tildes y la ñ llegan mal).
            ProcessBuilder pb = new ProcessBuilder(
                    WS3270,
                    "-script",
                    "-localcp", "65001"
            );

            // Evitamos que ws3270 mezcle mensajes con stderr
            pb.redirectErrorStream(true);

            process = pb.start();

            writer = new BufferedWriter(
                    new OutputStreamWriter(
                            process.getOutputStream(),
                            StandardCharsets.UTF_8
                    )
            );

            reader = new BufferedReader(
                    new InputStreamReader(
                            process.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            );

            System.out.println("    OK: ws3270 iniciado.");

            // --------------------------------------------------
            // 2. Conectar
            // --------------------------------------------------

            System.out.println("\n[2] Conectando al mainframe...");

            instanteConexion = System.currentTimeMillis();
            send(writer, "Connect(" + HOST + ":" + PORT + ")");

            waitForOk(reader);

            System.out.println("    OK: conexión establecida.");

            // --------------------------------------------------
            // 3. Esperar a que aparezca la pantalla
            // --------------------------------------------------

            System.out.println("\n[3] Esperando pantalla inicial...");

            send(writer, "Wait(InputField)");

            waitForOk(reader);

            // Wait(InputField) puede volver antes de que el mainframe
            // haya dibujado la pantalla (sale vacía o con "Working").
            // Repetimos Ascii() hasta que tenga contenido.
            waitForScreen(writer, reader, 10000);

            System.out.println("    OK: pantalla preparada.");

            // --------------------------------------------------
            // 4. Capturar pantalla
            // --------------------------------------------------

            System.out.println("\n[4] Pantalla recibida:");

            send(writer, "Ascii()");

            System.out.print(readScreen(reader));

            // 5. La desconexión se hace en finally, para que se
            //    respete el segundo mínimo también si hay un error.

        } catch (Exception e) {

            System.err.println("\nERROR:");
            e.printStackTrace();

        } finally {

            // --------------------------------------------------
            // 5. Desconectar (siempre por aquí, haya error o no)
            // --------------------------------------------------

            System.out.println("\n[5] Desconectando...");
            disconnect(process, writer, reader);

            System.out.println("\n========================================");
            System.out.println("             FIN DE LA PRUEBA");
            System.out.println("========================================");
        }
    }


    /**
     * Único punto de desconexión. Espera hasta que hayan pasado
     * MIN_CONEXION_MS desde Connect, desconecta, cierra ws3270 y, solo
     * si no responde, lo mata.
     */
    private static void disconnect(
            Process process,
            BufferedWriter writer,
            BufferedReader reader
    ) {

        if (process == null) {
            return;
        }

        try {
            if (instanteConexion != 0) {
                long transcurrido = System.currentTimeMillis() - instanteConexion;
                if (transcurrido < MIN_CONEXION_MS) {
                    Thread.sleep(MIN_CONEXION_MS - transcurrido);
                }
                System.out.println("    Tiempo conectado: "
                        + (System.currentTimeMillis() - instanteConexion) + " ms");
                send(writer, "Disconnect()");
                waitForOk(reader);
            }
            send(writer, "Quit()");
            process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("    Aviso al desconectar: " + e.getMessage());
        } finally {
            // Solo llega aquí vivo si ws3270 no ha respondido; como ya se
            // ha esperado el mínimo, matarlo no incumple la regla.
            if (process.isAlive()) {
                process.destroy();
            }
            System.out.println("    OK: desconectado.");
        }
    }


    /**
     * Repite Ascii() hasta que la pantalla tenga texto y el mainframe
     * no esté trabajando ("Working"), o hasta agotar el tiempo.
     */
    private static void waitForScreen(
            BufferedWriter writer,
            BufferedReader reader,
            long maxMs
    ) throws IOException, InterruptedException {

        long limite = System.currentTimeMillis() + maxMs;

        while (System.currentTimeMillis() < limite) {
            send(writer, "Ascii()");
            String pantalla = readScreen(reader);
            String texto = pantalla.replace("data:", "").trim();
            if (!texto.isEmpty() && !pantalla.contains("Working")) {
                return;
            }
            Thread.sleep(300);
        }

        throw new IOException("La pantalla no ha llegado en " + maxMs + " ms");
    }


    private static void send(
            BufferedWriter writer,
            String command
    ) throws IOException {

        System.out.println("    >> " + command);

        writer.write(command);
        writer.newLine();
        writer.flush();
    }


    private static void waitForOk(
            BufferedReader reader
    ) throws IOException {

        String line;

        while ((line = reader.readLine()) != null) {

            System.out.println("    << " + line);

            if (line.equals("ok")) {
                return;
            }

            if (line.startsWith("error")) {
                throw new IOException(
                        "ws3270 devolvió: " + line
                );
            }
        }

        throw new IOException(
                "ws3270 cerró la conexión inesperadamente."
        );
    }


    private static String readScreen(
            BufferedReader reader
    ) throws IOException {

        StringBuilder pantalla = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {

            System.out.println("    << " + line);

            /*
             * La salida de Ascii() termina cuando ws3270
             * vuelve a enviar "ok" (o "error").
             */
            if (line.equals("ok")) {
                return pantalla.toString();
            }

            if (line.startsWith("error")) {
                throw new IOException("ws3270 devolvió: " + line);
            }

            if (line.startsWith("data:")) {
                pantalla.append(line).append('\n');
            }
        }

        throw new IOException(
                "ws3270 cerró la conexión inesperadamente."
        );
    }
}