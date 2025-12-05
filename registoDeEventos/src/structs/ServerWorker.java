package structs;

import entities.Mensagem;
import entities.payloads.Agregacao;
import entities.payloads.Evento;
import entities.payloads.Filtrar;
import entities.payloads.Login;
import enums.TipoMsg;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerWorker implements Runnable {
    private final Socket socket;
    private final GestorLogins logins;
    private final DataOutputStream out;
    private final DataInputStream in;

    public ServerWorker(Socket socket, GestorLogins logins) throws IOException{
        this.socket = socket;
        this.logins = logins;
        this.out = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
        this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            while (true) {
                Mensagem mensagem = null;
                try {
                    mensagem = Mensagem.deserialize(in);
                } catch (IOException e) {
                    // Quando o cliente fecha o socket
                    System.out.println("[CLIENTE DESCONECTOU-SE]");
                    break; // Sai do loop, termina a thread
                }

                int id = mensagem.getID();
                TipoMsg tipo = mensagem.getTipo();
                System.out.println("Servidor recebeu mensagem > " + id + " (" + tipo + ")");

                String result = "";
                try {
                    result = execute(mensagem);
                    System.out.println("Result: " + result);
                } catch (Exception e) {
                    System.out.println("[ERRO AO EXECUTAR MENSAGEM] " + e.getMessage());
                    e.printStackTrace();
                }

                try {
                    Mensagem reply = new Mensagem(id, TipoMsg.RESPOSTA, result == null ? new byte[0] : result.getBytes());
                    reply.serialize(out);
                    out.flush();
                    System.out.println("Servidor enviou resposta da mensagem > " + id + " (" + tipo + ")");
                } catch (IOException e) {
                    System.out.println("[ERRO AO ENVIAR RESPOSTA] " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } finally {
            try {
                this.in.close();
                this.out.close();
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[THREAD DO CLIENTE TERMINOU]");
        }
    }

    private String execute(Mensagem mensagem) throws IOException{
        String result = "";
        TipoMsg tipo = mensagem.getTipo();

        switch(tipo){
            case LOGIN -> result = processLOGIN(mensagem.getData());
            case REGISTA_LOGIN -> result = processREGISTA_LOGIN(mensagem.getData());
            case REGISTO -> result = processREGISTO(mensagem.getData());
            case QUANTIDADE_VENDAS -> result = processQUANTIDADE_VENDAS(mensagem.getData());
            case VOLUME_VENDAS -> result = processVOLUME_VENDAS(mensagem.getData());
            case PRECO_MEDIO -> result =processPRECO_MEDIO(mensagem.getData());
            case PRECO_MAXIMO -> result = processPRECO_MAXIMO(mensagem.getData());
            case LISTA -> result = processLISTA(mensagem.getData());
            default -> throw new IllegalArgumentException("[TIPO DE MENSAGEM INVALIDO");
        }

        return result;
    }

    private String processLOGIN(byte[] bytes) throws IOException{
        Login login = Login.deserialize(bytes);
        String username = login.getUsername();
        String password = login.getPassword();

        // Lógica de verificar se o cliente está registado
        boolean b = this.logins.autenticar(username, password);
        
        return (b ? "true" : "false");
    }

    private String processREGISTA_LOGIN(byte[] bytes) throws IOException{
        Login login = Login.deserialize(bytes);
        String username = login.getUsername();
        String password = login.getPassword();

        // Lógica de resgistar o cliente no sistema

        boolean b = this.logins.registar(username, password);
        
        return (b ? "true" : "false");
    }
    
    private String processREGISTO(byte[] bytes) throws IOException{
        Evento evento = Evento.deserialize(bytes);

        // Lógica de resgistar um evento na série do dia

        return evento.toString();
    }

    private String processQUANTIDADE_VENDAS(byte[] bytes) throws IOException{
        Agregacao agregacao = Agregacao.deserialize(bytes);
        //String produto = agregacao.getProduto();
        //int dias = agregacao.getDias();

        // Lógica de realizar a query da quantidade de vendas
        System.out.println(agregacao.toString());

        return agregacao.toString();
    }

    private String processVOLUME_VENDAS(byte[] bytes) throws IOException{
        Agregacao agregacao = Agregacao.deserialize(bytes);
        //String produto = agregacao.getProduto();
        //int dias = agregacao.getDias();

        // Lógica de realizar a query do volume de vendas

        return agregacao.toString();
    }

    private String processPRECO_MEDIO(byte[] bytes) throws IOException{
        Agregacao agregacao = Agregacao.deserialize(bytes);
        //String produto = agregacao.getProduto();
        //int dias = agregacao.getDias();

        // Lógica de realizar a query do preço médio

        return agregacao.toString();
    }

    private String processPRECO_MAXIMO(byte[] bytes) throws IOException{
        Agregacao agregacao = Agregacao.deserialize(bytes);
        //String produto = agregacao.getProduto();
        //int dias = agregacao.getDias();

        // Lógica de realizar a query do preço máximo

        return agregacao.toString();
    }

    private String processLISTA(byte[] bytes) throws IOException{
        Filtrar filtrar = Filtrar.deserialize(bytes);
        //List<String> produto = filtrar.getProdutos();
        //int dias = filtrar.getDias();

        // Lógica de realizar a query da lista

        return filtrar.toString();
    }
}
