package structs;

import entities.payloads.NotificacaoVC;
import entities.payloads.NotificacaoVS;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerNotifier {
     private List<NotificationVSCounter> listavs;
     private List<NotificationVCCounter> listavc;
     private GestorSeries gestorSeries;


     public ServerNotifier(GestorSeries gestorSeries) {
          this.listavc = new ArrayList<>();
          this.listavs = new ArrayList<>();
          this.gestorSeries = gestorSeries;

     }

     public void notificar(String produto){
          for (NotificationVSCounter nvs : listavs){
               if (nvs.getProd1().equals(produto)){
                    nvs.setProd_1_sold(true);
               }
               if (nvs.getProd2().equals(produto)){
                    nvs.setProd_2_sold(true);
               }
               if (nvs.isProd_1_sold() && nvs.isProd_2_sold()){
                    /// to do
                    /// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    //Enviar notificação
                    System.out.println("[NOTIFICAÇÃO ENVIADA AO CLIENTE] -> " + nvs.getSocket().getInetAddress().toString() + " (Produtos: " + nvs.getProd1() + ", " + nvs.getProd2() + ")");
                    //Remover da lista
                    listavs.remove(nvs);
                    break;
               }
          }
          String produto_atual = NotificationVCCounter.getProduto();
          if (produto_atual.equals(produto)){
               for (NotificationVCCounter nvc : listavc){
                    nvc.incrementCounter();
                    if (nvc.getCounter() == nvc.getN()){
                         /// to do
                         /// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                         //Enviar notificação
                         System.out.println("[NOTIFICAÇÃO ENVIADA AO CLIENTE] -> " + nvc.getSocket().getInetAddress().toString() + " (Produto: " + produto + ")");
                         //Remover da lista
                         listavc.remove(nvc);
                         break;
                    }
               }
          } else {
               NotificationVCCounter.setProduto(produto);
               for (NotificationVCCounter nvc : listavc){
                    nvc.resetCounter();
               }
          }
          
     }

     public void add(NotificacaoVC noti, Socket socket){
          NotificationVCCounter nvc = new NotificationVCCounter(socket, noti.getN());
          this.listavc.add(nvc);
     }

     public void add(NotificacaoVS noti, Socket socket){
          NotificationVSCounter nvs = new NotificationVSCounter(socket, noti.getProduto_1(), noti.getProduto_2());
          this.listavs.add(nvs);
     }

}
