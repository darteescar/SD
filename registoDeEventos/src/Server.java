import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import structs.ServerWorker;

public class Server implements AutoCloseable{
    private final ServerSocket ss;
    // Cache
    // Base de Dados

    public Server() throws IOException{
        this.ss = new ServerSocket(12345);
    }

    public void start() throws IOException{
        while(true){
            // Começar a aceitar conexões dos clientes
            Socket socket = this.ss.accept();
            Thread worker  = new Thread(new ServerWorker(socket));
            worker.start();
        }
    }

    public void close() throws IOException{
        this.ss.close();
    }

    public static void main(String[] args){
        try (Server server = new Server();){
            server.start();
        }catch(Exception e){
            System.out.println("[ERRO SERVER] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
