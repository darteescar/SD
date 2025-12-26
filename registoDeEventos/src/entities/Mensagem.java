package entities;

import entities.payloads.Agregacao;
import entities.payloads.Evento;
import entities.payloads.Filtrar;
import entities.payloads.Login;
import entities.payloads.NotificacaoVC;
import entities.payloads.NotificacaoVS;
import enums.TipoMsg;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ProtocolException;

public class Mensagem {
    private int id;
    private TipoMsg tipo;
    private byte[] data;
    private static final int MAX_MSG_SIZE = 10_000_000;

    public Mensagem(int id, TipoMsg tipo, byte[] data){
        this.id = id;
        this.tipo = tipo;
        this.data = data;
    }
    
    public int getID(){
        return this.id;
    }

    public TipoMsg getTipo(){
        return this.tipo;
    }

    public byte[] getData(){
        return this.data;
    }

    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeInt(id);
        dos.writeInt(this.tipo.ordinal());
        dos.writeInt(this.data.length);
        dos.write(this.data);
    }


    public static Mensagem deserialize(DataInputStream dis)
        throws IOException, ProtocolException {

        int id;
        try {
            id = dis.readInt();
        } catch (EOFException e) {
            // Cliente fechou a ligação
            throw e;
        }

        int tipoOrdinal = dis.readInt();
        if (tipoOrdinal < 0 || tipoOrdinal >= TipoMsg.values().length) {
            throw new ProtocolException("Tipo de mensagem inválido: " + tipoOrdinal);
        }
        TipoMsg tipo = TipoMsg.values()[tipoOrdinal];

        int length = dis.readInt();
        if (length < 0 || length > MAX_MSG_SIZE) {
            throw new ProtocolException("Tamanho inválido da mensagem: " + length);
        }

        byte[] data = new byte[length];
        dis.readFully(data);

        return new Mensagem(id, tipo, data);
    }

    @Override
    public String toString() {
        if (data == null) {
            return "Mensagem[ID=" + id + ", Tipo=" + tipo + ", Dados=nulos]";
        }

        try {
            switch (tipo) {
                case LOGIN -> {
                    Login login = Login.deserialize(data);
                    return "Mensagem[ID=" + id + ", Tipo=" + tipo + ", Conteudo=" + login + "]";
                }
                case REGISTA_LOGIN -> {
                    Login login = Login.deserialize(data);
                    return "Mensagem[ID=" + id + ", Tipo=" + tipo + ", Conteudo=" + login + "]";
                }
                case REGISTO -> {
                    Evento evento = Evento.deserialize(data);
                    return "Mensagem[ID=" + id + ", Tipo=" + tipo + ", Conteudo=" + evento + "]";
                }
                case QUANTIDADE_VENDAS, VOLUME_VENDAS, PRECO_MEDIO, PRECO_MAXIMO -> {
                    Agregacao agregacao = Agregacao.deserialize(data);
                    return "Mensagem[ID=" + id + ", Tipo=" + tipo + ", Conteudo=" + agregacao + "]";
                }
                case LISTA -> {
                    Filtrar filtrar = Filtrar.deserialize(data);
                    return "Mensagem[ID=" + id + ", Tipo=" + tipo + ", Conteudo=" + filtrar + "]";
                }
                case NOTIFICACAO_VC -> {
                    NotificacaoVC noti = NotificacaoVC.deserialize(data);
                    return "Mensagem[ID=" + id + ", Tipo=" + tipo + ", Conteudo=" + noti + "]";
                }
                case NOTIFICACAO_VS -> {
                    NotificacaoVS noti = NotificacaoVS.deserialize(data);
                    return "Mensagem[ID=" + id + ", Tipo=" + tipo + ", Conteudo=" + noti + "]";
                }
                default -> {
                    return "Mensagem[ID=" + id + ", Tipo=" + tipo + ", TamanhoDados=" + data.length + " bytes]";
                }
            }
        } catch (Exception e) {
            // Se houver problema na desserialização, mostra apenas o tamanho
            return "Mensagem[ID=" + id + ", Tipo=" + tipo + ", TamanhoDados=" + data.length + " bytes, ErroAoDesserializar]";
        }
    }

}
