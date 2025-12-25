package structs.server;

import entities.Mensagem;
import entities.ServerData;
import java.io.*;
import java.net.ProtocolException;
import java.net.Socket;
import structs.notification.ConcurrentBuffer;

public class ServerReader implements Runnable {
     private final Socket socket;
     private final DataInputStream input;
     private final ConcurrentBuffer<ServerData> taskBuffer;
     private final int cliente;

     public ServerReader(Socket socket,
                         ConcurrentBuffer<ServerData> taskBuffer,
                         int cliente) throws IOException {

          this.socket = socket;
          this.input = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));
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
               // Cliente fechou o socket (fecho limpo ou durante escrita)
               System.out.println("SR: [CLIENTE " + cliente + " FECHOU A LIGACAO]");

          } catch (ProtocolException e) {
               // Mensagem mal formada
               System.out.println("SR: [PROTOCOLO INVALIDO CLIENTE " + cliente + "] " + e.getMessage());

          } catch (IOException e) {
               // Problema de rede / crash / reset
               System.out.println("SR: [ERRO IO CLIENTE " + cliente + "] " + e.getMessage());

          } finally {
               close();
          }
     }

     private void close() {
          try {
               input.close();
               socket.close();
          } catch (IOException e) {
               System.out.println("SR: [ERRO AO FECHAR CLIENTE " + cliente + "]");
          }
          System.out.println("SR: [THREAD DO CLIENTE " + cliente + " TERMINOU]");
     }
}
