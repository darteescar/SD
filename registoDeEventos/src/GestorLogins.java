import structs.Cache;

public class GestorLogins {
     Cache<String,String> cache; //<USER,PASSWORD>
     BDLogin users;

     public GestorLogins(int capacidade){
          this.cache = new Cache<>(capacidade);
          this.users = new BDLogin("files/users.txt");
     }

     public boolean registar(String username,String password) {
          if (!(username != null && password != null)) {
               return false;
          }
          if (!cache.containsKey(username) && !users.containsUser(username)){
               users.add(username, password);
               cache.put(username, password);
               return true;
          }
          return false;
     }
     
     public boolean autenticar(String username,String password) {
          if (cache.containsKey(username)){
               if (cache.get(username).equals(password)){
                    return true;
               }
          } else if (users.containsUser(username)){
               if (users.get(username).equals(password)) {
                    cache.put(username, password);
                    return true;
               }
          } 
          return false;
     }

     public boolean apagar(String username, String password) {
          if (cache.containsKey(username)){
               if (cache.get(username).equals(password)){
                    cache.remove(username, password);
               }
          } else if (users.containsUser(username)){
               if (users.get(username).equals(password)) {
                    users.remove(username,password);
               }
          } 
          return false;
     }

}