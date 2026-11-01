package legado;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ServicioLegado {

    private final VentanaDosBox dosbox;
    private final Teclado teclado;
    private final LectorPantalla lector;

    public ServicioLegado(File carpetaApp, File conf) throws Exception {
        this.dosbox = VentanaDosBox.lanzar(carpetaApp, conf, 10000);
        this.teclado = new Teclado(dosbox);
        this.lector = LectorPantalla.cargar();
    }

    /* Número total de registros */
    public synchronized int obtenerTotalRegistros() throws Exception {
        dosbox.traerAlFrente();

        teclado.escribir("4");  // Opción 4 del menú
        Thread.sleep(50);

        String[] lineas = capturar();
        teclado.enter();        // Pulsa Enter para volver al menú

        return ParserPantalla.extraerTotalRegistros(lineas);
    }

    /* Buscar programa por nombre */
    public synchronized List<Juego> buscarPorNombre(String nombre) throws Exception {
        dosbox.traerAlFrente();
        List<Juego> resultados = new ArrayList<>();

        teclado.escribir("7");        // Opción 7: BUSCAR DATOS
        teclado.responder("N");       // No sabemos el número de registro
        teclado.responder(nombre);
        Thread.sleep(100);

        while (true) {
            String[] lineas = capturar();
            String pantallaTexto = String.join("\n", lineas);

            if (pantallaTexto.contains("NO HAY NINGUN PROGRAMA")) {
                teclado.enter();        // Enter a "PULSA ENTER"
                teclado.responder("N");
                break; // Vuelve al MENÚ
            }

            Juego juego = ParserPantalla.parsearLineaBusqueda(lineas[4]);
            if (juego != null) {
                resultados.add(juego);
            }

            teclado.responder("N"); // Respondemos N para seguir leyendo todos los registros
            Thread.sleep(50);
        }

        return resultados;
    }

    /* Listar programas de una cinta */
    public synchronized List<Juego> listarPorCinta(String idCinta) throws Exception {
        dosbox.traerAlFrente();
        List<Juego> todosLosJuegos = new ArrayList<>();

        // Reordenar por antigüedad (Opción 3 -> 4)
        teclado.escribir("3");
        teclado.responder("4");
        Thread.sleep(100);

        // Listar desde el principio (Opción 6 -> Enter)
        teclado.escribir("6");
        teclado.enter();
        Thread.sleep(50);

        // Recorrer las 43 páginas del listado
        for (int i = 0; i < 43; i++) {
            String[] lineas = capturar();
            todosLosJuegos.addAll(ParserPantalla.parsearTablaListado(lineas));

            teclado.espacio(); // Pasar de página
            Thread.sleep(30);
        }

        return ParserPantalla.filtrarPorCinta(todosLosJuegos, idCinta);
    }

    private String[] capturar() throws Exception {
        BufferedImage img = dosbox.robot().createScreenCapture(dosbox.areaCliente());
        return lector.leer(img);
    }
}