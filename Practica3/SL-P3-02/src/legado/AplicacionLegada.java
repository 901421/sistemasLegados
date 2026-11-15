package legado;

import java.util.List;

/**
 * Lo que el servidor web puede pedir a la aplicación legada: las tres
 * funcionalidades del guion y saber si un carácter se puede escribir.
 *
 * La implementación real es ServicioLegado (DOSBox, Robot y LectorPantalla). Está
 * separada en una interfaz para poder probar el servidor web sin DOSBox, con una
 * aplicación falsa (PruebasOffline).
 */
public interface AplicacionLegada {

    /** F1: número de registros (opción 4). */
    int obtenerTotalRegistros() throws Exception;

    /** F2: datos de los programas cuyo nombre contiene el texto (opción 7). */
    List<Juego> buscarPorNombre(String nombre) throws Exception;

    /** F3: programas de una cinta en el orden en que están grabados (opciones 3 y 6). */
    List<Juego> listarPorCinta(String idCinta) throws Exception;

    /** true si el carácter se puede escribir en la aplicación (con Robot, en el teclado de DOSBox). */
    boolean puedeEscribir(char c);
}
