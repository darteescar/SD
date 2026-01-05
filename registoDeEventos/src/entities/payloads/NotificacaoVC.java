package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;

/** Guarda as informações de uma notificação de vendas consecutivas */
public class NotificacaoVC {

    /** Número de vendas consecutivas da notificação */
    private int n;

    /** 
     * Construtor parametrizado
     * 
     * @param n Número de vendas consecutivas da notificação
     * @return Uma nova instância de NotificaçãoVC
     */
    public NotificacaoVC(int n){
        this.n = n;
    }

    /** 
     * Devolve o número de entradas
     * 
     * @return Número de entradas
     */
    public int getN(){
        return this.n;
    }

    /** 
     * Devolve a representação em string da notificação
     * 
     * @return Representação em string da notificação
     */
    @Override
    public String toString(){
        return "N: " + this.n;
    }

    /** 
     * Serializa a notificação num array de bytes
     * 
     * @return Array de bytes representando a notificação
     * @throws IOException Se ocorrer um erro de I/O durante a serialização
     */
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.n);
        dos.flush();

        return baos.toByteArray(); 
    }

    /** 
     * Desserializa um array de bytes numa notificação
     * 
     * @param bytes Array de bytes a ser desserializado
     * @return Notificação desserializada
     * @throws IOException Se ocorrer um erro de I/O durante a desserialização
     * @throws ProtocolException Se os dados estiverem em um formato inválido
     */
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
