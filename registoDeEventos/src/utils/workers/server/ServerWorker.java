package utils.workers.server;

import entities.Mensagem;
import entities.ServerData;
import entities.payloads.Agregacao;
import entities.payloads.Evento;
import entities.payloads.Filtrar;
import entities.payloads.Login;
import entities.payloads.NotificacaoVC;
import entities.payloads.NotificacaoVS;
import enums.TipoMsg;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.List;
import java.util.Map;
import utils.structs.notification.BoundedBuffer;
import utils.structs.server.GestorLogins;
import utils.structs.server.GestorNotificacoes;
import utils.structs.server.GestorSeries;

public class ServerWorker implements Runnable {
    private final int d;
    private final GestorLogins logins;
    private final GestorSeries gestorSeries;
    private final ServerNotifier notifier;
    private final GestorNotificacoes gestornotificacoes;
    private final BoundedBuffer<ServerData> mensagensPendentes;
    private final Map<Integer, BoundedBuffer<Mensagem>> clientBuffers;

    public ServerWorker(GestorLogins logins, 
        GestorSeries gestorSeries, 
        ServerNotifier notifier, 
        BoundedBuffer<ServerData> mensagensPendentes,
        Map<Integer, BoundedBuffer<Mensagem>> clientBuffers,
        int d,
        GestorNotificacoes gestornotificacoes) throws IOException{
        this.logins = logins;
        this.gestorSeries = gestorSeries;
        this.notifier = notifier;
        this.mensagensPendentes = mensagensPendentes;
        this.clientBuffers = clientBuffers;
        this.d = d;
        this.gestornotificacoes = gestornotificacoes;
    }

    @Override
    public void run() {
        try {
            while (true) {
                ServerData serverData = this.mensagensPendentes.poll();
                int clienteID = serverData.getClienteID();

                Mensagem mensagem = serverData.getMensagem();
                
                int id = mensagem.getID();
                TipoMsg tipo = mensagem.getTipo();

                String result = null;
                boolean enviadoErro = false;

                if (tipo == TipoMsg.NOTIFICACAO_VC) {
                    if (!processNOTIFICACAOVC(id, mensagem.getData(), clienteID)) {
                        result = "Erro ao processar Notificação VC.";
                        enviadoErro = true;
                    }
                } else if (tipo == TipoMsg.NOTIFICACAO_VS) {
                    if (!processNOTIFICACAOVS(id, mensagem.getData(), clienteID)) {
                        result = "Erro ao processar Notificação VS.";
                        enviadoErro = true;
                    }
                } else {
                    result = execute(mensagem);
                    enviadoErro = true; // para enviar sempre resultado
                }

                // Só envia resposta se houver resultado/erro
                if (enviadoErro) {
                    BoundedBuffer<Mensagem> bufferCliente = this.clientBuffers.get(clienteID);
                    Mensagem reply = new Mensagem(id, TipoMsg.RESPOSTA, result == null ? new byte[0] : result.getBytes());
                    bufferCliente.add(reply);
                }
            }
        } finally {
            System.out.println("[SERVERWORKER]: [THREAD " + Thread.currentThread().getName() + " DO SERVIDOR TERMINOU]");
        }
    }


