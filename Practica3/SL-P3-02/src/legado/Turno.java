package legado;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * El turno para usar la aplicación legada: solo hay una ventana de DOSBox y
 * Robot solo escribe en una, así que las operaciones van de una en una (D5).
 *
 * Es una cola por orden de llegada: un candado "justo" (ReentrantLock con
 * fairness), que da el turno a quien más tiempo lleva esperando; el synchronized
 * de Java no garantiza ese orden. Si una operación no consigue el turno en el
 * tiempo máximo, se lanza AplicacionOcupada y la web lo muestra como un aviso.
 */
public class Turno {

    private final ReentrantLock candado = new ReentrantLock(true);
    private final long maxEsperaMs;

    /** @param maxEsperaMs lo que una operación espera su turno como mucho */
    public Turno(long maxEsperaMs) {
        this.maxEsperaMs = maxEsperaMs;
    }

    /** Lo que se hace con el turno. */
    @FunctionalInterface
    public interface Operacion<T> {
        T hacer() throws Exception;
    }

    /**
     * Espera el turno, hace la operación y lo deja libre para la siguiente,
     * también si la operación falla.
     *
     * @throws AplicacionOcupada si el turno no llega en el tiempo máximo
     */
    public <T> T con(Operacion<T> operacion) throws Exception {
        // tryLock con tiempo respeta el orden de llegada del candado justo.
        if (!candado.tryLock(maxEsperaMs, TimeUnit.MILLISECONDS)) throw new AplicacionOcupada(maxEsperaMs);
        try {
            return operacion.hacer();
        } finally {
            candado.unlock();
        }
    }

    /** true si alguna operación tiene el turno ahora mismo. */
    public boolean enUso() {
        return candado.isLocked();
    }

    /** Operaciones que esperan su turno ahora mismo (para el registro). */
    public int enEspera() {
        return candado.getQueueLength();
    }
}
