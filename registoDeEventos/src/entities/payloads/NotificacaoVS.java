package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NotificacaoVS {
     private String produto_1;
     private String produto_2;

     public NotificacaoVS(String prod1, String prod2){
          this.produto_1 = prod1;
          this.produto_2 = prod2;
     }

     public NotificacaoVS(NotificacaoVS notificacaoVS){
          this.produto_1 = notificacaoVS.getProduto_1();
          this.produto_2 = notificacaoVS.getProduto_2();
     }

     public String getProduto_1(){
          return this.produto_1;
     }

     public String getProduto_2(){
          return this.produto_2;
     }

     @Override
     public String toString(){
          return "Produto 1: " + this.produto_1 + " , Produto 2: " + this.getProduto_2();
     }

     @Override
     public NotificacaoVS clone(){
          return new NotificacaoVS(this);
     }

     public byte[] serialize(){
          try {

               ByteArrayOutputStream baos = new ByteArrayOutputStream();
               DataOutputStream dos = new DataOutputStream(baos);

               dos.writeUTF(produto_1);
               dos.writeUTF(produto_2);
               dos.flush();
               
               return baos.toByteArray();
               
          } catch (IOException e) {
               e.printStackTrace();
               return null;
          }
     }

     public static NotificacaoVS deserialize(byte[] bytes){
          try {

               DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
               String prod1 = dis.readUTF();
               String prod2 = dis.readUTF();
               return new NotificacaoVS(prod1, prod2);

          } catch (IOException e) {
               e.printStackTrace();
               return null;
          }
     }
}
