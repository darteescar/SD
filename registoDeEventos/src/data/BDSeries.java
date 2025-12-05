package data;

import entities.Serie;
import entities.payloads.Evento;
import java.sql.*;
import java.util.*;

public class BDSeries implements Map<String, Serie> {

    private static BDSeries singleton = null;

    public static BDSeries getInstance() {
        if (singleton == null) {
            singleton = new BDSeries();
        }
        return singleton;
    }

    private BDSeries() {
        try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
             Statement stm = conn.createStatement()) {

            String sqlSerie =
                    "CREATE TABLE IF NOT EXISTS serie (" +
                    " id_serie INT AUTO_INCREMENT PRIMARY KEY," +
                    " data VARCHAR(50) NOT NULL UNIQUE" +
                    ");";

            String sqlEvento =
                    "CREATE TABLE IF NOT EXISTS evento (" +
                    " id_evento INT AUTO_INCREMENT PRIMARY KEY," +
                    " id_serie INT NOT NULL," +
                    " produto VARCHAR(50) NOT NULL," +
                    " quantidade INT NOT NULL," +
                    " preco DOUBLE NOT NULL," +
                    " data VARCHAR(50) NOT NULL," +
                    " FOREIGN KEY (id_serie) REFERENCES serie(id_serie) ON DELETE CASCADE" +
                    ");";

            stm.executeUpdate(sqlSerie);
            stm.executeUpdate(sqlEvento);

        } catch (SQLException e) {
            System.out.println("Erro ao criar tabelas de séries/eventos: " + e.getMessage());
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    // ----------------------- Map methods -----------------------

    @Override
    public int size() {
        try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM serie")) {
            if (rs.next()) return rs.getInt("count");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        String data = (String) key;
        try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM serie WHERE data = ?")) {
            stmt.setString(1, data);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("count") > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if (!(value instanceof Serie)) return false;
        Serie s = (Serie) value;
        // podes verificar pelo primeiro evento ou outra lógica
        return containsKey(s.getData());
    }

    @Override
    public Serie get(Object key) {
        String data = (String) key;
        Serie serie = new Serie(data);
        try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT produto, quantidade, preco, data FROM evento e " +
                     "JOIN serie s ON s.id_serie = e.id_serie " +
                     "WHERE s.data = ? ORDER BY e.id_evento")) {

            stmt.setString(1, data);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Evento e = new Evento(
                        rs.getString("produto"),
                        rs.getInt("quantidade"),
                        rs.getDouble("preco")
                );
                serie.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return serie;
    }

    @Override
    public Serie put(String key, Serie value) {
        // cria nova série com data=key e adiciona eventos
        try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
             PreparedStatement stmtSerie = conn.prepareStatement(
                     "INSERT INTO serie (data) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {

            stmtSerie.setString(1, key);
            stmtSerie.executeUpdate();
            ResultSet rs = stmtSerie.getGeneratedKeys();
            if (!rs.next()) throw new SQLException("Erro ao criar série");
            int idSerie = rs.getInt(1);

            // inserir eventos
            try (PreparedStatement stmtEvento = conn.prepareStatement(
                    "INSERT INTO evento (id_serie, produto, quantidade, preco, data) VALUES (?, ?, ?, ?, ?)")) {
                for (Evento e : value.getEventos()) {
                    stmtEvento.setInt(1, idSerie);
                    stmtEvento.setString(2, e.getProduto());
                    stmtEvento.setInt(3, e.getQuantidade());
                    stmtEvento.setDouble(4, e.getPreco());
                    stmtEvento.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public Serie remove(Object key) {
        String data = (String) key;
        Serie removed = get(key);
        try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM serie WHERE data = ?")) {

            stmt.setString(1, data);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return removed;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Serie> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM evento");
            stmt.executeUpdate("DELETE FROM serie");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT data FROM serie")) {

            while (rs.next()) keys.add(rs.getString("data"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keys;
    }

    @Override
    public Collection<Serie> values() {
        List<Serie> series = new ArrayList<>();
        for (String key : keySet()) series.add(get(key));
        return series;
    }

    @Override
    public Set<Entry<String, Serie>> entrySet() {
        Set<Entry<String, Serie>> entries = new HashSet<>();
        for (String key : keySet()) {
            Serie s = get(key);
            entries.add(new AbstractMap.SimpleEntry<>(key, s));
        }
        return entries;
    }
}
