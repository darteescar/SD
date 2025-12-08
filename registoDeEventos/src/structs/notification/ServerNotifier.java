package structs.notification;

import entities.payloads.NotificacaoVC;
import entities.payloads.NotificacaoVS;
import enums.TipoMsg;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import structs.server.ClientContext;
import entities.Mensagem;

public class ServerNotifier implements Runnable {
     private List<NotificationVSCounter> listavs;
     private List<NotificationVCCounter> listavc;
     private final NotificationDispatcher dispatcher;
     private final ConcurrentBuffer<String> buffer;
     private final ReentrantLock lock1 = new ReentrantLock();
     private final ReentrantLock lock2 = new ReentrantLock();

     public ServerNotifier(NotificationDispatcher dispatcher) {
          this.listavc = new ArrayList<>();
          this.listavs = new ArrayList<>();
          this.dispatcher = dispatcher;
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
          processarNotificacoesVC(produto);
          processarNotificacoesVS(produto);
     }

     private void processarNotificacoesVS(String produto){
          for (NotificationVSCounter nvs : listavs){
               if (nvs.getProd1().equals(produto)){
                    nvs.setProd_1_sold(true);
               }
               if (nvs.getProd2().equals(produto)){
                    nvs.setProd_2_sold(true);
               }
               if (nvs.isProd_1_sold() && nvs.isProd_2_sold()){
                    Mensagem mensagem = new Mensagem(nvs.getId(), TipoMsg.NOTIFICACAO_VS, "true".getBytes());

                    this.dispatcher.send(mensagem);
               }
          }
     }

     private void processarNotificacoesVC(String produto){
          String produto_atual = NotificationVCCounter.getProduto();
          if (produto_atual.equals(produto)){
               for (NotificationVCCounter nvc : listavc){
                    nvc.incrementCounter();
                    if (nvc.getCounter() == nvc.getN()){
                         Mensagem mensagem = new Mensagem(nvc.getId(), TipoMsg.NOTIFICACAO_VC, produto.getBytes());

                         this.dispatcher.send(mensagem);
                    }
               }
          } else {
               NotificationVCCounter.setProduto(produto);
               for (NotificationVCCounter nvc : listavc){
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
          // envia todas as notificações pendentes antes de limpar
          this.listavs.clear();
          this.listavc.clear();
     }

}
