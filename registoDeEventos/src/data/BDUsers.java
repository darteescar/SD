package data;
import java.sql.*;

public class BDUsers {
     private static BDUsers singleton = null;

     public static BDUsers getInstance() {
        if (BDUsers.singleton == null) {
          BDUsers.singleton = new BDUsers();
        }
        return BDUsers.singleton;
    }

     // Construtor: cria a tabela se não existir
     private BDUsers() {
          try(Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
             Statement stm = conn.createStatement()){
          String sql = "CREATE TABLE IF NOT EXISTS users (" +
                       "username VARCHAR(50) PRIMARY KEY," +
                       "password VARCHAR(50) NOT NULL" +
                       ");";
          stm.executeUpdate(sql);
          } catch (SQLException e) {
               System.out.println("Erro ao criar tabela de utilizadores: " + e.getMessage());
               e.printStackTrace();
               throw new NullPointerException(e.getMessage());
          }
     }

     // Verifica se o utilizador existe
     public boolean containsUser(String username) {
          boolean exists;
          try(Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
               PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM users WHERE username = ?")) {

               pstmt.setString(1, username);
               try (ResultSet rs = pstmt.executeQuery()) {
                    rs.next();
                    exists = rs.getInt("count") > 0;
               }

          } catch (SQLException e) {
               System.out.println("Erro ao verificar utilizador: " + e.getMessage());
               e.printStackTrace();
               throw new NullPointerException(e.getMessage());
          }
          return exists;
     }

     // Adiciona um utilizador
     public void add(String username, String password) {
          try(Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
               PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {

               pstmt.setString(1, username);
               pstmt.setString(2, password);
               pstmt.executeUpdate();

          } catch (SQLException e) {
               System.out.println("Erro ao adicionar utilizador: " + e.getMessage());
               e.printStackTrace();
               throw new NullPointerException(e.getMessage());
          }
     }

     // Remove um utilizador com username e password
     public boolean remove(String username, String password) {
          boolean success;
          try(Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
               PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ? AND password = ?")) {

               pstmt.setString(1, username);
               pstmt.setString(2, password);
               int rowsAffected = pstmt.executeUpdate();
               success = rowsAffected > 0;

          } catch (SQLException e) {
               System.out.println("Erro ao remover utilizador: " + e.getMessage());
               e.printStackTrace();
               throw new NullPointerException(e.getMessage());
          }
          return success;
     }

     // Devolve a password de um utilizador
     public String get(String username) {
          String password = null;
          try(Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
               PreparedStatement pstmt = conn.prepareStatement("SELECT password FROM users WHERE username = ?")) {

               pstmt.setString(1, username);
               try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                         password = rs.getString("password");
                    }
               }

          } catch (SQLException e) {
               System.out.println("Erro ao obter password do utilizador: " + e.getMessage());
               e.printStackTrace();
               throw new NullPointerException(e.getMessage());
          }
          return password;
     }
}
