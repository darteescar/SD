package Mensagens;

import java.util.List;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class MsgFiltro extends Mensagem {
     private List<String> produtos;
     private int diaFiltro;

     public MsgFiltro(String nome_cliente, TipoMensagem tipo_mensagem, LocalDate dia, List<String> produtos, int diaFiltro){
          super(nome_cliente, tipo_mensagem, dia);
          this.produtos = produtos;
          this.diaFiltro = diaFiltro;
     }

     public List<String> getProdutos() {
          return this.produtos;
     }

     public int getDiaFiltro() {
          return this.diaFiltro;
     }

     @Override
     public void serialize(DataOutputStream out) throws IOException {
          super.serialize(out);
          out.writeInt(this.produtos.size());
          for(String produto : this.produtos){
               out.writeUTF(produto);
          }
          out.writeInt(this.diaFiltro);
     }

     @Override
     public MsgFiltro clone(){
          return new MsgFiltro(this.getNomeCliente(), this.getTipoMensagem(), this.getDia(), this.produtos, this.diaFiltro);
     }

}