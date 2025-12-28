package utils.structs.notification;

public class NotificationVCCounter {
     private static String produto = "";
     private final int clienteID;
     private int counter;
     private int number;
     private int id;

     public NotificationVCCounter(int id ,int clienteID, int number) {
          this.clienteID = clienteID;
          this.counter = 1;
          this.number = number;
          this.id = id;
     }

     // GETTERS
     public static String getProduto() {
          return produto;
     }

     public int getClienteID() {
          return clienteID;
     }

     public int getCounter() {
          return counter;
     }

     public int getN() {
          return number;
     }

     public int getId() {
          return id;
     }

     // SETTERS

     public static void setProduto(String produto) {
          NotificationVCCounter.produto = produto;
     }

     public void incrementCounter() {
          this.counter++;
     }

     public void resetCounter() {
          this.counter = 1;
     }

}
