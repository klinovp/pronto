/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.Collection;
import java.util.Set;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Mar 23, 2008
 */
public interface HittingSetsAlgorithm<T> {

	public Set<Set<T>> compute(Collection<Set<T>> conflictSets);
	public Set<Set<T>> addConflictSet(Set<T> conflictSet);
}
