package structs.notification;

import entities.Mensagem;
import structs.server.ClientContext;

public class NotificationMessage {
     private final Mensagem message;
     private final ClientContext contexto;

     public NotificationMessage(Mensagem message, ClientContext contexto) {
          this.message = message;
          this.contexto = contexto;
     }

     public Mensagem getMensagem() {
          return message;
     }

     public ClientContext getContexto() {
          return contexto;
     }

}
