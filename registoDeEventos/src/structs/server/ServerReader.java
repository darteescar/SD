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
          try {
               while (true) {
                    Mensagem mensagem = Mensagem.deserialize(input);

                    ServerData serverData = new ServerData(cliente, mensagem);
                    taskBuffer.add(serverData);
               }

          } catch (EOFException e) {
               // Cliente fechou o socket
               System.out.println("SR: [CLIENTE " + cliente + " FECHOU A LIGACAO]");

          } catch (ProtocolException e) {
               // Mensagem mal formada
               System.out.println("SR: [PROTOCOLO INVALIDO CLIENTE " + cliente + "] " + e.getMessage());

          } catch (IOException e) {
               // Problema de rede / crash / reset
               System.out.println("SR: [ERRO IO CLIENTE " + cliente + "] " + e.getMessage());

          } finally {
               System.out.println("SR: [THREAD READER CLIENTE " + cliente + " TERMINOU]");
               session.close();
          }
     }
}
