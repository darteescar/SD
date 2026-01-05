package main;

import databases.BDServerDay;
import entities.Data;
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
import utils.structs.server.BoundedBuffer;
import utils.structs.server.ClientSession;
import utils.structs.server.GestorLogins;
import utils.structs.server.GestorNotificacoes;
import utils.structs.server.GestorSeries;
import utils.workers.server.ServerNotifier;
import utils.workers.server.ServerSimulator;
import utils.workers.server.ServerWorker;

/** Classe do Servidor com todas as suas funcionalidades */
public class Server implements AutoCloseable{

    /** Socket do Servidor onde os clientes se ligam */
    private final ServerSocket ss;

    /** Gestor de logins dos clientes */
    private final GestorLogins logins;

    /** Gestor das séries de eventos */
    private final GestorSeries series;

    /** Gestor das notificações dos clientes */
    private final GestorNotificacoes gestornotificacoes;

    /** Contador de clientes conectados */
    private int cliente;

    /** Intervalo de número de dias para considerar nos cálculos */
    private final int d;

    /** Thread que simula a passagem do tempo */
    private final ServerSimulator simulator;

    /** Thread que notifica os clientes e guarda as estruturas onde os pedidos de notificações são armazenados */
    private final ServerNotifier notifier;

    /** Thread pool para processamento das mensagens dos clientes */
    private final ServerWorker[] threadpool;

    /** Mapa para gerir as sessões dos clientes */
    private final Map<Integer, ClientSession> clientSessions;

    /** Buffer de mensagens pendentes para processamento pelos ServerWorkers */
    private final BoundedBuffer<ServerData> mensagensPendentes;

    /**
     * Construtor da classe Server. Inicializa todas as estruturas de dados e threads necessárias.
     * 
     * @param d Intervalo de número de dias para considerar nos cálculos
     * @param s Intervalo de número de dias para ter em memória
     * @param w Número de threads na thread pool
     * @param intervalo Intervalo de tempo (em milissegundos) para simular a passagem de um dia
     * @throws IOException
     * @return Uma nova instância de Server
     */
    public Server(int d, int s, int w, long intervalo) throws IOException {
        this.ss = new ServerSocket(12345);
        this.logins = new GestorLogins();
        this.cliente = 0;
        this.d = d;

        Data dataAtual = carregarDataAtual();
        Serie serie_inicial = new Serie(dataAtual.getData());
        this.series = new GestorSeries(s, dataAtual, serie_inicial);
        this.gestornotificacoes = new GestorNotificacoes();

        this.clientSessions = new HashMap<>();

        this.simulator = new ServerSimulator(this, intervalo);
        this.notifier = new ServerNotifier(this.gestornotificacoes,this.clientSessions);

        this.mensagensPendentes = new BoundedBuffer<>();
        this.threadpool = new ServerWorker[w];
        startthreadpool(w);
    }

    /** 
     * Inicia a thread pool com o número especificado de threads.
     * 
     * @param numthreadpool Número de threads na thread pool
     * @throws IOException caso ocorra um erro ao criar os workers
     */
    private void startthreadpool(int numthreadpool) throws IOException {
        for (int i = 0; i < numthreadpool; i++) {
            threadpool[i] = new ServerWorker(logins, series, notifier, mensagensPendentes, clientSessions, d, gestornotificacoes);
            System.out.println("[THREAD-POOL]: Worker-" + i + " criado.");
        }
        for (int i = 0; i < numthreadpool; i++) {
            new Thread(threadpool[i], "Worker-" + i).start();
            System.out.println("[THREAD-POOL]: Worker-" + i + " começou.");
        }
    }

    /** 
     * Carrega a data atual do servidor a partir da base de dados (BDServerDay) e incrementa-a em um dia. Assim, o servidor inicia sempre um dia após a última data registrada.
     * 
     * @return A data atual do servidor
     */
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

    /** 
     * Inicia o ciclo do servidor, aceitando conexões de clientes e criando sessões e threads para cada um deles.
     * 
     * @throws IOException caso ocorra um erro ao aceitar conexões ou criar sessões
     */
    public void start() throws IOException {
        new Thread(simulator).start();
        new Thread(notifier).start();

        while (true) {
            Socket socket = ss.accept();
            int id = cliente++;

            try {
                ClientSession session = new ClientSession(socket, id, mensagensPendentes);
                clientSessions.put(id, session);
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

    /** 
     * Simula a passagem de um dia no servidor, guarda a série do dia, atualiza a data atual e notifica os clientes necessários.
     */
    public void passarDia() {
        this.series.passarDia();

        Data d = this.series.getDataAtual();
        BDServerDay.setCurrentDate(
            LocalDate.of(d.getAno(), d.getMes(), d.getDia())
        );

        new Thread(() -> {
        List<ServerData> notificacoes = gestornotificacoes.clear();
            for (ServerData m : notificacoes) {
                ClientSession session = clientSessions.get(m.getClienteID());
                if (session != null) {
                    session.addToBuffer(m.getMensagem());
                }
            }
        }).start();
    }

    /** 
     * Fecha o servidor, libertando todos os recursos associados.
     */
    @Override
    public void close() throws IOException{
        this.ss.close();
        this.series.close();
    }

    /**
     * Ponto de entrada do programa. Inicializa o servidor com os parâmetros fornecidos.
     * 
     * @param args Os argumentos da linha de comando (D, S, W, I, RESET)
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Uso: make server <D> <S> <W> <I> <RESET>");
            return;
        }

        int d = Integer.parseInt(args[0]);
        int s = Integer.parseInt(args[1]);
        int w = Integer.parseInt(args[2]);
        long intervalo = Long.parseLong(args[3]);

        boolean reset = args.length >= 5 && args[4].equals("1");

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
                System.out.println("[SERVER]: Base de dados apagada.");
            } else {
                System.out.println("[SERVER]: Reset cancelado.");
            }
        }

        try (Server server = new Server(d, s, w, intervalo)) {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
