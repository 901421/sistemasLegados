package legado;

public class Juego {
    private String numero;
    private String nombre;
    private String tipo;
    private String cinta;
    private String registro;

    public Juego(String numero, String nombre, String tipo, String cinta, String registro) {
        this.numero = numero != null ? numero.trim() : "";
        this.nombre = nombre != null ? nombre.trim() : "";
        this.tipo = tipo != null ? tipo.trim() : "";
        this.cinta = cinta != null ? cinta.trim() : "";
        this.registro = registro != null ? registro.trim() : "";
    }

    public String getNumero() { return numero; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getCinta() { return cinta; }
    public String getRegistro() { return registro; }

    /** Convierte el objeto a formato JSON manual para el servidor nativo de Java */
    public String toJson() {
        return String.format(
                "{\"numero\":\"%s\",\"nombre\":\"%s\",\"tipo\":\"%s\",\"cinta\":\"%s\",\"registro\":\"%s\"}",
                escapeJson(numero), escapeJson(nombre), escapeJson(tipo), escapeJson(cinta), escapeJson(registro)
        );
    }

    private String escapeJson(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}