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

/** A Mensagem que é usada entre os clientes e o servidor */
public class Mensagem {

    /** ID da mensagem */
    private int id;

    /** Tipo da mensagem */
    private TipoMsg tipo;

    /** Dados da mensagem */
    private byte[] data;

    /** Tamanho máximo da mensagem */
    private static final int MAX_MSG_SIZE = 10_000_000;

    /** 
     * Construtor de Mensagem
     * 
     * @param id ID da mensagem
     * @param tipo Tipo da mensagem
     * @param data Dados da mensagem
     * @return A nova Mensagem
     */
    public Mensagem(int id, TipoMsg tipo, byte[] data){
        this.id = id;
        this.tipo = tipo;
        this.data = data;
    }

    // Construtor usado no ServerReader para criar mensagens de erro (caso a mensagem recebida seja inválida)
    // envia uma mensagem com tipo ERRO e o texto do erro como dados

    /** 
     * Construtor de Mensagem de Erro
     * 
     * @param id ID da mensagem
     * @param errorMsg Mensagem de erro
     * @return A nova Mensagem de Erro
     */
    public Mensagem(int id, String errorMsg) {
        this.id = id;
        this.tipo = TipoMsg.ERRO;
        this.data = errorMsg.getBytes();
    }
    
    /** 
     * Devolve o ID da mensagem
     * 
     * @return ID da mensagem
     */
    public int getID(){
        return this.id;
    }

    /** 
     * Devolve o Tipo da mensagem
     * 
     * @return Tipo da mensagem
     */
    public TipoMsg getTipo(){
        return this.tipo;
    }

    /** 
     * Devolve os Dados da mensagem
     * 
     * @return Dados da mensagem
     */
    public byte[] getData(){
        return this.data;
    }

    /** 
     * Serialização da mensagem para o DataOutputStream
     * 
     * @param dos DataOutputStream onde a mensagem será escrita
     * @throws IOException Se ocorrer um erro de I/O durante a escrita
     */
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeInt(id);
        dos.writeInt(this.tipo.ordinal());
        dos.writeInt(this.data.length);
        dos.write(this.data);
    }

    /** 
     * Desserialização da mensagem a partir do DataInputStream
     * altera o id passado por referência para, caso lance exceção, sabermos o id da mensagem inválida
     * e podermos enviar uma mensagem de erro com esse id
     * 
     * @param dis DataInputStream de onde a mensagem será lida
     * @return A Mensagem desserializada
     * @throws IOException Se ocorrer um erro de I/O durante a leitura
     * @throws MensagemCorrompidaException Se a mensagem estiver corrompida
     */
    public static Mensagem deserializeWithId(DataInputStream dis) 
        throws IOException, MensagemCorrompidaException {

        int id = -1;
        try {
            id = dis.readInt(); // lê o id primeiro

            int tipoOrdinal = dis.readInt();
            if (tipoOrdinal < 0 || tipoOrdinal >= TipoMsg.values().length) {
                throw new MensagemCorrompidaException(id, "Tipo de mensagem inválido: " + tipoOrdinal);
            }
            TipoMsg tipo = TipoMsg.values()[tipoOrdinal];

            int length = dis.readInt();
            if (length < 0 || length > MAX_MSG_SIZE) {
                throw new MensagemCorrompidaException(id, "Tamanho inválido da mensagem: " + length);
            }

            byte[] data = new byte[length];
            dis.readFully(data);

            return new Mensagem(id, tipo, data);

        } catch (ProtocolException e) {
            throw new MensagemCorrompidaException(id, e.getMessage());
        }
    }


    /** 
     * Desserialização da mensagem a partir do DataInputStream
     * 
     * @param dis DataInputStream de onde a mensagem será lida
     * @return A Mensagem desserializada
     * @throws IOException Se ocorrer um erro de I/O durante a leitura
     * @throws ProtocolException Se a mensagem estiver corrompida
     */
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

    /** 
     * Representação da Mensagem como uma String, desserializando os dados conforme o tipo
     * 
     * @return String representando a Mensagem
     */
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
