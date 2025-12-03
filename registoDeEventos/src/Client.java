

import java.net.Socket;

import structs.buffers.ClientBuffer;
import structs.threads.ClientReader;
import structs.threads.ClientWriter;

public class Client {
     private ClientBuffer bufferOut;  // para enviar ao servidor
     private ClientBuffer bufferIn;   // para guardar respostas

     public static void main(String[] args) {
          ClientBuffer bufferOut = new ClientBuffer();
          ClientBuffer bufferIn = new ClientBuffer();

          try {
               int porta = Integer.parseInt(args[0]);
               Socket socket = new Socket("localhost", porta);

               // Lançar thread writer
               new Thread(new ClientWriter(socket, bufferOut)).start();

               // Lançar thread reader
               new Thread(new ClientReader(socket, bufferIn)).start();

          } catch(Exception e) {
               e.printStackTrace();
          }
     }

}