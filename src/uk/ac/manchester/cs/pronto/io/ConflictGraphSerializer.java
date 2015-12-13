/**
 * 
 */
package uk.ac.manchester.cs.pronto.io;

import java.io.IOException;

import uk.ac.manchester.cs.pronto.constraints.ConflictGraph;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface ConflictGraphSerializer {

	public void serialize(ConflictGraph cg) throws IOException;
}
