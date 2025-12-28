package utils.workers.server;

import entities.Mensagem;
import entities.ServerData;
import java.io.*;
import java.net.ProtocolException;
import java.net.SocketException;
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
                    Mensagem mensagem = Mensagem.deserialize(input);
                    ServerData serverData = new ServerData(cliente, mensagem);
                    taskBuffer.add(serverData);
               } catch (ProtocolException e) {
                    // System.out.println("SR: [ERRO] Mensagem inválida do cliente " + cliente + " e mensagem numero " + a + ", ignorando.");
                    Mensagem mensagem = new Mensagem("Erro: Mensagem inválida ou corrompida.");
                    ServerData serverData = new ServerData(cliente, mensagem);
                    taskBuffer.add(serverData);
               } catch (EOFException e) {
                    // System.out.println("SR: [INFO] Cliente " + cliente + " fechou a conexão.");
                    break;
               } catch (SocketException e) {
                    //System.out.println("SR: [ERRO] Conexão com cliente " + cliente + " terminada.");
                    break;
               } catch (IOException e) {
                    // System.out.println("SR: [ERRO] Problema de IO com o cliente " + cliente);
                    break;
               }
          }
          session.close();
          //System.out.println("SR: [THREAD READER DO CLIENTE " + cliente + " TERMINADA]");
     }
}
