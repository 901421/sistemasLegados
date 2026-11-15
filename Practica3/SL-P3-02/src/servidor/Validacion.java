package servidor;

import java.util.function.IntPredicate;

/**
 * Comprobación de lo que escribe el usuario de la web antes de enviarlo a la
 * aplicación legada (D3). Si algo no se puede enviar tal cual, se rechaza con un
 * mensaje que dice por qué, sin tocar DOSBox: así la aplicación nunca se queda a
 * medias por una tecla que no se puede pulsar.
 */
public final class Validacion {

    /** Longitud máxima del nombre: el campo de la aplicación, y el nombre más largo de los datos. */
    public static final int MAX_NOMBRE = 30;

    private Validacion() {
    }

    /**
     * El nombre de F2, sin los espacios del principio y del final y sin cambiar
     * mayúsculas ni minúsculas (la aplicación las distingue, R20).
     *
     * @param escribible si un carácter se puede escribir en la aplicación
     *                   (Teclado.puedeEscribir, a través de AplicacionLegada)
     * @throws EntradaNoValida si falta, pasa de 30 caracteres, lleva coma o
     *                         comillas (en un INPUT de BASIC separan y delimitan
     *                         valores) o algún carácter no se puede escribir
     */
    public static String nombre(String valor, IntPredicate escribible) throws EntradaNoValida {
        String nombre = valor == null ? "" : valor.strip();
        if (nombre.isEmpty()) throw new EntradaNoValida("Escribe el nombre del programa (o parte de él).");
        if (nombre.length() > MAX_NOMBRE) {
            throw new EntradaNoValida("El nombre puede tener como mucho " + MAX_NOMBRE + " caracteres (tiene "
                    + nombre.length() + ").");
        }
        for (char c : nombre.toCharArray()) {
            if (c == ',' || c == '"') {
                throw new EntradaNoValida("El nombre no puede llevar " + (c == ',' ? "comas" : "comillas")
                        + ": la aplicación legada no lo recibiría tal cual.");
            }
            if (!escribible.test(c)) {
                throw new EntradaNoValida("El carácter '" + c + "' no se puede escribir en la aplicación legada.");
            }
        }
        return nombre;
    }

    /**
     * El identificador de cinta de F3: una letra (A-Z) o un número (0-n), en
     * mayúsculas. Una cinta combinada como "A-45" no es un identificador: el
     * programa aparece al buscar A y al buscar 45.
     *
     * @throws EntradaNoValida si falta o no es una letra o un número
     */
    public static String cinta(String valor) throws EntradaNoValida {
        String id = valor == null ? "" : valor.strip().toUpperCase();
        if (id.isEmpty()) throw new EntradaNoValida("Escribe el identificador de la cinta (una letra o un número).");
        if (!id.matches("[A-Z]|[0-9]+")) {
            throw new EntradaNoValida("El identificador de cinta debe ser una letra (A-Z) o un número.");
        }
        return id;
    }
}
