package Conexao;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import Mensagens.Mensagem;

public class ServerClientConnection implements AutoCloseable {

    public static class Frame {
        public final int tag;
        public final Mensagem data;
        public Frame(int tag, Mensagem data) { this.tag = tag; this.data = data; }
    }

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private ReentrantLock lockSend = new ReentrantLock();
    private ReentrantLock lockReceive = new ReentrantLock();

    public ServerClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void sendMsg(Mensagem msg, int tag) throws IOException {
        this.lockSend.lock();
        try {
            this.out.writeInt(tag);
            msg.serialize(this.out);
            this.out.flush();
        } finally {
            this.lockSend.unlock();
        }
    }

    public void sendFrame(Frame frame) throws IOException {
        this.lockSend.lock();
        try {
            this.out.writeInt(frame.tag);
            frame.data.serialize(this.out);
            this.out.flush();
        } finally {
            this.lockSend.unlock();
        }
    }

    public Frame receiveMsg() throws IOException {
        this.lockReceive.lock();
        try {
            int tag = this.in.readInt();
            return new Frame(tag, Mensagem.deserialize(this.in));
        } finally {
            this.lockReceive.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        this.in.close();
        this.out.close();
        this.socket.close();
    }
}
