package uk.ac.manchester.cs.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import uk.ac.manchester.cs.graph.Hyperedge;
import uk.ac.manchester.cs.graph.HyperedgeImpl;
import uk.ac.manchester.cs.graph.Hypergraph;
import uk.ac.manchester.cs.graph.Vertex;
import uk.ac.manchester.cs.graph.VertexImpl;

public class HypergraphImpl<V, E> implements Hypergraph<V, E> {
   private Map<Vertex<V>, Set<HyperedgeImpl<V, E>>> m_vertexMap = new HashMap();
   private Set<Hyperedge<V, E>> m_edges = new HashSet();

   public Hyperedge<V, E> addHyperedge(E object, Collection<? extends Vertex<V>> vertices) {
      HyperedgeImpl edge = this.findHyperedge(vertices);
      if(null == edge) {
         edge = new HyperedgeImpl(object, vertices);
         Iterator i$ = vertices.iterator();

         while(i$.hasNext()) {
            Vertex vertex = (Vertex)i$.next();
            this.initVertex(vertex);
            ((Set)this.m_vertexMap.get(vertex)).add(edge);
         }

         this.m_edges.add(edge);
      }

      return edge;
   }

   public Vertex<V> addVertex(V v) {
      VertexImpl vertex = new VertexImpl(v);
      this.initVertex(vertex);
      return vertex;
   }

   protected void initVertex(Vertex<V> vertex) {
      if(!this.m_vertexMap.containsKey(vertex)) {
         this.m_vertexMap.put(vertex, new HashSet());
      }

   }

   public Set<Vertex<V>> getAdjacentVertices(Vertex<V> vertex) {
      HashSet neighbors = new HashSet();
      Iterator i$ = this.getIncidentHyperedges(vertex).iterator();

      while(i$.hasNext()) {
         HyperedgeImpl incident = (HyperedgeImpl)i$.next();
         neighbors.addAll(incident.getVertices());
      }

      neighbors.remove(vertex);
      return neighbors;
   }

   public Set<Hyperedge<V, E>> getHyperedges() {
      return this.m_edges;
   }

   public Set<HyperedgeImpl<V, E>> getIncidentHyperedges(Vertex<V> vertex) {
      return (Set)this.m_vertexMap.get(vertex);
   }

   public int getVertexDegree(Vertex<V> vertex) {
      Set incidentEdges = this.getIncidentHyperedges(vertex);
      return incidentEdges == null?-1:incidentEdges.size();
   }

   public Set<Vertex<V>> getVertices() {
      return this.m_vertexMap.keySet();
   }

   public void removeHyperedge(Collection<? extends Vertex<V>> vertices) {
      HyperedgeImpl edge = this.findHyperedge(vertices);
      Iterator i$ = vertices.iterator();

      while(i$.hasNext()) {
         Vertex vertex = (Vertex)i$.next();
         this.getIncidentHyperedges(vertex).remove(edge);
      }

      this.m_edges.remove(edge);
   }

   public HyperedgeImpl<V, E> findHyperedge(Collection<? extends Vertex<V>> vertices) {
      Vertex some = null;
      HyperedgeImpl edge = null;
      Iterator i$ = vertices.iterator();

      while(i$.hasNext()) {
         Vertex incident = (Vertex)i$.next();
         if(this.m_vertexMap.containsKey(incident)) {
            some = incident;
            break;
         }
      }

      if(null == some) {
         return null;
      } else {
         i$ = ((Set)this.m_vertexMap.get(some)).iterator();

         while(i$.hasNext()) {
            HyperedgeImpl incident1 = (HyperedgeImpl)i$.next();
            if(incident1.getVertices().size() == vertices.size() && incident1.getVertices().containsAll(vertices)) {
               edge = (HyperedgeImpl)incident1;
               break;
            }
         }

         return edge;
      }
   }

   protected void removeHyperedge(HyperedgeImpl<V, E> edge) {
      Iterator i$ = edge.getVertices().iterator();

      while(i$.hasNext()) {
         Vertex vertex = (Vertex)i$.next();
         ((Set)this.m_vertexMap.get(vertex)).remove(edge);
      }

      this.m_edges.remove(edge);
   }

   public void removeVertex(Vertex<V> vertex) {
      Iterator i$ = this.getIncidentHyperedges(vertex).iterator();

      while(i$.hasNext()) {
         HyperedgeImpl incident = (HyperedgeImpl)i$.next();
         incident.removeVertex(vertex);
         if(incident.getVertices().size() < 2) {
            this.m_edges.remove(incident);
         }
      }

      this.m_vertexMap.remove(vertex);
   }

   public boolean isConnected() {
      HashSet previous = new HashSet(this.m_vertexMap.size());
      HashSet frontier = new HashSet(this.m_vertexMap.size());
      if(!this.m_vertexMap.isEmpty()) {
         frontier.add(this.m_vertexMap.keySet().iterator().next());
         this.bfs(frontier, previous);
         return previous.size() == this.m_vertexMap.size();
      } else {
         return true;
      }
   }

   protected void bfs(Set<Vertex<V>> frontier, Set<Vertex<V>> previous) {
      while(!frontier.isEmpty()) {
         Vertex vertex = (Vertex)frontier.iterator().next();
         Set neighbors = this.getAdjacentVertices(vertex);
         previous.add(vertex);
         frontier.remove(vertex);
         neighbors.removeAll(previous);
         frontier.addAll(neighbors);
      }

   }
}
