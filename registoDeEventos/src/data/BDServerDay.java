package data;

import java.sql.*;
import java.time.LocalDate;

public class BDServerDay {

    static {
        // garante que a tabela existe
        try (Connection conn = DriverManager.getConnection(
                BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
             Statement stm = conn.createStatement()) {

            stm.executeUpdate(
                "CREATE TABLE IF NOT EXISTS server_state (" +
                " id INT PRIMARY KEY CHECK (id = 1)," +
                " server_day DATE NOT NULL" +
                ")"
            );

            // inicializa se ainda não existir
            stm.executeUpdate(
                "INSERT IGNORE INTO server_state (id, server_day) " +
                " VALUES (1, '2024-12-31')"
            );

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static LocalDate getCurrentDate() {
        try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
            Statement stm = conn.createStatement()) {

            // cria a tabela se não existir
            stm.executeUpdate(
                "CREATE TABLE IF NOT EXISTS server_state (" +
                "id INT PRIMARY KEY," +
                "server_day DATE NOT NULL)"
            );

            ResultSet rs = stm.executeQuery("SELECT server_day FROM server_state WHERE id=1");
            if (rs.next()) {
                return rs.getDate("server_day").toLocalDate();
            } else {
                // nenhum valor guardado, logo, inicializa a data para 31/12/2024
                LocalDate inicial = LocalDate.of(2024, 12, 31);
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO server_state (id, server_day) VALUES (1, ?)")) {
                    ps.setDate(1, Date.valueOf(inicial));
                    ps.executeUpdate();
                }
                return inicial;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static void setCurrentDate(LocalDate date) {
        try (Connection conn = DriverManager.getConnection(
                BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE server_state SET server_day = ? WHERE id = 1")) {

            ps.setDate(1, Date.valueOf(date));
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
