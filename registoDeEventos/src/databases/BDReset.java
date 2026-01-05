package databases;

import java.sql.*;

/** Classe responsável por reiniciar a base de dados */
public class BDReset {

    /** Reinicia todas as tabelas da base de dados (com exceção da dos users) */
    public static void resetAll() {
        try (Connection conn = DriverManager.getConnection(
                BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
            Statement stm = conn.createStatement()) {

            stm.executeUpdate("DROP TABLE IF EXISTS evento");
            stm.executeUpdate("DROP TABLE IF EXISTS serie");
            stm.executeUpdate("DROP TABLE IF EXISTS server_day");

            // recriar server_day com a data inicial
            stm.executeUpdate(
                "CREATE TABLE IF NOT EXISTS server_day (" +
                "id INT PRIMARY KEY," +
                "server_day DATE NOT NULL" +
                ")"
            );

            stm.executeUpdate(
                "INSERT INTO server_day (id, server_day) VALUES (1, '2024-12-31')"
            );

            System.out.println("[BDR]: Base de dados apagada e data reiniciada.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}

