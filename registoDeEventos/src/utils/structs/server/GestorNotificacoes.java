package utils.structs.server;

import entities.Mensagem;
import entities.ServerData;
import entities.payloads.NotificacaoVC;
import entities.payloads.NotificacaoVS;
import enums.TipoMsg;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import utils.structs.notification.NotificationVCCounter;
import utils.structs.notification.NotificationVSCounter;

/** Classe responsável por gerir notificações */
public class GestorNotificacoes {

     /** Lista de notificações do tipo Vendas Consecutivas */
     private final List<NotificationVCCounter> listavc = new ArrayList<>();

     /** Lista de notificações do tipo Vendas Simultâneas */
     private final List<NotificationVSCounter> listavs = new ArrayList<>();

     /** Lock para a lista de notificações do tipo Vendas Consecutivas */
     private final ReentrantLock lockVC = new ReentrantLock();

     /** Lock para a lista de notificações do tipo Vendas Simultâneas */
     private final ReentrantLock lockVS = new ReentrantLock();

     /** Último produto vendido */
     private String produtoAtualVC = null;

     /** Adiciona uma notificação do tipo Vendas Consecutivas 
      * 
      * @param id ID da notificação
      * @param noti Notificação a adicionar
      * @param clienteID ID do cliente que pediu a notificação
     */
     public void addVC(int id, NotificacaoVC noti, int clienteID) {
          lockVC.lock();
          try {
               listavc.add(new NotificationVCCounter(id, clienteID, noti.getN()));
          } finally {
               lockVC.unlock();
          }
     }

     /** Adiciona uma notificação do tipo Vendas Simultâneas 
      * 
      * @param id ID da notificação
      * @param noti Notificação a adicionar
      * @param clienteID ID do cliente que pediu a notificação
     */
     public void addVS(int id, NotificacaoVS noti, int clienteID) {
          lockVS.lock();
          try {
               listavs.add(new NotificationVSCounter(
                         id, clienteID,
                         noti.getProduto_1(),
                         noti.getProduto_2()));
          } finally {
               lockVS.unlock();
          }
     }

     /** Processa a venda de um produto 
      * 
      * @param produto Produto vendido
      * @return Lista de notificações a enviar
     */
     public List<ServerData> processarProdutoVendido(String produto) {
          List<ServerData> out = new ArrayList<>();

          lockVC.lock();
          lockVS.lock();
          try {
               processarVC(produto, out);
               processarVS(produto, out);
          } finally {
               lockVS.unlock();
               lockVC.unlock();

          }
          return out;
     }

     /** Processa as notificações do tipo Vendas Consecutivas
      * 
      * @param produto Produto vendido
      * @param out Lista de notificações a enviar
      */
     private void processarVC(String produto, List<ServerData> out) {
          if (produto.equals(produtoAtualVC)) {
               Iterator<NotificationVCCounter> it = listavc.iterator();
               while (it.hasNext()) {
                    NotificationVCCounter nvc = it.next();
                    nvc.incrementCounter();

                    if (nvc.getCounter() >= nvc.getN()) {
                         out.add(new ServerData(
                              nvc.getClienteID(), 
                              new Mensagem(
                                   nvc.getId(),
                                   TipoMsg.NOTIFICACAO_VC,
                                   produto.getBytes())));
                         it.remove();
                    }
               }
          } else {
               produtoAtualVC = produto;
               listavc.forEach(NotificationVCCounter::resetCounter);
          }
     }

     /** Processa as notificações do tipo Vendas Simultâneas
      * 
      * @param produto Produto vendido
      * @param out Lista de notificações a enviar
      */
     private void processarVS(String produto, List<ServerData> out) {
          Iterator<NotificationVSCounter> it = listavs.iterator();
          while (it.hasNext()) {
               NotificationVSCounter nvs = it.next();

               if (nvs.getProd1().equals(produto)) nvs.setProd_1_sold(true);
               if (nvs.getProd2().equals(produto)) nvs.setProd_2_sold(true);

               if (nvs.isProd_1_sold() && nvs.isProd_2_sold()) {
                    out.add(new ServerData(
                         nvs.getClienteID(), 
                         new Mensagem(
                              nvs.getId(),
                              TipoMsg.NOTIFICACAO_VS,
                              "true".getBytes())));
                    it.remove();
               }
          }
     }

     /** Obtem todas as notificações pendentes e limpa as listas
      * 
      * @return Lista de notificações a enviar
      */
     public List<ServerData> clear() {
          List<ServerData> out = new ArrayList<>();

          lockVC.lock();
          lockVS.lock();
          try {
               for (var nvc : listavc)
                    out.add(new ServerData(
               nvc.getClienteID(), 
               new Mensagem(
                         nvc.getId(),
                         TipoMsg.NOTIFICACAO_VC,
                         "null".getBytes())));
               listavc.clear();

               for (var nvs : listavs)
                    out.add(new ServerData(
                         nvs.getClienteID(), 
                         new Mensagem(
                              nvs.getId(),
                              TipoMsg.NOTIFICACAO_VS,
                              "false".getBytes())));
               listavs.clear();
          } finally {
               lockVS.unlock();
               lockVC.unlock();
          }
          return out;              
     }
}
