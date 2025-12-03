package structs;

import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import entities.Mensagem;

public class ServerBuffer {
    private Queue<Par<Socket, Mensagem>> queue;
    private ReentrantLock lock;
    private Condition condition;

    public ServerBuffer(){
        this.queue = new ArrayDeque<>();
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
    }

    public void add(Par<Socket, Mensagem> par){
        this.lock.lock();
        try{
            this.queue.add(par);
            condition.signal(); // Acorda uma thread que esteja a espera de consumir
        }finally{
            this.lock.unlock();
        } 
    }

    public Par<Socket, Mensagem> poll() throws InterruptedException{
        this.lock.lock();
        try{
            while(queue.isEmpty()){
                condition.await(); // Bloqueia até haver uma mensagem para processar
            }
            return this.queue.poll();
        }finally{
            this.lock.unlock();
        }
    }
}
