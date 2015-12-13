package uk.ac.manchester.cs.graph;

import uk.ac.manchester.cs.graph.Edge;
import uk.ac.manchester.cs.graph.OrderedVertex;

public interface OrderedEdge<V, E> extends Edge<V, E>, Comparable<OrderedEdge<V, E>> {
   OrderedVertex<V> getStart();

   OrderedVertex<V> getEnd();
}
