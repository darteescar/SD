package main;

import enums.TipoMsg;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import menu.Menu;
import menu.MenuOpcao;
import utils.structs.client.NotificacaoListener;
import utils.structs.client.Stud;

public class ClienteUI {

    private final Stud cliente;
    private final Scanner scanner;

    private boolean autenticado = false;
    private boolean notificacao1 = true;
    private boolean notificacao2 = true;

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
    private void switchAutenticacao() { this.autenticado = true; }
    private void switchNotificacao1() { this.notificacao1 = !this.notificacao1; }
    private void switchNotificacao2() { this.notificacao2 = !this.notificacao2; }

    private boolean isAutenticado() { return this.autenticado; }
    private boolean noti1IsAvailable() { return this.notificacao1; }
    private boolean noti2IsAvailable() { return this.notificacao2; }

    // --------------------------
    // Inicialização
    // --------------------------
    public void start(){
        Menu menuPrincipal = criaMenuPrincipal();
        menuPrincipal.run();
        System.exit(0);
    }

    // --------------------------
    // Menus
    // --------------------------
    private Menu criaMenuPrincipal(){
        List<MenuOpcao> menuPrincipal = new ArrayList<>();
        menuPrincipal.add(new MenuOpcao("Fazer login", this::fazerLogin));
        menuPrincipal.add(new MenuOpcao("Registar", this::fazerRegisto));
        menuPrincipal.add(new MenuOpcao("Enviar mensagem", this::isAutenticado, this::irParaMenuMensagens));
        menuPrincipal.add(new MenuOpcao("Ver Respostas", this::isAutenticado, this::verRespostas));
        return new Menu(menuPrincipal);
    }

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

    // --------------------------
    // Ações de Login / Registo
    // --------------------------
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

    private void irParaMenuMensagens(){
        Menu menuMensagens = criaMenuMensagens();
        menuMensagens.run();
    }

    private void verRespostas(){
        List<String> replies = cliente.getRepliesList();
        System.out.println("---- Respostas do Servidor ----");
        for(String r : replies){
            System.out.println(r);
        }
    }

    // --------------------------
    // Envio de mensagens
    // --------------------------
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

    public static void main(String[] args) {
        ClienteUI clienteUI = new ClienteUI();
        clienteUI.start();
    }
}
