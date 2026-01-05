package utils.structs.server;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Cache genérica thread-safe com capacidade limitada */
public class Cache<K,V>{

     /** Capacidade máxima da cache */
     private final int capacidade;

     /** Mapa que armazena os pares chave-valor da cache */
     private final Map<K,V> map;

     /** Lock para sincronização de acesso à cache */
     private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

     /** Lock para escrita na cache */
     private final ReentrantReadWriteLock.WriteLock writelock =  lock.writeLock();

     /** Lock para leitura na cache */
     private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();

     /** 
      * Construtor da cache com capacidade especificada
      * 
      * @param capacidade Capacidade máxima da cache
      * @return Uma nova instância de Cache
      */
     public Cache(int capacidade){
          this.capacidade = capacidade;
          this.map = new LinkedHashMap<>(capacidade, 1.0f, true){
               @Override
               protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    // Remove o elemento mais antigo quando exceder a capacidade
                    return size() > Cache.this.capacidade;
               }
          };
     }

     /** 
      * Obtém o valor associado à chave especificada. Usa write lock porque a cache é LRU, logo, uma simples leitura poderá alterar o estado da cache.
      * 
      * @param key Chave cujo valor deve ser obtido
      * @return Valor associado à chave, ou null se a chave não existir
      */
     public V get(K key){
          writelock.lock();
          try{
               return this.map.get(key);
          }finally{
               writelock.unlock();
          }
     }

     /** 
      * Adiciona um par chave-valor à cache
      * 
      * @param key Chave do valor a ser adicionado
      * @param value Valor a ser adicionado
      */
     public void put(K key, V value){
          writelock.lock();
          try{
               map.put(key, value);
          }finally{
               writelock.unlock();
          }
     }

     /** 
      * Obtém o tamanho atual da cache
      * 
      * @return Número de elementos na cache
      */
     public int size(){
          readLock.lock();
          try{
               return map.size();
          }finally{
               readLock.unlock();
          }
     }

     /** 
      * Verifica se a cache contém a chave especificada
      * 
      * @param key Chave a ser verificada
      * @return true se a chave existir na cache, false caso contrário
      */
     public boolean containsKey(K key){
          readLock.lock();
          try{
               return map.containsKey(key);
          }finally{
               readLock.unlock();
          }
     }

     /** 
      * Remove o valor associado à chave especificada da cache
      * 
      * @param key Chave do valor a ser removido
      * @return true se o valor foi removido, false caso contrário
      */
     public boolean remove(K key){
          writelock.lock();
          try {
               map.remove(key);
               return true;
          } finally {
               writelock.unlock();
          }
     }

     /** 
      * Adiciona um par chave-valor à cache se a chave não existir
      * 
      * @param key Chave do valor a ser adicionado
      * @param value Valor a ser adicionado
      */
     public void putIfAbsent(K key, V value){
          writelock.lock();
          try {
               map.putIfAbsent(key, value);
          } finally {
               writelock.unlock();
          }
     }

     /** 
      * Imprime o estado atual da cache
      */
     public void print() {
          readLock.lock();
          try {
               // imprime as chaves na cache
               System.out.println("[CACHE]: Estado atual da cache: " + map.keySet());
          } finally {
               readLock.unlock();
          }
     }

}