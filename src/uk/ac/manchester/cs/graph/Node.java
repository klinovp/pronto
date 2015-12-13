package uk.ac.manchester.cs.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Node<T> {
   private T m_object;
   private List<Node<T>> m_children;
   private Node<T> m_parent;

   public Node(Node<T> parent) {
      this.m_parent = parent;
   }

   public Node(Node<T> parent, T object) {
      this(parent);
      this.setObject(object);
   }

   public Node<T> getParent() {
      return this.m_parent;
   }

   protected void setParent(Node<T> parent) {
      this.m_parent = parent;
   }

   public List<Node<T>> getChildren() {
      if(this.m_children == null) {
         this.m_children = new ArrayList();
      }

      return this.m_children;
   }

   public void setChildren(List<Node<T>> children) {
      this.m_children = children;
   }

   public int getNumberOfChildren() {
      return this.m_children == null?0:this.m_children.size();
   }

   public boolean isLeaf() {
      return this.getNumberOfChildren() == 0;
   }

   public boolean isRoot() {
      return null == this.m_parent;
   }

   public Node<T> addChild(T child) {
      Node childNode = this.newNode(this);
      childNode.setObject(child);
      if(this.m_children == null) {
         this.m_children = new ArrayList();
      }

      this.m_children.add(childNode);
      return childNode;
   }

   public void setChildAt(Node<T> child, int index) {
      if(null != child && null != this.m_children && index < this.m_children.size()) {
         child.setParent(this);
         this.m_children.set(index, child);
      }

   }

   public void removeChildren() {
      this.m_children.clear();
   }

   public Node<T> getChildAt(int index) {
      return null != this.m_children && index < this.m_children.size()?(Node)this.m_children.get(index):null;
   }

   protected void copyFrom(Node<T> node) {
      List children = node.getChildren();
      if(null != children) {
         this.m_children = new ArrayList(children.size());
         Iterator i$ = children.iterator();

         while(i$.hasNext()) {
            Node child = (Node)i$.next();
            this.m_children.add(child.cloneNode(this));
         }
      }

      this.copyObject(node);
   }

   public T getObject() {
      return this.m_object;
   }

   public void setObject(T data) {
      this.m_object = data;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{").append(this.getObject().toString()).append(",[");
      int i = 0;

      for(Iterator i$ = this.getChildren().iterator(); i$.hasNext(); ++i) {
         Node e = (Node)i$.next();
         if(i > 0) {
            sb.append(",");
         }

         sb.append(e.getObject().toString());
      }

      sb.append("]").append("}");
      return sb.toString();
   }

   protected void output(StringBuffer sb, int level) {
      sb.append(System.getProperty("line.separator"));

      for(int i$ = 0; i$ < level; ++i$) {
         sb.append("\t");
      }

      sb.append(this.m_object.toString());
      Iterator var5 = this.getChildren().iterator();

      while(var5.hasNext()) {
         Node child = (Node)var5.next();
         child.output(sb, level + 1);
      }

   }

   public Node<T> cloneNode(Node<T> parent) {
      Node node = this.newNode(parent);
      node.copyFrom(this);
      return node;
   }

   protected Node<T> newNode(Node<T> parent) {
      return new Node(parent);
   }

   protected boolean isDead() {
      return null == this.m_object;
   }

   protected void copyObject(Node<T> node) {
      this.m_object = node.getObject();
   }
}
