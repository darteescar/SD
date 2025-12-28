package utils.workers.server;

import entities.Mensagem;
import entities.ServerData;
import java.io.*;
import java.net.ProtocolException;
import utils.structs.notification.BoundedBuffer;
import utils.structs.server.ClientSession;

public class ServerReader implements Runnable {
     private final ClientSession session;
     private final DataInputStream input;
     private final BoundedBuffer<ServerData> taskBuffer;
     private final int cliente;

     public ServerReader(ClientSession session,
                         BoundedBuffer<ServerData> taskBuffer,
                         int cliente, 
                         DataInputStream input) throws IOException {
          this.session = session;
          this.input = input;
          this.taskBuffer = taskBuffer;
          this.cliente = cliente;
     }

     @Override
     public void run() {
          int id = 0;
          while (true) {
               try {
                    Mensagem mensagem = Mensagem.deserialize(input,id);
                    ServerData serverData = new ServerData(cliente, mensagem);
                    taskBuffer.add(serverData);
               } catch (ProtocolException e) {
                    Mensagem mensagem = new Mensagem(id,"Erro: Mensagem inválida ou corrompida.");
                    ServerData serverData = new ServerData(cliente, mensagem);
                    taskBuffer.add(serverData);
               } catch (Exception e) {
                    break;
               }
          }
          session.close();
     }
}
