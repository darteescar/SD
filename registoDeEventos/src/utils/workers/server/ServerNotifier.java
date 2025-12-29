package utils.workers.server;

import entities.Mensagem;
import entities.ServerData;
import java.util.List;
import java.util.Map;
import utils.structs.notification.BoundedBuffer;
import utils.structs.server.GestorNotificacoes;

public class ServerNotifier implements Runnable {

     private final BoundedBuffer<String> buffer;
     private final GestorNotificacoes gestor;
     private final Map<Integer, BoundedBuffer<Mensagem>> clientBuffers;

     public ServerNotifier(
               GestorNotificacoes gestor,
               Map<Integer, BoundedBuffer<Mensagem>> clientBuffers) {

          this.buffer = new BoundedBuffer<>();
          this.gestor = gestor;
          this.clientBuffers = clientBuffers;
     }

     @Override
     public void run() {
          while (true) {
               String produto = buffer.poll();
               if (produto == null) continue;

               List<ServerData> notificacoes =
                         gestor.processarProdutoVendido(produto);

               for (ServerData m : notificacoes) {
                    clientBuffers
                         .get(m.getClienteID())
                         .add(m.getMensagem());
               }
          }
     }

     public void signall(String produto){
          buffer.add(produto);
     }

     /* Chamado pelo simulador */
     public void clear() {
          List<ServerData> notificacoes = gestor.clear();
          for (ServerData m : notificacoes)
               clientBuffers.get(m.getClienteID()).add(m.getMensagem());
     }
}
