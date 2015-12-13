package uk.ac.manchester.cs.graph;

public interface Edge<V, E> {
   Vertex<V> getStart();

   Vertex<V> getEnd();

   E getObject();
}
