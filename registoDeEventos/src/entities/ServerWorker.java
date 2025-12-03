package entities;

import java.net.Socket;

import structs.Par;
import structs.ServerBuffer;

public class ServerWorker implements Runnable {
    private int id;
    private ServerBuffer buffer;

    public ServerWorker(int id, ServerBuffer buffer) {
        this.id = id;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Bloqueia à espera que a queue tenha uma mensagem para processar
                Par<Socket, Mensagem> par = buffer.poll();
                
                // Processamento da mensagem
                processEvent(par.snd());
            }
        } catch (InterruptedException e) {
            System.out.println("Worker " + id + " interrompido.");
            e.printStackTrace();
        }
    }

    private void processEvent(Mensagem mensagem) {
        
    }
}
