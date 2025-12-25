package structs.server;

import data.BDSeries;
import entities.Data;
import entities.Serie;
import entities.payloads.Evento;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class GestorSeries {
     private final Cache<String,Serie> cache; //<Dia,Serie>
     private final BDSeries bd;
     private Data data_atual;
     private Serie serie_atual;
     private ReentrantLock lock = new ReentrantLock();

     public GestorSeries(int s, Data data, Serie serie_atual){
          this.cache = new Cache<>(s);
          this.bd = BDSeries.getInstance();
          this.data_atual = data;
          this.serie_atual = serie_atual;
     }

     // Métodos de gestão de séries

     public boolean add(Serie serie) {
          lock.lock();
          try {
               if (serie == null || serie.getData() == null) {
                    return false; // campos inválidos
               }
               if (!bd.containsKey(serie.getData())){ // se não está na BD, insere na BD e na Cache
                    //System.out.println("Adicionando série do dia " + serie.getData() + " à BD e Cache");
                    bd.put(serie.getData(), serie);
                    cache.put(serie.getData(), serie);
                    return true;
               }
               return false; // série existe
          } finally {
               lock.unlock();
          }
     }

     public boolean remove(String dia) {
          if (cache.containsKey(dia)){ // se está na Cache
               cache.remove(dia); // remove da cache
               bd.remove(dia); // remove da BD
               return true;
          } else if (bd.containsKey(dia)){ // se está na BD
               bd.remove(dia); // remove da BD
               return true;
          }
          return false; // série não existe
     }

     public boolean contains(String dia){
          return cache.containsKey(dia) || bd.containsKey(dia); 
     }

     public Serie get(String dia) {
          lock.lock();
          try {
          if (cache.containsKey(dia)){ // HIT - se está na cache
               return cache.get(dia);
          } else if (bd.containsKey(dia)){ // MISS - se está na BD
               Serie s = bd.get(dia);
               cache.put(dia, s);
               return s;
          } 
          return null; // série não existe
          } finally {
               lock.unlock();
          }
     }

     // Métodos sobre a série atual do dia atual

     public void passarDia() {
          Serie serieParaGuardar;
               lock.lock();
               try {
                    System.out.println("Passando do dia");
                    serieParaGuardar = this.serie_atual; // guarda a série atual
                    this.data_atual.incrementData();
                    this.serie_atual = new Serie(data_atual.getData());
               } finally {
                    lock.unlock();
               }

               // Thread fora do lock
               new Thread(() -> add(serieParaGuardar)).start();
     }


     public void addSerieAtual(Evento evento) {
          lock.lock();
          try {
               //System.out.println("Adicionando evento à série atual");
               this.serie_atual.add(evento);
          } finally {
               lock.unlock();
          }
     }

     // é de notar que como o lock é o mesmo passarDia e getSerieAtual são mutuamente exclusivos
     // logo não há hipótese de se obter uma série incompleta quando se está a passar o dia

     public Serie getSerieAtual() {
          lock.lock();
          try {
               return this.serie_atual;
          } finally {
               lock.unlock();
          }
     }

     public Data getDataAtual(){
          lock.lock();
          try {
               return this.data_atual;
          } finally {
               lock.unlock();
          }
     }

     // Métodos das Queries de Agregação

     public int calcQuantidadeVendas(String produto, int dias){ 
          // talvez devessemos lancar uma Thread por dia para paralelizar isto ???
          int total = 0;
          Data currentDate = this.data_atual.clone();

          for (int i = 0; i < dias; i++) {
               String diaStr = currentDate.getData();
               Serie serie = this.get(diaStr);
               if (serie != null) {
                    total += serie.calcQuantidadeVendas(produto);
               }
               currentDate.decrementData();
          }
          return total;
     }

     public double calcVolumeVendas(String produto, int dias){ 
          // talvez devessemos lancar uma Thread por dia para paralelizar isto ???
          double total = 0.0;
          Data currentDate = this.data_atual.clone();

          for (int i = 0; i < dias; i++) {
               String diaStr = currentDate.getData();
               Serie serie = this.get(diaStr);
               if (serie != null) {
                    total += serie.calcVolumeVendas(produto);
               }
               currentDate.decrementData();
          }
          return total;
     }

     public double calcPrecoMedio(String produto, int dias){

          double totalPreco = calcVolumeVendas(produto, dias);
          int totalQuantidade = calcQuantidadeVendas(produto, dias);

          return (totalQuantidade == 0) ? 0.0 : (totalPreco / totalQuantidade);
     }

     public double calcPrecoMaximo(String produto, int dias){
          double maxPreco = 0.0;
          Data currentDate = this.data_atual.clone();

          for (int i = 0; i < dias; i++) {
               String diaStr = currentDate.getData();
               Serie serie = this.get(diaStr);
               if (serie != null) {
                    double precoMaxDia = serie.calcPrecoMaximo(produto);
                    if (precoMaxDia > maxPreco) {
                         maxPreco = precoMaxDia;
                    }
               }
               currentDate.decrementData();
          }
          return maxPreco;
     }

     public List<Evento> filtrarEventos(List<String> produtos, int dias){
          Data targetDate = this.data_atual.clone();
          for (int i = 0; i < dias; i++) {
               targetDate.decrementData();
          }
          String diaStr = targetDate.getData();
          Serie serie = this.get(diaStr);
          if (serie != null) {
               return serie.filtrarEventos(produtos);
          } else {
               return List.of(); // Retorna uma lista vazia se a série não existir
          }
     }

     public void print(){
          this.bd.print();
     }

}