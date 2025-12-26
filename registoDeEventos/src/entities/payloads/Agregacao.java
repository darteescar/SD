package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;

public class Agregacao {
    private String produto;
    private int dias;
    private static final int MAX_PRODUTO_LENGTH = 1_000; // limite arbitrário

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
        return "Produto: " + this.produto + " ,Dias: " + this.dias;
    }

    @Override
    public Agregacao clone(){
        return new Agregacao(this);
    }

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
