package entities.requests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Login {
    private String username;
    private String password;

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
        return "Username: " + this.username + "Password: " + this.getPassword();
    }

    @Override
    public Login clone(){
        return new Login(this);
    }

    public byte[] serialize() throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF(username);
        dos.writeUTF(password);
        dos.flush();

        return baos.toByteArray();
    }

    public static Login deserialize(byte[] bytes) throws IOException{
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

        String username = dis.readUTF();
        String password = dis.readUTF();

        return new Login(username, password);
    }
}
