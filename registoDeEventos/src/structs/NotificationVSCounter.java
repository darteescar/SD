package structs;

import java.net.Socket;

public class NotificationVSCounter {
     private final Socket socket;
     private final String prod1;
     private final String prod2;
     private boolean prod_1_sold;
     private boolean prod_2_sold;

     public NotificationVSCounter(Socket socket, String prod1, String prod2) {
          this.socket = socket;
          this.prod1 = prod1;
          this.prod2 = prod2;
          this.prod_1_sold = false;
          this.prod_2_sold = false;
     }

     public Socket getSocket() {
          return socket;
     }

     public String getProd1() {
          return prod1;
     }

     public String getProd2() {
          return prod2;
     }

     public boolean isProd_1_sold() {
          return prod_1_sold;
     }

     public void setProd_1_sold(boolean prod_1_sold) {
          this.prod_1_sold = prod_1_sold;
     }

     public boolean isProd_2_sold() {
          return prod_2_sold;
     }

     public void setProd_2_sold(boolean prod_2_sold) {
          this.prod_2_sold = prod_2_sold;
     }
}
