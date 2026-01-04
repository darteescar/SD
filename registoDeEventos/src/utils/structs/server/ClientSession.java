package utils.structs.server;

import entities.Mensagem;
import entities.ServerData;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;
import utils.structs.notification.BoundedBuffer;
import utils.workers.server.ServerReader;
import utils.workers.server.ServerWriter;

/**
 * Representa a sessão de um cliente no servidor.
 * Gere a comunicação entre o servidor e o cliente através de threads leitoras e escritoras dedicadas.
 */
public class ClientSession {

    /** Identificador único do cliente */
    private final int clienteId;

    /** Socket de comunicação com o cliente */
    private final Socket socket;

    /** Thread responsável por ler dados do cliente do Socket */
    private ServerReader reader;

    /** Thread responsável por escrever dados para o cliente no Socket */
    private ServerWriter writer;

    /** Lock para sincronização de acesso à sessão */
    private final ReentrantLock lock = new ReentrantLock();

    /** Indica se a sessão está fechada */
    private final boolean closed = false;

    /** 
     * Construtor da sessão do cliente.
     * Inicializa o socket, identificador do cliente, leitor e escritor.
     * 
     * @param socket Socket de comunicação com o cliente
     * @param clienteId Identificador único do cliente
     * @param mensagensPendentes Buffer de mensagens pendentes para o servidor
     * @throws IOException Se ocorrer um erro ao configurar os fluxos de entrada/saída
     * @return Uma nova instância de ClientSession
     */
    public ClientSession(Socket socket, int clienteId, BoundedBuffer<ServerData> mensagensPendentes) throws IOException {

        this.socket = socket;
        this.clienteId = clienteId;

        boolean ok = false;
        try {
            DataInputStream input = new DataInputStream( new BufferedInputStream(socket.getInputStream()));
            DataOutputStream output = new DataOutputStream( new BufferedOutputStream(socket.getOutputStream()));

            this.reader = new ServerReader(this,mensagensPendentes, clienteId, input);
            this.writer = new ServerWriter(this,new BoundedBuffer<>(), clienteId, output);
            ok = true;
        } finally {
            if (!ok) {
                socket.close(); // close se algo falhar
            }
        }
    }

    /** 
     * Inicia as threads de leitura e escrita para a sessão do cliente.
     */
    public void start() {
        new Thread(reader).start();
        new Thread(writer).start();
    }

    /** 
     * Fecha a sessão do cliente, encerrando o socket e notificando, se necessário, o escritor para terminar.
     */
    public void close() {
        lock.lock();
        try {
            if (!socket.isClosed()) {
                socket.close();
                writer.send(ServerWriter.POISON_PILL);
                //System.out.println("CS: [LIGACAO COM CLIENTE " + clienteId + " FECHADA]");
            }
        } catch (IOException e) {
            //System.out.println("CS: [ERRO AO FECHAR SOCKET CLIENTE " + clienteId + "] " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /** 
     * Obtém o buffer de saída do escritor para enviar mensagens ao cliente.
     * 
     * @return Buffer de mensagens de saída
     */
    public BoundedBuffer<Mensagem> getOutBuffer() {
        return writer.getOutBuffer();
    }

}
