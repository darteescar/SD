package entities;

public class ServerData {
     private final int clienteID;
     private final Mensagem mensagem;

     public ServerData(int clienteID, Mensagem mensagem) {
          this.clienteID = clienteID;
          this.mensagem = mensagem;
     }

     public int getClienteID() {
          return clienteID;
     }

     public Mensagem getMensagem() {
          return mensagem;
     }

}
