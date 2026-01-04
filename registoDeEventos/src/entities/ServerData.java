package entities;

/** Representa os dados enviados que são processados pelo servidor */
public class ServerData {

     /** Identificador do cliente */
     private final int clienteID;

     /** Mensagem enviada pelo cliente */
     private final Mensagem mensagem;

     /** 
      * Construtor da classe ServerData
      * 
      * @param clienteID Identificador do cliente
      * @param mensagem Mensagem enviada pelo cliente
      * @return Uma nova instância de ServerData
      */
     public ServerData(int clienteID, Mensagem mensagem) {
          this.clienteID = clienteID;
          this.mensagem = mensagem;
     }

     /** 
      * Obtém o identificador do cliente
      * 
      * @return Identificador do cliente
      */
     public int getClienteID() {
          return clienteID;
     }

     /** 
      * Obtém a mensagem enviada pelo cliente
      * 
      * @return Mensagem enviada pelo cliente
      */
     public Mensagem getMensagem() {
          return mensagem;
     }

}