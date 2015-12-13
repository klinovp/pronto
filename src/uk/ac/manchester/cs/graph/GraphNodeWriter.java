package uk.ac.manchester.cs.graph;

import java.io.IOException;
import java.io.Writer;

public interface GraphNodeWriter<V> {

   void write(V var1, Writer var2) throws IOException;
}
