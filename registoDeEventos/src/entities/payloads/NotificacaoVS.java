package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;

/** Guarda as informações de uma notificação vendas simultâneas */
public class NotificacaoVS {

     /** Produto 1 */
     private String produto_1;

     /** Produto 2 */
     private String produto_2;

     /** Limite máximo para o comprimento do nome do produto */
     private static final int MAX_PRODUTO_LENGTH = 1_000;

     /** Construtor parametrizado
      * 
      * @param prod1 Produto 1
      * @param prod2 Produto 2
      * @return Uma nova instância de NotificacaoVS
      */
     public NotificacaoVS(String prod1, String prod2){
          this.produto_1 = prod1;
          this.produto_2 = prod2;
     }

     /** Construtor de cópia
      * 
      * @param notificacaoVS Notificação a ser copiada
      * @return Uma nova instância de NotificacaoVS
      */
     public NotificacaoVS(NotificacaoVS notificacaoVS){
          this.produto_1 = notificacaoVS.getProduto_1();
          this.produto_2 = notificacaoVS.getProduto_2();
     }

     /** 
      * Devolve o produto 1
      * 
      * @return Produto 1
      */
     public String getProduto_1(){
          return this.produto_1;
     }

     /** 
      * Devolve o produto 2
      * 
      * @return Produto 2
      */
     public String getProduto_2(){
          return this.produto_2;
     }

     /** 
     * Representação em String da notificação
     * @return String representando a notificação
     */
     @Override
     public String toString(){
          return "Produto 1: " + this.produto_1 + " , Produto 2: " + this.getProduto_2();
     }

     /** 
      * Serializa a notificação em um array de bytes
      * 
      * @return Array de bytes representando a notificação
      * @throws IOException Se ocorrer um erro de I/O durante a serialização
      */
     public byte[] serialize() throws IOException {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          DataOutputStream dos = new DataOutputStream(baos);

          dos.writeUTF(produto_1);
          dos.writeUTF(produto_2);
          dos.flush();
          
          return baos.toByteArray();
     }

     /** 
      * Desserializa um array de bytes numa notificação
      * 
      * @param bytes Array de bytes a ser desserializado
      * @return Notificação desserializada
      * @throws IOException Se ocorrer um erro de I/O durante a desserialização
      * @throws ProtocolException Se os dados estiverem num formato inválido
      */
     public static NotificacaoVS deserialize(byte[] bytes) throws IOException, ProtocolException{

          DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
          String prod1 = dis.readUTF();
          if (prod1.length() > MAX_PRODUTO_LENGTH) {
               throw new ProtocolException("Produto 1 muito longo: " + prod1.length());
          }
          String prod2 = dis.readUTF();
          if (prod2.length() > MAX_PRODUTO_LENGTH) {
               throw new ProtocolException("Produto 2 muito longo: " + prod2.length());
          }
          return new NotificacaoVS(prod1, prod2);
     }
}
