package main;

import enums.TipoMsg;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import menu.Menu;
import menu.MenuOpcao;
import utils.structs.client.NotificacaoListener;
import utils.structs.client.Stud;

/** Interface do Cliente */
public class ClienteUI {

    /** Stud que o cliente usa para comunicação */
    private final Stud cliente;

    /** Scanner para leitura de input do utilizador */
    private final Scanner scanner;

    /** Estado de autenticação do cliente */
    private boolean autenticado = false;

    /** Estado das notificações de vendas simultâneas do cliente */
    private boolean notificacao1 = true;

    /** Estado das notificações de vendas consecutivas do cliente */
    private boolean notificacao2 = true;


    /** 
     * Construtor
     * 
     * Inicializa o Stud e o Scanner, e regista os listeners para as notificações
     */
    public ClienteUI(){
        this.cliente = new Stud();
        cliente.start();
        this.scanner = new Scanner(System.in);
        // Regista listener para atualizar notificações
        this.cliente.setNotificacaoListener(new NotificacaoListener() {
            @Override
            public void notificacaoVSEnviada() { switchNotificacao1(); }

            @Override
            public void notificacaoVCEnviada() { switchNotificacao2(); }
        });
    }

    // --------------------------
    // Métodos de estado
    // --------------------------
    /** 
     * Muda o estado de autenticação do cliente para true
     */
    private void switchAutenticacao() { this.autenticado = true; }

    /** Muda o estado das notificações de vendas simultâneas */
    private void switchNotificacao1() { this.notificacao1 = !this.notificacao1; }

    /** Muda o estado das notificações de vendas consecutivas */
    private void switchNotificacao2() { this.notificacao2 = !this.notificacao2; }

    /** 
     * Verifica se o cliente está autenticado
     * 
     * @return true se o cliente estiver autenticado, false caso contrário
     */
    private boolean isAutenticado() { return this.autenticado; }

    /** 
     * Verifica se o cliente pode enviar a notificação de vendas simultâneas
     * 
     * @return true se a notificação estiver disponível, false caso contrário
     */
    private boolean noti1IsAvailable() { return this.notificacao1; }

    /**
     * Verifica se o cliente pode enviar a notificação de vendas consecutivas
     * 
     * @return true se a notificação estiver disponível, false caso contrário
     */
    private boolean noti2IsAvailable() { return this.notificacao2; }

    /** 
     * Inicia a interface do cliente. Executa o menu principal e fecha o stud ao terminar.
     */
    public void start(){
        Menu menuPrincipal = criaMenuPrincipal();
        menuPrincipal.run();
        try {
            this.cliente.close();   
        } catch (IOException e) {
            System.out.println("[ERRO AO FECHAR STUD] " + e.getMessage());
        }
        System.exit(0);
    }

    /** 
     * Cria o menu principal do cliente
     * 
     * @return Menu principal do cliente
     */
    private Menu criaMenuPrincipal(){
        List<MenuOpcao> menuPrincipal = new ArrayList<>();
        menuPrincipal.add(new MenuOpcao("Fazer login", this::fazerLogin));
        menuPrincipal.add(new MenuOpcao("Registar", this::fazerRegisto));
        menuPrincipal.add(new MenuOpcao("Enviar mensagem", this::isAutenticado, this::irParaMenuMensagens));
        menuPrincipal.add(new MenuOpcao("Ver Respostas", this::isAutenticado, this::verRespostas));
        return new Menu(menuPrincipal);
    }

    /** 
     * Cria o menu de mensagens do cliente
     * 
     * @return Menu de mensagens do cliente
     */
    private Menu criaMenuMensagens(){
        List<MenuOpcao> menuMensagens = new ArrayList<>();
        menuMensagens.add(new MenuOpcao("Registar evento", this::enviarEvento));
        menuMensagens.add(new MenuOpcao("Quantidade de vendas", this::enviarQuantidade));
        menuMensagens.add(new MenuOpcao("Volume vendas", this::enviarVolume));
        menuMensagens.add(new MenuOpcao("Preço médio de vendas", this::enviarPrecoMedio));
        menuMensagens.add(new MenuOpcao("Preço máximo de vendas", this::enviarPrecoMaximo));
        menuMensagens.add(new MenuOpcao("Lista de eventos", this::enviarLista));
        menuMensagens.add(new MenuOpcao("Notificação de Vendas Simultâneas",
                this::noti1IsAvailable, this::enviarNotificacaoVS));
        menuMensagens.add(new MenuOpcao("Notificação de Vendas Consecutivas",
                this::noti2IsAvailable, this::enviarNotificacaoVC));
        return new Menu(menuMensagens);
    }