    private String execute(Mensagem mensagem) {
        String result = null;
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
            case ERRO ->{
                result = "Erro: Mensagem inválida ou corrompida.";
                System.out.println("[SERVERWORKER]: Mensagem de erro recebida do cliente.");
            } 
            
            default -> {
                return "Erro: tipo de mensagem inválido.";
            }
        }
        return result;
    }

    private String processLOGIN(byte[] bytes){
        try {
            Login login = Login.deserialize(bytes);

            String username = login.getUsername();
            String password = login.getPassword();

            // Lógica de verificar se o cliente está registado
            boolean b = this.logins.autenticar(username, password);
            
            return (b ? "true" : "false");   
        } catch (ProtocolException e) {
            //System.out.println("[ERRO] Login inválido ou incompleto recebido, ignorando.");
            return "false";
        } catch (IOException e) {
            //System.out.println("[ERRO] Erro na desserialização do login recebido.");
            return "false";
        }
    }

    private String processREGISTA_LOGIN(byte[] bytes){
        try {
            Login login = Login.deserialize(bytes);

            String username = login.getUsername();
            String password = login.getPassword();

            // Lógica de verificar se o cliente está registado
            boolean b = this.logins.registar(username, password);
            
            return (b ? "true" : "false");   
        } catch (ProtocolException e) {
            //System.out.println("[ERRO] Login inválido ou incompleto recebido, ignorando.");
            return "false";
        } catch (IOException e) {
            //System.out.println("[ERRO] Erro na desserialização do login recebido.");
            return "false";
        }
    }
    
    private String processREGISTO(byte[] bytes) {
        String resposta;
        try {
            Evento evento = Evento.deserialize(bytes);   

            this.gestorSeries.addSerieAtual(evento);
            this.notifier.signall(evento.getProduto());
            resposta = evento.toString() + " adicionado com sucesso na série do dia " + this.gestorSeries.getDataAtual().getData() + ".";
            return resposta;
            
        } catch (ProtocolException e) {
            resposta = "Erro, dados do evento inválidos ou corrompidos.";
            return resposta;
        } catch (IOException e) {
            resposta = "Erro na desserialização do evento.";
            return resposta;
        }
    }

    private String processQUANTIDADE_VENDAS(byte[] bytes) {
        String resposta;
        try {
            Agregacao agregacao = Agregacao.deserialize(bytes);

            String produto = agregacao.getProduto();
            int dias = agregacao.getDias();

            if (dIsInvalid(dias)) {
                return "Insira num valor entre 1 e " + this.d + ".";
            } else {
                int x = this.gestorSeries.calcQuantidadeVendas(produto, dias);
                resposta = "Quantidade de vendas do produto " + produto + " nos últimos " + dias + " dias: " + x;
                return resposta;
            }
            
        } catch (ProtocolException e) {
            resposta = "Erro, dados da agregação inválidos ou corrompidos.";
            //System.out.println("[ERRO] Agregação inválida ou incompleta recebida, ignorando.");
            return resposta;
        } catch (IOException e) {
            resposta = "Erro na desserialização da agregação.";
            //System.out.println("[ERRO] Erro na desserialização da agregação recebida.");
            return resposta;
        }

    }

    private String processVOLUME_VENDAS(byte[] bytes) {
        String resposta;
        try {
            Agregacao agregacao = Agregacao.deserialize(bytes);

            String produto = agregacao.getProduto();
            int dias = agregacao.getDias();

            if (dIsInvalid(dias)) {
                return "Insira num valor entre 1 e " + this.d + ".";
            } else {
                double x = this.gestorSeries.calcVolumeVendas(produto, dias);
                resposta = "Volume de vendas do produto " + produto + " nos últimos " + dias + " dias: " + x + ".";
                return resposta;
            }   
        } catch (ProtocolException e) {
            resposta = "Erro, dados da agregação inválidos ou corrompidos.";
            //System.out.println("[ERRO] Agregação inválida ou incompleta recebida, ignorando.");
            return resposta;
        } catch (IOException e) {
            resposta = "Erro na desserialização da agregação.";
            //System.out.println("[ERRO] Erro na desserialização da agregação recebida.");
            return resposta;
        }
    }

    private String processPRECO_MEDIO(byte[] bytes) {
        String resposta;
        try {
            Agregacao agregacao = Agregacao.deserialize(bytes);

            String produto = agregacao.getProduto();
            int dias = agregacao.getDias();

            if (dIsInvalid(dias)) {
                return "Insira num valor entre 1 e " + this.d + ".";
            } else {
                double x = this.gestorSeries.calcPrecoMedio(produto, dias);
                resposta = "Preço médio de venda do produto " + produto + " nos últimos " + dias + " dias: " + x + ".";
                return resposta;
            }   
        } catch (ProtocolException e) {
            resposta = "Erro, dados da agregação inválidos ou corrompidos.";
            //System.out.println("[ERRO] Agregação inválida ou incompleta recebida, ignorando.");
            return resposta;
        } catch (IOException e) {
            resposta = "Erro na desserialização da agregação.";
            //System.out.println("[ERRO] Erro na desserialização da agregação recebida.");
            return resposta;
        }
    }

    private String processPRECO_MAXIMO(byte[] bytes) {
        String resposta;
        try {

            Agregacao agregacao = Agregacao.deserialize(bytes);

            String produto = agregacao.getProduto();
            int dias = agregacao.getDias();

            if (dIsInvalid(dias)) {
                return "Insira num valor entre 1 e " + this.d + ".";
            } else {
                double x = this.gestorSeries.calcPrecoMaximo(produto, dias);
                resposta = "Preço máximo de venda do produto " + produto + " nos últimos " + dias + " dias: " + x + ".";
                return resposta;
            }
            
        } catch (ProtocolException e) {
            resposta = "Erro, dados da agregação inválidos ou corrompidos.";
            //System.out.println("[ERRO] Agregação inválida ou incompleta recebida, ignorando.");
            return resposta;
        } catch (IOException e) {
            resposta = "Erro na desserialização da agregação.";
            //System.out.println("[ERRO] Erro na desserialização da agregação recebida.");
            return resposta;
        }
    }

    private String processLISTA(byte[] bytes) {

        String resposta;
        try {

            Filtrar filtrar = Filtrar.deserialize(bytes);

            List<String> produto = filtrar.getProdutos();
            int dia = filtrar.getDias();

            if (dIsInvalid(dia)) {
                return "Insira num valor entre 1 e " + this.d + ".";
            } else {
                List<Evento> eventos = this.gestorSeries.filtrarEventos(produto, dia);
                return eventos.toString();
            }
        } catch (ProtocolException e) {
            resposta = "Erro, dados do filtro inválidos ou corrompidos.";
            //System.out.println("[ERRO] Filtro inválido ou incompleto recebido, ignorando");
            return resposta;
        } catch (IOException e) {
            resposta = "Erro na desserialização do filtro.";
            System.out.println("[ERRO] Erro na desserialização do filtro recebido.");
            return resposta;    
        }
    }

    private boolean processNOTIFICACAOVC(int id, byte[] bytes, int clienteID) {
        try {
            NotificacaoVC noti = NotificacaoVC.deserialize(bytes);

            this.gestornotificacoes.addVC(id,noti,clienteID);
            return true;
        } catch (ProtocolException e) {
            //System.out.println("[ERRO] Notificação VC inválida ou incompleta recebida, ignorando.");
            return false;

        } catch (IOException e) {
            //System.out.println("[ERRO] Erro na desserialização da notificação VC recebida.");
            return false;
        }
    }

    private boolean processNOTIFICACAOVS(int id, byte[] bytes, int clienteID) {

        try {
            NotificacaoVS noti = NotificacaoVS.deserialize(bytes);

            this.gestornotificacoes.addVS(id,noti,clienteID);
            return true;
        } catch (ProtocolException e) {
            //System.out.println("[ERRO] Notificação VS inválida ou incompleta recebida, ignorando.");
            return false;

        } catch (IOException e) {
            //System.out.println("[ERRO] Erro na desserialização da notificação VS recebida.");
            return false;
        }
    }

    private boolean dIsInvalid(int dias) {
       return (dias < 1 || dias > this.d);
    }
}
