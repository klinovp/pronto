/**
 * 
 */
package uk.ac.manchester.cs.pronto.io;

import java.io.IOException;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Serializes conditional constraints in some format
 */
public interface CCSerializer {

	public void serialize(ConditionalConstraint cc) throws IOException;
}
