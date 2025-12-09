package structs.notification;

import structs.server.ClientContext;

public class NotificationVCCounter {
     private static String produto = "";
     private ClientContext context;
     private int counter;
     private int number;
     private int id;

     public NotificationVCCounter(int id ,ClientContext context, int number) {
          this.context = context;
          this.counter = 1;
          this.number = number;
          this.id = id;
     }

     // GETTERS
     public static String getProduto() {
          return produto;
     }

     public ClientContext getContext() {
          return context;
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
