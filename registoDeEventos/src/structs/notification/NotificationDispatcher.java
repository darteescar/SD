package structs.notification;

public class NotificationDispatcher {
     private final ConcurrentBuffer buffer;

     public NotificationDispatcher() {
          this.buffer = new ConcurrentBuffer<Mensagem>();
     }



}
