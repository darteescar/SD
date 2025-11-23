package Mensagens;

import java.time.LocalDate;

import Data.Evento;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MsgCliente extends Mensagem {
    private String nome_cliente;
    private LocalDate dia;

    public String getNomeCliente(){ return this.nome_cliente; }
    public LocalDate getDia(){ return this.dia; }

    //MsgEvento
    private Evento evento;

    public MsgCliente(String nome_cliente, TipoMensagem tipo_mensagem, LocalDate dia, Evento evento){
        super(tipo_mensagem);
        this.nome_cliente = nome_cliente;
        this.dia = dia;
        this.evento = evento;
    }

    public Evento getEvento() {
        return this.evento;
    }

    //MsgLogin
    private String password;

    public MsgCliente(String nome_cliente, TipoMensagem tipo_mensagem, LocalDate dia, String password){
        super(tipo_mensagem);
        this.nome_cliente = nome_cliente;
        this.dia = dia;
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    //MsgOcorrSimultanea
    private String nome_produto1;
    private String nome_produto2;

    public MsgCliente(String nome_cliente, TipoMensagem tipo_mensagem, LocalDate dia, String nome_produto1, String nome_produto2){
        super(tipo_mensagem);
        this.nome_cliente = nome_cliente;
        this.dia = dia;
        this.nome_produto1 = nome_produto1;
        this.nome_produto2 = nome_produto2;
    }

    public String getNomeProduto1() {
        return this.nome_produto1;
    }

    public String getNomeProduto2() {
        return this.nome_produto2;
    }

    //MsgOcorrConsecutiva
    private int numOcorrencias;

    public MsgCliente(String nome_cliente, TipoMensagem tipo_mensagem, LocalDate dia, int numOcorrencias){
        super(tipo_mensagem);
        this.nome_cliente = nome_cliente;
        this.dia = dia;
        this.numOcorrencias = numOcorrencias;
    }

    public int getNumOcorrencias() {
        return this.numOcorrencias;
    }
    
    //MsgInformacao
    private String nome_produto;
    private int diasAnteriores;

    public MsgCliente(String nome_cliente, TipoMensagem tipo_mensagem, LocalDate dia, String nome_produto, int diasAnteriores){
        super(tipo_mensagem);
        this.nome_cliente = nome_cliente;
        this.dia = dia;
        this.nome_produto = nome_produto;
        this.diasAnteriores = diasAnteriores;
    }

    public String getNomeProduto() {
        return this.nome_produto;
    }

    public int getDiasAnteriores() {
        return this.diasAnteriores;
    }

    //MsgFiltro
    private ArrayList<String> produtos;
    private int diaFiltro;

    public MsgCliente(String nome_cliente, TipoMensagem tipo_mensagem, LocalDate dia, ArrayList<String> produtos, int diaFiltro){
        super(tipo_mensagem);
        this.nome_cliente = nome_cliente;
        this.dia = dia;
        this.produtos = produtos;
        this.diaFiltro = diaFiltro;
    }

    public ArrayList<String> getProdutos() {
        return this.produtos;
    }

    public int getDiaFiltro() {
        return this.diaFiltro;
    }

    //Serialize
    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeUTF(this.nome_cliente);
        String dia_str = this.dia.format(Mensagem.formatador_data);
        out.writeUTF(dia_str);

        TipoMensagem tipo_mensagem = this.getTipoMensagem();

        if (tipo_mensagem == TipoMensagem.REGISTO_EVENTO) {
            this.evento.serialize(out);

        } else if (tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_1 ||
                            tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_2 ||
                            tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_3 ||
                            tipo_mensagem == TipoMensagem.QUERY_INFORMACAO_4) {

            out.writeUTF(this.nome_produto);
            out.writeInt(this.diasAnteriores);

        } else if (tipo_mensagem == TipoMensagem.QUERY_FILTRO) {

            out.writeInt(this.produtos.size());
            for(String produto : this.produtos){
                out.writeUTF(produto);
            }
            out.writeInt(this.diaFiltro);

        } else if (tipo_mensagem == TipoMensagem.QUERY_OCORRENCIAS_SIMULTANEAS) {

            out.writeUTF(this.nome_produto1);
            out.writeUTF(this.nome_produto2);

        } else if (tipo_mensagem == TipoMensagem.QUERY_OCORRENCIAS_CONSECUTIVAS) {
            
            out.writeInt(this.numOcorrencias);

        } else if (tipo_mensagem == TipoMensagem.LOGIN) {
            
            out.writeUTF(this.password);
       
        }
    }

    
}
