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

/** Classe que realiza a comunicação com o servidor e gere o envio e receção de mensagens nos scripts */
public class Stud2 implements AutoCloseable {

    /** Socket para comunicação com o servidor */
    private final Socket socket;

    /** Thread Demultiplexer para gerir a comunicação assíncrona */
    private final Demultiplexer demu;

    /** Contador para identificar mensagens */
    private int idMensagem = 0;

    /** Lista para armazenar respostas recebidas */
    private final List<String> replies;

    /** Mapa para armazenar timestamps de mensagens por tipo */
    private final Map<TipoMsg, List<Long>> messageTimestamps;

    /** Listener para notificações recebidas */
    private NotificacaoListener listener;

    /** Lock para sincronização de acesso a recursos compartilhados (lista de respostas e timestamps) */
    private ReentrantLock lock = new ReentrantLock();

    /** 
     * Construtor da classe Stud2
     * 
     * @throws IOException se ocorrer um erro ao conectar ao servidor
     * @return Uma nova instância de Stud2
     */
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

    /** 
     * Define o listener para notificações recebidas
     * 
     * @param listener O listener a ser definido
     */
    public void setNotificacaoListener(NotificacaoListener listener){
        this.listener = listener;
    }

    /** 
     * Inicia as threads que gerem a comunicação com o servidor (Demultiplexer)
     * 
     * @return void
     */
    public void start(){
        this.demu.start();
    }

    /** 
     * Fecha a comunicação com o servidor e termina o Demultiplexer
     * 
     * @throws IOException se ocorrer um erro ao fechar o socket ou demultiplexer
     */
    @Override
    public void close() throws IOException {
        this.demu.close();
        this.socket.close();
    }

    /** 
     * Envia uma mensagem ao servidor utilizando uma thread separada (Sender2)
     * 
     * @param mensagem A mensagem a ser enviada
     */
    public void send(Mensagem mensagem){
        Thread sender = new Thread(new Sender2(this.demu, mensagem, replies, listener, lock, messageTimestamps));
        sender.start();
    }

    /** 
     * Envia uma mensagem ao servidor e aguarda a resposta (sincronamente)
     * 
     * @param mensagem A mensagem a ser enviada
     * @return true se a resposta for "true", false caso contrário
     */
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

    /** 
     * Envia uma mensagem de login ao servidor e aguarda a resposta
     * 
     * @param tipo O tipo de mensagem (deve ser LOGIN)
     * @param username O nome de usuário
     * @param password A senha do usuário
     * @return true se o login for bem-sucedido, false caso contrário
     * @throws IOException se ocorrer um erro ao enviar a mensagem
     */
    public boolean sendLOGIN(TipoMsg tipo, String username, String password) throws IOException{
        Login login = new Login(username, password);
        byte[] bytes = login.serialize();
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, bytes);
        return sendAndWait(mensagem);
    }

    /** 
     * Envia uma mensagem de registo de evento ao servidor e lança uma thread para aguardar a resposta
     * 
     * @param tipo O tipo de mensagem (deve ser REGISTA_LOGIN)
     * @param username O nome de usuário
     * @param password A senha do usuário
     * @return true se o registo for bem-sucedido, false caso contrário
     * @throws IOException se ocorrer um erro ao enviar a mensagem
     */
    public void sendEVENTO_INVALIDO(TipoMsg tipo) throws IOException {
        String produtoInvalido = "";   // Produto vazio
        int quantidadeInvalida = -10;  // Quantidade negativa
        double precoInvalido = -5.0;   // Preço negativo

        Evento eventoInvalido = new Evento(produtoInvalido, quantidadeInvalida, precoInvalido);
        
        byte[] bytes = eventoInvalido.serialize();
        
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, bytes);
        this.send(mensagem);
    }

    /** 
     * Envia uma mensagem de registo de evento ao servidor e lança uma thread para aguardar a resposta
     * 
     * @param tipo O tipo de mensagem
     * @param produto O nome do produto
     * @param quantidade A quantidade do produto
     * @param preco O preço do produto
     * @throws IOException se ocorrer um erro ao enviar a mensagem
     */
    public void sendEVENTO(TipoMsg tipo, String produto, int quantidade, double preco) throws IOException{
        Evento evento = new Evento(produto, quantidade, preco);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, evento.serialize());
        this.send(mensagem);
    }

    /** 
     * Envia uma mensagem de agregação ao servidor e lança uma thread para aguardar a resposta
     * 
     * @param tipo O tipo de mensagem
     * @param produto O nome do produto
     * @param dias O número de dias para agregação
     * @throws IOException se ocorrer um erro ao enviar a mensagem
     */
    public void sendAGREGACAO(TipoMsg tipo, String produto, int dias) throws IOException{
        Agregacao agregacao = new Agregacao(produto, dias);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, agregacao.serialize());
        this.send(mensagem);
    }

    /** 
     * Envia uma mensagem de filtro ao servidor e lança uma thread para aguardar a resposta
     * 
     * @param tipo O tipo de mensagem
     * @param produtos A lista de produtos a filtrar
     * @param dias O número de dias para filtragem
     * @throws IOException se ocorrer um erro ao enviar a mensagem
     */
    public void sendFILTRAR(TipoMsg tipo, List<String> produtos, int dias) throws IOException{
        Filtrar filtrar = new Filtrar(produtos, dias);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, filtrar.serialize());
        this.send(mensagem);
    }

    /** 
     * Envia uma mensagem de notificação de vendas consecutivas ao servidor e lança uma thread para aguardar a resposta
     * 
     * @param tipo O tipo de mensagem
     * @param n O número de vendas consecutivas
     * @throws IOException se ocorrer um erro ao enviar a mensagem
     */
    public void sendNotificacaoVC(TipoMsg tipo, int n) throws IOException{
        NotificacaoVC notificacao = new NotificacaoVC(n);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, notificacao.serialize());
        this.send(mensagem);
    }

    /** 
     * Envia uma mensagem de notificação de vendas simultâneas ao servidor e lança uma thread para aguardar a resposta
     * 
     * @param tipo O tipo de mensagem
     * @param prod1 O primeiro produto
     * @param prod2 O segundo produto
     * @throws IOException se ocorrer um erro ao enviar a mensagem
     */
    public void sendNotificacaoVS(TipoMsg tipo, String prod1, String prod2) throws IOException{
        NotificacaoVS notificacao = new NotificacaoVS(prod1, prod2);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, notificacao.serialize());
        this.send(mensagem);
    }

    /** 
     * Obtém uma cópia da lista de respostas recebidas
     * 
     * @return Uma cópia da lista de respostas recebidas
     */
    public List<String> getRepliesList() {
        lock.lock(); 
        try {
            return new ArrayList<>(replies);
        } finally {
            lock.unlock();
        }
    }

    /** 
     * Obtém uma cópia do mapa de timestamps de mensagens
     * 
     * @return Uma cópia do mapa de timestamps de mensagens
     */
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
