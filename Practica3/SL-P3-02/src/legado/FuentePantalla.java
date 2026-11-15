package legado;

/**
 * De dónde sale la pantalla de la aplicación legada: normalmente, una captura
 * de la ventana de DOSBox leída con LectorPantalla.
 *
 * Está separado en una interfaz para poder probar las esperas (Espera) sin
 * DOSBox: en PruebasOffline se usan pantallas guardadas que cambian cuando
 * interesa.
 */
@FunctionalInterface
public interface FuentePantalla {

    /**
     * Las 25 líneas de 80 caracteres que hay ahora en pantalla.
     *
     * @throws LectorPantalla.CeldaDesconocida si la captura tiene un dibujo que no
     *                                         está en las plantillas (por ejemplo, si
     *                                         DOSBox estaba redibujando)
     */
    String[] capturar() throws Exception;
}
