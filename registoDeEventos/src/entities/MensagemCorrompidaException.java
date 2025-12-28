package entities;

import java.net.ProtocolException;

public class MensagemCorrompidaException extends ProtocolException {
    private final int id;

    public MensagemCorrompidaException(int id, String msg) {
        super(msg);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
