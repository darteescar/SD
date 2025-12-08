package structs;

import java.net.Socket;

public class NotificationVCCounter {
     private static String produto = "";
     private Socket socket;
     private int counter;
     private int number;

     public NotificationVCCounter(Socket socket, int number) {
          this.socket = socket;
          this.counter = 0;
          this.number = number;
     }

     // GETTERS
     public static String getProduto() {
          return produto;
     }

     public Socket getSocket() {
          return socket;
     }

     public int getCounter() {
          return counter;
     }

     public int getN() {
          return number;
     }

     // SETTERS

     public static void setProduto(String produto) {
          NotificationVCCounter.produto = produto;
     }

     public void incrementCounter() {
          this.counter++;
     }

     public void resetCounter() {
          this.counter = 0;
     }

}
