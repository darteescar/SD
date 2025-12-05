package structs;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import enums.TipoMsg;
import main.Cliente;
import menu.Menu;
import menu.MenuOpcao;

public class ClienteView {
    private final Cliente cliente;
    private final ReentrantLock lock;
    private boolean autenticado = false;

    public ClienteView(Cliente cliente){
        this.cliente = cliente;
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

    public boolean autenticado(){
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
        menuPrincipal.add(new MenuOpcao("Registar", () -> registar()));
        return new Menu(menuPrincipal);
    }

    public Menu criaMenuOpcoes(){
        List<MenuOpcao> menuOpcoes = new ArrayList<>();
        menuOpcoes.add(new MenuOpcao("Enviar mensagem", () -> fazerLogin()));
        menuOpcoes.add(new MenuOpcao("Ver resposta", () -> verRespostas()));
        return new Menu(menuOpcoes);
    }
    
    public Menu criaMenuMensagens(){
        List<MenuOpcao> menuMenuMensagens = new ArrayList<>();
        menuMenuMensagens.add(new MenuOpcao("Registar evento", () -> fazerLogin()));
        menuMenuMensagens.add(new MenuOpcao("Quantidade de vendas", () -> verRespostas()));
        menuMenuMensagens.add(new MenuOpcao("Volume vendas", () -> verRespostas()));
        menuMenuMensagens.add(new MenuOpcao("Preço médio de vendas", () -> verRespostas()));
        menuMenuMensagens.add(new MenuOpcao("Preço máximo de vendas", () -> verRespostas()));
        return new Menu(menuMenuMensagens);
    }



    //Métodos para os menus
    public void fazerLogin(){
        this.cliente.sendLOGIN(TipoMsg.LOGIN, username, password);
    }

    public void registar(){

    }

    public void verRespostas(){

    }
}