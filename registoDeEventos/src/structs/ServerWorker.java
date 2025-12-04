package structs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import entities.Mensagem;

public class ServerWorker implements Runnable {
    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;

    public ServerWorker(Socket socket) throws IOException{
        this.socket = socket;
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    @Override
    public void run() {

    }

    private void processEvent(Mensagem mensagem) {
        
    }
}
