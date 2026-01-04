package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;

/** Guarda as informações de uma agregação */
public class Agregacao {

    /** Nome do produto */
    private String produto;

    /** Número de dias para a agregação */
    private int dias;

    /** Limite máximo para o comprimento do nome do produto */
    private static final int MAX_PRODUTO_LENGTH = 1_000;

    /** 
     * Construtor parametrizado
     * 
     * @param produto Nome do produto
     * @param dias Número de dias para a agregação
     * @return Uma nova instância de Agregação
     */
    public Agregacao(String produto, int dias){
        this.produto = produto;
        this.dias = dias;
    }

    /** 
     * Construtor de cópia
     * 
     * @param agregacao Agregação a ser copiada
     * @return Uma nova instância de Agregação
     */
    public Agregacao(Agregacao agregacao){
        this.produto = agregacao.getProduto();
        this.dias = agregacao.getDias();
    }

    /** 
     * Devolve o nome do produto
     * 
     * @return Nome do produto
     */
    public String getProduto(){
        return this.produto;
    }

    /** 
     * Devolve o número de dias
     * 
     * @return Número de dias
     */
    public int getDias(){
        return this.dias;
    }

    /** 
     * Representação em String da agregação
     * 
     * @return String representando a agregação
     */
    @Override
    public String toString(){
        return "Produto: " + this.produto + " ,Dias: " + this.dias;
    }

    /** 
     * Cria uma cópia da agregação
     * 
     * @return Cópia da agregação
     */
    @Override
    public Agregacao clone(){
        return new Agregacao(this);
    }

    /** 
     * Serializa a agregação em um array de bytes
     * 
     * @return Array de bytes representando a agregação
     */
    public byte[] serialize() {
        try {
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeUTF(produto);
            dos.writeInt(dias);
            dos.flush();

            return baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** 
     * Desserializa um array de bytes numa agregação
     * 
     * @param bytes Array de bytes a ser desserializado
     * @return Agregação desserializada
     * @throws IOException Se ocorrer um erro de I/O durante a desserialização
     * @throws ProtocolException Se os dados estiverem num formato inválido
     */
    public static Agregacao deserialize(byte[] bytes) throws IOException, ProtocolException {
        if (bytes == null) {
            throw new ProtocolException("Bytes nulos recebidos");
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);

        String produto = dis.readUTF();
        if (produto.length() > MAX_PRODUTO_LENGTH) {
            throw new ProtocolException("Nome do produto muito longo: " + produto.length());
        }
        int dias = dis.readInt();
        if (dias < 0) {
            throw new ProtocolException("Número de dias inválido: " + dias);
        }

        return new Agregacao(produto, dias);   
    }
}
