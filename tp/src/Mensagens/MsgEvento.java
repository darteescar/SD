package Mensagens;

import Data.Evento;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class MsgEvento extends Mensagem {
     private Evento evento;

     public MsgEvento(String nome_cliente, TipoMensagem tipo_mensagem, LocalDate dia, Evento evento){
          super(nome_cliente, tipo_mensagem, dia);
          this.evento = evento;
     }

     public Evento getEvento() {
          return this.evento;
     }

     @Override
     public void serialize(DataOutputStream out) throws IOException {
          super.serialize(out);
          this.evento.serialize(out);
     }

     @Override
     public MsgEvento clone(){
          return new MsgEvento(this.getNomeCliente(), this.getTipoMensagem(), this.getDia(), this.evento);
     }
}
