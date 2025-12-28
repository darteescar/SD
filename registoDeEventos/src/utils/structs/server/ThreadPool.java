package utils.structs.server;

import entities.Mensagem;
import entities.ServerData;
import utils.structs.notification.ConcurrentBuffer;
import utils.workers.server.ServerNotifier;
import utils.workers.server.ServerWorker;

import java.io.IOException;

public class ThreadPool {
     private final ServerWorker[] workers;
     
     public ThreadPool(int numWorkers, ConcurrentBuffer<ServerData> taskBuffer, int d, GestorLogins logins, GestorSeries gestorSeries, ServerNotifier notifier, SafeMap<Integer, ConcurrentBuffer<Mensagem>> clientBuffers) throws IOException {
          this.workers = new ServerWorker[numWorkers];
     
          for (int i = 0; i < numWorkers; i++) {
               workers[i] = new ServerWorker(logins, gestorSeries, notifier, taskBuffer, clientBuffers, d);
               System.out.println("[THREAD-POOL]: Worker-" + i + " criado.");
          }
          startWorkers();
     }

     private void startWorkers() {
          for (int i = 0; i < workers.length; i++) {
               new Thread(workers[i], "Worker-" + i).start();
               System.out.println("[THREAD-POOL]: Worker-" + i + " começou.");
          }
     }
}


