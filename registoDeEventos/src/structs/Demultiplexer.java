package structs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import entities.Mensagem;

public class Demultiplexer implements AutoCloseable{
    class Entry{
        public Queue<String> queue;
        public Condition cond;

        public Entry(ReentrantLock lock){
            this.queue = new ArrayDeque<>();
            this.cond = lock.newCondition();
        }
    }

    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;
    private final ReentrantLock lock;
    private Map<Integer, Entry> mapEntries;
    private Exception ex; // Isto vai servir para acordar todas as threads caso ocorra alguma Exception

    public Demultiplexer(Socket socket) throws IOException{
        this.socket = socket;
        this.out = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
        this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
        this.lock = new ReentrantLock();
        this.mapEntries = new HashMap<>();
        this.ex = null;
    }

    public Entry getEntry(int id){
        if(!this.mapEntries.containsKey(id)){
            this.mapEntries.put(id, new Entry(this.lock));
        }
        return this.mapEntries.get(id);
    }

    // Começar Thread de background que vai orientar as replies para a thread correta
    public void start(){
        Thread background = new Thread(() -> {
            try{
                while(true){
                    Mensagem m = Mensagem.deserialize(in);
                    int id = m.getID();
                    String result = new String(m.getData());

                    this.lock.lock();
                    try{
                        Entry entry = this.getEntry(id);
                        entry.queue.add(result);
                        entry.cond.signalAll();

                    }finally{
                        this.lock.unlock();
                    }
                }
            }catch(Exception e){    
                // Tratamos do caso da Exception, acordando todas as Threads e nada fica bloqueado
                this.lock.lock();
                try{
                    this.ex = e;
                    for(Entry entry : this.mapEntries.values()){
                        entry.cond.signalAll();
                    }
                }finally{
                    this.lock.unlock();
                }
            }
        });
        background.start();
    }        

    public void send(Mensagem mensagem) throws IOException{
        mensagem.serialize(out);
        out.flush();
    }

    public String receive(int id) throws InterruptedException{
        this.lock.lock();
        try{
            Entry entry = this.getEntry(id);
            while(entry.queue.isEmpty()){
                entry.cond.await();
                if(this.ex != null){
                    return null;
                }
            }
            String reply = entry.queue.poll();
            return reply;
        }finally{
            this.lock.unlock();
        }
    }

    public void close() throws IOException{
        this.socket.close();
    }
}
