/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.Collection;
import java.util.Set;

/**
 * Computes the maximal subset of a system that does not contain any of known conflict sets
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Mar 24, 2008
 */
public interface MaxConflictlessSubsetsAlgorithm {
	
	public <T> Set<Set<T>> compute(Set<T> set, Collection<Set<T>> conflictSets);

}
