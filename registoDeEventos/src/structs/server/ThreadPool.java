package structs.server;

import entities.Mensagem;
import entities.ServerData;
import java.io.IOException;
import structs.notification.ConcurrentBuffer;
import structs.notification.ServerNotifier;

public class ThreadPool {
     private final ServerWorker[] workers;
     
     public ThreadPool(int numWorkers, ConcurrentBuffer<ServerData> taskBuffer, int d, GestorLogins logins, GestorSeries gestorSeries, ServerNotifier notifier, SafeMap<Integer, ConcurrentBuffer<Mensagem>> clientBuffers) throws IOException {
          this.workers = new ServerWorker[numWorkers];
     
          for (int i = 0; i < numWorkers; i++) {
               workers[i] = new ServerWorker(logins, gestorSeries, notifier, taskBuffer, clientBuffers, d);
          }
          startWorkers();
     }

     private void startWorkers() {
          for (int i = 0; i < workers.length; i++) {
               new Thread(workers[i], "Worker-" + i).start();
          }
     }
}


