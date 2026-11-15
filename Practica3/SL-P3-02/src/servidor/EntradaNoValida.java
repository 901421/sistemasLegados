package servidor;

/**
 * La petición de la web no se puede atender tal como viene: falta un dato, la
 * dirección está mal codificada o el texto no se puede enviar a la aplicación
 * legada (por ejemplo, un nombre con coma o con una ñ). Se responde con el
 * código 400 y el mensaje, sin tocar la aplicación (D3).
 */
public class EntradaNoValida extends Exception {

    private static final long serialVersionUID = 1L;

    /** @param mensaje explicación para el usuario de la web */
    public EntradaNoValida(String mensaje) {
        super(mensaje);
    }
}
