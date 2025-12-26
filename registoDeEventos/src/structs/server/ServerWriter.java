package structs.server;

import entities.Mensagem;
import enums.TipoMsg;
import java.io.*;
import structs.notification.ConcurrentBuffer;

public class ServerWriter implements Runnable {
     private final ClientSession session;
     private final DataOutputStream output;
     private final ConcurrentBuffer<Mensagem> taskBuffer;
     private final int cliente;
     public static final Mensagem POISON_PILL = new Mensagem(0, TipoMsg.POISON_PILL, null);

     public ServerWriter(ClientSession session,
                         ConcurrentBuffer<Mensagem> taskBuffer,
                         int cliente,
                         DataOutputStream output
                         ) throws IOException {
          this.session = session;
          this.taskBuffer = taskBuffer;
          this.cliente = cliente;
          this.output = output;
     }

     @Override
     public void run() {
          while (true) {
               Mensagem msg = taskBuffer.poll();

               if (msg.getTipo() == TipoMsg.POISON_PILL) {
                    break;
               }

               try {
                    msg.serialize(output);
                    output.flush();
               } catch (IOException e) {
                    System.out.println("[ERRO] Não foi possível enviar mensagem para o cliente " + cliente + ": " + e.getMessage());
                    break; // sai do loop e fecha a sessão
               }
          }
          session.close();
     }

     public void send(Mensagem data) {
          taskBuffer.add(data);
     }

     public ConcurrentBuffer<Mensagem> getOutBuffer() {
          return taskBuffer;
     }
}
