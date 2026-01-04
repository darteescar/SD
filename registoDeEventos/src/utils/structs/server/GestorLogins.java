package utils.structs.server;
import databases.BDUsers;

/** Classe responsável pela gestão dos utilizadores registados */
public class GestorLogins {

     /** Instância da base de dados de utilizadores */
     BDUsers BDUsers;

     /** 
      * Construtor da classe GestorLogins. Inicializa a instância da base de dados de utilizadores.
      * 
      * @return Uma nova instância de GestorLogins
      */
     public GestorLogins(){
          this.BDUsers = BDUsers.getInstance();
     }

     /** 
      * Regista um novo utilizador na base de dados.
      * 
      * @param username Nome de utilizador
      * @param password Palavra-passe do utilizador
      * @return true se o registo for bem-sucedido, false caso contrário
      */
     public boolean registar(String username,String password) {
          if (username == null || password == null) {
               return false; // campos inválidos
          }
          if (!BDUsers.containsUser(username)){ // se não está na BD, insere na BD
               BDUsers.add(username, password);
               return true;
          }
          return false; // user existe
     }
     
     /** 
      * Autentica um utilizador com base no nome de utilizador e palavra-passe fornecidos.
      * 
      * @param username Nome de utilizador
      * @param password Palavra-passe do utilizador
      * @return true se a autenticação for bem-sucedida, false caso contrário
      */
     public boolean autenticar(String username,String password) {
          if (BDUsers.containsUser(username)){ // se está na BD
               if (BDUsers.get(username).equals(password)) { // se a password guardada é igual à passada
                    return true; // autenticação correta
               }
          } 
          return false; // user nao existe ou password incorreta
     }

     /** 
      * Apaga um utilizador da base de dados.
      * 
      * @param username Nome de utilizador
      * @param password Palavra-passe do utilizador
      * @return true se a remoção for bem-sucedida, false caso contrário
      */
     public boolean apagar(String username, String password) {
          if (BDUsers.containsUser(username)){ // se está na BD
               if (BDUsers.get(username).equals(password)) {  // se a password guardada é igual à passada
                    BDUsers.remove(username,password); // remove da BD
                    return true; // correu bem
               }
          } 
          return false; // user nao existe ou password incorreta
     }

}