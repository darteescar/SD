package structs.server;

import entities.Mensagem;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import structs.notification.ConcurrentBuffer;

public class ServerWriter implements Runnable {
     private final Socket socket;
     private final DataOutputStream output;
     private final ConcurrentBuffer<Mensagem> taskBuffer;
     private final int cliente;

     public ServerWriter(Socket socket, int cliente, ConcurrentBuffer<Mensagem> taskBuffer) {
          this.socket = socket;
          this.taskBuffer = taskBuffer;
          this.cliente = cliente;
          try {
               this.output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
          } catch (Exception e) {
               throw new RuntimeException("SW: [ERRO] Erro a inicializar o output stream", e);
          }
     }

     @Override
     public void run() {
          try {
               while (true) {
                    Mensagem data = taskBuffer.poll();
                    data.serialize(output);
                    output.flush();
               }
          } catch (Exception e) {
               System.out.println("SW: [CLIENTE " + cliente + " DESCONECTOU-SE]");
          } finally {
               try {
                    output.close();
                    socket.close();
               } catch (Exception e) {
                    System.out.println("SW: [ERRO] Fechando recursos para o cliente " + cliente);
               }
          }
     }

     public void send(Mensagem data) {
          taskBuffer.add(data);
     }

}
