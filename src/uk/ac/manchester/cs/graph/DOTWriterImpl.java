package uk.ac.manchester.cs.graph;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;

public class DOTWriterImpl<V, E> implements GraphWriter<V, E> {

   static String LFCR = System.getProperty("line.separator");
   private float m_width = 7.0F;
   private float m_height = 7.0F;
   private float m_nodeSize = 0.4F;
   private float m_fontSize = 10.0F;
   private float m_edgeLength = 1.0F;

   private GraphNodeWriter<V> m_nodeWriter = new GraphNodeWriter<V>() {

      public void write(V vertex, Writer out) throws IOException {
         out.write(vertex.toString());
      }
   };

   public void setSize(float width, float height) {
      this.m_width = width;
      this.m_height = height;
   }

   public void write(Graph<V, E> graph, Writer writer) throws IOException {
      HashSet<Edge<V,E>> edges = new HashSet<>();
      writer.write("graph G {");
      writer.write("size=\"" + this.m_width + "," + this.m_height + "\";" + LFCR);
      Iterator<Vertex<V>> i$ = graph.getVertices().iterator();

      Vertex<V> vertex;
      while(i$.hasNext()) {
         vertex = i$.next();
         this.writeVertex(vertex, writer);
         writer.write("[height=" + this.m_nodeSize + ", width=" + this.m_nodeSize + ", fontsize=" + this.m_fontSize + "];" + LFCR);
      }

      i$ = graph.getVertices().iterator();

      while(i$.hasNext()) {
         vertex = i$.next();
         Iterator<Edge<V,E>> i$1 = graph.getIncidentEdges(vertex).iterator();

         while(i$1.hasNext()) {
            Edge<V,E> incident = i$1.next();
            if(!edges.contains(incident)) {
               this.writeVertex(incident.getStart(), writer);
               writer.write(" -- ");
               this.writeVertex(incident.getEnd(), writer);
               writer.write("[label=\"" + incident.getObject() + "\", ");
               writer.write("len=" + this.m_edgeLength + ", fontsize=" + this.m_fontSize + "];" + LFCR);
               edges.add(incident);
            }
         }
      }

      writer.write("}");
   }

   private void writeVertex(Vertex<V> vertex, Writer writer) throws IOException {
      writer.write("\"");
      this.m_nodeWriter.write(vertex.getObject(), writer);
      writer.write("\" ");
   }

   public void setNodeSize(float size) {
      this.m_nodeSize = size;
   }

   public void setFontSize(float size) {
      this.m_fontSize = size;
   }

   public void setEdgeLength(float length) {
      this.m_edgeLength = length;
   }

   public void setNodeWriter(GraphNodeWriter<V> writer) {
      this.m_nodeWriter = writer;
   }
}
