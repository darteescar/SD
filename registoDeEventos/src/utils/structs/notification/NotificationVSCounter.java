package utils.structs.notification;

public class NotificationVSCounter {
     private final int clienteID;
     private final String prod1;
     private final String prod2;
     private boolean prod_1_sold;
     private boolean prod_2_sold;
     private int id;

     public NotificationVSCounter(int id, int clienteID, String prod1, String prod2) {
          this.clienteID = clienteID;
          this.prod1 = prod1;
          this.prod2 = prod2;
          this.prod_1_sold = false;
          this.prod_2_sold = false;
          this.id = id;
     }

     public int getClienteID() {
          return clienteID;
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

     public int getId() {
          return id;
     }

     public boolean isProd_2_sold() {
          return prod_2_sold;
     }

     public void setProd_1_sold(boolean prod_1_sold) {
          this.prod_1_sold = prod_1_sold;
     }

     public void setProd_2_sold(boolean prod_2_sold) {
          this.prod_2_sold = prod_2_sold;
     }
}
