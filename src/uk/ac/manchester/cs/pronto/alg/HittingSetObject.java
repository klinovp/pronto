/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.Set;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface HittingSetObject<T> {
	
	public Set<Set<T>> leftSubset(T element);
	public Set<Set<T>> rightSubset(T element);
	public T getElement();
	public Set<Set<T>> getConflictSets();
	public void setConflictSets(Set<Set<T>> conflictSets);
	public Set<Set<T>> getMinimalHittingSets();
	public void setMinimalHittingSets(Set<Set<T>> hittingSets);
	public void setChanged(boolean changed);
	public boolean isChanged();
}
