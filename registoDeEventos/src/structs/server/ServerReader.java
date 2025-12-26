package structs.server;

import entities.Mensagem;
import entities.ServerData;
import java.io.*;
import java.net.ProtocolException;
import structs.notification.ConcurrentBuffer;

public class ServerReader implements Runnable {
     private final ClientSession session;
     private final DataInputStream input;
     private final ConcurrentBuffer<ServerData> taskBuffer;
     private final int cliente;

     public ServerReader(ClientSession session,
                         ConcurrentBuffer<ServerData> taskBuffer,
                         int cliente, 
                         DataInputStream input) throws IOException {
          this.session = session;
          this.input = input;
          this.taskBuffer = taskBuffer;
          this.cliente = cliente;
     }

     @Override
     public void run() {
          while (true) {
               try {
                    Mensagem mensagem = Mensagem.deserialize(input);
                    ServerData serverData = new ServerData(cliente, mensagem);
                    taskBuffer.add(serverData);
               } catch (ProtocolException e) {
                    System.out.println("[AVISO] Mensagem inválida do cliente " + cliente);
                    // opcional: enviar feedback de erro para o cliente
               } catch (IOException e) {
                    System.out.println("[ERRO] Problema de IO com o cliente " + cliente);
                    break; // sai do while e fecha sessão
               }
          }
          session.close();

     }
}
