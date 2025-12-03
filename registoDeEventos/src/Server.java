import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import entities.Mensagem;
import structs.ServerBuffer;
import structs.ThreadPool;
import structs.Par;


public class Server {
    private final ServerSocket ss;
    private final ServerBuffer buffer;
    private final ThreadPool threads;
    private static final int N_WORKERS = 10; // Depois pomos dinâmico

    public Server() throws IOException{
        this.ss = new ServerSocket(12345);
        this.buffer = new ServerBuffer();
        this.threads = new ThreadPool(N_WORKERS, buffer);
    }

    public void start() throws IOException{
       // Começar a ThreadPool
        this.threads.start();

        while(true){
            // Começar a aceitar conexões dos clientes
            Socket socket = this.ss.accept();
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            // Lê a Mensagem do cliente
            Mensagem msg = Mensagem.deserialize(dis);
            Par<Socket, Mensagem> par = new Par<>(socket, msg);
            this.buffer.add(par);
        }
    }

    public static void main(String[] args){
        try{
            Server server = new Server();
            server.start();
        }catch(Exception e){
            
        }
    }
}
