package databases;

import entities.Serie;
import entities.payloads.Evento;
import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BDSeries implements Map<String, Serie> {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock writelock =  lock.writeLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
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
            System.out.println("[BDS]: Erro ao criar tabelas de séries/eventos: " + e.getMessage());
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    // ----------------------- Map methods -----------------------

    @Override
    public int size() {
        readLock.lock();
        try {
            try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM serie")) {
                if (rs.next()) return rs.getInt("count");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;   
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            return size() == 0;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        readLock.lock();
        try {
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
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        readLock.lock();
        try {
            if (!(value instanceof Serie)) return false;
            Serie s = (Serie) value;
            // podes verificar pelo primeiro evento ou outra lógica
            return containsKey(s.getData());   
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Serie get(Object key) {
        readLock.lock();
        try {
            String data = (String) key;
            Serie serie = new Serie(data);

            try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT e.produto, e.quantidade, e.preco " +
                        "FROM evento e " +
                        "JOIN serie s ON s.id_serie = e.id_serie " +
                        "WHERE s.data = ? " +
                        "ORDER BY e.id_evento")) {

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
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Serie put(String key, Serie value) {
        writelock.lock();
        try {
            System.out.println("[BDS]: Adicionando série do dia " + key + " à base de dados.");
            System.out.println("[BDS]: Eventos na série: " + value.getEventos().size());
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
                        stmtEvento.setString(5, key);
                        stmtEvento.executeUpdate();
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("[BDS]: Série do dia " + key + " adicionada com sucesso à base de dados.");
            return value;   
        } finally {
            writelock.unlock();
        }
    }

    @Override
    public Serie remove(Object key) {
        writelock.lock();
        try {
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
        } finally {
            writelock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends Serie> m) {
        writelock.lock();
        try {
            m.forEach(this::put);
        } finally {
            writelock.unlock();
        }
    }

    @Override
    public void clear() {
        writelock.lock();
        try {
            try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
                Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM evento");
                stmt.executeUpdate("DELETE FROM serie");
            } catch (SQLException e) {
                e.printStackTrace();
            }   
        } finally {
            writelock.unlock();
        }
    }

    @Override
    public Set<String> keySet() {
        readLock.lock();
        try {
            Set<String> keys = new HashSet<>();
            try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT data FROM serie")) {

                while (rs.next()) keys.add(rs.getString("data"));

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return keys;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Collection<Serie> values() {
        readLock.lock();
        try {
            List<Serie> series = new ArrayList<>();
            for (String key : keySet()) series.add(get(key));
            return series;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<Entry<String, Serie>> entrySet() {
        readLock.lock();
        try {
            Set<Entry<String, Serie>> entries = new HashSet<>();
            for (String key : keySet()) {
                Serie s = get(key);
                entries.add(new AbstractMap.SimpleEntry<>(key, s));
            }
            return entries;
        } finally {
            readLock.unlock();
        }
    }

    public void print() {
        readLock.lock();
        try {
            System.out.println("======== CONTEÚDO DA BASE DE DADOS ========");
            try (Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD)) {

                // 1) Imprimir séries
                System.out.println("\n--- TABELA SERIE ---");
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id_serie, data FROM serie ORDER BY id_serie")) {

                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        System.out.println(
                                "Serie { id_serie=" + rs.getInt("id_serie") +
                                ", data=" + rs.getString("data") +
                                " }"
                        );
                    }
                }

                // 2) Imprimir eventos
                System.out.println("\n--- TABELA EVENTO ---");
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id_evento, id_serie, produto, quantidade, preco " +
                        "FROM evento ORDER BY id_evento")) {

                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        System.out.println(
                                "Evento { id_evento=" + rs.getInt("id_evento") +
                                ", id_serie=" + rs.getInt("id_serie") +
                                ", produto=" + rs.getString("produto") +
                                ", quantidade=" + rs.getInt("quantidade") +
                                ", preco=" + rs.getDouble("preco") +
                                " }"
                        );
                    }
                }

                // 3) Eventos agrupados por série (extra útil)
                System.out.println("\n--- EVENTOS POR SERIE ---");
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT s.data, s.id_serie, e.id_evento, e.produto, e.quantidade, e.preco " +
                        "FROM serie s LEFT JOIN evento e ON s.id_serie = e.id_serie " +
                        "ORDER BY s.id_serie, e.id_evento")) {

                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        System.out.println(
                                "[Dia " + rs.getString("data") + "] " +
                                "Serie " + rs.getInt("id_serie") +
                                " -> Evento " + rs.getInt("id_evento") +
                                " (" + rs.getString("produto") + ", Q=" + rs.getInt("quantidade") +
                                ", P=" + rs.getDouble("preco") + ")"
                        );
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            System.out.println("============================================");   
        } finally {
            readLock.unlock();
        }
    }

}
