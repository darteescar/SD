package Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Evento {
     private String nome_produto;
     private int quantidade;
     private float preço_venda;
     private LocalDateTime timestamp; // ?????? nao sei se é este tipo de dado
     static DateTimeFormatter formatador_timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

     public Evento(String nome_produto, int quantidade, float preço_venda, LocalDateTime timestamp){
          this.nome_produto = nome_produto;
          this.quantidade = quantidade;
          this.preço_venda = preço_venda;
          this.timestamp = timestamp;
     }

     public String getNomeProduto(){ return this.nome_produto; }
     public int getQuantidade(){ return this.quantidade; }
     public float getPrecoVenda(){ return this.preço_venda; }
     public LocalDateTime getTimestamp(){ return this.timestamp; }


     public void serialize(DataOutputStream out) throws IOException {
          out.writeUTF(this.nome_produto);
          out.writeInt(this.quantidade);
          out.writeFloat(this.preço_venda);
          out.writeUTF(timestamp.format(Evento.formatador_timestamp));
     }

     public static Evento deserialize(DataInputStream in) throws IOException {
          String nome_produto = in.readUTF();
          int quantidade = in.readInt();
          float preco_venda = in.readFloat();
          String timestamp_str = in.readUTF();
          LocalDateTime timestamp = LocalDateTime.parse(timestamp_str,Evento.formatador_timestamp);
          return new Evento(nome_produto,quantidade,preco_venda,timestamp);
     }


}