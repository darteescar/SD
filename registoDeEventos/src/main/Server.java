package main;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import structs.GestorLogins;
import structs.ServerWorker;

public class Server implements AutoCloseable{
    private final ServerSocket ss;
    private final GestorLogins logins;

    public Server() throws IOException{
        this.ss = new ServerSocket(12345);
        this.logins = new GestorLogins(10);
    }

    public void start() throws IOException{
        while(true){
            // Aceita a conexão de um cliente
            Socket socket = this.ss.accept();
            // Cada cliente tem um thread dedicada a processar e executar mensagens
            Thread worker  = new Thread(new ServerWorker(socket, logins));
            worker.start();
        }
    }

    @Override
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
