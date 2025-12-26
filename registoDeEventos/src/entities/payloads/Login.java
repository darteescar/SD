package entities.payloads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;

public class Login {
    private String username;
    private String password;
    private static final int MAX_USERNAME_LENGTH = 1_000; // limite arbitrário

    public Login(String username, String password){
        this.username = username;
        this.password = password;
    }

    public Login(Login login){
        this.username = login.getUsername();
        this.password = login.getPassword();
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    @Override
    public String toString(){
        return "Username: " + this.username + " , Password: " + this.getPassword();
    }

    @Override
    public Login clone(){
        return new Login(this);
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF(username);
        dos.writeUTF(password);
        dos.flush();

        return baos.toByteArray();  
    }

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
