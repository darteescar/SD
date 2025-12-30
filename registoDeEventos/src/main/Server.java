package main;

import databases.BDServerDay;
import entities.Data;
import entities.Mensagem;
import entities.Serie;
import entities.ServerData;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import utils.structs.notification.BoundedBuffer;
import utils.structs.server.ClientSession;
import utils.structs.server.GestorLogins;
import utils.structs.server.GestorNotificacoes;
import utils.structs.server.GestorSeries;
import utils.workers.server.ServerNotifier;
import utils.workers.server.ServerReader;
import utils.workers.server.ServerSimulator;
import utils.workers.server.ServerWorker;
import utils.workers.server.ServerWriter;

public class Server implements AutoCloseable{
    private final ServerSocket ss;
    private final GestorLogins logins;
    private final GestorSeries series;
    private int cliente;
    private final int d;
    private final ServerSimulator simulator;
    private final ServerNotifier notifier;
    private final GestorNotificacoes gestornotificacoes;

    private final ServerWorker[] workers;
    private final Map<Integer, ServerReader> readers;
    private final Map<Integer, ServerWriter> writers;
    private final Map<Integer, BoundedBuffer<Mensagem>> clientBuffers;
    private final BoundedBuffer<ServerData> mensagensPendentes;

    public Server(int d, int s, int w) throws IOException {
        this.ss = new ServerSocket(12345);
        this.logins = new GestorLogins();
        this.cliente = 0;
        this.d = d;

        Data dataAtual = carregarDataAtual();
        Serie serie_inicial = new Serie(dataAtual.getData());
        this.series = new GestorSeries(s, dataAtual, serie_inicial);
        this.gestornotificacoes = new GestorNotificacoes();

        this.readers = new HashMap<>();
        this.writers = new HashMap<>();
        this.clientBuffers = new HashMap<>();

        this.simulator = new ServerSimulator(this);
        this.notifier = new ServerNotifier(this.gestornotificacoes,this.clientBuffers);

        this.mensagensPendentes = new BoundedBuffer<>();
        this.workers = new ServerWorker[d];
        startWorkers(w);
    }

    private void startWorkers(int numWorkers) throws IOException {
        for (int i = 0; i < numWorkers; i++) {
            workers[i] = new ServerWorker(logins, series, notifier, mensagensPendentes, clientBuffers, d, gestornotificacoes);
            System.out.println("[THREAD-POOL]: Worker-" + i + " criado.");
        }
        for (int i = 0; i < numWorkers; i++) {
            new Thread(workers[i], "Worker-" + i).start();
            System.out.println("[THREAD-POOL]: Worker-" + i + " começou.");
        }
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
        System.out.println("[SERVER]: Servidor arrancado na data: " + data);
        return dataAtual;
    }

    public void start() throws IOException {
        new Thread(simulator).start();
        new Thread(notifier).start();

        while (true) {
            Socket socket = ss.accept();
            int id = cliente++;

            try {
                ClientSession session = new ClientSession(socket, id, mensagensPendentes);
                clientBuffers.put(id, session.getOutBuffer());
                session.start();
                //System.out.println("[NOVO CLIENTE " + id + " LIGADO]");
            } catch (IOException e) {
                System.err.println("[SERVER]: Falha ao criar sessão para o cliente " + id + ": " + e.getMessage());
                e.printStackTrace();
                try {
                    socket.close(); // fecha socket
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void passarDia() {
        this.series.passarDia();

        Data d = this.series.getDataAtual();
        BDServerDay.setCurrentDate(
            LocalDate.of(d.getAno(), d.getMes(), d.getDia())
        );

        new Thread(() -> {
        List<ServerData> notificacoes = gestornotificacoes.clear();
            for (ServerData m : notificacoes)
                clientBuffers.get(m.getClienteID()).add(m.getMensagem());
        }).start();
    }

    public void printGS(){
        this.series.print();
    }

    @Override
    public void close() throws IOException{
        this.ss.close();
        this.series.close();
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: make server <D> <S> <W> <RESET>");
            return;
        }

        int d = Integer.parseInt(args[0]);
        int s = Integer.parseInt(args[1]);
        int w = Integer.parseInt(args[2]);

        boolean reset = args.length == 4 && args[3].equals("1");

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
                databases.BDReset.resetAll();
                System.out.println("Base de dados apagada.");
            } else {
                System.out.println("Reset cancelado.");
            }
        }

        try (Server server = new Server(d, s, w)) {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
