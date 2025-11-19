import java.util.ArrayList;
import java.util.List;

public class Serie {
     private List<Evento> eventos;

     public Serie() {
          this.eventos = new ArrayList<>();
     }

     public void addEvento(Evento evento) {
          this.eventos.add(evento);
     }

     public List<Evento> getEventos() {
          return this.eventos;
     }
}