package uk.ac.manchester.cs.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import uk.ac.manchester.cs.graph.Node;

public class Tree<T> {
   private Node<T> m_root;

   public Tree(T root) {
      this.init(root);
   }

   public Node<T> getRoot() {
      return this.m_root;
   }

   public void setRoot(Node<T> root) {
      this.m_root = root;
      root.setParent((Node)null);
   }

   public List<Node<T>> nodeList(Node<T> start) {
      ArrayList list = new ArrayList();
      this.walk(start, list, false);
      return list;
   }

   public List<T> dataList(Node<T> start) {
      ArrayList list = new ArrayList();
      this.dataWalk(start, list, false);
      return list;
   }

   public List<Node<T>> getLeafs(Node<T> start) {
      ArrayList list = new ArrayList();
      this.walk(start, list, true);
      return list;
   }

   public List<T> getLeafObjects(Node<T> start) {
      ArrayList list = new ArrayList();
      this.dataWalk(start, list, true);
      return list;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      this.m_root.output(sb, 0);
      return sb.toString();
   }

   private void walk(Node<T> element, List<Node<T>> list, boolean leafsOnly) {
      if((!leafsOnly || element.isLeaf()) && !element.isDead()) {
         list.add(element);
      }

      Iterator i$ = element.getChildren().iterator();

      while(i$.hasNext()) {
         Node data = (Node)i$.next();
         if(!data.isDead()) {
            this.walk(data, list, leafsOnly);
         }
      }

   }

   private void dataWalk(Node<T> element, List<T> list, boolean leafsOnly) {
      if((!leafsOnly || element.isLeaf()) && !element.isDead()) {
         list.add(element.getObject());
      }

      Iterator i$ = element.getChildren().iterator();

      while(i$.hasNext()) {
         Node data = (Node)i$.next();
         if(!data.isDead()) {
            this.dataWalk(data, list, leafsOnly);
         }
      }

   }

   public Tree<T> cloneTree() {
      Tree tree = this.newTree();
      if(null != this.m_root) {
         Node root = this.m_root.cloneNode((Node)null);
         tree.setRoot(root);
      }

      return tree;
   }

   protected Tree<T> newTree() {
      return new Tree((Object)null);
   }

   protected void init(T object) {
      this.m_root = new Node((Node)null, object);
   }
}
