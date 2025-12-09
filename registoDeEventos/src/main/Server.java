package main;
import entities.Data;
import entities.Serie;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import structs.notification.ServerNotifier;
import structs.server.ClientContext;
import structs.server.GestorLogins;
import structs.server.GestorSeries;
import structs.server.ServerSimulator;
import structs.server.ServerWorker;

public class Server implements AutoCloseable{
    private final ServerSocket ss;
    private final GestorLogins logins;
    private final GestorSeries series;
    private int cliente;
    private final int d;
    private final ServerSimulator simulator;
    private final ServerNotifier notifier;

    public Server(int d, int s) throws IOException{
        this.ss = new ServerSocket(12345);
        this.logins = new GestorLogins(s+1);
        Data data_inicial = new Data(01, 01, 2025);
        Serie serie_inicial = new Serie(data_inicial.getData());
        this.series = new GestorSeries(s, data_inicial, serie_inicial);
        this.cliente = 0;
        this.d = d;
        this.simulator = new ServerSimulator(this);
        this.notifier = new ServerNotifier();
    }

    public void start() throws IOException{

        Thread simulator = new Thread(this.simulator); // Inicia a thread que simula a passagem dos dias
        simulator.start();

        Thread notifierThread = new Thread(this.notifier); // Inicia a thread que gere as notificações
        notifierThread.start();

        while(true){
            // Aceita a conexão de um cliente
            Socket socket = this.ss.accept();
            // Cria o contexto do cliente
            ClientContext context = new ClientContext(socket);
            // Cada cliente tem um thread dedicada a processar e executar mensagens
            Thread worker  = new Thread(new ServerWorker(context,logins, cliente++, series, d, notifier));
            worker.start();
        }
    }

    public void passarDia(){
        this.series.passarDia();
        this.notifier.clear();
    }

    public void printGS(){
        this.series.print();
    }

    @Override
    public void close() throws IOException{
        this.ss.close();
        //this.logins.close();
        //this.series.close();
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
