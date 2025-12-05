package main;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import entities.Data;
import structs.GestorLogins;
import structs.GestorSeries;
import structs.ServerWorker;

public class Server implements AutoCloseable{
    private final ServerSocket ss;
    private final GestorLogins logins;
    private final GestorSeries eventos;
    private int cliente;
    private Data data;

    public Server(int d, int s) throws IOException{
        this.ss = new ServerSocket(12345);
        this.logins = new GestorLogins(s+1);
        this.eventos = new GestorSeries(d, s, data);
    }

    public void start() throws IOException{
        while(true){
            // Aceita a conexão de um cliente
            Socket socket = this.ss.accept();
            // Cada cliente tem um thread dedicada a processar e executar mensagens
            Thread worker  = new Thread(new ServerWorker(socket, logins, cliente++, data));
            worker.start();
        }
    }

    @Override
    public void close() throws IOException{
        this.ss.close();
    }

    public static void main(String[] args){
        if (args.length != 2){
            System.out.println("Uso: make server <D> <S>");
            return;
        }
        int d = Integer.parseInt(args[0]);
        int s = Integer.parseInt(args[1]);

        try (Server server = new Server(d,s);){
            server.start();

        }catch(Exception e){
            System.out.println("[ERRO SERVER] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
