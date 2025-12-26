package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;

public class NotificacaoVC {
    private int n;

    public NotificacaoVC(int n){
        this.n = n;
    }

    public NotificacaoVC(NotificacaoVC notificacaoVC){
        this.n = notificacaoVC.getN();
    }

    public int getN(){
        return this.n;
    }

    @Override
    public String toString(){
        return "N: " + this.n;
    }

    @Override
    public NotificacaoVC clone(){
        return new NotificacaoVC(this);
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.n);
        dos.flush();

        return baos.toByteArray(); 
    }

    public static NotificacaoVC deserialize(byte[] bytes) throws IOException, ProtocolException{
        if (bytes == null) {
            throw new IOException("Bytes nulos recebidos");
        }
    
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        
        int n = dis.readInt();
        if (n < 0) {
            throw new ProtocolException("Valor de n inválido: " + n);
        }

        return new NotificacaoVC(n);   
    }
}
