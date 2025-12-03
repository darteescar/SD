package entities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Evento{
    private String produto;
    private int quantidade;
    private double preco;
    private String data;

    public Evento(){
        this.produto = "";
        this.quantidade = 0;
        this.preco = 0.0;
        this.data = "";
    }

    public Evento(String produto, int quantidade, double preco, String data){
        this.produto = produto;
        this.quantidade = quantidade;
        this.preco = preco;
        this.data = data;
    }

    public Evento(Evento evento){
        this.produto = evento.getProduto();
        this.quantidade = evento.getQuantidade();
        this.preco = evento.getPreco();
        this.data = evento.getData();
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

    public String getData(){
        return this.data;
    }

    @Override
    public String toString(){
        return "Produto: " + this.produto + ", Quantidade: " + this.quantidade + ", Preço: " + this.preco + ", Data: " + this.data;
    }

    @Override
    public Evento clone(){
        return new Evento(this);
    }
    
    public void serialize(DataOutputStream dos) throws IOException{   
        dos.writeUTF(this.produto);
        dos.writeInt(this.quantidade);
        dos.writeDouble(this.preco);
        dos.writeUTF(this.data);
    }

    public static Evento deserialize(DataInputStream dis) throws IOException{
        String produto = dis.readUTF();
        int quantidade = dis.readInt();
        double preco = dis.readDouble();
        String data = dis.readUTF();
        return new Evento(produto, quantidade, preco, data);
    }
}
