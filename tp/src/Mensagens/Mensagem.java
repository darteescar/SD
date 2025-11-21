package Mensagens;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import Data.Evento;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Mensagem {
     private String nome_cliente;
     private TipoMensagem tipo_mensagem;
     private LocalDate dia; // dia em que o cliente envia a mensagem
     static DateTimeFormatter formatador_data = DateTimeFormatter.ofPattern("yyyy-MM-dd");

     public Mensagem(String nome_cliente, TipoMensagem tipo_mensagem, LocalDate dia){
          this.nome_cliente = nome_cliente;
          this.tipo_mensagem = tipo_mensagem;
          this.dia = dia;
     }

     public String getNomeCliente(){ return this.nome_cliente; }
     public TipoMensagem getTipoMensagem(){ return this.tipo_mensagem; }
     public LocalDate getDia(){ return this.dia; }

     public void serialize(DataOutputStream out) throws IOException {
          out.writeUTF(this.nome_cliente);
          out.writeInt(this.tipo_mensagem.ordinal());
          String dia_str = this.dia.format(Mensagem.formatador_data);
          out.writeUTF(dia_str);
     }

     public static Mensagem deserialize(DataInputStream in) throws IOException {
          String nome_cliente = in.readUTF();
          int tipo_mensagem_ordinal = in.readInt();
          TipoMensagem tipo_mensagem = TipoMensagem.values()[tipo_mensagem_ordinal];
          String dia_str = in.readUTF();
          LocalDate dia = LocalDate.parse(dia_str, Mensagem.formatador_data);
          
          if (tipo_mensagem == TipoMensagem.REGISTO_EVENTO) {
               Evento evento = Evento.deserialize(in);

               return new MsgEvento(nome_cliente, tipo_mensagem, dia, evento);
          } else if (tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_1 ||
                              tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_2 ||
                              tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_3 ||
                              tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_4) {

               String nome_produto = in.readUTF();
               int diasAnteriores = in.readInt();

               return new MsgInformacao(nome_cliente, tipo_mensagem, dia, nome_produto, diasAnteriores);
          } else if (tipo_mensagem == TipoMensagem.QUERY_FILTRO) {

               int size = in.readInt();
               ArrayList<String> produtos = new ArrayList<>();
               for (int i = 0; i < size; i++) {
                    produtos.add(in.readUTF());
               }
               int diaFiltro = in.readInt();

               return new MsgFiltro(nome_cliente, tipo_mensagem, dia, produtos, diaFiltro);
          } else if (tipo_mensagem == TipoMensagem.QUERY_OCORRENCIAS_SIMULTANEAS) {

               String p1 = in.readUTF();
               String p2 = in.readUTF();

               return new MsgOcorrSimultanea(nome_cliente, tipo_mensagem, dia, p1, p2);
          } else if (tipo_mensagem == TipoMensagem.QUERY_OCORRENCIAS_CONSECUTIVAS) {
               
               int n = in.readInt();

               return new MsgOcorrConsecutiva(nome_cliente, tipo_mensagem, dia, n);
          } else {
               return null;// Tipo de mensagem desconhecido
          }
          
     }

     public Mensagem clone(){
          return new Mensagem(this.nome_cliente, this.tipo_mensagem, this.dia);
     }
}