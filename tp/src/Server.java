import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Server {
     private Cache cache_eventos;
     private HashMap<String, Cliente> clientes;
     private ThreadPool threadPool;

     public Server (int days, int series, int threads, int cache_size) {
     }

     public static void main(String[] args) {          
          if (args.length != 4) {
               System.out.println("Usage: java Server <Days> <Series> <Threads> <Cache_size>");
               System.exit(1);
          } 
               
          int days = Integer.parseInt(args[0]);
          int series = Integer.parseInt(args[1]);
          int threads = Integer.parseInt(args[2]);
          int cache_size = Integer.parseInt(args[3]);
          
          Server server = new Server(days, series, threads, cache_size);
     }
}