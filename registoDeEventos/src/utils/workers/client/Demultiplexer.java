package utils.workers.client;

import entities.Mensagem;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** Thread responsável por demultiplexar mensagens recebidas do servidor */
public class Demultiplexer implements AutoCloseable {

    /** 
     * Entrada na tabela do demultiplexador, contendo a fila de mensagens e a condição
     */
    class Entry {

        /** Fila de mensagens associada a esta entrada */
        public Queue<String> queue;

        /** Condição para notificar threads aguardando mensagens nesta entrada */
        public Condition cond;

        /** Construtor que inicializa a fila e a condição associada ao lock fornecido */
        public Entry(ReentrantLock lock) {
            this.queue = new ArrayDeque<>();
            this.cond = lock.newCondition();
        }
    }

    /** Socket para comunicação com o servidor */
    private final Socket socket;

    /** Stream de saída para enviar dados ao servidor */
    private final DataOutputStream out;

    /** Stream de entrada para receber dados do servidor */
    private final DataInputStream in;

    /** Lock para sincronização de acesso às estruturas internas */
    private final ReentrantLock lock;


    /** Mapa de entradas do Demultiplexer, indexadas por ID */
    private final Map<Integer, Entry> mapEntries;

    /** Exceção capturada durante a execução da thread de fundo */
    private Exception ex;

    /** Indica se o Demultiplexer foi fechado */
    private boolean closed = false;

    /** Thread responsável por ler mensagens do servidor em segundo plano */
    private Thread backgroundThread;

    /** 
     * Construtor que inicializa o Demultiplexer com o socket fornecido
     * 
     * @param socket Socket para comunicação com o servidor
     * @throws IOException Se ocorrer um erro ao obter os streams do socket
     * @return Uma nova instância do Demultiplexer
     */
    public Demultiplexer(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
        this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
        this.lock = new ReentrantLock();
        this.mapEntries = new HashMap<>();
        this.ex = null;
    }

    /** 
     * Obtém a entrada associada ao ID fornecido, criando uma nova se necessário
     * 
     * @param id ID da entrada a ser obtida
     * @return A entrada associada ao ID fornecido
     */
    public Entry getEntry(int id) {
        return mapEntries.computeIfAbsent(id, k -> new Entry(lock));
    }

    /** 
     * Inicia a thread de fundo que lê mensagens do servidor e demultiplexa-as para as filas apropriadas
     */
    public void start() {
        backgroundThread = new Thread(() -> {
            try {
                while (true) {
                    lock.lock();
                    try {
                        if (closed) break; // sai do loop se fechado
                    } finally {
                        lock.unlock();
                    }

                    Mensagem m = Mensagem.deserialize(in);
                    int id = m.getID();
                    String result = new String(m.getData());

                    lock.lock();
                    try {
                        Entry entry = getEntry(id);
                        entry.queue.add(result);
                        entry.cond.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (Exception e) {
                // sinaliza exceção e acorda threads
                lock.lock();
                try {
                    this.ex = e;
                    for (Entry entry : mapEntries.values()) {
                        entry.cond.signalAll();
                    }
                } finally {
                    lock.unlock();
                }
            }
        }, "Demultiplexer-Thread");

        backgroundThread.start();
    }

    /** 
     * Método usado pelas threads Sender para enviar mensagens ao servidor
     * 
     * @param mensagem Mensagem a ser enviada
     * @throws IOException Se ocorrer um erro ao enviar a mensagem
     */
    public void send(Mensagem mensagem) throws IOException {
        lock.lock();
        try {
            mensagem.serialize(out);
            out.flush();
        } finally {
            lock.unlock();
        }
    }

    /** 
     * Método usado pelas threads Sender para receber mensagens do servidor
     * 
     * @param id ID da entrada da qual receber a mensagem
     * @return A mensagem recebida ou null se o Demultiplexer estiver fechado ou ocorrer uma exceção
     * @throws InterruptedException Se a thread for interrompida enquanto aguarda
     */
    public String receive(int id) throws InterruptedException {
        lock.lock();
        try {
            Entry entry = getEntry(id);
            while (entry.queue.isEmpty()) {
                if (closed || ex != null) return null; // termina se fechado ou exceção
                entry.cond.await();
                if (closed || ex != null) return null; // termina se fechado ou exceção
            }
            return entry.queue.poll();
        } finally {
            lock.unlock();
        }
    }

    /** 
     * Fecha o Demultiplexer, encerrando a thread de fundo e libertando os recursos
     */
    @Override
    public void close() throws IOException {
        lock.lock();
        try {
            closed = true;
            for (Entry entry : mapEntries.values()) {
                entry.cond.signalAll(); // acorda threads bloqueadas
            }
        } finally {
            lock.unlock();
        }

        in.close();
        out.close();
    }
}
