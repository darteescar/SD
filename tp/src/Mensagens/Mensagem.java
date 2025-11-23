package Mensagens;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import Data.Evento;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Mensagem {
     private TipoMensagem tipo_mensagem;
     static DateTimeFormatter formatador_data = DateTimeFormatter.ofPattern("yyyy-MM-dd");

     public Mensagem(TipoMensagem tipo_mensagem){
          this.tipo_mensagem = tipo_mensagem;
     }

     public TipoMensagem getTipoMensagem(){ return this.tipo_mensagem; }

     public void serialize(DataOutputStream out) throws IOException {
          out.writeInt(this.tipo_mensagem.ordinal());
     }

     public static Mensagem deserialize(DataInputStream in) throws IOException {
          int tipo_ordinal = in.readInt();
          TipoMensagem tipo_mensagem = TipoMensagem.values()[tipo_ordinal];
          if (tipo_mensagem == TipoMensagem.REGISTO_EVENTO){

               String nome_cliente = in.readUTF();
               String dia_str = in.readUTF();
               LocalDate dia = LocalDate.parse(dia_str, Mensagem.formatador_data);

               Evento evento = Evento.deserialize(in);
               return new MsgCliente(nome_cliente, tipo_mensagem, dia, evento);

          } else if (tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_1 ||
                     tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_2 ||
                     tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_3 ||
                     tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_4) {

               String nome_cliente = in.readUTF();
               String dia_str = in.readUTF();
               LocalDate dia = LocalDate.parse(dia_str, Mensagem.formatador_data);

               String nome_produto = in.readUTF();
               int diasAnteriores = in.readInt();
               return new MsgCliente(nome_cliente, tipo_mensagem, dia, nome_produto, diasAnteriores);

          } else if (tipo_mensagem == TipoMensagem.QUERY_FILTRO) {

               String nome_cliente = in.readUTF();
               String dia_str = in.readUTF();
               LocalDate dia = LocalDate.parse(dia_str, Mensagem.formatador_data);

               int size = in.readInt();
               ArrayList<String> produtos = new ArrayList<>();
               for (int i = 0; i < size; i++) {
                    produtos.add(in.readUTF());
               }
               int diaFiltro = in.readInt();
               return new MsgCliente(nome_cliente, tipo_mensagem, dia, produtos, diaFiltro);

          } else if (tipo_mensagem == TipoMensagem.LOGIN ||
                     tipo_mensagem == TipoMensagem.LOGOUT) {

               String nome_cliente = in.readUTF();
               String dia_str = in.readUTF();
               LocalDate dia = LocalDate.parse(dia_str, Mensagem.formatador_data);

               String password = in.readUTF();
               return new MsgCliente(nome_cliente, tipo_mensagem, dia, password);

          } else if (tipo_mensagem == TipoMensagem.QUERY_OCORRENCIAS_SIMULTANEAS) {

               String nome_cliente = in.readUTF();
               String dia_str = in.readUTF();
               LocalDate dia = LocalDate.parse(dia_str, Mensagem.formatador_data);

               String nome_produto1 = in.readUTF();
               String nome_produto2 = in.readUTF();
               return new MsgCliente(nome_cliente, tipo_mensagem, dia, nome_produto1, nome_produto2);

          } else if (tipo_mensagem == TipoMensagem.QUERY_OCORRENCIAS_CONSECUTIVAS) {

               String nome_cliente = in.readUTF();
               String dia_str = in.readUTF();
               LocalDate dia = LocalDate.parse(dia_str, Mensagem.formatador_data);
               
               int numOcorrencias = in.readInt();
               return new MsgCliente(nome_cliente, tipo_mensagem, dia, numOcorrencias);

          } else if (tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_1_RESPOSTA) {

               int quantidadeVendida = in.readInt();
               return new MsgResposta(tipo_mensagem, quantidadeVendida);

          } else if (tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_2_RESPOSTA ||
                     tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_3_RESPOSTA ||
                     tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_4_RESPOSTA) {

               double valor = in.readDouble();
               return new MsgResposta(tipo_mensagem, valor);

          } else if (tipo_mensagem == TipoMensagem.QUERY_FILTRO_RESPOSTA) {

               int size = in.readInt();
               ArrayList<Evento> eventos = new ArrayList<>();
               for (int i = 0; i < size; i++) {
                    eventos.add(Evento.deserialize(in));
               }
               return new MsgResposta(tipo_mensagem, eventos);

          } else if (tipo_mensagem == TipoMensagem.QUERY_OCORRENCIAS_SIMULTANEAS_RESPOSTA) {

               boolean ocorrenciasSimultaneas = in.readBoolean();
               return new MsgResposta(tipo_mensagem, ocorrenciasSimultaneas);

          } else if (tipo_mensagem == TipoMensagem.QUERY_OCORRENCIAS_CONSECUTIVAS_RESPOSTA) {

               boolean flag = in.readBoolean();
               if (flag) {
                    String produto = in.readUTF();
                    return new MsgResposta(tipo_mensagem, flag, produto);
               } else {
                    return new MsgResposta(tipo_mensagem, flag, null);
               }

          } else {
               return null;
               
          }
     }

     public Mensagem clone(){
          return new Mensagem(this.tipo_mensagem);
     }
}