package legado;

/**
 * Un programa de la base de datos tal como lo muestra la aplicación legada:
 * número, nombre, tipo, cinta y registro.
 *
 * La búsqueda por nombre (opción 7) no muestra el registro, así que ahí queda
 * vacío; en el listado (opción 6) sí está. Los textos se guardan tal cual salen
 * de la pantalla, sin los espacios de relleno.
 *
 * La conversión a JSON la hace el servidor web (servidor.Json): el wrapper no
 * sabe nada de HTTP.
 */
public class Juego {
    private String numero;
    private String nombre;
    private String tipo;
    private String cinta;
    private String registro;

    /** Un programa con sus cinco campos; null cuenta como vacío y se quitan los espacios de los extremos. */
    public Juego(String numero, String nombre, String tipo, String cinta, String registro) {
        this.numero = numero != null ? numero.trim() : "";
        this.nombre = nombre != null ? nombre.trim() : "";
        this.tipo = tipo != null ? tipo.trim() : "";
        this.cinta = cinta != null ? cinta.trim() : "";
        this.registro = registro != null ? registro.trim() : "";
    }

    /** El Nº que muestra la aplicación (con el orden ANTIGUEDAD, igual al registro). */
    public String getNumero() { return numero; }
    /** El nombre del programa, hasta 30 caracteres. */
    public String getNombre() { return nombre; }
    /** El tipo (ARCADE, VIDEOAVENTURA…). */
    public String getTipo() { return tipo; }
    /** La cinta o cintas, como "A" o "A-45". */
    public String getCinta() { return cinta; }
    /** El registro, o vacío si la pantalla no lo muestra (la búsqueda). */
    public String getRegistro() { return registro; }

    /** Para los mensajes de error y las pruebas: "Nº 50: DEFENDER | ARCADE | cinta C". */
    @Override
    public String toString() {
        return "Nº " + numero + ": " + nombre + " | " + tipo + " | cinta " + cinta
                + (registro.isEmpty() ? "" : " | registro " + registro);
    }
}
