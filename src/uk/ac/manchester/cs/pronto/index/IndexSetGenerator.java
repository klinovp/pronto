/**
 * 
 */
package uk.ac.manchester.cs.pronto.index;

import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.cache.IndexCache;

/**
 * <p>Title: IndexSetGenerator</p>
 * 
 * <p>Description: 
 *  Generates an index set for a PTBox
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public interface IndexSetGenerator {

	public IndexSet generate(PTBox ptbox);
	public void useCache(IndexCache cache);
	public void setTermNumber(int size);
}
