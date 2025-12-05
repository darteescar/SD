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
          return true;
     }

     public boolean remove(String dia) {
          return true;
     }
}
