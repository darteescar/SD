package client;

import entities.Mensagem;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import structs.ClientBuffer;

public class ClientWriter implements Runnable {
     private final Socket socket;
     private final ClientBuffer buffer;

     public ClientWriter(Socket socket, ClientBuffer buffer){
          this.socket = socket;
          this.buffer = buffer;
     }

     @Override
     public void run() {
          try {
               DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
               while (true) { 
                    Mensagem msg = this.buffer.poll();
                    msg.serialize(dos);
                    dos.flush();
               }
          } catch (IOException e) {
               e.printStackTrace();
          }
     }
}