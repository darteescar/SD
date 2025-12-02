package structs;

import entities.ServerWorker;

public class ThreadPool {
    private Thread[] threads;
    private ServerBuffer buffer;

    public ThreadPool(int size, ServerBuffer buffer) {
        this.threads = new Thread[size];
        this.buffer = buffer;

        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(new ServerWorker(i, this.buffer));
        }
    }

    public void start() {
        for (Thread t : threads) {
            t.start();
        }
    }
}
