import java.util.Arrays;
import java.util.List;
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
                cliente.conectar();
                if (cliente.isConectado()) {
                    System.out.println("Ligação estabelecida!");
                    mostrarMenuPrincipal();
                }
            })
        );

        Menu menu = new Menu(opcoes);
        menu.run();
    }

    // ------------------------ MENU PRINCIPAL -------------------------

    private void mostrarMenuPrincipal() {

        List<MenuOpcao> opcoes = Arrays.asList(

            new MenuOpcao("Enviar Evento",
                () -> cliente.enviarEvento()
            ),

            new MenuOpcao("Enviar Query 1",
                () -> cliente.enviarQuery1()
            ),

            new MenuOpcao("Enviar Query 2",
                () -> cliente.enviarQuery2()
            ),

            new MenuOpcao("Enviar Query 3",
                () -> cliente.enviarQuery3()
            ),

            new MenuOpcao("Consultar Respostas",
                () -> cliente.consultarRespostas()
            ),

            new MenuOpcao("Desconectar",
                () -> {
                    cliente.desconectar();
                    mostrarMenuConexao();
                }
            )
        );

        Menu menu = new Menu(opcoes);
        menu.run();
    }
}