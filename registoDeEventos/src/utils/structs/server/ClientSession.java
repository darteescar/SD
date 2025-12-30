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

public class ClientSession {

    private final int clienteId;
    private final Socket socket;
    private ServerReader reader;
    private ServerWriter writer;
    private final ReentrantLock lock = new ReentrantLock();
    private final boolean closed = false;

    public ClientSession(Socket socket, int clienteId,
                         BoundedBuffer<ServerData> mensagensPendentes)
            throws IOException {

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

    public void start() {
        new Thread(reader).start();
        new Thread(writer).start();
    }

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

    public BoundedBuffer<Mensagem> getOutBuffer() {
        return writer.getOutBuffer();
    }

}
