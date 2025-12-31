package scripts;

import entities.Mensagem;
import entities.payloads.*;
import enums.TipoMsg;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import utils.structs.client.NotificacaoListener;
import utils.workers.client.Demultiplexer;

public class Stud2 implements AutoCloseable {

    private final Socket socket;
    private final Demultiplexer demu;
    private int idMensagem = 0;
    private final List<String> replies;
    private final Map<TipoMsg, List<Long>> messageTimestamps;
    private NotificacaoListener listener;
    private ReentrantLock lock = new ReentrantLock();

    public Stud2() {
        try {
            this.socket = new Socket("localhost", 12345);
            this.demu = new Demultiplexer(socket);
            this.replies = new ArrayList<>();
            this.messageTimestamps = new HashMap<>();
            this.messageTimestamps.put(TipoMsg.LOGIN, new ArrayList<>());
            this.messageTimestamps.put(TipoMsg.REGISTO, new ArrayList<>());
            this.messageTimestamps.put(TipoMsg.QUANTIDADE_VENDAS, new ArrayList<>());
            this.messageTimestamps.put(TipoMsg.VOLUME_VENDAS, new ArrayList<>());
            this.messageTimestamps.put(TipoMsg.PRECO_MEDIO, new ArrayList<>());
            this.messageTimestamps.put(TipoMsg.PRECO_MAXIMO, new ArrayList<>());
            this.messageTimestamps.put(TipoMsg.LISTA, new ArrayList<>());
            this.messageTimestamps.put(TipoMsg.NOTIFICACAO_VC, new ArrayList<>());
            this.messageTimestamps.put(TipoMsg.NOTIFICACAO_VS, new ArrayList<>());
            this.messageTimestamps.put(TipoMsg.RESPOSTA, new ArrayList<>());

        } catch (IOException e) {
            throw new RuntimeException("[ERRO CLIENTE] Nao foi possivel conectar ao servidor: " + e.getMessage());
        }
    }

    public void setNotificacaoListener(NotificacaoListener listener){
        this.listener = listener;
    }

    public void start(){
        this.demu.start();
    }

    @Override
    public void close() throws IOException {
        this.demu.close();
        this.socket.close();
    }

    
    public void send(Mensagem mensagem){
        Thread sender = new Thread(new Sender2(this.demu, mensagem, replies, listener, lock, messageTimestamps));
        sender.start();
    }

    
    private boolean sendAndWait(Mensagem mensagem) {
        boolean result = false;
        try {
            int id = mensagem.getID();
            
            long sendTime = System.currentTimeMillis();
            // Registar timestamp de envio

            this.demu.send(mensagem);
            String resposta = this.demu.receive(id);

            long receiveTime = System.currentTimeMillis();
            // Registar timestamp de receção

            lock.lock();
            try {
                // Registar timestamp de receção
                messageTimestamps.putIfAbsent(mensagem.getTipo(), new ArrayList<>());
                messageTimestamps.get(mensagem.getTipo()).add(receiveTime - sendTime);
            } finally {
                lock.unlock();
            }

            result = "true".equals(resposta);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean sendLOGIN(TipoMsg tipo, String username, String password) throws IOException{
        Login login = new Login(username, password);
        byte[] bytes = login.serialize();
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, bytes);
        return sendAndWait(mensagem);
    }

    public void sendEVENTO_INVALIDO(TipoMsg tipo) throws IOException {
        String produtoInvalido = "";   // Produto vazio
        int quantidadeInvalida = -10;  // Quantidade negativa
        double precoInvalido = -5.0;   // Preço negativo

        Evento eventoInvalido = new Evento(produtoInvalido, quantidadeInvalida, precoInvalido);
        
        byte[] bytes = eventoInvalido.serialize();
        
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, bytes);
        this.send(mensagem);
    }

    public void sendEVENTO(TipoMsg tipo, String produto, int quantidade, double preco) throws IOException{
        Evento evento = new Evento(produto, quantidade, preco);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, evento.serialize());
        this.send(mensagem);
    }

    public void sendAGREGACAO(TipoMsg tipo, String produto, int dias) throws IOException{
        Agregacao agregacao = new Agregacao(produto, dias);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, agregacao.serialize());
        this.send(mensagem);
    }

    public void sendFILTRAR(TipoMsg tipo, List<String> produtos, int dias) throws IOException{
        Filtrar filtrar = new Filtrar(produtos, dias);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, filtrar.serialize());
        this.send(mensagem);
    }

    public void sendNotificacaoVC(TipoMsg tipo, int n) throws IOException{
        NotificacaoVC notificacao = new NotificacaoVC(n);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, notificacao.serialize());
        this.send(mensagem);
    }

    public void sendNotificacaoVS(TipoMsg tipo, String prod1, String prod2) throws IOException{
        NotificacaoVS notificacao = new NotificacaoVS(prod1, prod2);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, notificacao.serialize());
        this.send(mensagem);
    }

    public List<String> getRepliesList() { // o sender pode estar a modificar a lista
        lock.lock(); 
        try {
            return new ArrayList<>(replies);
        } finally {
            lock.unlock();
        }
    }

    public Map<TipoMsg, List<Long>> getMessageTimestamps() {
        lock.lock();
        try {
            Map<TipoMsg, List<Long>> copy = new HashMap<>();
            for (Map.Entry<TipoMsg, List<Long>> entry : messageTimestamps.entrySet()) {
                copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            return copy;
        } finally {
            lock.unlock();
        }
    }

}
