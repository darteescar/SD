package data;

import java.sql.*;

public class BDReset {

    public static void resetAll() {
        try (Connection conn = DriverManager.getConnection(
                BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
            Statement stm = conn.createStatement()) {

            stm.executeUpdate("DROP TABLE IF EXISTS evento");
            stm.executeUpdate("DROP TABLE IF EXISTS serie");
            stm.executeUpdate("DROP TABLE IF EXISTS server_state");

            // recriar server_state com a data inicial
            stm.executeUpdate(
                "CREATE TABLE IF NOT EXISTS server_state (" +
                "id INT PRIMARY KEY," +
                "server_day DATE NOT NULL" +
                ")"
            );

            stm.executeUpdate(
                "INSERT INTO server_state (id, server_day) VALUES (1, '2024-12-31')"
            );

            System.out.println("Base de dados apagada e data reiniciada.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}

