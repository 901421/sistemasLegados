package legado;

/**
 * Una captura de la pantalla de la aplicación legada ya leída: sus 25 líneas y
 * cuándo se hizo. El wrapper guarda la última que hace al esperar cada pantalla,
 * y la web la pide mientras dura una consulta para mostrar en directo lo que hace
 * la aplicación (D9). No es un dato de la base de datos: es la imagen de la
 * pantalla en ese momento.
 *
 * @param lineas  las 25 líneas de 80 caracteres
 * @param instante cuándo se hizo, en milisegundos desde 1970 (System.currentTimeMillis)
 */
public record Captura(String[] lineas, long instante) {
}
