package structs.threads;

import entities.Mensagem;
import structs.buffers.ClientBuffer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientReader implements Runnable  {
    private final Socket socket;
    private final ClientBuffer buffer;

    public ClientReader(Socket socket, ClientBuffer buffer){
        this.socket = socket;
        this.buffer = buffer;
    }
    @Override
    public void run() {
        try {
               DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
               while (true) {
                    Mensagem resposta = Mensagem.deserialize(in);
                    buffer.add(resposta);
               }
        } catch (IOException e) {
               e.printStackTrace();
          }
    }
}
