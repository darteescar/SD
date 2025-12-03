package entities.requests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Agregacao {
    private String produto;
    private int dias;

    public Agregacao(String produto, int dias){
        this.produto = produto;
        this.dias = dias;
    }

    public Agregacao(Agregacao agregacao){
        this.produto = agregacao.getProduto();
        this.dias = agregacao.getDias();
    }

    public String getProduto(){
        return this.produto;
    }

    public int getDias(){
        return this.dias;
    }

    @Override
    public String toString(){
        return "Produto: " + this.produto + "Dias: " + this.dias;
    }

    @Override
    public Agregacao clone(){
        return new Agregacao(this);
    }

    public byte[] serialize() throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF(produto);
        dos.writeInt(dias);
        dos.flush();

        return baos.toByteArray();
    }

    public static Agregacao deserialize(byte[] bytes) throws IOException{
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

        String produto = dis.readUTF();
        int dias = dis.readInt();

        return new Agregacao(produto, dias);
    }
}
