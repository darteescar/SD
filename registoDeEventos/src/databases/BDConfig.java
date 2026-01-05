package databases;

// Devolve a password de um utilizador/** Configurações da base de dados */
public class BDConfig {

    /** Nome de utilizador da base de dados */
    static final String USERNAME = "me";

    /** Password da base de dados */
    static final String PASSWORD = "mypass";

    /** Nome da base de dados */
    private static final String DATABASE = "base_dados_sd";

    /** Driver JDBC da base de dados */
    private static final String DRIVER = "jdbc:mariadb";

    /** URL de ligação à base de dados */
    static final String URL = DRIVER + "://localhost:3306/" + DATABASE;
}

/*
Para a Base de Dados funcionar é necessário ter a Mariadb instalada.
Feito isso, é preciso correr os seguintes comandos, dentro da Mariadb:

CREATE DATABASE base_dados_sd;

CREATE USER IF NOT EXISTS 'me'@'localhost' IDENTIFIED BY 'mypass';

GRANT ALL PRIVILEGES ON base_dados_sd.* TO 'me'@'localhost';

FLUSH PRIVILEGES;

Agora é só compilar e correr o Server e o Cliente.
*/
