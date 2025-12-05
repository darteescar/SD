package main;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import entities.Mensagem;
import entities.payloads.Agregacao;
import entities.payloads.Evento;
import entities.payloads.Filtrar;
import entities.payloads.Login;
import enums.TipoMsg;
import structs.Sender;
import structs.ClienteView;
import structs.Demultiplexer;

public class Cliente implements AutoCloseable{
    private final Socket socket;
    private final Demultiplexer demu;
    private int idMensagem = 0;
    private List<String> replies;
    private final ClienteView view;

    public Cliente() throws IOException{
        this.socket = new Socket("localhost", 12345);
        this.demu = new Demultiplexer(socket);
        this.replies = new ArrayList<>();
        this.view = new ClienteView(this);
    }

    public void start(){
        this.view.init();
    }
    
    @Override
    public void close() throws IOException{
        this.socket.close();
    }

    public void send(Mensagem mensagem){
        Thread sender = new Thread(new Sender(this.demu, mensagem, replies, view));
        sender.start();
    }

    public void sendLOGIN(TipoMsg tipo, String username, String password) throws IOException{
        Login login = new Login(username, password);
        byte[] bytes = login.serialize();
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, bytes);
        
        this.send(mensagem);
    }

    public void sendEVENTO(TipoMsg tipo, String produto, int quantidade, double preco, String data) throws IOException{
        Evento evento = new Evento(produto, quantidade, preco, data);
        byte[] bytes = evento.serialize();
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, bytes);

        this.send(mensagem);
    }

    public void sendAGREGACAO(TipoMsg tipo, String produto, int  dias) throws IOException{
        Agregacao agregacao = new Agregacao(produto, dias);
        byte[] bytes = agregacao.serialize();
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, bytes);
        
        this.send(mensagem);
    }

    public void sendFILTRAR(TipoMsg tipo, List<String> produtos, int dias) throws IOException{
        Filtrar filtrar = new Filtrar(produtos, dias);
        byte[] bytes = filtrar.serialize();
        Mensagem mensagem = new Mensagem(idMensagem++, tipo, bytes);
        
        this.send(mensagem);
    }

    public static void main(String[] args){
        try(Cliente cliente = new Cliente()){
            cliente.start();
        }catch(Exception e){
            System.out.println("[ERRO CLIENTE] " + e.getMessage());
            e.printStackTrace();
        }
    }
}