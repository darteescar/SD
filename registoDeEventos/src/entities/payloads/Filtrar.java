package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Filtrar {
    private List<String> produtos;
    private int dias;

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

    public byte[] serialize() throws IOException{
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

    public static Filtrar deserialize(byte[] bytes) throws IOException{
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

        int size = dis.readInt();
        List<String> produtos = new ArrayList<>();
        for(int i = 0; i < size; i++){
            produtos.add(dis.readUTF());
        }
        int dias = dis.readInt();

        return new Filtrar(produtos, dias);
    }
}