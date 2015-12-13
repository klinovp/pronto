package uk.ac.manchester.cs.graph;

import uk.ac.manchester.cs.graph.OrderedVertex;
import uk.ac.manchester.cs.graph.VertexImpl;

public class OrderedVertexImpl<V> extends VertexImpl<V> implements OrderedVertex<V> {
   public int m_order;

   public OrderedVertexImpl(V object) {
      super(object);
   }

   protected OrderedVertexImpl(V object, int order) {
      super(object);
      this.m_order = order;
   }

   public int getOrder() {
      return this.m_order;
   }

   protected void setOrder(int order) {
      this.m_order = order;
   }

   public int compareTo(OrderedVertex<V> arg) {
      return this.m_order - arg.getOrder();
   }
}
