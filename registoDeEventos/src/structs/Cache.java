package structs;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


public class Cache<K,V>{
     private final int capacidade;
     private final Map<K,V> map;
     private final ReentrantLock lock = new ReentrantLock();

     public Cache(int capacidade){
          this.capacidade = capacidade;
          this.map = new LinkedHashMap<>(capacidade, 1.0f, true){
               protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    // Remove o elemento mais antigo quando exceder a capacidade
                    return size() > Cache.this.capacidade;
               }
          };
     }

     public V get(K key){
          lock.lock();
          try{
               return map.get(key);
          }finally{
               lock.unlock();
          }
     }

     public void put(K key, V value){
          lock.lock();
          try{
               map.put(key, value);
          }finally{
               lock.unlock();
          }
     }

     public int size(){
          lock.lock();
          try{
               return map.size();
          }finally{
               lock.unlock();
          }
     }

     public boolean containsKey(K key){
          lock.lock();
          try{
               return map.containsKey(key);
          }finally{
               lock.unlock();
          }
     }

     public boolean remove(K key,V value){
          lock.lock();
          try {
               return map.remove(key, value);
          } finally {
               lock.unlock();
          }
     }

     // Para depuração: mostra o estado da cache
     @Override
     public String toString() {
          lock.lock();
          try {
               return map.toString();
          } finally {
               lock.unlock();
          }
     }

}