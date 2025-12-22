package structs.server;

import entities.Mensagem;
import entities.payloads.Agregacao;
import entities.payloads.Evento;
import entities.payloads.Filtrar;
import entities.payloads.Login;
import entities.payloads.NotificacaoVC;
import entities.payloads.NotificacaoVS;
import enums.TipoMsg;
import java.io.IOException;
import java.util.List;
import structs.notification.ServerNotifier;

public class ServerWorker implements Runnable {
    private final GestorLogins logins;
    private final GestorSeries gestorSeries;
    private final int cliente;
    private final int d;
    private final ServerNotifier notifier;
    private final ClientContext contexto;

    public ServerWorker(ClientContext contexto,GestorLogins logins, int cliente, GestorSeries gestorSeries, int d, ServerNotifier notifier) throws IOException{
        this.logins = logins;
        this.gestorSeries = gestorSeries;
        this.cliente = cliente;
        this.d = d;
        this.notifier = notifier;
        this.contexto = contexto;
    }

    @Override
    public void run() {
        try {
            while (true) {

                Mensagem mensagem;
                mensagem = this.contexto.receive();
                if (mensagem == null) {
                    // Quando o cliente fecha o socket
                    System.out.println("[CLIENTE DESCONECTOU-SE]");
                    break; // Sai do loop, e termina a thread
                }
                
                int id = mensagem.getID();
                TipoMsg tipo = mensagem.getTipo();
                //System.out.println("[RECEIVED MESSAGE] -> " + id + " (" + tipo + ") [FROM] -> " + cliente);

                if (TipoMsg.NOTIFICACAO_VC == tipo) {
                    
                    processNOTIFICACAOVC(id,mensagem.getData());

                } else if (TipoMsg.NOTIFICACAO_VS == tipo)
                {   
                    processNOTIFICACAOVS(id,mensagem.getData());

                } else {
                    String result = "";
                    
                    result = execute(mensagem);
                
                    Mensagem reply = new Mensagem(id, TipoMsg.RESPOSTA, result == null ? new byte[0] : result.getBytes());
                    
                    this.contexto.send(reply);
                    //System.out.println("[SENT MESSAGE] -> " + id + " (" + tipo + ") [TO] -> " + cliente);
                }
            }
        } finally {
            this.contexto.close();
            System.out.println("[THREAD DO CLIENTE TERMINOU]");
        }
    }

    private String execute(Mensagem mensagem) {
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
            default -> throw new IllegalArgumentException("[TIPO DE MENSAGEM INVALIDO]");
        }

        return result;
    }

    private String processLOGIN(byte[] bytes){
        Login login = Login.deserialize(bytes);
        String username = login.getUsername();
        String password = login.getPassword();

        // Lógica de verificar se o cliente está registado
        boolean b = this.logins.autenticar(username, password);
        
        return (b ? "true" : "false");
    }

    private String processREGISTA_LOGIN(byte[] bytes){
        Login login = Login.deserialize(bytes);
        String username = login.getUsername();
        String password = login.getPassword();

        // Lógica de resgistar o cliente no sistema

        boolean b = this.logins.registar(username, password);
        
        return (b ? "true" : "false");
    }
    
    private String processREGISTO(byte[] bytes) {
        Evento evento = Evento.deserialize(bytes);

        this.gestorSeries.addSerieAtual(evento);
        this.notifier.signall(evento.getProduto());
        String resposta = evento.toString() + " adicionado com sucesso na série do dia " + this.gestorSeries.getDataAtual().getData() + ".";

        return resposta;
    }

    private String processQUANTIDADE_VENDAS(byte[] bytes) {
        Agregacao agregacao = Agregacao.deserialize(bytes);
        String produto = agregacao.getProduto();
        int dias = agregacao.getDias();

        if (dIsInvalid(dias)) {
            return "Insira num valor entre 1 e " + this.d + ".";
        } else {
            int x = this.gestorSeries.calcQuantidadeVendas(produto, dias);
            String resposta = "Quantidade de vendas do produto " + produto + " nos últimos " + dias + " dias: " + x;

            return resposta;
        }
    }

    private String processVOLUME_VENDAS(byte[] bytes) {
        Agregacao agregacao = Agregacao.deserialize(bytes);
        String produto = agregacao.getProduto();
        int dias = agregacao.getDias();

        if (dIsInvalid(dias)) {
            return "Insira num valor entre 1 e " + this.d + ".";
        } else {
            double x = this.gestorSeries.calcVolumeVendas(produto, dias);
            String resposta = "Volume de vendas do produto " + produto + " nos últimos " + dias + " dias: " + x + ".";
            return resposta;
        }
    }

    private String processPRECO_MEDIO(byte[] bytes) {
        Agregacao agregacao = Agregacao.deserialize(bytes);
        String produto = agregacao.getProduto();
        int dias = agregacao.getDias();

        if (dIsInvalid(dias)) {
            return "Insira num valor entre 1 e " + this.d + ".";
        } else {
            double x = this.gestorSeries.calcPrecoMedio(produto, dias);
            String resposta = "Preço médio de venda do produto " + produto + " nos últimos " + dias + " dias: " + x + ".";
            return resposta;
        }
    }

    private String processPRECO_MAXIMO(byte[] bytes) {
        Agregacao agregacao = Agregacao.deserialize(bytes);
        String produto = agregacao.getProduto();
        int dias = agregacao.getDias();

        if (dIsInvalid(dias)) {
            return "Insira num valor entre 1 e " + this.d + ".";
        } else {
            double x = this.gestorSeries.calcPrecoMaximo(produto, dias);
            String resposta = "Preço máximo de venda do produto " + produto + " nos últimos " + dias + " dias: " + x + ".";
            return resposta;
        }

    }

    private String processLISTA(byte[] bytes) {
        Filtrar filtrar = Filtrar.deserialize(bytes);
        List<String> produto = filtrar.getProdutos();
        int dia = filtrar.getDias();

        if (dIsInvalid(dia)) {
            return "Insira num valor entre 1 e " + this.d + ".";
        } else {
            List<Evento> eventos = this.gestorSeries.filtrarEventos(produto, dia);
            return eventos.toString();
        }
    }

    private void processNOTIFICACAOVC(int id, byte[] bytes) {
        NotificacaoVC noti = NotificacaoVC.deserialize(bytes);

        this.notifier.add(id,noti,this.contexto);
    }

    private void processNOTIFICACAOVS(int id, byte[] bytes) {
        NotificacaoVS noti = NotificacaoVS.deserialize(bytes);

        this.notifier.add(id,noti,this.contexto);
    }

    private boolean dIsInvalid(int dias) {
       return (dias < 1 || dias > this.d);
    }
}
