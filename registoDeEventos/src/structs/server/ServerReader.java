package structs.server;

import entities.Mensagem;
import entities.ServerData;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import structs.notification.ConcurrentBuffer;

public class ServerReader implements Runnable {
     private final Socket socket;
     private final DataInputStream input;
     private final ConcurrentBuffer<ServerData> taskBuffer;
     private final int cliente;

     public ServerReader(Socket socket, ConcurrentBuffer<ServerData> taskBuffer, int cliente) throws IOException {
          this.socket = socket;
          this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
          this.taskBuffer = taskBuffer;
          this.cliente = cliente;
     }
     
     @Override
     public void run() {
     try {
          while (true) {
               Mensagem mensagem = Mensagem.deserialize(input);

               if (mensagem == null) {
                    System.out.println("[CLIENTE " + cliente + " DESCONECTOU-SE]");
                    break; // Sai do loop e termina a thread
               }

               ServerData serverData = new ServerData(cliente, mensagem);
               taskBuffer.add(serverData);
          }
     } finally {
          try {
               input.close();
               socket.close();
          } catch (IOException e) {
               System.out.println("[ERRO] Fechando recursos do cliente " + cliente);
               e.printStackTrace();
          }
          System.out.println("[THREAD DO CLIENTE " + cliente + " TERMINOU]");
     }
     }



}
