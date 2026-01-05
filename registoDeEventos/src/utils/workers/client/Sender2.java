package utils.workers.client;

import entities.Mensagem;
import enums.TipoMsg;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import utils.structs.client.NotificacaoListener;

/** Thread responsável por enviar mensagens ao servidor e processar respostas, adicionando o timestamp ao mapa do Stud2. Usada apenas em testes */
public class Sender2 implements Runnable {

    /** Thread Demultiplexer */
    private final Demultiplexer demu;

    /** Mensagem a ser enviada */
    private final Mensagem mToSend;

    /** Lista para armazenar respostas recebidas */
    private final List<String> replies;

    /** Listener para notificações recebidas */
    private final NotificacaoListener listener;

    /** Lock para sincronização de acesso a recursos compartilhados (lista de respostas e mapa de timestamps) */
    private final ReentrantLock lock;

    /** Mapa para armazenar timestamps de mensagens enviadas */
    private final Map<TipoMsg, List<Long>> messageTimestamps;

    /** Construtor da classe Sender2
     * 
     * @param demu Thread Demultiplexer
     * @param mToSend Mensagem a ser enviada
     * @param replies Lista para armazenar respostas recebidas
     * @param listener Listener para notificações recebidas
     * @param lock Lock para sincronização de acesso a recursos compartilhados (lista de respostas e mapa de timestamps)
     * @param messageTimestamps Mapa para armazenar timestamps de mensagens enviadas
     * @return Uma nova instância de Sender2
     */
    public Sender2(Demultiplexer demu, Mensagem mToSend, List<String> replies, NotificacaoListener listener, ReentrantLock lock, Map<TipoMsg, List<Long>> messageTimestamps){
        this.demu = demu;
        this.mToSend = mToSend;
        this.replies = replies;
        this.listener = listener;
        this.lock = lock;
        this.messageTimestamps = messageTimestamps;
    }

    /** 
     * Executa a thread para enviar a mensagem e processar a resposta. Regista os timestamps de envio e receção da mensagem
     */
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
