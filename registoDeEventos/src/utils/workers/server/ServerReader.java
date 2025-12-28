package utils.workers.server;

import entities.Mensagem;
import entities.MensagemCorrompidaException;
import entities.ServerData;
import java.io.*;
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
     while (true) {
          try {
               Mensagem mensagem = Mensagem.deserializeWithId(input);
               taskBuffer.add(new ServerData(cliente, mensagem));

          } catch (MensagemCorrompidaException e) {
               taskBuffer.add(new ServerData(cliente, new Mensagem(e.getId(), "Erro: Mensagem inválida ou corrompida.")));

          } catch (Exception e) {
               break;
          }
     }

     session.close();
     }

}
