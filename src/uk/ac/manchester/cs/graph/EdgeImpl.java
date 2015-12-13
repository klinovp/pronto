package uk.ac.manchester.cs.graph;

import uk.ac.manchester.cs.graph.Edge;
import uk.ac.manchester.cs.graph.Vertex;

public class EdgeImpl<V, E> implements Edge<V, E> {
   private E m_object;
   private Vertex<V> m_start;
   private Vertex<V> m_end;

   public EdgeImpl(Vertex<V> start, Vertex<V> end) {
      this.m_object = null;
      this.m_start = null;
      this.m_end = null;
      this.m_start = start;
      this.m_end = end;
   }

   public EdgeImpl(E object, Vertex<V> start, Vertex<V> end) {
      this(start, end);
      this.m_object = object;
   }

   public Vertex<V> getEnd() {
      return this.m_end;
   }

   public E getObject() {
      return this.m_object;
   }

   public Vertex<V> getStart() {
      return this.m_start;
   }

   public boolean equals(Object obj) {
      return obj instanceof Edge && this.m_start.equals(((Edge)obj).getStart()) && this.m_end.equals(((Edge)obj).getEnd());
   }

   public int hashCode() {
      return this.m_start.hashCode() + this.m_end.hashCode();
   }

   public String toString() {
      return "<" + this.m_start + "," + this.m_end + ">";
   }
}
