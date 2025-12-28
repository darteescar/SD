package utils.workers.server;

import entities.Mensagem;
import enums.TipoMsg;
import java.io.DataOutputStream;
import utils.structs.notification.BoundedBuffer;
import utils.structs.server.ClientSession;

public class ServerWriter implements Runnable {
    private final ClientSession session;
    private final DataOutputStream output;
    private final BoundedBuffer<Mensagem> taskBuffer;
    private final int cliente;

    public static final Mensagem POISON_PILL = new Mensagem(0, TipoMsg.POISON_PILL, null);

    public ServerWriter(ClientSession session,
                        BoundedBuffer<Mensagem> taskBuffer,
                        int cliente,
                        DataOutputStream output) {
        this.session = session;
        this.taskBuffer = taskBuffer;
        this.cliente = cliente;
        this.output = output;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Mensagem msg = taskBuffer.poll();

                if (msg.getTipo() == TipoMsg.POISON_PILL) {
                    // Poison pill recebido - termina a thread
                    break;
                }

                try {
                    String message = msg.toString();
                    msg.serialize(output);
                    output.flush();
                } catch (Exception e) {
                    // Erro ao enviar a mensagem - termina a thread
                    break;
                }
            }
        } finally {
            // Garante que o socket e a sessão sejam fechados
            //session.close();
        }
    }


    public void send(Mensagem data) {
        if (data != null) {
            taskBuffer.add(data);
        }
    }

    public BoundedBuffer<Mensagem> getOutBuffer() {
        return taskBuffer;
    }
}
