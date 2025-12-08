package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Evento{
    private String produto;
    private int quantidade;
    private double preco;

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
    
    public byte[] serialize() {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeUTF(this.produto);
            dos.writeInt(this.quantidade);
            dos.writeDouble(this.preco);
            dos.flush();

            return baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Evento deserialize(byte[] bytes){
        try {

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

            String produto = dis.readUTF();
            int quantidade = dis.readInt();
            double preco = dis.readDouble();
            return new Evento(produto, quantidade, preco);   
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
