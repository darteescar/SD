package scripts;

import entities.Mensagem;
import enums.TipoMsg;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import utils.structs.client.NotificacaoListener;
import utils.workers.client.Demultiplexer;

public class Sender2 implements Runnable {
    private final Demultiplexer demu;
    private final Mensagem mToSend;
    private final List<String> replies;
    private final NotificacaoListener listener;
    private final ReentrantLock lock;
    private final Map<TipoMsg, List<Long>> messageTimestamps;

    public Sender2(Demultiplexer demu, Mensagem mToSend, List<String> replies, NotificacaoListener listener, ReentrantLock lock, Map<TipoMsg, List<Long>> messageTimestamps){
        this.demu = demu;
        this.mToSend = mToSend;
        this.replies = replies;
        this.listener = listener;
        this.lock = lock;
        this.messageTimestamps = messageTimestamps;
    }

    @Override
    public void run(){
        try {
            int id = mToSend.getID();
            TipoMsg tipo = mToSend.getTipo();

            long sendTime = System.currentTimeMillis();
            // Registar timestamp de envio

            demu.send(mToSend);
            String reply = demu.receive(id);

            long receiveTime = System.currentTimeMillis();
            // Registar timestamp de receção

            if (reply == null) { // Demultiplexer fechado
                //System.out.println("Sender thread " + id + " terminou porque Demultiplexer foi fechado.");
                return;
            }

            if(tipo != TipoMsg.REGISTA_LOGIN && tipo != TipoMsg.LOGIN){
                lock.lock();
                try {
                    // Registar timestamp de receção
                    replies.add("Resposta da mensagem " + id + " -> " + reply);

                    List <Long> timestamps = messageTimestamps.get(tipo);
                    timestamps.add(receiveTime - sendTime);

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
