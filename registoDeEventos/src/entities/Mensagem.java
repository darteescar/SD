package entities;

import enums.TipoMsg;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Mensagem {
    private TipoMsg tipo_mensagem;
    private static DateTimeFormatter formatador_data = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private LocalDate data;

    private String idOrg;
    private String idDst;

    private byte[] payload;

    public Mensagem(TipoMsg tipo_mensagem, LocalDate data, String idOrg, String idDst, byte[] payload) {
        this.tipo_mensagem = tipo_mensagem;
        this.data = data;
        this.idOrg = idOrg;
        this.idDst = idDst;
        this.payload = payload;
    }

    public TipoMsg getTipoMensagem() {
        return this.tipo_mensagem;
    }

    public LocalDate getData() {
        return this.data;
    }

    public String getIdOrg() {
        return this.idOrg;
    }

    public String getIdDst() {
        return this.idDst;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(this.tipo_mensagem.ordinal());
        out.writeUTF(this.data.format(formatador_data));
        out.writeUTF(this.idOrg);
        out.writeUTF(this.idDst);
        out.writeInt(this.payload.length);
        out.write(this.payload);
    }

    public static Mensagem deserialize(DataInputStream in) throws IOException {
        int tipoOrdinal = in.readInt();
        LocalDate data = LocalDate.parse(in.readUTF(), formatador_data);
        String idOrg = in.readUTF();
        String idDst = in.readUTF();
        int len = in.readInt();
        byte[] payload = null;
        if (len > 0) {
            payload = new byte[len];
            in.readFully(payload);
        }
        TipoMsg tipo = TipoMsg.values()[tipoOrdinal];
        return new Mensagem(tipo, data, idOrg, idDst, payload);
    }

    public Mensagem clone(){
        return new Mensagem(this.tipo_mensagem, this.data, this.idOrg, this.idDst, this.payload);
    }

}
