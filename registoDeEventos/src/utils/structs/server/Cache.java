package utils.structs.server;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Cache<K,V>{
     private final int capacidade;
     private final Map<K,V> map;
     private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
     private final ReentrantReadWriteLock.WriteLock writelock =  lock.writeLock();
     private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();

     public Cache(int capacidade){
          this.capacidade = capacidade;
          this.map = new LinkedHashMap<>(capacidade, 1.0f, false){
               @Override
               protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    // Remove o elemento mais antigo quando exceder a capacidade
                    return size() > Cache.this.capacidade;
               }
          };
     }

     public V get(K key){
          readLock.lock();
          try{
               if (this.map.containsKey(key)){
                    return this.map.get(key);
               }
               return null;
          }finally{
               readLock.unlock();
          }
     }

     public void put(K key, V value){
          writelock.lock();
          try{
               map.put(key, value);
          }finally{
               writelock.unlock();
          }
     }

     public int size(){
          readLock.lock();
          try{
               return map.size();
          }finally{
               readLock.unlock();
          }
     }

     public boolean containsKey(K key){
          readLock.lock();
          try{
               return map.containsKey(key);
          }finally{
               readLock.unlock();
          }
     }

     public boolean remove(K key,V value){
          writelock.lock();
          try {
               return map.remove(key, value);
          } finally {
               writelock.unlock();
          }
     }

     public boolean remove(K key){
          writelock.lock();
          try {
               map.remove(key);
               return true;
          } finally {
               writelock.unlock();
          }
     }

     public void putIfAbsent(K key, V value){
          writelock.lock();
          try {
               map.putIfAbsent(key, value);
          } finally {
               writelock.unlock();
          }
     }

     // Para depuração: mostra o estado da cache
     @Override
     public String toString() {
          readLock.lock();
          try {
               return map.toString();
          } finally {
               readLock.unlock();
          }
     }

}