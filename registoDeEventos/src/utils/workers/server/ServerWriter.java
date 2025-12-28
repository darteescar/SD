package utils.workers.server;

import entities.Mensagem;
import enums.TipoMsg;
import utils.structs.notification.ConcurrentBuffer;
import utils.structs.server.ClientSession;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

public class ServerWriter implements Runnable {
    private final ClientSession session;
    private final DataOutputStream output;
    private final ConcurrentBuffer<Mensagem> taskBuffer;
    private final int cliente;

    public static final Mensagem POISON_PILL = new Mensagem(0, TipoMsg.POISON_PILL, null);

    public ServerWriter(ClientSession session,
                        ConcurrentBuffer<Mensagem> taskBuffer,
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
                    // System.out.println("SW: [POISON PILL RECEBIDO, TERMINANDO THREAD DO CLIENTE " + cliente + "]");
                    break;
                }

                try {
                    msg.serialize(output);
                    output.flush();
                } catch (EOFException e) {
                    // Cliente fechou o socket de forma limpa
                    // System.out.println("SW: [CLIENTE " + cliente + " FECHOU A CONEXÃO]");
                    break;
                } catch (SocketException e) {
                    // Conexão resetada ou fechada abruptamente
                    // System.out.println("SW: [CONEXÃO RESETADA COM CLIENTE " + cliente + "]");
                    break;
                } catch (IOException e) {
                    // Outro problema de IO
                    // System.out.println("SW: [ERRO DE IO AO ENVIAR MENSAGEM PARA CLIENTE " + cliente + "]: " + e.getMessage());
                    break;
                } catch (Exception e) {
                    // Qualquer outra exceção na serialização
                    // System.out.println("SW: [ERRO AO SERIALIZAR MENSAGEM PARA CLIENTE " + cliente + "]: " + e.getMessage());
                }
            }
        } finally {
            // Garante que o socket e a sessão sejam fechados
            session.close();
            //System.out.println("SW: [THREAD WRITER CLIENTE " + cliente + " TERMINOU]");
        }
    }


    public void send(Mensagem data) {
        if (data != null) {
            taskBuffer.add(data);
        }
    }

    public ConcurrentBuffer<Mensagem> getOutBuffer() {
        return taskBuffer;
    }
}
