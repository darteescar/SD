import java.io.File;
import java.io.IOException;


public class BDLogin {
     private File file;

     public BDLogin(String path){
          this.file = new File(path);
          try {
               if (!file.exists()) {
                    file.getParentFile().mkdirs(); // cria a pasta se não existir
                    file.createNewFile(); // cria o arquivo
               }
          } catch (IOException e) {
               e.printStackTrace();
          }
     }

     public boolean containsUser(String username){
          // procura pelo user no ficheiro
          return true;
     }

     public void add(String username, String password){
          // adiciona user e password no fim do ficheiro
     }

     public boolean remove(String username, String password){
          // remove user e password do ficheiro
          return true;
     }

     public String get(String username){
          // devolve password do username passado
          return null;
     }
}
