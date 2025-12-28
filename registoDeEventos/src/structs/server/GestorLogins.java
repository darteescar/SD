package structs.server;
import data.BDUsers;

public class GestorLogins {
     Cache<String,String> cache; //<USER,PASSWORD>
     BDUsers users;

     public GestorLogins(int capacidade){
          this.cache = new Cache<>(capacidade);
          this.users = BDUsers.getInstance();
     }

     public boolean registar(String username,String password) {
          if (username == null || password == null) {
               return false; // campos inválidos
          }
          if (!users.containsUser(username)){ // se não está na BD, insere na BD e na Cache
               users.add(username, password);
               cache.put(username, password);
               return true;
          }
          return false; // user existe
     }
     
     public boolean autenticar(String username,String password) {
          if (cache.containsKey(username)){ // MISS - se está na cache
               if (cache.get(username).equals(password)){ // se a password guardada é igual à passada
                    return true; // autenticação correta
               }
          } else if (users.containsUser(username)){ // MISS - se está na BD
               if (users.get(username).equals(password)) { // se a password guardada é igual à passada
                    cache.put(username, password);
                    return true; // autenticação correta
               }
          } 
          return false; // user nao existe ou password incorreta
     }

     public boolean apagar(String username, String password) {
          if (cache.containsKey(username)){ // se está na Cache
               if (cache.get(username).equals(password)){ // se a password guardada é igual à passada
                    cache.remove(username, password); // remove da cache
                    return true; // correu bem
               }
          } else if (users.containsUser(username)){ // se está na BD
               if (users.get(username).equals(password)) {  // se a password guardada é igual à passada
                    users.remove(username,password); // remove da BD
                    return true; // correu bem
               }
          } 
          return false; // user nao existe ou password incorreta
     }

}