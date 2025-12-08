package structs.notification;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentBuffer<T> {

    private final Queue<T> queue;
    private final ReentrantLock lock;
    private final Condition condition;

    public ConcurrentBuffer() {
        this.queue = new ArrayDeque<>();
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
    }

    public void add(T item) {
        lock.lock();
        try {
            queue.add(item);
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

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
