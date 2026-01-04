package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;

/** Guarda as informações para filtrar eventos */
public class Filtrar {

    /** Lista de produtos para filtrar */
    private List<String> produtos;

    /** Número de dias para o filtro */
    private int dias;

    /** Limite máximo para o comprimento do nome do produto */
    private static final int MAX_PRODUTO_LENGTH = 1_000;

    /** 
     * Construtor parametrizado
     * 
     * @param produtos Lista de produtos para filtrar
     * @param dias Número de dias para o filtro
     * @return Uma nova instância de Filtrar
     */
    public Filtrar(List<String> produtos, int dias){
        this.produtos = new ArrayList<>(produtos);
        this.dias = dias;
    }

    /** 
     * Construtor de cópia
     * 
     * @param filtrar Filtrar a ser copiado
     * @return Uma nova instância de Filtrar
     */
    public Filtrar(Filtrar filtrar){
        this.produtos = filtrar.getProdutos();
        this.dias = filtrar.getDias();
    }

    /** 
     * Devolve a lista de produtos
     * 
     * @return Lista de produtos
     */
    public List<String> getProdutos(){
        return new ArrayList<>(this.produtos);
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
     * Representação em String do filtro
     * 
     * @return String representando o filtro
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Produtos: ");
        for(String produto : this.produtos){
            sb.append(produto).append(", ");
        }
        sb.append("Dias: ").append(this.dias);
        return sb.toString();
    }

    /** 
     * Cria uma cópia do filtro
     * 
     * @return Cópia do filtro
     */
    @Override
    public Filtrar clone(){
        return new Filtrar(this);
    }

    /** 
     * Serializa o filtro em um array de bytes
     * 
     * @return Array de bytes representando o filtro
     * @throws IOException Se ocorrer um erro de I/O durante a serialização
     */
    public byte[] serialize() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(produtos.size());
            for(String produto : produtos){
                dos.writeUTF(produto);
            }
            dos.writeInt(dias);
            dos.flush();

            return baos.toByteArray();
    }

    /** 
     * Desserializa um array de bytes num filtro
     * 
     * @param bytes Array de bytes a ser desserializado
     * @return Filtro desserializado
     * @throws IOException Se ocorrer um erro de I/O durante a desserialização
     * @throws ProtocolException Se os dados estiverem numformato inválido
     */
    public static Filtrar deserialize(byte[] bytes) throws IOException, ProtocolException {
        if (bytes == null) {
            throw new ProtocolException("Bytes nulos recebidos");
        }

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

        int size = dis.readInt();
        if (size < 0) {
            throw new ProtocolException("Quantidade negativa inválida: " + size);
        }

        List<String> produtos = new ArrayList<>();
        for(int i = 0; i < size; i++){
            String produto = dis.readUTF();
            if (produto.length() > MAX_PRODUTO_LENGTH) { // limite arbitrário
                throw new ProtocolException("Nome do produto muito longo: " + produto.length());
            }
            produtos.add(produto);
        }
        int dias = dis.readInt();
        if (dias < 0) {
            throw new ProtocolException("Número de dias negativo inválido: " + dias);
        }

        return new Filtrar(produtos, dias);
    }
}