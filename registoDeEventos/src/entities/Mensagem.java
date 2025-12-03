package entities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import enums.TipoMsg;

public class Mensagem {
    private TipoMsg tipo;
    private byte[] data;

    public Mensagem(TipoMsg tipo, byte[] data){
        this.tipo = tipo;
        this.data = data;
    }

    public TipoMsg getTipo(){
        return this.tipo;
    }

    public byte[] getData(){
        return this.data;
    }

    public void serialize(DataOutputStream dos) throws IOException{
        dos.writeInt(this.tipo.ordinal());
        dos.writeInt(this.data.length);
        dos.write(this.data);
    }

    public static Mensagem deserialize(DataInputStream dis) throws IOException{
        int tipoOrdinal = dis.readInt();
        TipoMsg tipo = TipoMsg.values()[tipoOrdinal];
        int length = dis.readInt();
        byte[] data = new byte[length];
        dis.readFully(data);
        return new Mensagem(tipo, data);
    }
}
