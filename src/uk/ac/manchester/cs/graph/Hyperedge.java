package uk.ac.manchester.cs.graph;

import java.util.Set;
import uk.ac.manchester.cs.graph.Vertex;

public interface Hyperedge<V, E> {
   Set<? extends Vertex<V>> getVertices();

   E getObject();
}
