package enums;

/** Tipo de uma Mensagem */
public enum TipoMsg {

    /** Mensagem de login */
    LOGIN,

    /** Mensagem de registo de um user */
    REGISTA_LOGIN,

    /** Mensagem de registo de um evento */
    REGISTO,

    /** Mensagem de agregação de eventos */
    QUANTIDADE_VENDAS,

    /** Mensagem de agregação do volume de vendas */
    VOLUME_VENDAS,

    /** Mensagem de agregação do preço médio */
    PRECO_MEDIO,

    /** Mensagem de agregação do preço máximo */
    PRECO_MAXIMO,

    /** Mensagem para o filtro de eventos se uma série */
    LISTA,

    /** Mensagem de notificação de vendas concorrentes */
    NOTIFICACAO_VC,

    /** Mensagem de notificação de vendas simultâneas */
    NOTIFICACAO_VS,

    /** Mensagem de resposta que o servidor envia ao cliente */
    RESPOSTA,

    /** Mensagem que o ServerReader usa para sinalizar o ServerWriter de que o Cliente terminou */
    POISON_PILL,

    /** Mensagem de erro */
    ERRO
}
