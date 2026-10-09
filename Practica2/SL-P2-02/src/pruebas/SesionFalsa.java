package pruebas;

import mainframe.SesionMusic;

/** Sesión sin mainframe, para probar la interfaz: cerrar() solo tarda medio segundo. */
public class SesionFalsa extends SesionMusic {

    public SesionFalsa() {
        super(null);
    }

    @Override
    public void cerrar() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
