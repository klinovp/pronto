/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.Set;

/**
 * <p>Title: IndexedPTBox</p>
 * 
 * <p>Description: 
 *  PTBox that can store index sets (used for optimization)
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public interface IndexedPTBox extends PTBox {

	public boolean hasNewConstraints();
	public Set<ConditionalConstraint> getNewConstraints();	
}
