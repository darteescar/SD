package structs;

import data.BDSeries;
import entities.Data;
import entities.Serie;

public class GestorSeries {
     private Cache<String,Serie> cache; //<Dia,Serie>
     private BDSeries bd;
     private int d;
     private int s;
     private Data data;

     public GestorSeries(int d, int s, Data data){
          this.cache = new Cache<>(s);
          this.bd = BDSeries.getInstance();
          this.d = d;
          this.s = s;
          this.data = data;
     }

     public boolean add(Serie serie) {
          if (serie == null || serie.getData() == null) {
               return false; // campos inválidos
          }
          if (!bd.containsKey(serie.getData())){ // se não está na BD, insere na BD e na Cache
               bd.put(serie.getData(), serie);
               cache.put(serie.getData(), serie);
               return true;
          }
          return false; // série existe
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
          if (cache.containsKey(dia)){ // MISS - se está na cache
               return cache.get(dia);
          } else if (bd.containsKey(dia)){ // MISS - se está na BD
               Serie s = bd.get(dia);
               cache.put(dia, s);
               return s;
          } 
          return null; // série não existe
     }

     public int calcQuantidadeVendas(String produto, int dias){ 
          // talvez devessemos lancar uma Thread por dia para paralelizar isto ???
          int total = 0;
          Data currentDate = this.data.clone();

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

}