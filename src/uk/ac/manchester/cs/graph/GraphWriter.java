package uk.ac.manchester.cs.graph;

import java.io.IOException;
import java.io.Writer;
import uk.ac.manchester.cs.graph.Graph;
import uk.ac.manchester.cs.graph.GraphNodeWriter;

public interface GraphWriter<V, E> {
   void write(Graph<V, E> var1, Writer var2) throws IOException;

   void setNodeWriter(GraphNodeWriter<V> var1);
}
