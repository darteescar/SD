package Mensagens;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class MsgLogin extends Mensagem {
     private String password;

     public MsgLogin(String nome_cliente, TipoMensagem tipo_mensagem, LocalDate dia, String password){
          super(nome_cliente, tipo_mensagem, dia);
          this.password = password;
     }

     public String getPassword() {
          return this.password;
     }

     @Override
     public void serialize(DataOutputStream out) throws IOException {
          super.serialize(out);
          out.writeUTF(this.password);
     }

     @Override
     public MsgLogin clone(){
          return new MsgLogin(this.getNomeCliente(), this.getTipoMensagem(), this.getDia(), this.password);
     }
    
}
