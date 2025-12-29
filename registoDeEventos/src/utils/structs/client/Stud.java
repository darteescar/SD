package utils.structs.client;

import entities.Mensagem;
import entities.payloads.*;
import enums.TipoMsg;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import utils.workers.client.Demultiplexer;
import utils.workers.client.Sender;

public class Stud implements AutoCloseable {

    /** Socket de comunicação com o servidor */
    private final Socket socket;

    /** Demultiplexador para gerir o envio e receção das mensagens */
    private final Demultiplexer demu;

    /** Identificador único para as mensagens enviadas */
    private int idMensagem = 0;

    /** Lista de respostas recebidas */
    private final List<String> replies;

    /** Listener para notificações recebidas */
    private NotificacaoListener listener;

    /** Lock para sincronização de acesso às respostas */
    private ReentrantLock lock = new ReentrantLock();

    /** 
     * Construtor vazio que inicializa o Stud, conectando ao servidor na porta 12345 e inicializando o demultiplexador.
     */
    public Stud() {
        try {
            this.socket = new Socket("localhost", 12345);
            this.demu = new Demultiplexer(socket);
            this.replies = new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("[ERRO CLIENTE] Nao foi possivel conectar ao servidor: " + e.getMessage());
        }
    }

    /** Define o listener para notificações recebidas
     * 
     * @param listener O listener a ser definido
     */
    public void setNotificacaoListener(NotificacaoListener listener){
        this.listener = listener;
    }

    /** Inicia o demultiplexador */
    public void start(){
        this.demu.start();
    }

    /** Fecha o socket de comunicação com o servidor */
    @Override
    public void close() throws IOException {
        this.socket.close();
    }

    /** Envia uma mensagem. Cria uma nova thread para o envio da mensagem e inicia-a.
     * 
     * @param mensagem A mensagem a ser enviada
    */
    public void send(Mensagem mensagem){
        Thread sender = new Thread(new Sender(this.demu, mensagem, replies, listener, lock));
        sender.start();
    }

    /** Envia uma mensagem e espera pela resposta. Retorna true se a resposta for "true", caso contrário, false.
     * 
     * @param mensagem A mensagem a ser enviada
     * @return true se a resposta for "true", caso contrário, false
     */
    private boolean sendAndWait(Mensagem mensagem) {
        boolean result = false;
        try {
            int id = mensagem.getID();
            this.demu.send(mensagem);
            String resposta = this.demu.receive(id);
            result = "true".equals(resposta);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /** Envia uma mensagem de login/registo e espera pela resposta. Retorna true se a resposta for "true" ou "false" caso contrário.
     * 
     * @param tipo O tipo da mensagem
     * @param username O nome de usuário
     * @param password A senha
     * @return true se a resposta for "true" ou "false" caso contrário
     * @throws IOException Se ocorrer um erro de I/O durante o envio da mensagem
     */
    public boolean sendLOGIN(TipoMsg tipo, String username, String password) throws IOException{
        Login login = new Login(username, password);
        byte[] bytes = login.serialize();
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, bytes);
        return sendAndWait(mensagem);
    }

    /** Envia uma mensagem de evento com valores inválidos. Cria um objeto Evento, serializa-o e envia-o dentro de uma mensagem.
     * É criada uma nova thread para o envio da mensagem, já que usa o método send().
     * 
     * @param tipo O tipo da mensagem
     * @throws IOException Se ocorrer um erro de I/O durante o envio da mensagem
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

    /** Envia uma mensagem de evento. Cria um objeto Evento com os parâmetros fornecidos, serializa-o e envia-o dentro de uma mensagem.
     * É criada uma nova thread para o envio da mensagem, já que usa o método send().
     * 
     * @param tipo O tipo da mensagem
     * @param produto O nome do produto
     * @param quantidade A quantidade do produto
     * @param preco O preço do produto
     * @throws IOException Se ocorrer um erro de I/O durante o envio da mensagem
     */
    public void sendEVENTO(TipoMsg tipo, String produto, int quantidade, double preco) throws IOException{
        Evento evento = new Evento(produto, quantidade, preco);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, evento.serialize());
        this.send(mensagem);
    }


    /** Envia uma mensagem de agregação. Cria um objeto Agregacao com os parâmetros fornecidos, serializa-o e envia-o dentro de uma mensagem.
     * É criada uma nova thread para o envio da mensagem, já que usa o método send().
     * 
     * @param tipo O tipo da mensagem
     * @param produto O nome do produto
     * @param dias O número de dias
     * @throws IOException Se ocorrer um erro de I/O durante o envio da mensagem
     */
    public void sendAGREGACAO(TipoMsg tipo, String produto, int dias) throws IOException{
        Agregacao agregacao = new Agregacao(produto, dias);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, agregacao.serialize());
        this.send(mensagem);
    }

    /** Envia uma mensagem de filtragem. Cria um objeto Filtrar com os parâmetros fornecidos, serializa-o e envia-o dentro de uma mensagem.
     * É criada uma nova thread para o envio da mensagem, já que usa o método send().
     * 
     * @param tipo O tipo da mensagem
     * @param produtos A lista de produtos
     * @param dias O número de dias
     * @throws IOException Se ocorrer um erro de I/O durante o envio da mensagem
     */
    public void sendFILTRAR(TipoMsg tipo, List<String> produtos, int dias) throws IOException{
        Filtrar filtrar = new Filtrar(produtos, dias);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, filtrar.serialize());
        this.send(mensagem);
    }

    /** Envia uma mensagem de notificação VC. Cria um objeto NotificacaoVC com o parâmetro fornecido, serializa-o e envia-o dentro de uma mensagem.
     * É criada uma nova thread para o envio da mensagem, já que usa o método send().
     * 
     * @param tipo O tipo da mensagem
     * @param n O valor da notificação
     * @throws IOException Se ocorrer um erro de I/O durante o envio da mensagem
     */
    public void sendNotificacaoVC(TipoMsg tipo, int n) throws IOException{
        NotificacaoVC notificacao = new NotificacaoVC(n);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, notificacao.serialize());
        this.send(mensagem);
    }

    /** Envia uma mensagem de notificação VS. Cria um objeto NotificacaoVS com os parâmetros fornecidos, serializa-o e envia-o dentro de uma mensagem.
     * É criada uma nova thread para o envio da mensagem, já que usa o método send().
     * 
     * @param tipo O tipo da mensagem
     * @param prod1 O primeiro produto
     * @param prod2 O segundo produto
     * @throws IOException Se ocorrer um erro de I/O durante o envio da mensagem
     */
    public void sendNotificacaoVS(TipoMsg tipo, String prod1, String prod2) throws IOException{
        NotificacaoVS notificacao = new NotificacaoVS(prod1, prod2);
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, notificacao.serialize());
        this.send(mensagem);
    }

    /** Retorna uma cópia da lista de respostas recebidas.
     * 
     * @return Uma cópia da lista de respostas
     */
    public List<String> getRepliesList() { // o sender pode estar a modificar a lista
        lock.lock(); 
        try {
            return new ArrayList<>(replies);
        } finally {
            lock.unlock();
        }
    }
}
