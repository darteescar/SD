import entities.Mensagem;
import entities.payloads.Login;
import enums.TipoMsg;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Cliente {
    private Socket socket;
    private int num_pedidos = 0;
    // Lista/Queue de respostas prontas a serem 'poped' pelo metodo consultarRespostas()
    // Demultiplexer de mensagens recebidas

    public static void main(String[] args) throws IOException {
        Cliente cliente = new Cliente();
        ClienteView view = new ClienteView(cliente);
        view.iniciar();
    }

    // ------------------------ AÇÕES ------------------------

    public boolean conectar() {
        try {
            this.socket = new Socket("localhost",12345);
            if (socket.isConnected()){
                return true;
            }

        } catch (IOException e) {
            System.out.println("Cliente não conseguiu estabelecer conexão. "+e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public void desconectar() {
        try {

            // Esperar por todas as mensagens das Threads ?
            this.socket.close();

        } catch (IOException e) {
            System.out.println("Cliente não conseguiu estabelecer conexão. "+e.getMessage());
            e.printStackTrace();
        }

    }

    public boolean login(String username, String password) {
        Login login = new Login(username, password);
        byte[] payload;
        try {
            payload = login.serialize();
            enviarMensagem(TipoMsg.LOGIN, payload);
            // consoante valores retornados pelo Server retornamos true ou false
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean registar(String username, String password) {
        Login login = new Login(username, password);
        byte[] payload;
        try {
            payload = login.serialize();
            enviarMensagem(TipoMsg.LOGIN, payload);
            // consoante valores retornados pelo Server retornamos true ou false
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void enviarMensagem(TipoMsg tipo, byte[] data) {
        Thread thread = new Thread(() -> {
            try {
                Mensagem msg = new Mensagem(this.num_pedidos, tipo, data);

                this.num_pedidos += 1;

                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                msg.serialize(dos);
                dos.flush();

                // esperaMensagem

            } catch (IOException e) {
                System.out.println("Erro ao enviar mensagem: " + e.getMessage());
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void consultarRespostas() {
        // ler da queue
    }
}