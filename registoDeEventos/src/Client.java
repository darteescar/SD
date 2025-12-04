

import java.io.IOException;
import java.net.Socket;

import structs.buffers.ClientBuffer;
import structs.threads.ClientReader;
import structs.threads.ClientWriter;

public class Client {
     private final Socket socket;
     private final ClientBuffer bufferOut;  // para enviar ao servidor
     private final  ClientBuffer bufferIn;   // para guardar respostas

     public Client() throws IOException{
          this.socket = new Socket("localhost", 12345);
          this.bufferOut = new ClientBuffer();
          this.bufferIn = new ClientBuffer();
     }

     public void start(){
          // Lançar thread writer
          new Thread(new ClientWriter(this.socket, this.bufferOut)).start();

          // Lançar thread reader
          new Thread(new ClientReader(this.socket, this.bufferIn)).start();
          
          // Restante lógica do cliente
     }

     public static void main(String[] args){
          try {
               Client client = new Client();
               client.start();
          } catch(Exception e) {
               e.printStackTrace();
          }
     }
}