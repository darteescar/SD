package Mensagens;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class MsgOcorrConsecutiva extends Mensagem {
    private int numOcorrencias;

    public MsgOcorrConsecutiva(String nome_cliente, TipoMensagem tipo_mensagem, LocalDate dia, int numOcorrencias){
        super(nome_cliente, tipo_mensagem, dia);
        this.numOcorrencias = numOcorrencias;
    }

    public int getNumOcorrencias() {
        return this.numOcorrencias;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeInt(this.numOcorrencias);
    }
    
    @Override
    public MsgOcorrConsecutiva clone(){
        return new MsgOcorrConsecutiva(this.getNomeCliente(), this.getTipoMensagem(), this.getDia(), this.numOcorrencias);
    }
}
