/**
 * 
 */
package uk.ac.manchester.cs.pronto.query;

import java.io.IOException;
import java.io.Writer;

/**
 * <p>Title: PQueryResult</p>
 * 
 * <p>Description: 
 *  Represents an answer to a probabilistic query
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public interface PQueryResult {

	public void serialize(Writer writer) throws IOException;
}
