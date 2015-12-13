package uk.ac.manchester.cs.graph;

import java.util.Collection;
import java.util.Set;
import uk.ac.manchester.cs.graph.Hyperedge;
import uk.ac.manchester.cs.graph.Vertex;

public interface Hypergraph<V, E> {
   Set<? extends Vertex<V>> getVertices();

   Set<? extends Hyperedge<V, E>> getHyperedges();

   Hyperedge<V, E> addHyperedge(E var1, Collection<? extends Vertex<V>> var2);

   Vertex<V> addVertex(V var1);

   void removeHyperedge(Collection<? extends Vertex<V>> var1);

   void removeVertex(Vertex<V> var1);

   int getVertexDegree(Vertex<V> var1);

   Set<? extends Vertex<V>> getAdjacentVertices(Vertex<V> var1);

   Set<? extends Hyperedge<V, E>> getIncidentHyperedges(Vertex<V> var1);

   Hyperedge<V, E> findHyperedge(Collection<? extends Vertex<V>> var1);

   boolean isConnected();
}
