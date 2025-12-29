package utils.structs.server;
import databases.BDUsers;

public class GestorLogins {
     BDUsers BDUsers;

     public GestorLogins(){
          this.BDUsers = BDUsers.getInstance();
     }

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
     
     public boolean autenticar(String username,String password) {
          if (BDUsers.containsUser(username)){ // se está na BD
               if (BDUsers.get(username).equals(password)) { // se a password guardada é igual à passada
                    return true; // autenticação correta
               }
          } 
          return false; // user nao existe ou password incorreta
     }

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