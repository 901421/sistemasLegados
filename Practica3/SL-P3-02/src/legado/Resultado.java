package legado;

/**
 * El resultado de una operación sobre la aplicación legada y la pantalla de la
 * aplicación de la que se ha sacado (D9): la web la muestra junto a los datos,
 * para que se vea qué ha leído el wrapper.
 *
 * @param datos    lo que se ha leído (el número de registros o la lista de programas)
 * @param pantalla las 25 líneas de 80 caracteres de esa pantalla: en F1, la de
 *                 información; en F2, la última de la búsqueda; en F3, la última
 *                 página del listado
 */
public record Resultado<T>(T datos, String[] pantalla) {
}
