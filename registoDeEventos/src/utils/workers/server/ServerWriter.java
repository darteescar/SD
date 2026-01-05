package utils.workers.server;

import entities.Mensagem;
import enums.TipoMsg;
import java.io.DataOutputStream;
import utils.structs.notification.BoundedBuffer;
import utils.structs.server.ClientSession;

/** Thread responsável por enviar mensagens de resposta aos clientes através do socket */
public class ServerWriter implements Runnable {

    /** Sessão do cliente associada a esta thread */
    private final ClientSession session;

    /** Stream de saída para enviar dados ao cliente */
    private final DataOutputStream output;

    /** Buffer de mensagens de resposta ao cliente */
    private final BoundedBuffer<Mensagem> mensagensResposta;

    /** Identificador do cliente associado a esta thread */
    private final int cliente;

    /** Mensagem especial para indicar o término da thread */
    public static final Mensagem POISON_PILL = new Mensagem(0, TipoMsg.POISON_PILL, null);

    /** 
     * Construtor da classe ServerWriter
     * 
     * @param session Sessão do cliente
     * @param mensagensResposta Buffer de mensagens de resposta ao cliente
     * @param cliente Identificador do cliente
     * @param output Stream de saída para enviar dados ao cliente
     * @return Uma nova instância de ServerWriter
     */
    public ServerWriter(ClientSession session,
                        BoundedBuffer<Mensagem> mensagensResposta,
                        int cliente,
                        DataOutputStream output) {
        this.session = session;
        this.mensagensResposta = mensagensResposta;
        this.cliente = cliente;
        this.output = output;
    }

    /** 
     * Método run da thread que envia mensagens de resposta ao cliente através do socket. Consome mensagens do buffer de mensagens de resposta.
     */
    @Override
    public void run() {
        try {
            while (true) {
                Mensagem msg = mensagensResposta.poll();

                if (msg.getTipo() == TipoMsg.POISON_PILL) {
                    // Poison pill recebido - termina a thread
                    break;
                }

                try {
                    msg.serialize(output);
                    output.flush();
                } catch (Exception e) {
                    // Erro ao enviar a mensagem - termina a thread
                    break;
                }
            }
        } finally {
            // Garante que o socket e a sessão sejam fechados
            session.close();
        }
    }

    /** 
     * Adiciona uma mensagem ao buffer de mensagens de resposta do cliente. Usado pelos ServerWorkers e pelo ServerNotifier.
     * 
     * @param data Mensagem a ser enviada ao cliente
     */
    public void send(Mensagem data) {
        if (data != null) {
            mensagensResposta.add(data);
        }
    }

    /** 
     * Retorna o buffer de mensagens de resposta do cliente.
     * 
     * @return Buffer de mensagens de resposta do cliente
     */
    public BoundedBuffer<Mensagem> getOutBuffer() {
        return mensagensResposta;
    }
}
