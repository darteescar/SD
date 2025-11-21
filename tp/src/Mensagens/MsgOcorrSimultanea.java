package Mensagens;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class MsgOcorrSimultanea extends Mensagem {
     private String nome_produto1;
     private String nome_produto2;

     public MsgOcorrSimultanea(String nome_cliente, TipoMensagem tipo_mensagem, LocalDate dia, String nome_produto1, String nome_produto2){
          super(nome_cliente, tipo_mensagem, dia);
          this.nome_produto1 = nome_produto1;
          this.nome_produto2 = nome_produto2;
     }

     public String getNomeProduto1() {
          return this.nome_produto1;
     }

     public String getNomeProduto2() {
          return this.nome_produto2;
     }

     @Override
     public void serialize(DataOutputStream out) throws IOException {
          super.serialize(out);
          out.writeUTF(this.nome_produto1);
          out.writeUTF(this.nome_produto2);
     }

     @Override
     public MsgOcorrSimultanea clone(){
          return new MsgOcorrSimultanea(this.getNomeCliente(), this.getTipoMensagem(), this.getDia(), this.nome_produto1, this.nome_produto2);
     }

}