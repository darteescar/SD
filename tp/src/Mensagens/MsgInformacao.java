package Mensagens;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class MsgInformacao extends Mensagem {
     private String nome_produto;
     private int diasAnteriores;

     public MsgInformacao(String nome_cliente, TipoMensagem tipo_mensagem, LocalDate dia, String nome_produto, int diasAnteriores){
          super(nome_cliente, tipo_mensagem, dia);
          this.nome_produto = nome_produto;
          this.diasAnteriores = diasAnteriores;
     }

     public String getNomeProduto() {
          return this.nome_produto;
     }

     public int getDiasAnteriores() {
          return this.diasAnteriores;
     }

     @Override
     public void serialize(DataOutputStream out) throws IOException {
          super.serialize(out);
          out.writeUTF(this.nome_produto);
          out.writeInt(this.diasAnteriores);
     }

     @Override
     public MsgInformacao clone(){
          return new MsgInformacao(this.getNomeCliente(), this.getTipoMensagem(), this.getDia(), this.nome_produto, this.diasAnteriores);
     }
}