package Conexao;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.HashMap;

import Mensagens.Mensagem;

public class Demultiplexer implements AutoCloseable {

    ReentrantLock l = new ReentrantLock();
    Map<Integer, Entry> entries = new HashMap<>();
    IOException ioe = null;

    private class Entry {
        Condition cond = l.newCondition();
        Queue<Mensagem> queue = new ArrayDeque<>();
    }

    private Entry getEntry(int tag) {
        Entry e = entries.get(tag);
        if (e == null) {
            e = new Entry();
            entries.put(tag, e);
        }
        return e;
    }

    private ServerClientConnection conn;

    public Demultiplexer(ServerClientConnection conn) {
        this.conn = conn;
        this.start();
    }

    public void start() {
        new Thread(() -> {
                try {
                    while(true){
                        ServerClientConnection.Frame frame = conn.receive();
                        l.lock();
                        try {
                            Entry e = getEntry(frame.tag);
                            e.queue.add(frame.data);
                            e.cond.signal();
                        } finally {
                            l.unlock();
                        }
                    }
                } catch (IOException e) {
                    l.lock();
                    try {
                        ioe = e;
                        for (Entry entry : entries.values()) {
                            entry.cond.signalAll();//signal()?
                        }
                    } finally {
                        l.unlock();
                    }
                }
        }).start();
    }

    public void send(ServerClientConnection.Frame frame) throws IOException {
        conn.sendFrame(frame);
    }

    public void send(Mensagem msg, int tag) throws IOException {
        conn.sendMsg(msg, tag);
    }

    public Mensagem receive(int tag) throws IOException, InterruptedException {
        l.lock();
        try {
            Entry e = getEntry(tag);

            while (e.queue.isEmpty() && ioe == null) {
                e.cond.await();
            }

            if (!e.queue.isEmpty()) {
                return e.queue.poll();
            } else {//Caso onde ioe não é null
                throw ioe;//o cliente que chamou receive lida com a IOException
            } 

        } finally {
            l.unlock();
        }
    }

    public void close() throws IOException {
        conn.close();
    }
}

