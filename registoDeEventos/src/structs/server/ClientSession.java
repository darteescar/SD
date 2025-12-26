package structs.server;

import entities.Mensagem;
import entities.ServerData;
import java.io.IOException;
import java.net.Socket;
import structs.notification.ConcurrentBuffer;

public class ClientSession {

    private final int clienteId;
    private final Socket socket;
    private ServerReader reader;
    private ServerWriter writer;

    public ClientSession(Socket socket, int clienteId,
                         ConcurrentBuffer<ServerData> taskBuffer)
            throws IOException {

        this.socket = socket;
        this.clienteId = clienteId;

        boolean ok = false;
        try {
            this.reader = new ServerReader(socket, taskBuffer, clienteId);
            this.writer = new ServerWriter(socket, clienteId, new ConcurrentBuffer<>());
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

    public ConcurrentBuffer<Mensagem> getOutBuffer() {
        return writer.getOutBuffer();
    }

}
