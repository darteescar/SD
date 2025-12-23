package main;
import data.BDServerDay;
import entities.Data;
import entities.Serie;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Scanner;
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

    public Server(int d, int s) throws IOException {
        this.ss = new ServerSocket(12345);
        this.logins = new GestorLogins(s + 1);

        LocalDate ultimaData = BDServerDay.getCurrentDate();
        LocalDate dataArranque = ultimaData.plusDays(1);

        BDServerDay.setCurrentDate(dataArranque);

        Data dataAtual = new Data(
                dataArranque.getDayOfMonth(),
                dataArranque.getMonthValue(),
                dataArranque.getYear()
        );

        String data = dataAtual.toString();
        System.out.println("Servidor arrancado na data: " + data);

        Serie serie_inicial = new Serie(dataAtual.getData());
        this.series = new GestorSeries(s, dataAtual, serie_inicial);

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

    public void passarDia() {
        this.series.passarDia();

        Data d = this.series.getDataAtual();
        BDServerDay.setCurrentDate(
            LocalDate.of(d.getAno(), d.getMes(), d.getDia())
        );

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
        
        // Guarda a série atual na BD antes de fechar
        Serie serieAtual = this.series.getSerieAtual();
        if (serieAtual != null) {
            this.series.add(serieAtual);
    }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: make server <D> <S> <RESET>");
            return;
        }

        int d = Integer.parseInt(args[0]);
        int s = Integer.parseInt(args[1]);

        boolean reset = args.length == 3 && args[2].equals("1");

        if (s >= d) {
            System.out.println("Erro: S deve ser menor que D.");
            return;
        }

        if (reset) {
            System.out.println("RESET DA BASE DE DADOS!!!");
            System.out.println("Tem a certeza que quer apagar toda a base de dados? (sim/nao)");

            Scanner scanner = new Scanner(System.in);
            String resposta = scanner.nextLine().trim().toLowerCase();

            if (resposta.equals("sim") || resposta.equals("s")) {
                data.BDReset.resetAll();
                System.out.println("Base de dados apagada.");
            } else {
                System.out.println("Reset cancelado.");
            }
        }

        try (Server server = new Server(d, s)) {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
