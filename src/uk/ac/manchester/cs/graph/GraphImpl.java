package uk.ac.manchester.cs.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import uk.ac.manchester.cs.graph.Edge;
import uk.ac.manchester.cs.graph.EdgeImpl;
import uk.ac.manchester.cs.graph.Graph;
import uk.ac.manchester.cs.graph.Vertex;
import uk.ac.manchester.cs.graph.VertexImpl;

public class GraphImpl<V, E> implements Graph<V, E> {
   private Map<Vertex<V>, Set<Edge<V, E>>> m_vertexMap = new HashMap();

   public Edge<V, E> addEdge(E object, Vertex<V> start, Vertex<V> end) {
      EdgeImpl edge = null;
      if(null == (edge = this.findEdge(start, end))) {
         edge = new EdgeImpl(object, start, end);
         this.addVertex(start.getObject());
         this.addVertex(end.getObject());
         this.getIncidentEdges(start).add(edge);
         this.getIncidentEdges(end).add(edge);
      }

      return edge;
   }

   public Set<Edge<V, E>> getIncidentEdges(Vertex<V> vertex) {
      return (Set)this.m_vertexMap.get(vertex);
   }

   protected void deleteVertex(Vertex<V> vertex) {
      this.m_vertexMap.remove(vertex);
   }

   protected void initVertex(Vertex<V> vertex) {
      if(!this.m_vertexMap.containsKey(vertex)) {
         this.m_vertexMap.put(vertex, new HashSet());
      }

   }

   public Vertex<V> addVertex(V object) {
      VertexImpl vertex = new VertexImpl(object);
      this.initVertex(vertex);
      return vertex;
   }

   public Set<Vertex<V>> getAdjacentVertices(Vertex<V> vertex) {
      Set incidentEdges = this.getIncidentEdges(vertex);
      if(incidentEdges == null) {
         return null;
      } else {
         HashSet neighbors = new HashSet();
         Iterator i$ = incidentEdges.iterator();

         while(i$.hasNext()) {
            Edge incident = (Edge)i$.next();
            neighbors.add(incident.getStart());
            neighbors.add(incident.getEnd());
         }

         neighbors.remove(vertex);
         return neighbors;
      }
   }

   public Set<Vertex<V>> getVertices() {
      return this.m_vertexMap.keySet();
   }

   public void removeEdge(Vertex<V> start, Vertex<V> end) {
      EdgeImpl edge = this.findEdge(start, end);
      if(null != edge) {
         this.getIncidentEdges(start).remove(edge);
         this.getIncidentEdges(end).remove(edge);
      }

   }

   public void removeVertex(Vertex<V> vertex) {
      Set incidentEdges = this.getIncidentEdges(vertex);
      if(incidentEdges != null) {
         Iterator i$ = incidentEdges.iterator();

         while(i$.hasNext()) {
            Edge incident = (Edge)i$.next();
            Vertex otherEnd = vertex.equals(incident.getStart())?incident.getEnd():incident.getStart();
            this.getIncidentEdges(otherEnd).remove(incident);
         }

         this.deleteVertex(vertex);
      }

   }

   protected void removeEdge(EdgeImpl<V, E> edge) {
      this.getIncidentEdges(edge.getStart()).remove(edge);
      this.getIncidentEdges(edge.getEnd()).remove(edge);
   }

   public EdgeImpl<V, E> findEdge(Vertex<V> start, Vertex<V> end) {
      EdgeImpl edge = null;
      Iterator i$ = this.getIncidentEdges(start).iterator();

      while(i$.hasNext()) {
         Edge incident = (Edge)i$.next();
         if(incident.getEnd().equals(end) && incident.getStart().equals(start) || incident.getStart().equals(end) && incident.getEnd().equals(start)) {
            edge = (EdgeImpl)incident;
            break;
         }
      }

      return edge;
   }
}