    /** 
     * Faz o login do cliente. Se o login falhar, pede ao utilizador para se registar
     */
    private void fazerLogin(){
        try {
            System.out.print("Introduza o username > ");
            String username = scanner.nextLine();
            System.out.print("Introduza a password > ");
            String password = scanner.nextLine();

            boolean result = this.cliente.sendLOGIN(TipoMsg.LOGIN, username, password);
            if(result){
                System.out.println("[LOGIN EFETUADO COM SUCESSO]");
                switchAutenticacao();
                irParaMenuMensagens();
            } else {
                System.out.println("[CLIENTE NAO REGISTADO, POR FAVOR REGISTE-SE]");
                fazerRegisto();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /** 
     * Regista o cliente. Se o registo falhar, pede ao utilizador para tentar novamente
     */
    private void fazerRegisto(){
        try {
            System.out.print("Introduza o username > ");
            String username = scanner.nextLine();
            System.out.print("Introduza a password > ");
            String password = scanner.nextLine();

            boolean result = this.cliente.sendLOGIN(TipoMsg.REGISTA_LOGIN, username, password);
            if(result){
                System.out.println("[REGISTO EFETUADO COM SUCESSO]");
                switchAutenticacao();
                irParaMenuMensagens();
            } else {
                System.out.println("[PROBLEMA AO REGISTAR, TENTE NOVAMENTE]");
                fazerRegisto();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /** 
     * Vai para o menu de mensagens do cliente
     */
    private void irParaMenuMensagens(){
        Menu menuMensagens = criaMenuMensagens();
        menuMensagens.run();
    }

    /** 
     * Mostra as respostas recebidas do servidor
     */
    private void verRespostas(){
        List<String> replies = cliente.getRepliesList();
        System.out.println("---- Respostas do Servidor ----");
        for(String r : replies){
            System.out.println(r);
        }
    }

    /** 
     * Envia uma mensagem de inserção de um evento para o servidor. Pede ao utilizador o nome, quantidade e preço do evento
     */
    private void enviarEvento(){
        try {
            System.out.print("Nome do produto > ");
            String produto = scanner.nextLine();
            System.out.print("Quantidade > ");
            int qtd = Integer.parseInt(scanner.nextLine());
            System.out.print("Preço > ");
            double preco = Double.parseDouble(scanner.nextLine());

            cliente.sendEVENTO(TipoMsg.REGISTO, produto, qtd, preco);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /** 
     * Envia uma mensagem de agregação de quantidade de vendas para o servidor. Pede ao utilizador o nome do produto e o número de dias
     */
    private void enviarQuantidade(){
        try {
            System.out.print("Nome do produto > ");
            String produto = scanner.nextLine();
            System.out.print("Número de dias > ");
            int dias = Integer.parseInt(scanner.nextLine());

            cliente.sendAGREGACAO(TipoMsg.QUANTIDADE_VENDAS, produto, dias);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /** 
     * Envia uma mensagem de agregação de volume de vendas para o servidor. Pede ao utilizador o nome do produto e o número de dias
     */
    private void enviarVolume(){
        try {
            System.out.print("Nome do produto > ");
            String produto = scanner.nextLine();
            System.out.print("Número de dias > ");
            int dias = Integer.parseInt(scanner.nextLine());

            cliente.sendAGREGACAO(TipoMsg.VOLUME_VENDAS, produto, dias);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /** 
     * Envia uma mensagem de agregação de preço médio para o servidor. Pede ao utilizador o nome do produto e o número de dias
     */
    private void enviarPrecoMedio(){
        try {
            System.out.print("Nome do produto > ");
            String produto = scanner.nextLine();
            System.out.print("Número de dias > ");
            int dias = Integer.parseInt(scanner.nextLine());

            cliente.sendAGREGACAO(TipoMsg.PRECO_MEDIO, produto, dias);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Envia uma mensagem de agregação de preço máximo para o servidor. Pede ao utilizador o nome do produto e o número de dias
     */
    private void enviarPrecoMaximo(){
        try {
            System.out.print("Nome do produto > ");
            String produto = scanner.nextLine();
            System.out.print("Número de dias > ");
            int dias = Integer.parseInt(scanner.nextLine());

            cliente.sendAGREGACAO(TipoMsg.PRECO_MAXIMO, produto, dias);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Envia uma mensagem de filtro de eventos para o servidor. Pede ao utilizador a lista de produtos e o número de dias
     */
    private void enviarLista(){
        try {
            System.out.print("Produtos (separados por ; ) > ");
            String line = scanner.nextLine();
            System.out.print("Número de dias > ");
            int dias = Integer.parseInt(scanner.nextLine());

            List<String> produtos = new ArrayList<>(Arrays.asList(line.split(";")));
            cliente.sendFILTRAR(TipoMsg.LISTA, produtos, dias);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /** 
     * Envia uma notificação de vendas simultâneas para o servidor. Pede ao utilizador os dois produtos a comparar
     */
    private void enviarNotificacaoVS(){
        try {
            System.out.print("Produto 1 > ");
            String p1 = scanner.nextLine();
            System.out.print("Produto 2 > ");
            String p2 = scanner.nextLine();
            cliente.sendNotificacaoVS(TipoMsg.NOTIFICACAO_VS, p1, p2);
            switchNotificacao1();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /** 
     * Envia uma notificação de vendas consecutivas para o servidor. Pede ao utilizador o número de vendas consecutivas
     */
    private void enviarNotificacaoVC(){
        try {
            System.out.print("Número de vendas consecutivas > ");
            int n = Integer.parseInt(scanner.nextLine());
            cliente.sendNotificacaoVC(TipoMsg.NOTIFICACAO_VC, n);
            switchNotificacao2();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /** 
     * Método main do ClienteUI
     * 
     * @param args Argumentos da linha de comandos
     */
    public static void main(String[] args) {
        ClienteUI clienteUI = new ClienteUI();
        clienteUI.start();
    }
}
