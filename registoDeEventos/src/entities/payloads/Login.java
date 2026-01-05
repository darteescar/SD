package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;

/** Guarda as informações de um Login */
public class Login {

    /** Username do user */
    private String username;

    /** Password do user */
    private String password;

    /** Limite máximo para o comprimento do username e da password */
    private static final int MAX_USERNAME_LENGTH = 1_000;

    /** 
     * Construtor parametrizado
     * 
     * @param username Username do user
     * @param password Password do user
     * @return Uma nova instância de Login
     */
    public Login(String username, String password){
        this.username = username;
        this.password = password;
    }

    /** 
     * Devolve o username
     * 
     * @return Username
     */
    public String getUsername(){
        return this.username;
    }

    /** 
     * Devolve a password
     * 
     * @return Password
     */
    public String getPassword(){
        return this.password;
    }

    /** 
     * Representação em String do login
     * 
     * @return String representando o login
     */
    @Override
    public String toString(){
        return "Username: " + this.username + " , Password: " + this.getPassword();
    }
    
    /** 
     * Serializa o login num array de bytes
     * 
     * @return Array de bytes representando o login
     * @throws IOException Se ocorrer um erro de I/O durante a serialização
     */
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF(username);
        dos.writeUTF(password);
        dos.flush();

        return baos.toByteArray();  
    }

    /** 
     * Desserializa um array de bytes num login
     * 
     * @param bytes Array de bytes a ser desserializado
     * @return Login desserializado
     * @throws IOException Se ocorrer um erro de I/O durante a desserialização
     * @throws ProtocolException Se os dados estiverem num formato inválido
     */
    public static Login deserialize(byte[] bytes) throws IOException, ProtocolException{
            if (bytes == null) {
                throw new ProtocolException("Bytes nulos recebidos");
            }

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

            String username = dis.readUTF();
            if (username.length() > MAX_USERNAME_LENGTH) {
                throw new ProtocolException("Username muito longo: " + username.length());
            }
            String password = dis.readUTF();
            if (password.length() > MAX_USERNAME_LENGTH) {
                throw new ProtocolException("Password muito longa: " + password.length());
            }

            return new Login(username, password);
    }
}
