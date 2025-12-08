package structs;

import entities.Mensagem;
import enums.TipoMsg;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Sender implements Runnable{
    private final Demultiplexer demu;
    private final ReentrantLock lock;
    private final ClienteView view;
    private Mensagem mToSend;
    private List<String> replies;

    public Sender(Demultiplexer demu, Mensagem mToSend, List<String> replies, ClienteView view){
        this.demu = demu;
        this.lock =  new ReentrantLock();
        this.mToSend = mToSend;
        this.replies = replies;
        this.view = view;
    }

    @Override
    public void run(){
        try{
            int id = this.mToSend.getID();
            TipoMsg tipo = this.mToSend.getTipo();

            // Enviar a mensagem
            this.demu.send(this.mToSend);

            // Esperar pela resposta (especificamente com o ID passado)
            String reply = demu.receive(id);

            if(tipo != TipoMsg.REGISTA_LOGIN && tipo != TipoMsg.LOGIN){
                String paraLista = "Resposta da mensagem " + id + " -> " + reply;
                if (tipo == TipoMsg.NOTIFICACAO_VS) {
                    this.view.switchNotificacao1();
                } else if (tipo == TipoMsg.NOTIFICACAO_VC) {
                    this.view.switchNotificacao2();
                }
                this.lock.lock();
                try{
                    this.replies.add(paraLista);
                }finally{
                    this.lock.unlock();
                }
            }

        }catch(Exception e){
            System.out.println("[ERRO CLIENTE-WORKER] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
