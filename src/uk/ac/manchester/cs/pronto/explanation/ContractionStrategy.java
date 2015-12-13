/**
 * 
 */
package uk.ac.manchester.cs.pronto.explanation;

import java.util.Collection;
import java.util.Set;

import aterm.ATermAppl;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface ContractionStrategy {

	
	public Set<ATermAppl> prune(Collection<ATermAppl> conjucnts, SATChecker checker);
}
