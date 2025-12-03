import java.sql.*;

public class BDUsers {
     private static final String DB_URL = "jdbc:sqlite:users.db";

     // Conectar à base de dados
     private Connection connect() {
          try {
               return DriverManager.getConnection(DB_URL);
          } catch (SQLException e) {
               e.printStackTrace();
               return null;
          }
     }

     // Construtor: cria a tabela se não existir
     public BDUsers() {
          String sql = """
               CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    password TEXT NOT NULL
               );
          """;
          try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
               stmt.execute(sql);
          } catch (SQLException e) {
               e.printStackTrace();
          }
     }

     // Verifica se o utilizador existe
     public boolean containsUser(String username) {
          String sql = "SELECT 1 FROM users WHERE username = ?";
          try (Connection conn = connect();
               PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, username);
               try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
               }
          } catch (SQLException e) {
               e.printStackTrace();
               return false;
          }
     }

     // Adiciona um utilizador
     public void add(String username, String password) {
          String sql = "INSERT INTO users(username, password) VALUES (?, ?)";
          try (Connection conn = connect();
               PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, username);
               pstmt.setString(2, password);
               pstmt.executeUpdate();

          } catch (SQLException e) {
               System.out.println("Erro ao adicionar utilizador: " + e.getMessage());
          }
     }

     // Remove um utilizador com username e password
     public boolean remove(String username, String password) {
          String sql = "DELETE FROM users WHERE username = ? AND password = ?";
          try (Connection conn = connect();
               PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, username);
               pstmt.setString(2, password);
               int rows = pstmt.executeUpdate();
               return rows > 0;

          } catch (SQLException e) {
               e.printStackTrace();
               return false;
          }
     }

     // Devolve a password de um utilizador
     public String get(String username) {
          String sql = "SELECT password FROM users WHERE username = ?";
          try (Connection conn = connect();
               PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, username);
               try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                         return rs.getString("password");
                    }
               }

          } catch (SQLException e) {
               e.printStackTrace();
          }
          return null;
     }
}
