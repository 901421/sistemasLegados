package legado;

/**
 * La aplicación legada está atendiendo otras consultas y esta no ha conseguido
 * su turno a tiempo (D5). La web lo muestra como un aviso, no como un error: basta
 * con volver a intentarlo.
 */
public class AplicacionOcupada extends Exception {

    private static final long serialVersionUID = 1L;

    /** @param esperaMs lo que se ha esperado el turno, en milisegundos */
    public AplicacionOcupada(long esperaMs) {
        super("La aplicación legada está atendiendo otras consultas (se ha esperado " + esperaMs / 1000
                + " s); inténtalo de nuevo en unos segundos.");
    }
}
