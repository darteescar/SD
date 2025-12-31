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
     private final BoundedBuffer<ServerData> mensagensPendentes;
     private final int cliente;

     public ServerReader(ClientSession session,
                         BoundedBuffer<ServerData> mensagensPendentes,
                         int cliente, 
                         DataInputStream input) throws IOException {
          this.session = session;
          this.input = input;
          this.mensagensPendentes = mensagensPendentes;
          this.cliente = cliente;
     }

     @Override
     public void run() {
     while (true) {
          try {
               Mensagem mensagem = Mensagem.deserializeWithId(input);
               mensagensPendentes.add(new ServerData(cliente, mensagem));

          } catch (MensagemCorrompidaException e) {
               mensagensPendentes.add(new ServerData(cliente, new Mensagem(e.getId(), "Erro: Mensagem inválida ou corrompida.")));

          } catch (Exception e) {
               // Erro ao ler a mensagem - termina a thread
               break;
          }
     }
     session.close();
     }
}
