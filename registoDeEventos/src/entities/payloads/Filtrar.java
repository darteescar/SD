package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;

public class Filtrar {
    private List<String> produtos;
    private int dias;
    private static final int MAX_PRODUTO_LENGTH = 1_000; // limite arbitrário

    public Filtrar(List<String> produtos, int dias){
        this.produtos = new ArrayList<>(produtos);
        this.dias = dias;
    }

    public Filtrar(Filtrar filtrar){
        this.produtos = filtrar.getProdutos();
        this.dias = filtrar.getDias();
    }

    public List<String> getProdutos(){
        return new ArrayList<>(this.produtos);
    }

    public int getDias(){
        return this.dias;
    }

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

    @Override
    public Filtrar clone(){
        return new Filtrar(this);
    }

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