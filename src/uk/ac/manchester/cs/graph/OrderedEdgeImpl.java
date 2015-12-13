package uk.ac.manchester.cs.graph;

import uk.ac.manchester.cs.graph.EdgeImpl;
import uk.ac.manchester.cs.graph.OrderedEdge;
import uk.ac.manchester.cs.graph.OrderedVertex;

public class OrderedEdgeImpl<V, E> extends EdgeImpl<V, E> implements OrderedEdge<V, E> {
   public OrderedEdgeImpl(OrderedVertex<V> start, OrderedVertex<V> end) {
      super(start, end);
   }

   public OrderedEdgeImpl(E object, OrderedVertex<V> start, OrderedVertex<V> end) {
      super(object, start, end);
   }

   public OrderedVertex<V> getStart() {
      return (OrderedVertex)super.getStart();
   }

   public OrderedVertex<V> getEnd() {
      return (OrderedVertex)super.getEnd();
   }

   public int compareTo(OrderedEdge<V, E> edge) {
      int s = this.getStart().getOrder() - edge.getStart().getOrder();
      int l = this.getEnd().getOrder() - edge.getEnd().getOrder();
      return s < 0?-1:(s > 0?1:l);
   }
}
