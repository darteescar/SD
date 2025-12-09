package structs.notification;

import entities.Mensagem;
import entities.payloads.NotificacaoVC;
import entities.payloads.NotificacaoVS;
import enums.TipoMsg;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import structs.server.ClientContext;

public class ServerNotifier implements Runnable {
     private List<NotificationVSCounter> listavs;
     private List<NotificationVCCounter> listavc;
     private final ConcurrentBuffer<String> buffer;
     private final ReentrantLock lock1 = new ReentrantLock();
     private final ReentrantLock lock2 = new ReentrantLock();

     public ServerNotifier() {
          this.listavc = new ArrayList<>();
          this.listavs = new ArrayList<>();
          this.buffer = new ConcurrentBuffer<>();
     }

     @Override
     public void run() {
          while (true) {
               String produto = buffer.poll();
               if (produto != null) {
                    processar(produto);
               }
          }
     }

     public void processar(String produto){
          this.lock1.lock();
          try {
              processarNotificacoesVC(produto);
          } finally {
               this.lock1.unlock();
          }
          
          this.lock2.lock();
          try {
               processarNotificacoesVS(produto);
          } finally {
               this.lock2.unlock();
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
                    // Ambos os produtos foram vendidos → envia notificação e remove

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
                         // Sequência atingida → envia notificação e remove
                         envia_notificacao(nvc, produto);
                         it.remove();
                    }
               }
          } else {
               // Produto diferente → reseta todos os contadores e atualiza o produto atual
               NotificationVCCounter.setProduto(produto);
               for (NotificationVCCounter nvc : listavc) {
                    nvc.resetCounter();
               }
          }
     }

     public void add(int id, NotificacaoVC noti, ClientContext context){
          this.lock1.lock();
          try {
               NotificationVCCounter nvc = new NotificationVCCounter(id, context, noti.getN());
               this.listavc.add(nvc);
          } finally {
               this.lock1.unlock();
          }
     }

     public void add(int id, NotificacaoVS noti, ClientContext context){
          this.lock2.lock();
          try {
               NotificationVSCounter nvs = new NotificationVSCounter(id, context, noti.getProduto_1(), noti.getProduto_2());
               this.listavs.add(nvs);
          } finally {
               this.lock2.unlock();
          }
     }

     public void signall(String produto){
          this.buffer.add(produto);
     }

     public void clear() {
          this.lock1.lock();
          try {
              sendFalseToAllVCCounters();
              this.listavc.clear();
          } finally {
               this.lock1.unlock();
          }

          this.lock2.lock();
          try {
               sendFalseToAllVSCounters();
               this.listavs.clear();
          } finally {
               this.lock2.unlock();
          }
     }

     private void sendFalseToAllVCCounters() {
          this.lock1.lock();
          try {
               for (NotificationVCCounter nvc : listavc) {
                    envia_notificacao(nvc, "null");              
               } 
          } finally {
               this.lock1.unlock();
          }
     }

     private void sendFalseToAllVSCounters() {
          this.lock2.lock();
          try {
               for (NotificationVSCounter nvs : listavs) {
                    envia_notificacao(nvs, "false");
               }
          } finally {
               this.lock2.unlock();
          }
     }

     private void envia_notificacao(NotificationVCCounter nvc, String produto) {
          Mensagem mensagem = new Mensagem(nvc.getId(), TipoMsg.NOTIFICACAO_VC, produto.getBytes());
          ClientContext context = nvc.getContext();
          Thread t = new Thread(() -> {
               context.send(mensagem);
          });
          t.start();
     }

     private void envia_notificacao(NotificationVSCounter nvs, String produto) {
          Mensagem mensagem = new Mensagem(nvs.getId(), TipoMsg.NOTIFICACAO_VS, produto.getBytes());
          ClientContext context = nvs.getContext();
          Thread t = new Thread(() -> {
               context.send(mensagem);
          });
          t.start();
     }
}
