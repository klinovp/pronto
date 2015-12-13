/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.Collection;
import java.util.Set;

/**
 * Iterative algorithm which computes minimal hitting sets one after another
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface  IterativeHittingSetAlgorithm<T> {

	public void setSets(Collection<Set<T>> sets);
	public Set<T> next();
	public boolean hasNext();
}
