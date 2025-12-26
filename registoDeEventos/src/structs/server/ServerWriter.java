package structs.server;

import entities.Mensagem;
import enums.TipoMsg;
import java.io.*;
import java.net.SocketException;
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
          try {
               while (true) {
                    Mensagem msg = taskBuffer.poll();
                    if (msg.getTipo() == TipoMsg.POISON_PILL) {
                         System.out.println("SW: [RECEBEU POISON PILL, A TERMINAR THREAD DO CLIENTE " + cliente + "]");
                         break;
                    }
                    msg.serialize(output);
                    output.flush();
               }

          } catch (SocketException | EOFException e) {
               System.out.println("SW: [CLIENTE " + cliente + " DESCONECTOU-SE]");

          } catch (IOException e) {
               System.out.println("SW: [ERRO IO CLIENTE " + cliente + "] " + e.getMessage());

          } finally {
               System.out.println("SW: [THREAD WRITER CLIENTE " + cliente + " TERMINOU]");
               session.close();
          }
     }

     public void send(Mensagem data) {
          taskBuffer.add(data);
     }

     public ConcurrentBuffer<Mensagem> getOutBuffer() {
          return taskBuffer;
     }
}
