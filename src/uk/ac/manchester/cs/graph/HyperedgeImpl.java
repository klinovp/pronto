package uk.ac.manchester.cs.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import uk.ac.manchester.cs.graph.Hyperedge;
import uk.ac.manchester.cs.graph.Vertex;

public class HyperedgeImpl<V, E> implements Hyperedge<V, E> {
   private E m_object = null;
   private Set<Vertex<V>> m_vertices = new HashSet();

   public HyperedgeImpl() {
   }

   public HyperedgeImpl(E object) {
      this.m_object = object;
   }

   public E getObject() {
      return this.m_object;
   }

   public HyperedgeImpl(Collection<? extends Vertex<V>> vertices) {
      this.m_vertices.addAll(vertices);
   }

   public HyperedgeImpl(E object, Collection<? extends Vertex<V>> vertices) {
      this.m_object = object;
      this.m_vertices.addAll(vertices);
   }

   public Set<Vertex<V>> getVertices() {
      return this.m_vertices;
   }

   public void setVertices(Set<Vertex<V>> vertices) {
      this.m_vertices.clear();
      this.m_vertices.addAll(vertices);
   }

   protected void removeVertex(Vertex<V> vertex) {
      this.m_vertices.remove(vertex);
   }

   protected void addVertex(Vertex<V> vertex) {
      this.m_vertices.add(vertex);
   }

   public String toString() {
      return "E" + this.m_object + ": " + this.m_vertices.toString();
   }
}
