package entities;

import java.net.ProtocolException;

/** Exceção lançada quando uma Mensagem está corrompida */
public class MensagemCorrompidaException extends ProtocolException {

    /** ID da mensagem corrompida */
    private final int id;

    /** Construtor da exceção com o ID da mensagem corrompida e a mensagem de erro */
    public MensagemCorrompidaException(int id, String msg) {
        super(msg);
        this.id = id;
    }

    /** Devolve o ID da mensagem corrompida */
    public int getId() {
        return id;
    }
}
