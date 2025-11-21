package Data;

public class Login {
    public String nome; // assumo que é único
    public String password;

    public Login(String nome, String password) {
        this.nome = nome;
        this.password = password;
    }

    public String getNome() {
        return nome;
    }

    public String getPassword() {
        return password;
    }

    public Login clone() {
        return new Login(this.nome, this.password);
    }
}
