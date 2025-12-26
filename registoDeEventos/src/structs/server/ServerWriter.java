package structs.server;

import entities.Mensagem;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import structs.notification.ConcurrentBuffer;

public class ServerWriter implements Runnable {
     private final Socket socket;
     private final DataOutputStream output;
     private final ConcurrentBuffer<Mensagem> taskBuffer;
     private final int cliente;

     public ServerWriter(Socket socket,
                         int cliente,
                         ConcurrentBuffer<Mensagem> taskBuffer) throws IOException {

          this.socket = socket;
          this.taskBuffer = taskBuffer;
          this.cliente = cliente;
          this.output = new DataOutputStream(
                    new BufferedOutputStream(socket.getOutputStream()));
     }

     @Override
     public void run() {
          try {
               while (true) {
                    Mensagem msg = taskBuffer.poll();
                    msg.serialize(output);
                    output.flush();
               }

          } catch (SocketException | EOFException e) {
               System.out.println("SW: [CLIENTE " + cliente + " DESCONECTOU-SE]");

          } catch (IOException e) {
               System.out.println("SW: [ERRO IO CLIENTE " + cliente + "] " + e.getMessage());

          } finally {
               close();
          }
     }

     private void close() {
          try {
               output.close();
               socket.close();
          } catch (IOException e) {
               System.out.println("SW: [ERRO AO FECHAR CLIENTE " + cliente + "]");
          }
          System.out.println("SW: [THREAD WRITER CLIENTE " + cliente + " TERMINOU]");
     }

     public void send(Mensagem data) {
          taskBuffer.add(data);
     }

     public ConcurrentBuffer<Mensagem> getOutBuffer() {
          return taskBuffer;
     }
}
