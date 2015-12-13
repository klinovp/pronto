package uk.ac.manchester.cs.graph;

import java.util.Set;
import uk.ac.manchester.cs.graph.Edge;
import uk.ac.manchester.cs.graph.Vertex;

public interface Graph<V, E> {
   Set<Vertex<V>> getVertices();

   Set<Vertex<V>> getAdjacentVertices(Vertex<V> var1);

   Set<Edge<V, E>> getIncidentEdges(Vertex<V> var1);

   Vertex<V> addVertex(V var1);

   void removeVertex(Vertex<V> var1);

   Edge<V, E> addEdge(E var1, Vertex<V> var2, Vertex<V> var3);

   void removeEdge(Vertex<V> var1, Vertex<V> var2);

   Edge<V, E> findEdge(Vertex<V> var1, Vertex<V> var2);
}
