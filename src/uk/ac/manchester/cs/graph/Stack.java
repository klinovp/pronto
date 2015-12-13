package uk.ac.manchester.cs.graph;

import java.util.Comparator;

public interface Stack<T> extends Iterable<T> {
   void push(T var1);

   T pop();

   T peek();

   Stack.Entry<T> find(T var1);

   Stack.Entry<T> find(T var1, Comparator<T> var2);

   public static class Entry<T> {
      private T m_object = null;
      private int m_depth = -1;

      Entry(T object, int depth) {
         this.m_object = object;
         this.m_depth = depth;
      }

      public T getObject() {
         return this.m_object;
      }

      public int getDepth() {
         return this.m_depth;
      }
   }
}
