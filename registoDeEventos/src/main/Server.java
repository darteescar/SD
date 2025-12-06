package main;
import entities.Data;
import entities.Serie;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import structs.GestorLogins;
import structs.GestorSeries;
import structs.ServerWorker;

public class Server implements AutoCloseable{
    private final ServerSocket ss;
    private final GestorLogins logins;
    private final GestorSeries series;
    private int cliente;
    private final Data data;
    private final int d;

    public Server(int d, int s) throws IOException{
        this.ss = new ServerSocket(12345);
        this.logins = new GestorLogins(s+1);
        this.data = new Data(01, 01, 2025);
        Serie serie_inicial = new Serie(data.getData());
        this.series = new GestorSeries(s, data, serie_inicial);
        this.cliente = 0;
        this.d = d;
    }

    public void start() throws IOException{
        while(true){
            // Aceita a conexão de um cliente
            Socket socket = this.ss.accept();
            // Cada cliente tem um thread dedicada a processar e executar mensagens
            Thread worker  = new Thread(new ServerWorker(socket, logins, cliente++, data, series, d));
            worker.start();
        }
    }

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // Este método ainda não está a ser usado
    public void passarDia(){
        this.series.passarDia();
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

        if ( s >= d ) {
            System.out.println("Erro: S deve ser menor que D.");
            return;
        }

        try (Server server = new Server(d,s);){
            server.start();

        }catch(Exception e){
            System.out.println("[ERRO SERVER] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
