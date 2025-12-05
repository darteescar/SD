package structs;

import data.BDSeries;
import entities.Serie;

public class GestorSeries {
     Cache<String,Serie> cache; //<Dia,Serie>
     BDSeries bd;

     public GestorSeries(int d, int s){
          this.cache = new Cache<>(d);
          this.bd = BDSeries.getInstance();
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

}