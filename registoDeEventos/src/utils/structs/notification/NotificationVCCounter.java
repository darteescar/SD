package utils.structs.notification;

/** Estrutura para contar notificações de vendas consecutivas de um cliente */
public class NotificationVCCounter {

     /** Último produto vendido */
     private static String produto = "";

     /** ID do cliente que enviou a notificação */
     private final int clienteID;

     /** Número total de notificações consecutivas realizadas até ao momento */
     private int counter;

     /** Contador de vendas consecutivas objetivo */
     private int number;

     /** Identificador único da mensagem de notificação */
     private int id;

     /** 
      * Construtor da estrutura NotificationVCCounter
      * 
      * @param clienteID ID do cliente que enviou a notificação
      * @param number Número total de notificações consecutivas realizadas até ao momento
      * @param id Identificador único da mensagem de notificação
      * @return Uma nova instância de NotificationVCCounter
      */
     public NotificationVCCounter(int id ,int clienteID, int number) {
          this.clienteID = clienteID;
          this.counter = 1;
          this.number = number;
          this.id = id;
     }

     /** 
      * Devolve o produto associado à notificação
      * 
      * @return O produto associado à notificação
      */
     public static String getProduto() {
          return produto;
     }

     /**
      * Devolve o ID do cliente que enviou a notificação
      * 
      * @return O ID do cliente que enviou a notificação
      */
     public int getClienteID() {
          return clienteID;
     }

     /**
      * Devolve o valor do contador de vendas consecutivas
      * 
      * @return O contador de vendas consecutivas
      */
     public int getCounter() {
          return counter;
     }

     /**
      * Devolve o número objetivo de notificações consecutivas
      * 
      * @return O número objetivo de notificações consecutivas
      */
     public int getN() {
          return number;
     }

     /**
      * Devolve o identificador único da mensagem de notificação
      * 
      * @return O identificador único da mensagem de notificação
      */
     public int getId() {
          return id;
     }

     /**
      * Define o produto associado à notificação
      * 
      * @param produto O produto a ser associado à notificação
      */
     public static void setProduto(String produto) {
          NotificationVCCounter.produto = produto;
     }

     /** 
      * Incrementa o contador de vendas consecutivas em 1. Usado quando um registo com o mesmo produto guardado é feito.
      */
     public void incrementCounter() {
          this.counter++;
     }

     /** 
      * Reseta o contador de vendas consecutivas para 1. Usado quando um registo com um produto diferente do guardado é feito.
      */
     public void resetCounter() {
          this.counter = 1;
     }

}
