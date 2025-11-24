import java.net.Socket;
import java.time.LocalDate;

//import Data.Evento;
//import Data.Login;
import Mensagens.*;
import Conexao.*;

public class Cliente {

     public void main(String[] args) throws Exception {
          if (args.length != 3) {
               System.out.println("Usage: java Cliente <Username> <Password> <nº Threads>");
               System.exit(1);
          } 
               
          String username = args[0];
          String password = args[1];
          int n = Integer.parseInt(args[2]);

          Thread[] threads = new Thread[n];
          //Thread posicao x no array -> usa sempre tag x ao enviar mensagens

          Socket s = new Socket("localhost", 12345);
          ServerClientConnection conn = new ServerClientConnection(s);
          Demultiplexer dem = new Demultiplexer(conn);


          Mensagem msgInicial = new MsgCliente(username, TipoMensagem.LOGIN, LocalDate.now(), password);
     }
}
