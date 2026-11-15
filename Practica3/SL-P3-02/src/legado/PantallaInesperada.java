package legado;

/**
 * La aplicación legada no muestra lo que se esperaba: otra pantalla, una fila
 * con otro formato o una pantalla que no llega a tiempo.
 *
 * Se lanza en lugar de devolver un dato aproximado (R20): la web recibe el
 * resultado de la aplicación o un error que dice qué se esperaba y qué había.
 */
public class PantallaInesperada extends Exception {

    private static final long serialVersionUID = 1L;

    /** Lo que se esperaba (por ejemplo, "CONTIENE n ARCHIVOS en la fila 5"). */
    public final String esperado;
    /** Lo que había en su lugar (la fila leída, sin espacios al final). */
    public final String encontrado;

    /**
     * Error con lo que se esperaba y lo que había.
     *
     * @param esperado   lo que se esperaba ver
     * @param encontrado lo que había en su lugar
     */
    public PantallaInesperada(String esperado, String encontrado) {
        super("Se esperaba " + esperado + " y hay: \"" + encontrado + "\"");
        this.esperado = esperado;
        this.encontrado = encontrado;
    }
}
