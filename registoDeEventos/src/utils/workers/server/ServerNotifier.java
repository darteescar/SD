package utils.workers.server;

import entities.Mensagem;
import entities.payloads.NotificacaoVC;
import entities.payloads.NotificacaoVS;
import enums.TipoMsg;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import utils.structs.notification.BoundedBuffer;
import utils.structs.notification.NotificationVCCounter;
import utils.structs.notification.NotificationVSCounter;

public class ServerNotifier implements Runnable {
     private List<NotificationVSCounter> listavs;
     private List<NotificationVCCounter> listavc;
     private final BoundedBuffer<String> buffer;
     private final ReentrantLock lock1 = new ReentrantLock();
     private final ReentrantLock lock2 = new ReentrantLock();
     private final Map<Integer, BoundedBuffer<Mensagem>> clientBuffers;

     public ServerNotifier(Map<Integer, BoundedBuffer<Mensagem>> clientBuffers) {
          this.listavc = new ArrayList<>();
          this.listavs = new ArrayList<>();
          this.buffer = new BoundedBuffer<>();
          this.clientBuffers = clientBuffers;
     }

     // Thread Notifier usa:

     @Override
     public void run() {
          while (true) {
               String produto = buffer.poll();
               if (produto != null) {
                    processar(produto);
               }
          }
     }

     private void processar(String produto){
          this.lock1.lock();
          this.lock2.lock();
          try {
              processarNotificacoesVC(produto);
              processarNotificacoesVS(produto);
          } finally {
               this.lock2.unlock();
               this.lock1.unlock();
          }
     }

     private void processarNotificacoesVS(String produto) {
          Iterator<NotificationVSCounter> it = listavs.iterator();
          while (it.hasNext()) {
               NotificationVSCounter nvs = it.next();

               if (nvs.getProd1().equals(produto)) {
                    nvs.setProd_1_sold(true);
               }
               if (nvs.getProd2().equals(produto)) {
                    nvs.setProd_2_sold(true);
               }

               if (nvs.isProd_1_sold() && nvs.isProd_2_sold()) {
                    // Ambos os produtos foram vendidos - envia notificação e remove

                    envia_notificacao(nvs, "true");

                    it.remove(); // Remove da lista após enviar a notificação
               }
          }
     }

     private void processarNotificacoesVC(String produto) {
          String produtoAtual = NotificationVCCounter.getProduto(); // Produto da sequência atual
          if (produtoAtual.equals(produto)) {
               // Produto igual ao anterior, incrementa contadores de todos
               Iterator<NotificationVCCounter> it = listavc.iterator();
               while (it.hasNext()) {
                    NotificationVCCounter nvc = it.next();
                    nvc.incrementCounter();

                    if (nvc.getCounter() >= nvc.getN()) {
                         // Sequência atingida - envia notificação e remove
                         envia_notificacao(nvc, produto);
                         it.remove();
                    }
               }
          } else {
               // Produto diferente - reseta todos os contadores e atualiza o produto atual
               NotificationVCCounter.setProduto(produto);
               for (NotificationVCCounter nvc : listavc) {
                    nvc.resetCounter();
               }
          }
     }

     // Threads Workers usam:

     public void add(int id, NotificacaoVC noti, int clienteID){
          this.lock1.lock();
          try {
               NotificationVCCounter nvc = new NotificationVCCounter(id, clienteID, noti.getN());
               this.listavc.add(nvc);
          } finally {
               this.lock1.unlock();
          }
     }

     public void add(int id, NotificacaoVS noti, int clienteID){
          this.lock2.lock();
          try {
               NotificationVSCounter nvs = new NotificationVSCounter(id, clienteID, noti.getProduto_1(), noti.getProduto_2());
               this.listavs.add(nvs);
          } finally {
               this.lock2.unlock();
          }
     }

     public void signall(String produto){
          this.buffer.add(produto);
     }

     // Thread Simulator

     public void clear() {
          // Thread separada faz tudo: bloqueio + cópia + envio
          new Thread(() -> {
               List<NotificationVCCounter> tmpVCCounters;
               List<NotificationVSCounter> tmpVSCounters;

               // Locks feitos dentro da thread
               lock1.lock();
               lock2.lock();
               try {
                    tmpVCCounters = new ArrayList<>(listavc);
                    listavc.clear();
                    tmpVSCounters = new ArrayList<>(listavs);
                    listavs.clear();
               } finally {
                    lock2.unlock();
                    lock1.unlock();
               }

               // Envio das notificações
               for (NotificationVCCounter nvc : tmpVCCounters)
                    envia_notificacao(nvc, "null");
               for (NotificationVSCounter nvs : tmpVSCounters)
                    envia_notificacao(nvs, "false");
          }).start();
     }


     private void envia_notificacao(NotificationVCCounter nvc, String produto) {
          Mensagem mensagem = new Mensagem(nvc.getId(), TipoMsg.NOTIFICACAO_VC, produto.getBytes());
          int clienteID = nvc.getClienteID();
          clientBuffers.get(clienteID).add(mensagem);
     }

     private void envia_notificacao(NotificationVSCounter nvs, String produto) {
          Mensagem mensagem = new Mensagem(nvs.getId(), TipoMsg.NOTIFICACAO_VS, produto.getBytes());
          int clienteID = nvs.getClienteID();
          clientBuffers.get(clienteID).add(mensagem);
     }
}
