package utils.workers.client;

import entities.Mensagem;
import enums.TipoMsg;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import utils.structs.client.NotificacaoListener;

/** Thread responsável por enviar 1 mensagem e esperar pela resposta */
public class Sender implements Runnable {

    /** Demultiplexer utilizado para enviar e receber mensagens */
    private final Demultiplexer demu;

    /** Mensagem a ser enviada */
    private final Mensagem mToSend;

    /** Lista do Stud onde serão guardadas as respostas recebidas */
    private final List<String> replies;

    /** Listener para notificações enviadas */
    private final NotificacaoListener listener;

    /** Lock para sincronizar o acesso à lista de respostas do Stud */
    private final ReentrantLock lock;

    /**
     * Construtor da classe Sender
     * 
     * @param demu Demultiplexer utilizado para enviar e receber mensagens
     * @param mToSend Mensagem a enviar
     * @param replies Lista onde serão guardadas as respostas recebidas
     * @param listener Listener para notificações enviadas
     * @param lock Lock para sincronizar o acesso à lista de respostas
     * @return Uma nova instância de Sender
     */
    public Sender(Demultiplexer demu, Mensagem mToSend, List<String> replies, NotificacaoListener listener, ReentrantLock lock){
        this.demu = demu;
        this.mToSend = mToSend;
        this.replies = replies;
        this.listener = listener;
        this.lock = lock;
    }

    /** 
     * Método executado pela thread para enviar a mensagem e esperar pela resposta
     */
    @Override
    public void run(){
        try {
            int id = mToSend.getID();
            TipoMsg tipo = mToSend.getTipo();

            demu.send(mToSend);
            String reply = demu.receive(id);

            if (reply == null) { // Demultiplexer fechado
                //System.out.println("Sender thread " + id + " terminou porque Demultiplexer foi fechado.");
                return;
            }

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
