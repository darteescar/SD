package structs.notification;

import entities.Mensagem;
import structs.server.ClientContext;

public class NotificationDispatcher implements Runnable {
     private final ConcurrentBuffer<NotificationMessage> buffer;

     public NotificationDispatcher() {
          this.buffer = new ConcurrentBuffer<>();
     }

     public void add(NotificationMessage mensagem){
          this.buffer.add(mensagem);
     }

     @Override
     public void run (){
          while (true) {
               NotificationMessage mensagem = buffer.poll();
               if (mensagem != null) {
                    Mensagem notificacao = mensagem.getMensagem();
                    ClientContext contexto = mensagem.getContexto();

                    contexto.send(notificacao);
               }
          }
     }

}
