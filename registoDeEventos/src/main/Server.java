package main;
import data.BDServerDay;
import entities.Data;
import entities.Mensagem;
import entities.Serie;
import entities.ServerData;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Scanner;
import structs.notification.ConcurrentBuffer;
import structs.notification.ServerNotifier;
import structs.server.GestorLogins;
import structs.server.GestorSeries;
import structs.server.SafeMap;
import structs.server.ServerReader;
import structs.server.ServerSimulator;
import structs.server.ServerWriter;
import structs.server.ThreadPool;

public class Server implements AutoCloseable{
    private final ServerSocket ss;
    private final GestorLogins logins;
    private final GestorSeries series;
    private int cliente;
    private final int d;
    private final ServerSimulator simulator;
    private final ServerNotifier notifier;

    private final ThreadPool threadPool;
    private final SafeMap<Integer, ServerReader> readers;
    private final SafeMap<Integer, ServerWriter> writers;
    private final SafeMap<Integer, ConcurrentBuffer<Mensagem>> clientBuffers;
    private final ConcurrentBuffer<ServerData> taskBuffer;

    public Server(int d, int s) throws IOException {
        this.ss = new ServerSocket(12345);
        this.logins = new GestorLogins(s + 1);
        this.cliente = 0;
        this.d = d;

        Data dataAtual = carregarDataAtual();
        Serie serie_inicial = new Serie(dataAtual.getData());
        this.series = new GestorSeries(s, dataAtual, serie_inicial);

        this.readers = new SafeMap<>();
        this.writers = new SafeMap<>();
        this.clientBuffers = new SafeMap<>();

        this.simulator = new ServerSimulator(this);
        this.notifier = new ServerNotifier(this.clientBuffers);

        this.taskBuffer = new ConcurrentBuffer<>();
        this.threadPool = new ThreadPool(8, this.taskBuffer, d, this.logins, this.series, this.notifier, this.clientBuffers);
    }

    private Data carregarDataAtual() {
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
        return dataAtual;
    }

    public void start() throws IOException{

        Thread simulator = new Thread(this.simulator); // Inicia a thread que simula a passagem dos dias
        simulator.start();

        Thread notifierThread = new Thread(this.notifier); // Inicia a thread que gere as notificações
        notifierThread.start();

        while(true){
            // Aceita a conexão de um cliente
            Socket socket = this.ss.accept();
            System.out.println("[NOVO CLIENTE " + this.cliente + " LIGADO]");
            ConcurrentBuffer<Mensagem> bufferCliente = new ConcurrentBuffer<>();
            this.clientBuffers.put(this.cliente, bufferCliente);
            ServerReader reader = new ServerReader(socket, this.taskBuffer, this.cliente);
            ServerWriter writer = new ServerWriter(socket, this.cliente, bufferCliente);
            Thread readerThread = new Thread(reader);
            Thread writerThread = new Thread(writer);
            writers.put(this.cliente, writer);
            readers.put(this.cliente, reader);
            this.cliente++;
            readerThread.start();
            writerThread.start();
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
