import java.io.IOException;
import java.net.Socket;

public class Cliente {
    private Socket socket;
    private boolean conectado = false;

    public static void main(String[] args) throws IOException {
        Cliente cliente = new Cliente();
        ClienteView view = new ClienteView(cliente);
        view.iniciar();
    }

    // ------------------------ ESTADO ------------------------

    public boolean isConectado() {
        return conectado;
    }

    // ------------------------ AÇÕES ------------------------

    public void conectar() {
        try {
            this.socket = new Socket("localhost",12345);
            if (socket.isConnected()){
                conectado = true;
            }

        } catch (IOException e) {
            System.out.println("Cliente não conseguiu estabelecer conexão. "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void desconectar() {
        try {
            // Esperar por todas as mensagens das Threads ?
            this.socket.close();
            if (socket.isClosed()){
                conectado = false;
            }

        } catch (IOException e) {
            System.out.println("Cliente não conseguiu estabelecer conexão. "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviarEvento() {
        // criar Thread e chamar esta função
    }

    public void enviarQuery1() {
        // criar Thread e chamar esta função
    }

    public void enviarQuery2() {
        // criar Thread e chamar esta função
    }

    public void enviarQuery3() {
        // criar Thread e chamar esta função
    }

    public void consultarRespostas() {
        // ler da queue
    }
}
