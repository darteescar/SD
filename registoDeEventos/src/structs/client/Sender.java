package structs.client;

import entities.Mensagem;
import enums.TipoMsg;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Sender implements Runnable {
    private final Demultiplexer demu;
    private final Mensagem mToSend;
    private final List<String> replies;
    private final NotificacaoListener listener;
    private final ReentrantLock lock = new ReentrantLock();

    public Sender(Demultiplexer demu, Mensagem mToSend, List<String> replies, NotificacaoListener listener){
        this.demu = demu;
        this.mToSend = mToSend;
        this.replies = replies;
        this.listener = listener;
    }

    @Override
    public void run(){
        try {
            int id = mToSend.getID();
            TipoMsg tipo = mToSend.getTipo();

            demu.send(mToSend);
            String reply = demu.receive(id);

            if(tipo != TipoMsg.REGISTA_LOGIN && tipo != TipoMsg.LOGIN){
                lock.lock();
                try {
                    replies.add("Resposta da mensagem " + id + " -> " + reply);
                } finally {
                    lock.unlock();
                }

                if(listener != null){
                    if(tipo == TipoMsg.NOTIFICACAO_VS) listener.notificacaoVSEnviada();
                    else if(tipo == TipoMsg.NOTIFICACAO_VC) listener.notificacaoVCEnviada();
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
