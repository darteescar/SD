package structs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import entities.Mensagem;
import entities.payloads.Agregacao;
import entities.payloads.Evento;
import entities.payloads.Filtrar;
import entities.payloads.Login;
import enums.TipoMsg;

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
    public void run(){
        try{
            // Deserialize da mensagem
            Mensagem mensagem = Mensagem.deserialize(in);
            int id = mensagem.getID();
            TipoMsg tipo = mensagem.getTipo();
            System.out.println("Servidor recebeu mensagem > " + id + " (" + tipo + ")" );

            // Execução da mensagem
            String result = execute(mensagem);
            System.out.println("Servidor executou mensagem > " + id + " (" + tipo + ")" );

            // Envio de resposta
            Mensagem reply = new Mensagem(id, TipoMsg.RESPOSTA, result.getBytes());
            reply.serialize(out);
            out.flush();
            System.out.println("Servidor enviou resposta da mensagem > " + id + " (" + tipo + ")" );

        }catch(Exception e){
            System.out.println("[ERRO SERVER-WORKER] " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String execute(Mensagem mensagem) throws IOException{
        String result = "";
        TipoMsg tipo = mensagem.getTipo();

        switch(tipo){
            case LOGIN -> result = processLOGIN(mensagem.getData());
            case REGISTA_LOGIN -> result = processREGISTA_LOGIN(mensagem.getData());
            case REGISTO -> result = processREGISTO(mensagem.getData());
            case QUANTIDADE_VENDAS -> processQUANTIDADE_VENDAS(mensagem.getData());
            case VOLUME_VENDAS -> processVOLUME_VENDAS(mensagem.getData());
            case PRECO_MEDIO -> processPRECO_MEDIO(mensagem.getData());
            case PRECO_MAXIMO -> processPRECO_MAXIMO(mensagem.getData());
            case LISTA -> processLISTA(mensagem.getData());
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

        return null;
    }

    private String processQUANTIDADE_VENDAS(byte[] bytes) throws IOException{
        Agregacao agregacao = Agregacao.deserialize(bytes);
        String produto = agregacao.getProduto();
        int dias = agregacao.getDias();

        // Lógica de realizar a query da quantidade de vendas

        return null;
    }

    private String processVOLUME_VENDAS(byte[] bytes) throws IOException{
        Agregacao agregacao = Agregacao.deserialize(bytes);
        String produto = agregacao.getProduto();
        int dias = agregacao.getDias();

        // Lógica de realizar a query do volume de vendas

        return null;
    }

    private String processPRECO_MEDIO(byte[] bytes) throws IOException{
        Agregacao agregacao = Agregacao.deserialize(bytes);
        String produto = agregacao.getProduto();
        int dias = agregacao.getDias();

        // Lógica de realizar a query do preço médio

        return null;
    }

    private String processPRECO_MAXIMO(byte[] bytes) throws IOException{
        Agregacao agregacao = Agregacao.deserialize(bytes);
        String produto = agregacao.getProduto();
        int dias = agregacao.getDias();

        // Lógica de realizar a query do preço máximo

        return null;
    }

    private String processLISTA(byte[] bytes) throws IOException{
        Filtrar filtrar = Filtrar.deserialize(bytes);
        List<String> produto = filtrar.getProdutos();
        int dias = filtrar.getDias();

        // Lógica de realizar a query da lista

        return null;
    }
}
