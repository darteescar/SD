package utils.workers.server;

import entities.ServerData;
import java.util.List;
import java.util.Map;
import utils.structs.server.BoundedBuffer;
import utils.structs.server.ClientSession;
import utils.structs.server.GestorNotificacoes;

/** Thread responsável por submeter notificações aos buffers dos ServerWriters dos clientes */
public class ServerNotifier implements Runnable {

     /** Buffer para armazenar produtos vendidos */
     private final BoundedBuffer<String> buffer;

     /** Gestor de notificações para processar produtos vendidos */
     private final GestorNotificacoes gestor;

     /** Mapa de sessões dos clientes, indexados por ID do cliente */
     private final Map<Integer, ClientSession> clientSessions;

     /** 
      * Construtor que inicializa o ServerNotifier com o gestor e os buffers dos clientes
      * 
      * @param gestor Gestor de notificações
      * @param clientBuffers Mapa de buffers dos ServerWriters dos clientes
      * @return Uma nova instância do ServerNotifier
      */
     public ServerNotifier(
               GestorNotificacoes gestor,
               Map<Integer, ClientSession> clientSessions) {

          this.buffer = new BoundedBuffer<>();
          this.gestor = gestor;
          this.clientSessions = clientSessions;
     }

     /** 
      * Método principal da thread que processa produtos vendidos (quando notificado) e submete notificações aos buffers dos ServerWriters dos clientes
      */
     @Override
     public void run() {
          while (true) {
               String produto = buffer.poll();

               List<ServerData> notificacoes =
                         gestor.processarProdutoVendido(produto);

               for (ServerData m : notificacoes) {
                    ClientSession session = clientSessions.get(m.getClienteID());
                     if (session != null) {
                         session.addToBuffer(m.getMensagem());
                    }
               }
          }
     }

     /**
      * Notifica o ServerNotifier sobre um novo produto vendido. Usado pelos ServerWorkers.
      * 
      * @param produto O produto vendido a ser notificado
      */
     public void signall(String produto){
          buffer.add(produto);
     }
}
