package entities;

import enums.TipoMsg;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Mensagem {
    private int id;
    private TipoMsg tipo;
    private byte[] data;

    public Mensagem(int id, TipoMsg tipo, byte[] data){
        this.id = id;
        this.tipo = tipo;
        this.data = data;
    }
    
    public int getID(){
        return this.id;
    }

    public TipoMsg getTipo(){
        return this.tipo;
    }

    public byte[] getData(){
        return this.data;
    }

    public void serialize(DataOutputStream dos) {
        try {

            dos.writeInt(id);
            dos.writeInt(this.tipo.ordinal());
            dos.writeInt(this.data.length);
            dos.write(this.data);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Mensagem deserialize(DataInputStream dis) {
        try {

            int id = dis.readInt();
            int tipoOrdinal = dis.readInt();
            TipoMsg tipo = TipoMsg.values()[tipoOrdinal];
            int length = dis.readInt();
            byte[] data = new byte[length];
            dis.readFully(data);
            
            return new Mensagem(id, tipo, data);
            
        } catch (IOException e) {
            return null;
        }
    }
}
