package utils.structs.notification;

/** Estrutura para contar notificações de vendas simultâneas de dois produtos diferentes por um cliente */
public class NotificationVSCounter {

     /** ID do cliente que enviou a notificação */
     private final int clienteID;

     /** Primeiro produto que ser quer verificar se é vendido */
     private final String prod1;

     /** Segundo produto que ser quer verificar se é vendido */
     private final String prod2;

     /** Indica se o primeiro produto foi vendido */
     private boolean prod_1_sold;

     /** Indica se o segundo produto foi vendido */
     private boolean prod_2_sold;

     /** Identificador único da mensagem de notificação */
     private int id;

     /** 
      * Construtor da estrutura NotificationVSCounter
      * 
      * @param id Identificador único da mensagem de notificação
      * @param clienteID ID do cliente que enviou a notificação
      * @param prod1 Primeiro produto que ser quer verificar se é vendido
      * @param prod2 Segundo produto que ser quer verificar se é vendido
      * @return Uma nova instância de NotificationVSCounter
      */
     public NotificationVSCounter(int id, int clienteID, String prod1, String prod2) {
          this.clienteID = clienteID;
          this.prod1 = prod1;
          this.prod2 = prod2;
          this.prod_1_sold = false;
          this.prod_2_sold = false;
          this.id = id;
     }

     /** 
      * Devolve o ID do cliente que enviou a notificação
      * 
      * @return ID do cliente
      */
     public int getClienteID() {
          return clienteID;
     }

     /** 
      * Devolve o primeiro produto que ser quer verificar se é vendido
      * 
      * @return Primeiro produto
      */
     public String getProd1() {
          return prod1;
     }

     /** 
      * Devolve o segundo produto que ser quer verificar se é vendido
      * 
      * @return Segundo produto
      */
     public String getProd2() {
          return prod2;
     }

     /** 
      * Devolve se o primeiro produto foi vendido
      * 
      * @return true se o primeiro produto foi vendido, false caso contrário
      */
     public boolean isProd_1_sold() {
          return prod_1_sold;
     }

     /** 
      * Devolve se o segundo produto foi vendido
      * 
      * @return true se o segundo produto foi vendido, false caso contrário
      */
     public boolean isProd_2_sold() {
          return prod_2_sold;
     }

     /** 
      * Devolve o identificador único da mensagem de notificação
      * 
      * @return Identificador único da mensagem de notificação
      */
     public int getId() {
          return id;
     }

     /**
      * Define que o primeiro produto foi vendido
      * 
      * @param prod_1_sold true se o primeiro produto foi vendido, false caso contrário
      */
     public void setProd_1_sold(boolean prod_1_sold) {
          this.prod_1_sold = prod_1_sold;
     }

     /**
      * Define que o segundo produto foi vendido
      * 
      * @param prod_2_sold true se o segundo produto foi vendido, false caso contrário
      */
     public void setProd_2_sold(boolean prod_2_sold) {
          this.prod_2_sold = prod_2_sold;
     }
}
