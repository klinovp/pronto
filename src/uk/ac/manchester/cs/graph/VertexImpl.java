package uk.ac.manchester.cs.graph;

import uk.ac.manchester.cs.graph.Vertex;

public class VertexImpl<V> implements Vertex<V> {
   public V m_object = null;

   public VertexImpl(V object) {
      this.m_object = object;
   }

   public V getObject() {
      return this.m_object;
   }

   public boolean equals(Object obj) {
      return obj instanceof Vertex && ((Vertex)obj).getObject().equals(this.m_object);
   }

   public int hashCode() {
      return this.m_object.hashCode();
   }

   public String toString() {
      return "V" + this.m_object;
   }
}
