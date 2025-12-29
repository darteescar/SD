package utils.workers.client;

import entities.Mensagem;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements AutoCloseable {

    class Entry {
        public Queue<String> queue;
        public Condition cond;

        public Entry(ReentrantLock lock) {
            this.queue = new ArrayDeque<>();
            this.cond = lock.newCondition();
        }
    }

    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;
    private final ReentrantLock lock;
    private final Map<Integer, Entry> mapEntries;
    private Exception ex;
    private boolean closed = false; // protegido pelo lock
    private Thread backgroundThread;

    public Demultiplexer(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
        this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
        this.lock = new ReentrantLock();
        this.mapEntries = new HashMap<>();
        this.ex = null;
    }

    public Entry getEntry(int id) {
        return mapEntries.computeIfAbsent(id, k -> new Entry(lock));
    }

    public void start() {
        backgroundThread = new Thread(() -> {
            try {
                while (true) {
                    lock.lock();
                    try {
                        if (closed) break; // sai do loop se fechado
                    } finally {
                        lock.unlock();
                    }

                    Mensagem m = Mensagem.deserialize(in);
                    int id = m.getID();
                    String result = new String(m.getData());

                    lock.lock();
                    try {
                        Entry entry = getEntry(id);
                        entry.queue.add(result);
                        entry.cond.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (Exception e) {
                // sinaliza exceção e acorda threads
                lock.lock();
                try {
                    this.ex = e;
                    for (Entry entry : mapEntries.values()) {
                        entry.cond.signalAll();
                    }
                } finally {
                    lock.unlock();
                }
            }
        }, "Demultiplexer-Thread");

        backgroundThread.start();
    }

    public void send(Mensagem mensagem) throws IOException {
        mensagem.serialize(out);
        out.flush();
    }

    public String receive(int id) throws InterruptedException {
        lock.lock();
        try {
            Entry entry = getEntry(id);
            while (entry.queue.isEmpty()) {
                if (closed || ex != null) return null; // termina se fechado ou exceção
                entry.cond.await();
                if (closed || ex != null) return null;
            }
            return entry.queue.poll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        lock.lock();
        try {
            closed = true;
            for (Entry entry : mapEntries.values()) {
                entry.cond.signalAll(); // acorda threads bloqueadas
            }
        } finally {
            lock.unlock();
        }

        in.close();
        out.close();
    }
}
