/**
 * 
 */
package uk.ac.manchester.cs.pronto.io;

import java.io.IOException;
import java.io.Reader;

import uk.ac.manchester.cs.pronto.constraints.ConflictGraph;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface ConflictGraphDeserializer {

	public ConflictGraph deserialize(Reader reader) throws IOException;
}
