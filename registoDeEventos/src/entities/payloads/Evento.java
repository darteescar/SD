package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;

/** Guarda as informações de um evento */
public class Evento{

    /** Nome do produto */
    private String produto;

    /** Quantidade do produto */
    private int quantidade;

    /** Preço do produto */
    private double preco;

    /** Limite máximo para o comprimento do nome do produto */
    private static final int MAX_PRODUTO_LENGTH = 1_000;

    /** 
     * Construtor parametrizado
     * 
     * @param produto Nome do produto
     * @param quantidade Quantidade do produto
     * @param preco Preço do produto
     * @return Uma nova instância de Evento
     */
    public Evento(String produto, int quantidade, double preco){
        this.produto = produto;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    /** 
     * Construtor de cópia
     * 
     * @param evento Evento a ser copiado
     * @return Uma nova instância de Evento
     */
    public Evento(Evento evento){
        this.produto = evento.getProduto();
        this.quantidade = evento.getQuantidade();
        this.preco = evento.getPreco();
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
     * Devolve a quantidade do produto
     * 
     * @return Quantidade do produto
     */
    public int getQuantidade(){
        return this.quantidade;
    }

    /** 
     * Devolve o preço do produto
     * 
     * @return Preço do produto
     */
    public double getPreco(){
        return this.preco;
    }

    /** 
     * Representação em String do evento
     * 
     * @return String representando o evento
     */
    @Override
    public String toString(){
        return "Produto: " + this.produto + ", Quantidade: " + this.quantidade + ", Preço: " + this.preco;
    }
  
    /** 
     * Serializa o evento em um array de bytes
     * 
     * @return Array de bytes representando o evento
     * @throws IOException Se ocorrer um erro de I/O durante a serialização
     */
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF(produto);
        dos.writeInt(quantidade);
        dos.writeDouble(preco);
        dos.flush();

        return baos.toByteArray();
    }

    /** 
     * Desserializa um array de bytes num evento
     * 
     * @param bytes Array de bytes a ser desserializado
     * @return Evento desserializado
     * @throws IOException Se ocorrer um erro de I/O durante a desserialização
     * @throws ProtocolException Se os dados estiverem num formato inválido
     */
    public static Evento deserialize(byte[] bytes) throws IOException, ProtocolException {
        if (bytes == null) {
            throw new ProtocolException("Bytes nulos recebidos");
        }

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

        String produto = dis.readUTF();
        if (produto.length() > MAX_PRODUTO_LENGTH) {
            throw new ProtocolException("Nome do produto muito longo: " + produto.length());
        }

        int quantidade = dis.readInt();
        if (quantidade < 0) {
            throw new ProtocolException("Quantidade negativa inválida: " + quantidade);
        }

        double preco = dis.readDouble();
        if (preco < 0) {
            throw new ProtocolException("Preço negativo inválido: " + preco);
        }

        return new Evento(produto, quantidade, preco);
    }
}
