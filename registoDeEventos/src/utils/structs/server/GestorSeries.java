package utils.structs.server;

import databases.BDSeries;
import entities.Data;
import entities.Serie;
import entities.payloads.Evento;
import enums.TipoMsg;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/** Classe responsável pela gestão das séries de eventos */
public class GestorSeries {

     /** Cache para armazenar séries de eventos em memória */
     private final Cache<String,Serie> cache;

     /** Instância da base de dados de séries */
     private final BDSeries bd;

     /** Cache para armazenar resultados de agregações */
     private final Cache<String, Map<TipoMsg, Map<String, Double>>> cacheQueries; // dia -> (tipo -> (produto -> valor))

     /** Data do dia atual */
     private Data data_atual;

     /** Série de eventos do dia atual */
     private Serie serie_atual;

     /** Lock para garantir a sincronização em operações sobre o dia corrente */
     private ReentrantLock lock = new ReentrantLock();

     public GestorSeries(int s, Data data, Serie serie_atual){
          this.cache = new Cache<>(s);
          this.cacheQueries = new Cache<>(s);
          this.bd = BDSeries.getInstance();
          this.data_atual = data;
          this.serie_atual = serie_atual;
     }

     /** 
      * Método a ser chamado quando o Gestor de Séries for encerrado. Garante que a série do dia atual é guardada na base de dados
     */
     public void close() {
          lock.lock();
          try {
               add(serie_atual);
          } finally {
               lock.unlock();
          }
     }

     /** 
      * Adiciona uma nova série de eventos à base de dados e à cache
      * 
      * @param serie Série de eventos a ser adicionada
      * @return true se a série foi adicionada com sucesso, false caso contrário
      */
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

     /** 
      * Remove uma série de eventos da base de dados e da cache
      * 
      * @param dia Data da série a ser removida
      * @return true se a série foi removida com sucesso, false caso contrário
      */
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

     /** 
      * Verifica se uma série de eventos existe na base de dados ou na cache
      * 
      * @param dia Data da série a ser verificada
      * @return true se a série existe, false caso contrário
      */
     public boolean contains(String dia){
          return cache.containsKey(dia) || bd.containsKey(dia); 
     }

     /** 
      * Obtém uma série de eventos da base de dados ou da cache
      * 
      * @param dia Data da série a ser obtida
      * @return Série de eventos correspondente à data, ou null se não existir
      */
     public Serie get(String dia) {
          if (cache.containsKey(dia)){ // HIT - se está na cache
               return cache.get(dia);
          } else if (bd.containsKey(dia)){ // MISS - se está na BD
               Serie s = bd.get(dia);
               cache.put(dia, s);
               return s;
          }
          return null; // série não existe
     }

     // Métodos sobre a série atual do dia atual

     /** 
      * Passa para o próximo dia, guardando a série atual na base de dados (com recurso a uma thread separada) e iniciando uma nova série para o novo dia
      */
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

     /** 
      * Adiciona um evento à série do dia atual
      * 
      * @param evento Evento a ser adicionado
      */
     public void addSerieAtual(Evento evento) {
          lock.lock();
          try {
               this.serie_atual.add(evento);
          } finally {
               lock.unlock();
          }
     }

     /** 
      * Obtém a série do dia atual
      * 
      * @return Série do dia atual
      */
     public Serie getSerieAtual() {
          lock.lock();
          try {
               return this.serie_atual;
          } finally {
               lock.unlock();
          }
     }

     /** 
      * Obtém a data atual
      * 
      * @return Data atual
      */
     public Data getDataAtual(){
          lock.lock();
          try {
               return this.data_atual;
          } finally {
               lock.unlock();
          }
     }

     /** 
      * Imprime o conteúdo da base de dados de séries
      */
     public void print(){
          this.bd.print();
     }

     // Métodos das Queries de Agregação

     /** 
      * Calcula a quantidade de vendas de um produto num determinado número de dias
      * 
      * @param produto Produto a ser consultado
      * @param dias Número de dias a considerar
      * @return Quantidade total de vendas do produto nos dias especificados
      */
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

     /** 
      * Calcula o volume de vendas de um produto num determinado número de dias
      * 
      * @param produto Produto a ser consultado
      * @param dias Número de dias a considerar
      * @return Volume total de vendas do produto nos dias especificados
      */
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

     /** 
      * Calcula o preço médio de um produto num determinado número de dias
      * 
      * @param produto Produto a ser consultado
      * @param dias Número de dias a considerar
      * @return Preço médio do produto nos dias especificados
      */
     public double calcPrecoMedio(String produto, int dias) {
          double volume = calcVolumeVendas(produto, dias);
          int quantidade = calcQuantidadeVendas(produto, dias);
          return (quantidade == 0) ? 0.0 : volume / quantidade;
     }

     /** 
      * Calcula o preço máximo de um produto num determinado número de dias
      * 
      * @param produto Produto a ser consultado
      * @param dias Número de dias a considerar
      * @return Preço máximo do produto nos dias especificados
      */
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

     /** 
      * Filtra eventos de produtos específicos num determinado dia
      * 
      * @param produtos Lista de produtos a serem filtrados
      * @param dias Dia a considerar
      * @return Lista de eventos filtrados
      */
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

     /** 
      * Obtém o resultado de uma agregação guardada em cache
      */
     private Double getCachedQuery(String dia, TipoMsg tipo, String produto) {
          Map<TipoMsg, Map<String, Double>> byTipo = cacheQueries.get(dia);
          if (byTipo == null) return null;

          Map<String, Double> byProduto = byTipo.get(tipo);
          if (byProduto == null) return null;

          return byProduto.get(produto);
     }

     /** 
      * Armazena o resultado de uma agregação em cache
      */
     private void putCachedQuery(String dia, TipoMsg tipo, String produto, double valor) {
          cacheQueries.putIfAbsent(dia, new java.util.EnumMap<>(TipoMsg.class));
          Map<TipoMsg, Map<String, Double>> byTipo = cacheQueries.get(dia);

          byTipo.putIfAbsent(tipo, new java.util.HashMap<>());
          byTipo.get(tipo).put(produto, valor);
     }
}