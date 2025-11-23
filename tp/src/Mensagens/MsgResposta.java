package Mensagens;

import Data.Evento;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;


public class MsgResposta extends Mensagem {
    //MsgInformacao1
    private int quantidadeVendida;

    public int getQuantidadeVendida() {
        return this.quantidadeVendida;
    }

    public MsgResposta(TipoMensagem tipo_mensagem, int quantidadeVendida){
        super(tipo_mensagem);
        this.quantidadeVendida = quantidadeVendida;
    }

    //MsgInformacao2,3,4
    private double valor;

    public double getValor() {
        return this.valor;
    }

    public MsgResposta(TipoMensagem tipo_mensagem, double valor){
        super(tipo_mensagem);
        this.valor = valor;
    }

    //MsgFiltro
    private List<Evento> eventos;

    public List<Evento> getEventos() {
        return this.eventos;
    }

    public MsgResposta(TipoMensagem tipo_mensagem, List<Evento> eventos){
        super(tipo_mensagem);
        this.eventos = eventos;
    }

    //MsgOcorrSimultaneas
    private boolean ocorrenciasSimultaneas;

    public boolean getOcorrenciasSimultaneas() {
        return this.ocorrenciasSimultaneas;
    }

    public MsgResposta(TipoMensagem tipo_mensagem, boolean ocorrenciasSimultaneas){
        super(tipo_mensagem);
        this.ocorrenciasSimultaneas = ocorrenciasSimultaneas;
    }

    //MsgOcorrConsecutivas
    private boolean flag;
    private String produto;

    public boolean getFlag() {
        return this.flag;
    }

    public String getProduto() {
        return this.produto;
    }

    public MsgResposta(TipoMensagem tipo_mensagem, boolean flag, String produto){
        super(tipo_mensagem);
        this.flag = flag;
        this.produto = produto;
    }

    //Serialize
    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        TipoMensagem tipo_mensagem = this.getTipoMensagem();

        if (tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_1_RESPOSTA) {

            out.writeInt(this.quantidadeVendida);

        } else if (tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_2_RESPOSTA ||
                   tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_3_RESPOSTA ||
                   tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_4_RESPOSTA) {

            out.writeDouble(this.valor);

        } else if (tipo_mensagem == TipoMensagem.QUERY_FILTRO_RESPOSTA) {

            out.writeInt(this.eventos.size());
            for (Evento evento : this.eventos) {
                evento.serialize(out);
            }

        } else if (tipo_mensagem == TipoMensagem.QUERY_OCORRENCIAS_SIMULTANEAS_RESPOSTA) {

            out.writeBoolean(this.ocorrenciasSimultaneas);

        } else if (tipo_mensagem == TipoMensagem.QUERY_OCORRENCIAS_CONSECUTIVAS_RESPOSTA) {
            
            if (flag == true){
                out.writeBoolean(this.flag);
                out.writeUTF(this.produto);
            } else {out.writeBoolean(this.flag);}
        }
    }
}
