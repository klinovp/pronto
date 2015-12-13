package uk.ac.manchester.cs.graph;

public class KeyValue<K extends Comparable<K>, V> implements Comparable<KeyValue<K, ? extends Object>> {
   private K m_key;
   private V m_value;

   public KeyValue(K key, V value) {
      this.m_key = key;
      this.m_value = value;
   }

   public K getKey() {
      return this.m_key;
   }

   public V getValue() {
      return this.m_value;
   }

   public int compareTo(KeyValue<K, ? extends Object> arg) {
      return this.m_key.compareTo(arg.getKey());
   }
}
