package utils.structs.server;
import databases.BDUsers;

public class GestorLogins {
     Cache<String,String> CacheUsers; //<USER,PASSWORD>
     BDUsers BDUsers;

     public GestorLogins(int capacidade){
          this.CacheUsers = new Cache<>(capacidade);
          this.BDUsers = BDUsers.getInstance();
     }

     public boolean registar(String username,String password) {
          if (username == null || password == null) {
               return false; // campos inválidos
          }
          if (!BDUsers.containsUser(username)){ // se não está na BD, insere na BD e na Cache
               BDUsers.add(username, password);
               CacheUsers.put(username, password);
               return true;
          }
          return false; // user existe
     }
     
     public boolean autenticar(String username,String password) {
          if (CacheUsers.containsKey(username)){ // MISS - se está na cache
               if (CacheUsers.get(username).equals(password)){ // se a password guardada é igual à passada
                    return true; // autenticação correta
               }
          } else if (BDUsers.containsUser(username)){ // MISS - se está na BD
               if (BDUsers.get(username).equals(password)) { // se a password guardada é igual à passada
                    CacheUsers.put(username, password);
                    return true; // autenticação correta
               }
          } 
          return false; // user nao existe ou password incorreta
     }

     public boolean apagar(String username, String password) {
          if (CacheUsers.containsKey(username)){ // se está na Cache
               if (CacheUsers.get(username).equals(password)){ // se a password guardada é igual à passada
                    CacheUsers.remove(username, password); // remove da cache
                    return true; // correu bem
               }
          } else if (BDUsers.containsUser(username)){ // se está na BD
               if (BDUsers.get(username).equals(password)) {  // se a password guardada é igual à passada
                    BDUsers.remove(username,password); // remove da BD
                    return true; // correu bem
               }
          } 
          return false; // user nao existe ou password incorreta
     }

}