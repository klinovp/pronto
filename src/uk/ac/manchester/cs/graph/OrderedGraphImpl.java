package uk.ac.manchester.cs.graph;

import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class OrderedGraphImpl<V, E> extends GraphImpl<V, E> implements OrderedGraph<V, E> {
   private SortedMap<Vertex<V>, SortedSet<OrderedEdge<V, E>>> m_vertexMap = new TreeMap<>();

   protected void deleteVertex(Vertex<V> vertex) {
      this.m_vertexMap.remove(vertex);
   }

   public Set<Vertex<V>> getVertices() {
      return this.m_vertexMap.keySet();
   }

   public Set<Edge<V, E>> getIncidentEdges(Vertex<V> vertex) {
      return super.getIncidentEdges(vertex);
   }

   protected void initVertex(Vertex<V> vertex) {
      this.m_vertexMap.put(vertex, new TreeSet<OrderedEdge<V, E>>());
   }

   protected void removeEdge(EdgeImpl<V, E> edge) {
      this.getIncidentEdges(edge.getStart()).remove(edge);
      this.getIncidentEdges(edge.getEnd()).remove(edge);
   }

   protected int nextIndex() {
      return this.m_vertexMap.isEmpty()?0:((OrderedVertex)this.m_vertexMap.lastKey()).getOrder() + 1;
   }

   public Vertex<V> addVertex(V object) {
      OrderedVertexImpl<V> vertex = new OrderedVertexImpl<>(object, this.nextIndex());
      this.initVertex(vertex);

      return vertex;
   }

   public Edge<V, E> addEdge(E object, OrderedVertex<V> start, OrderedVertex<V> end) {
	   Edge<V, E> edge = null;

      if(null == (edge = this.findEdge(start, end))) {
         edge = new OrderedEdgeImpl<>(object, start, end);
         this.addVertex(start.getObject());
         this.addVertex(end.getObject());
         this.getIncidentEdges(start).add(edge);
         this.getIncidentEdges(end).add(edge);
      }

      return edge;
   }
}
