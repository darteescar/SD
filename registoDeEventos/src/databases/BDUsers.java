package databases;
import java.sql.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Classe responsável por gerir os utilizadores na base de dados de utilizadores */
public class BDUsers {

     /** Lock para garantir a sincronização de acesso aos utilizadores */
     private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

     /** Lock para escrita */
     private final ReentrantReadWriteLock.WriteLock writelock =  lock.writeLock();

     /** Lock para leitura */
     private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();

     /** Instância singleton da classe BDUsers */
     private static BDUsers singleton = null;

     /** 
      * Obtém a instância singleton da classe BDUsers
      * 
      * @return A instância singleton da classe BDUsers
      */
     public static BDUsers getInstance() {
        if (BDUsers.singleton == null) {
          BDUsers.singleton = new BDUsers();
        }
        return BDUsers.singleton;
     }

     /** 
      * Construtor privado que inicializa a tabela de utilizadores na base de dados
      *
      * @throws NullPointerException se ocorrer um erro ao criar a tabela 
     */ 

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

     /** 
      * Verifica se um utilizador existe na base de dados
      * 
      * @param username O nome do utilizador a verificar
      * @return true se o utilizador existir, false caso contrário
      */
     public boolean containsUser(String username) {
          readLock.lock();
          try {
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
          } finally {
               readLock.unlock();
          }
     }

     /**
      * Adiciona um novo utilizador à base de dados
      * 
      * @param username O nome do utilizador a adicionar
      * @param password A password do utilizador a adicionar
      */
     public void add(String username, String password) {
          writelock.lock();
          try {

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
          } finally {
               writelock.unlock();
          }
     }

     /** 
      * Remove um utilizador da base de dados
      * 
      * @param username O nome do utilizador a remover
      * @param password A password do utilizador a remover
      * @return true se o utilizador foi removido, false caso contrário
      */
     public boolean remove(String username, String password) {
          writelock.lock();
          try {
               boolean success;
               try(Connection conn = DriverManager.getConnection(BDConfig.URL, BDConfig.USERNAME, BDConfig.PASSWORD);
                    PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ? AND password = ?")) {

                    pstmt.setString(1, username);
                    pstmt.setString(2, password);
                    int rowsAffected = pstmt.executeUpdate();
                    success = rowsAffected > 0;

               } catch (SQLException e) {
                    System.out.println("[BDU]: Erro ao remover utilizador: " + e.getMessage());
                    e.printStackTrace();
                    throw new NullPointerException(e.getMessage());
               }
               return success;
          } finally {
               writelock.unlock();
          }
     }

     /** 
      * Obtém a password de um utilizador da base de dados
      * 
      * @param username O nome do utilizador
      * @return A password do utilizador, ou null se o utilizador não existir
      */
     public String get(String username) {
          readLock.lock();
          try {
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
                    System.out.println("[BDU]: Erro ao obter password do utilizador: " + e.getMessage());
                    e.printStackTrace();
                    throw new NullPointerException(e.getMessage());
               }
               return password;
          } finally {
               readLock.unlock();
          }
     }
}
