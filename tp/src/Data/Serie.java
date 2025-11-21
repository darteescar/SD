package Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Serie {
     private LocalDate dia;
     private List<Evento> eventos;
     static DateTimeFormatter formatador_data = DateTimeFormatter.ofPattern("yyyy-MM-dd");

     public Serie() {
          this.dia = LocalDate.now();
          this.eventos = new ArrayList<>();
     }

     public Serie(LocalDate dia, List<Evento> eventos){
          this.dia = dia;
          this.eventos = new ArrayList<>();
          eventos.forEach(e -> this.addEvento(e)); //  é preciso fazer clone?
     }

     public void addEvento(Evento evento) {
          this.eventos.add(evento);
     }

     public List<Evento> getEventos() { return this.eventos; }
     public LocalDate getDia(){ return this.dia; }
     public int getNrEventos(){ return this.eventos.size(); }


     public void serialize(DataOutputStream out) throws IOException {
          String dia_str = this.dia.format(Serie.formatador_data);
          out.writeUTF(dia_str);
          int n = this.getNrEventos();
          out.writeInt(n);
          this.eventos.forEach(e -> {
               try { e.serialize(out); } 
               catch (IOException ex) {}
          });   
     }

     public static Serie deserialize(DataInputStream in) throws IOException {
          String dia_str = in.readUTF();
          LocalDate dia = LocalDate.parse(dia_str, Serie.formatador_data);
          int n = in.readInt();
          ArrayList<Evento> eventos = new ArrayList<>();
          for(int i = 0; i<n; i++) eventos.add(Evento.deserialize(in));
          return new Serie(dia,eventos);
     }
}