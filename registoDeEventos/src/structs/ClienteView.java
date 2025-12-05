package structs;
import enums.TipoMsg;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import main.Cliente;
import menu.Menu;
import menu.MenuOpcao;

public class ClienteView {
    private final Cliente cliente;
    private final ReentrantLock lock;
    private final Scanner scanner;
    private boolean autenticado = false;

    public ClienteView(Cliente cliente){
        this.cliente = cliente;
        this.scanner = new Scanner(System.in);
        this.lock = new ReentrantLock();
    }

    public void switchAutenticacao(){
        this.lock.lock();
        try{
            this.autenticado = true;
        }finally{
            this.lock.unlock();
        }
    }

    public boolean isAutenticado(){
        return this.autenticado;
    }

    public void init(){
        Menu menuPrincipal = criaMenuPrincipal();
        menuPrincipal.run();
    }

    // Criação de menus
    public Menu criaMenuPrincipal(){
        List<MenuOpcao> menuPrincipal = new ArrayList<>();
        menuPrincipal.add(new MenuOpcao("Fazer login", () -> fazerLogin()));
        menuPrincipal.add(new MenuOpcao("Registar", () -> fazerRegisto()));
        menuPrincipal.add(new MenuOpcao("Enviar mensagem", () -> this.isAutenticado() ,() -> irParaMenuMensagens()));
        menuPrincipal.add(new MenuOpcao("Ver Respostas", () -> this.isAutenticado(),() -> verRespostas()));
        return new Menu(menuPrincipal);
    }
    
    public Menu criaMenuMensagens(){
        List<MenuOpcao> menuMenuMensagens = new ArrayList<>();
        menuMenuMensagens.add(new MenuOpcao("Registar evento", () -> enviarEvento()));
        menuMenuMensagens.add(new MenuOpcao("Quantidade de vendas", () -> enviarQuantidade()));
        menuMenuMensagens.add(new MenuOpcao("Volume vendas", () -> enviarVolume()));
        menuMenuMensagens.add(new MenuOpcao("Preço médio de vendas", () -> enviarPrecoMedio()));
        menuMenuMensagens.add(new MenuOpcao("Preço máximo de vendas", () -> enviarPrecoMaximo()));
        menuMenuMensagens.add(new MenuOpcao("Lista de eventos", () -> enviarLista()));
        return new Menu(menuMenuMensagens);
    }

    //Métodos para os menus
    public void fazerLogin(){
        try{
            System.out.print("Introduza o username > ");
            String username = scanner.nextLine();
            System.out.print("Introduza a password > ");
            String password = scanner.nextLine();

            boolean result = this.cliente.sendLOGIN(TipoMsg.LOGIN, username, password);
            if (result) {
                System.out.println("[LOGIN EFETUADO COM SUCESSO]");
                this.switchAutenticacao();
                Menu menuMensagens = this.criaMenuMensagens();
                menuMensagens .run();

            } else {
                System.out.println("[CLIENTE NAO REGISTADO, POR FAVOR REGISTE-SE]");
                fazerLogin();
            }
        }catch(Exception e){
            System.out.println("[ERRO AO ENVIAR LOGIN] " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void fazerRegisto(){
        try{
            System.out.print("Introduza o username > ");
            String username = scanner.nextLine();
            System.out.print("Introduza a password > ");
            String password = scanner.nextLine();

            boolean result = this.cliente.sendLOGIN(TipoMsg.REGISTA_LOGIN, username, password);
            if (result) {
                System.out.println("[REGISTO EFETUADO COM SUCESSO]");
                this.switchAutenticacao();
                Menu menuMensagens = this.criaMenuMensagens();
                menuMensagens .run();
            } else {
                System.out.println("[PROBLEMA AO REGISTAR, POR FAVOR TENTE NOVAMENTE]");
                fazerRegisto();
            }

        }catch(Exception e){
            System.out.println("[ERRO AO ENVIAR REGISTO DE LOGIN] " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void irParaMenuMensagens(){
        Menu menuMensagens = criaMenuMensagens();
        menuMensagens.run();
    }

    public void verRespostas(){
        this.cliente.listReplies();
    }

    public void enviarEvento(){
        try{
            System.out.print("Introduza o nome do produto > ");
            String produto = scanner.nextLine();
            System.out.print("Introduza a quantidade> ");
            String quantidade = scanner.nextLine();
            System.out.print("Introduza o preço> ");
            String preco = scanner.nextLine();
            System.out.print("Introduza a data> ");
            String data = scanner.nextLine();

            this.cliente.sendEVENTO(TipoMsg.REGISTO, produto, Integer.parseInt(quantidade), Double.parseDouble(preco), data);

        }catch(IOException e){
            System.out.println("[ERRO AO ENVIAR REGISTO DE LOGIN] " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviarQuantidade(){
        try{
            System.out.print("Introduza o nome do produto > ");
            String produto = scanner.nextLine();
            System.out.print("Introduza o número de dias > ");
            String dias = scanner.nextLine();

            this.cliente.sendAGREGACAO(TipoMsg.QUANTIDADE_VENDAS, produto, Integer.parseInt(dias));

        }catch(Exception e){
            System.out.println("[ERRO AO ENVIAR QUANTIDADE] " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviarVolume(){
        try{
            System.out.print("Introduza o nome do produto > ");
            String produto = scanner.nextLine();
            System.out.print("Introduza o número de dias > ");
            String dias = scanner.nextLine();

            this.cliente.sendAGREGACAO(TipoMsg.VOLUME_VENDAS, produto, Integer.parseInt(dias));

        }catch(Exception e){
            System.out.println("[ERRO AO ENVIAR VOLUME] " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviarPrecoMedio(){
        try{
            System.out.print("Introduza o nome do produto > ");
            String produto = scanner.nextLine();
            System.out.print("Introduza o número de dias > ");
            String dias = scanner.nextLine();

            this.cliente.sendAGREGACAO(TipoMsg.PRECO_MEDIO, produto, Integer.parseInt(dias));

        }catch(Exception e){
            System.out.println("[ERRO AO ENVIAR PREÇO MÉDIO] " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviarPrecoMaximo(){
        try{
            System.out.print("Introduza o nome do produto > ");
            String produto = scanner.nextLine();
            System.out.print("Introduza o número de dias > ");
            String dias = scanner.nextLine();

            this.cliente.sendAGREGACAO(TipoMsg.PRECO_MAXIMO, produto, Integer.parseInt(dias));

        }catch(Exception e){
            System.out.println("[ERRO AO ENVIAR PREÇO MÁXIMO] " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviarLista(){
        try{
            System.out.print("Introduza os nomes dos produtos (separados por ; ) > ");
            String produto = scanner.nextLine();
            System.out.print("Introduza o número de dias > ");
            String dias = scanner.nextLine();

            List<String> produtos = new ArrayList<>(Arrays.asList(produto.split(";")));

            this.cliente.sendFILTRAR(TipoMsg.LISTA, produtos, Integer.parseInt(dias));

        }catch(Exception e){
            System.out.println("[ERRO AO ENVIAR LISTA DE EVENTOS] " + e.getMessage());
            e.printStackTrace();
        }
    }
}