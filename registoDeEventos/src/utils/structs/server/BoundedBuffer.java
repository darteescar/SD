package utils.structs.server;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** Buffer genérico sinalizador usado pelo Server */
public class BoundedBuffer<T> {

    /** Fila para armazenar os itens do buffer */
    private final Queue<T> queue;

    /** Lock para controlo de acesso ao buffer */
    private final ReentrantLock lock;

    /** Condition para controlo de acesso ao buffer */
    private final Condition condition;

    /** 
     * Construtor do BoundedBuffer. Inicializa a fila, o lock e a condition.
     * 
     * @return Uma nova instância de BoundedBuffer
     */
    public BoundedBuffer() {
        this.queue = new ArrayDeque<>();
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
    }

    /** 
     * Adiciona um item ao buffer e sinaliza qualquer thread à espera.
     * 
     * @param item O item a ser adicionado ao buffer
     */
    public void add(T item) {
        lock.lock();
        try {
            queue.add(item);
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove e retorna um item do buffer. Se o buffer estiver vazio, a thread aguarda até que um item esteja disponível.
     *
     * @return O item removido do buffer
     */
    public T poll() {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                condition.await();
            }
            return queue.poll();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            lock.unlock();
        }
    }
}
