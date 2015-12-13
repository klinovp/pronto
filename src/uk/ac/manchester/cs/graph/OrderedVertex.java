package uk.ac.manchester.cs.graph;

import uk.ac.manchester.cs.graph.Vertex;

public interface OrderedVertex<V> extends Vertex<V>, Comparable<OrderedVertex<V>> {
   int getOrder();
}
