package structs;

import entities.Mensagem;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ClientBuffer {
    private Queue<Mensagem> queue;
    private ReentrantLock lock;
    private Condition condition;

    public ClientBuffer(){
        this.queue = new ArrayDeque<>();
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
    }

    public void add(Mensagem mensagem){
        this.lock.lock();
        try{
            this.queue.add(mensagem);
            condition.signal(); // Acorda uma thread que esteja a espera de consumir
        }finally{
            this.lock.unlock();
        } 
    }

    public Mensagem poll(){
        this.lock.lock();
        try{
            while(queue.isEmpty()){
                condition.await(); // Bloqueia até haver uma mensagem para processar
            }
            return this.queue.poll();
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally{
            this.lock.unlock();
        }
    }
}
