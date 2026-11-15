package legado;

import java.util.List;

/**
 * Lo que el servidor web puede pedir a la aplicación legada: las tres
 * funcionalidades del guion y saber si un carácter se puede escribir. Cada
 * funcionalidad devuelve los datos y la pantalla de la que salen (Resultado).
 *
 * La implementación real es ServicioLegado (DOSBox, Robot y LectorPantalla). Está
 * separada en una interfaz para poder probar el servidor web sin DOSBox, con una
 * aplicación falsa (PruebasOffline).
 */
public interface AplicacionLegada {

    /** F1: número de registros (opción 4). */
    Resultado<Integer> obtenerTotalRegistros() throws Exception;

    /** F2: datos de los programas cuyo nombre contiene el texto (opción 7). */
    Resultado<List<Juego>> buscarPorNombre(String nombre) throws Exception;

    /** F3: programas de una cinta en el orden en que están grabados (opciones 3 y 6). */
    Resultado<List<Juego>> listarPorCinta(String idCinta) throws Exception;

    /**
     * La última captura de la pantalla que ha hecho el wrapper, para verla en
     * directo durante una consulta (null si todavía no hay ninguna). No espera
     * turno ni toca DOSBox: devuelve la que ya se hizo.
     */
    Captura ultimaCaptura();

    /** true si ahora mismo hay una operación usando la aplicación. */
    boolean operando();

    /** true si el carácter se puede escribir en la aplicación (con Robot, en el teclado de DOSBox). */
    boolean puedeEscribir(char c);
}
