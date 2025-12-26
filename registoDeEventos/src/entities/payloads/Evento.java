package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;

public class Evento{
    private String produto;
    private int quantidade;
    private double preco;
    private static final int MAX_PRODUTO_LENGTH = 1_000; // limite arbitrário

    public Evento(String produto, int quantidade, double preco){
        this.produto = produto;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    public Evento(Evento evento){
        this.produto = evento.getProduto();
        this.quantidade = evento.getQuantidade();
        this.preco = evento.getPreco();
    }

    public String getProduto(){
        return this.produto;
    }

    public int getQuantidade(){
        return this.quantidade;
    }

    public double getPreco(){
        return this.preco;
    }

    @Override
    public String toString(){
        return "Produto: " + this.produto + ", Quantidade: " + this.quantidade + ", Preço: " + this.preco;
    }

    @Override
    public Evento clone(){
        return new Evento(this);
    }
  
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF(produto);
        dos.writeInt(quantidade);
        dos.writeDouble(preco);
        dos.flush();

        return baos.toByteArray();
    }

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
