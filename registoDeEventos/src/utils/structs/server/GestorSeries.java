package utils.structs.server;

import databases.BDSeries;
import entities.Data;
import entities.Serie;
import entities.payloads.Evento;
import enums.TipoMsg;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class GestorSeries {
     private final Cache<String,Serie> cache; //<Dia,Serie>
     private final BDSeries bd;
     private final Cache<String, Map<TipoMsg, Map<String, Double>>> cacheQueries;
     private Data data_atual;
     private Serie serie_atual;
     private ReentrantLock lock = new ReentrantLock();

     public GestorSeries(int s, Data data, Serie serie_atual){
          this.cache = new Cache<>(s);
          this.cacheQueries = new Cache<>(s);
          this.bd = BDSeries.getInstance();
          this.data_atual = data;
          this.serie_atual = serie_atual;
     }

     public void close() {
          lock.lock();
          try {
               add(serie_atual);
          } finally {
               lock.unlock();
          }
     }

     // Métodos de gestão de séries

     public boolean add(Serie serie) {
          if (serie == null || serie.getData() == null) {
               return false; // campos inválidos
          }
          if (!bd.containsKey(serie.getData())){ // se não está na BD, insere na BD e na Cache
               System.out.println("[GS]: Adicionando série do dia " + serie.getData() + " à BD e Cache");
               System.out.println("[GS]: Série de tamanho " + serie.size());
               System.out.println("[GS]: Adicionando à cache");
               cache.put(serie.getData(), serie);
               System.out.println("[GS]: Série do dia " + serie.getData() + " adicionada à Cache");
               System.out.println("[GS]: Adicionando à BD ...");
               bd.put(serie.getData(), serie);
               System.out.println("[GS]: Série do dia " + serie.getData() + " adicionada à BD");
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
          if (cache.containsKey(dia)){ // HIT - se está na cache
               System.out.println("[GS]: Cache HIT para o dia " + dia);
               return cache.get(dia);
          } else if (bd.containsKey(dia)){ // MISS - se está na BD
               Serie s = bd.get(dia);
               cache.put(dia, s);
               return s;
          }
          return null; // série não existe
     }

     // Métodos sobre a série atual do dia atual

     public void passarDia() {
          Serie serieParaGuardar;
          lock.lock();
          try {
               System.out.println("[GS]: Passando o dia");
               serieParaGuardar = this.serie_atual; // guarda a série atual
               this.data_atual.incrementData();
               this.serie_atual = new Serie(data_atual.getData());
               this.cache.print();
          } finally {
               lock.unlock();
          }

          // Thread fora do lock, para não bloquear outras operações enquanto guarda a série
          // como a bd é thread-safe, não há problema em fazer isto fora do lock
          new Thread(() -> add(serieParaGuardar)).start();
     }

     public void addSerieAtual(Evento evento) {
          lock.lock();
          try {
               this.serie_atual.add(evento);
          } finally {
               lock.unlock();
          }
     }

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

     public int calcQuantidadeVendas(String produto, int dias) {
          int total = 0;
          Data currentDate = getDataAtual().clone();
          currentDate.decrementData(); // começa no dia fechado

          for (int i = 0; i < dias; i++) {
               String diaStr = currentDate.getData();

               Double cached = getCachedQuery(diaStr, TipoMsg.QUANTIDADE_VENDAS, produto);
               if (cached != null) {
                    total += cached.intValue();
               } else {
                    Serie serie = this.get(diaStr);
                    if (serie != null) {
                         int v = serie.calcQuantidadeVendas(produto);
                         putCachedQuery(diaStr, TipoMsg.QUANTIDADE_VENDAS, produto, v);
                         total += v;
                    }
               }
               currentDate.decrementData();
          }
          return total;
     }


     public double calcVolumeVendas(String produto, int dias) {
          double total = 0.0;
          Data currentDate = getDataAtual().clone();
          currentDate.decrementData();

          for (int i = 0; i < dias; i++) {
               String diaStr = currentDate.getData();

               Double cached = getCachedQuery(diaStr, TipoMsg.VOLUME_VENDAS, produto);
               if (cached != null) {
                    total += cached;
               } else {
                    Serie serie = this.get(diaStr);
                    if (serie != null) {
                         double v = serie.calcVolumeVendas(produto);
                         putCachedQuery(diaStr, TipoMsg.VOLUME_VENDAS, produto, v);
                         total += v;
                    }
               }
               currentDate.decrementData();
          }
          return total;
          }


     public double calcPrecoMedio(String produto, int dias) {
          double volume = calcVolumeVendas(produto, dias);
          int quantidade = calcQuantidadeVendas(produto, dias);
          return (quantidade == 0) ? 0.0 : volume / quantidade;
     }


     public double calcPrecoMaximo(String produto, int dias) {
          double max = 0.0;
          Data currentDate = getDataAtual().clone();
          currentDate.decrementData();

          for (int i = 0; i < dias; i++) {
               String diaStr = currentDate.getData();

               Double cached = getCachedQuery(diaStr, TipoMsg.PRECO_MAXIMO, produto);
               double v;

               if (cached != null) {
                    v = cached;
               } else {
                    Serie serie = this.get(diaStr);
                    if (serie == null) {
                         currentDate.decrementData();
                         continue;
                    }
                    v = serie.calcPrecoMaximo(produto);
                    putCachedQuery(diaStr, TipoMsg.PRECO_MAXIMO, produto, v);
               }

               if (v > max) max = v;
               currentDate.decrementData();
          }
          return max;
     }


     public List<Evento> filtrarEventos(List<String> produtos, int dias){
          Data targetDate =  getDataAtual().clone();
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

     private Double getCachedQuery(String dia, TipoMsg tipo, String produto) {
          Map<TipoMsg, Map<String, Double>> byTipo = cacheQueries.get(dia);
          if (byTipo == null) return null;

          Map<String, Double> byProduto = byTipo.get(tipo);
          if (byProduto == null) return null;

          return byProduto.get(produto);
     }

     private void putCachedQuery(String dia, TipoMsg tipo, String produto, double valor) {
          cacheQueries.putIfAbsent(dia, new java.util.EnumMap<>(TipoMsg.class));
          Map<TipoMsg, Map<String, Double>> byTipo = cacheQueries.get(dia);

          byTipo.putIfAbsent(tipo, new java.util.HashMap<>());
          byTipo.get(tipo).put(produto, valor);
     }
}