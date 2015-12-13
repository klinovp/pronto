package uk.ac.manchester.cs.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.Iterator;
import uk.ac.manchester.cs.graph.Stack;

public class StackImpl<T> extends ArrayList<T> implements Stack<T> {
   private static final long serialVersionUID = 6710851383021328998L;

   public Stack.Entry<T> find(T object) {
      int depth = 0;

      for(Iterator i$ = this.iterator(); i$.hasNext(); ++depth) {
         Object element = i$.next();
         if(element.equals(object)) {
            return new Stack.Entry(element, depth);
         }
      }

      return null;
   }

   public Stack.Entry<T> find(T object, Comparator<T> comparator) {
      throw new UnsupportedOperationException();
   }

   public T peek() {
      if(!this.isEmpty()) {
         return this.get(0);
      } else {
         throw new EmptyStackException();
      }
   }

   public T pop() {
      if(!this.isEmpty()) {
         return this.remove(0);
      } else {
         throw new EmptyStackException();
      }
   }

   public void push(T object) {
      super.add(0, object);
   }

   public Iterator<T> iterator() {
      return super.iterator();
   }
}
