import enums.TipoMsg;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import menu.*;

public class ClienteView {

    private final Cliente cliente;

    public ClienteView(Cliente cliente) {
        this.cliente = cliente;
    }

    public void iniciar() {
        mostrarMenuConexao();
    }

    // ------------------------ MENU DE CONEXÃO ------------------------

    private void mostrarMenuConexao() {

        List<MenuOpcao> opcoes = Arrays.asList(
            new MenuOpcao("Conectar ao Servidor", () -> {
                
                    mostrarMenuLogin();
            })
        );

        Menu menu = new Menu(opcoes);
        menu.run();
    }

    private void mostrarMenuLogin() {

        List<MenuOpcao> opcoes = Arrays.asList(

            new MenuOpcao("Registar", () -> {


                // ler dados de registo


                if (cliente.registar()) {
                    System.out.println("Registo realizado com sucesso!");
                    mostrarMenuPrincipal();
                } else {
                    System.out.println("Cliente já existente. Insira outro nome!");
                    mostrarMenuLogin();
                }
            }),

            new MenuOpcao("Login", () -> {


                // ler dados de login


                if (cliente.login()) {
                    System.out.println("Login realizado com sucesso!");
                    mostrarMenuPrincipal();
                } else {
                    System.out.println("Insira um nome de cliente e password válidos!");
                    mostrarMenuLogin();
                }
            }),

            new MenuOpcao("Desconectar", () -> {
                    cliente.desconectar();
                    System.out.println("A desconectar ...");
                    mostrarMenuConexao();
                }
            )
        );

        Menu menu = new Menu(opcoes);
        menu.run();
    }

    private void mostrarMenuPrincipal() {

        List<MenuOpcao> opcoes = Arrays.asList(

            new MenuOpcao("Enviar Mensagem", () -> {
                mostrarMenuMensagens();
            }),

            new MenuOpcao("Consultar Respostas", () -> {
                mostrarRespostas();
            }),

            new MenuOpcao("Desconectar", () -> {
                cliente.desconectar();
                System.out.println("A desconectar ...");
                mostrarMenuConexao();

            })
        );

        Menu menu = new Menu(opcoes);
        menu.run();
    }

    private void mostrarMenuMensagens() {
        Scanner sc = new Scanner(System.in);

        List<MenuOpcao> opcoes = Arrays.asList(

            new MenuOpcao("Enviar Mensagem", () -> {

                // Mostrar opções de TipoMsg
                System.out.println("Escolha o tipo de mensagem:");
                for (TipoMsg tipo : TipoMsg.values()) {
                    System.out.println(tipo.ordinal() + " - " + tipo);
                }

                // Ler escolha do usuário e converter para TipoMsg
                int tipoEscolhido = Integer.parseInt(sc.nextLine());
                TipoMsg tipo = TipoMsg.values()[tipoEscolhido];

                // Perguntar dados da mensagem
                System.out.print("Digite os dados da mensagem: ");
                String dados = sc.nextLine();

                // Chamar o Cliente
                cliente.enviarMensagem(tipo, dados);

                // Voltar para o menu principal
                mostrarMenuPrincipal();
            })
        );

        Menu menu = new Menu(opcoes);
        menu.run();
    }


    private void mostrarRespostas() {
        ////////////////////////////////////////////////////
        ///  String stg = cliente.consultar_respostas();
        mostrarMenuMensagens();
    }
}