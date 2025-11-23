import java.net.Socket;
import java.time.LocalDate;

//import Data.Evento;
//import Data.Login;
import Mensagens.*;

public class Cliente {

     public void main(String[] args) throws Exception {
          if (args.length != 2) {
               System.out.println("Usage: java Server <Username> <Password>");
               System.exit(1);
          } 
               
          String username = args[0];
          String password = args[1];
          Mensagem msgInicial = new MsgCliente(username, TipoMensagem.LOGIN, LocalDate.now(), password);

          Socket s = new Socket("localhost", 12345);
         
     }
}
