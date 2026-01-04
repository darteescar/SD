package utils.structs.client;

/** Interface para controlar as notificações que são enviadas por um cliente */
public interface NotificacaoListener {

    /** 
     * Método chamado quando uma notificação de vendas simultâneas é enviada
     */
    void notificacaoVSEnviada();

    /** 
     * Método chamado quando uma notificação de vendas consecutivas é enviada
     */
    void notificacaoVCEnviada();
}

