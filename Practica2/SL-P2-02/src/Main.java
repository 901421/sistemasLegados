import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {

    private static final String WS3270 = "C:\\Program Files\\wc3270\\ws3270.exe";

    private static final String HOST = "155.210.152.51";
    private static final int PORT = 3270;

    private static final String USER = "grupo_02";
    private static final String PASSWORD = "secreto6";

    public static void main(String[] args) {

        Process process = null;

        try {
            System.out.println("========================================");
            System.out.println("   PRUEBA JAVA + WS3270 + MAINFRAME");
            System.out.println("========================================");

            // --------------------------------------------------
            // 1. Arrancar ws3270
            // --------------------------------------------------

            System.out.println("\n[1] Arrancando ws3270...");

            ProcessBuilder pb = new ProcessBuilder(
                    WS3270,
                    "-script"
            );

            // Evitamos que ws3270 mezcle mensajes con stderr
            pb.redirectErrorStream(true);

            process = pb.start();

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            process.getOutputStream(),
                            StandardCharsets.UTF_8
                    )
            );

            BufferedReader reader = new BufferedReader(
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

            send(writer, "Connect(" + HOST + ":" + PORT + ")");

            waitForOk(reader);

            System.out.println("    OK: conexión establecida.");

            // --------------------------------------------------
            // 3. Esperar a que aparezca la pantalla
            // --------------------------------------------------

            System.out.println("\n[3] Esperando pantalla inicial...");

            send(writer, "Wait(InputField)");

            waitForOk(reader);

            System.out.println("    OK: pantalla preparada.");

            // --------------------------------------------------
            // 4. Capturar pantalla
            // --------------------------------------------------

            System.out.println("\n[4] Pantalla recibida:");

            send(writer, "Ascii()");

            readScreen(reader);

            // --------------------------------------------------
            // 5. Desconectar
            // --------------------------------------------------

            System.out.println("\n[5] Esperando antes de desconectar...");

            // IMPORTANTE:
            // El enunciado exige al menos 1 segundo
            // entre conexión y desconexión.
            Thread.sleep(1500);

            send(writer, "Disconnect()");

            waitForOk(reader);

            System.out.println("    OK: desconectado.");

        } catch (Exception e) {

            System.err.println("\nERROR:");
            e.printStackTrace();

        } finally {

            if (process != null) {
                process.destroy();
            }

            System.out.println("\n========================================");
            System.out.println("             FIN DE LA PRUEBA");
            System.out.println("========================================");
        }
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


    private static void readScreen(
            BufferedReader reader
    ) throws IOException {

        String line;

        while ((line = reader.readLine()) != null) {

            System.out.println("    << " + line);

            /*
             * La salida de Ascii() termina cuando ws3270
             * vuelve a enviar "ok".
             */
            if (line.equals("ok")) {
                break;
            }
        }
    }
}