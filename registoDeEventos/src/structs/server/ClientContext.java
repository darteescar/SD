package structs.server;

import entities.Mensagem;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class ClientContext {
     private final Socket socket;
     private final ReentrantLock lockSend = new ReentrantLock();
     private final ReentrantLock lockReceive = new ReentrantLock();
     private final DataOutputStream out;
     private final DataInputStream in;

     public ClientContext(Socket socket) {
          try {
               this.socket = socket;
               this.out = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
               this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
          } catch (IOException e) {
               throw new RuntimeException(e);
          }
     }

     public Socket getSocket() {
          return socket;
     }

     public void send(Mensagem mensagem) {
          lockSend.lock();
          try {
               mensagem.serialize(out);
               out.flush();
          }
          catch (IOException e) {
               System.err.println("Erro ao enviar mensagem "+ mensagem.toString() + " para o cliente.");
               close();
          }
          finally {
               lockSend.unlock();
          }
     }
     
     public Mensagem receive() {
          lockReceive.lock();
          try {
               return Mensagem.deserialize(in);
          } finally {
               lockReceive.unlock();
          }
     }

     public void close() {
          try {
               
               System.out.println("Fechando conexão com o cliente: " + socket.getRemoteSocketAddress());
               this.in.close();
               this.out.close();
               this.socket.close();

          } catch (IOException e) {
               System.err.println("Erro ao fechar conexão com o cliente.");
          }
     }
}
