public class Cliente {

    private boolean conectado = false;

    public static void main(String[] args) {
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
        

        // código real de ligação ao servidor
        // socket = new Socket(...)

        conectado = true; // trocar para o real
    }

    public void desconectar() {
        
        conectado = false;
        // Fechar socket, parar threads, etc.
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
