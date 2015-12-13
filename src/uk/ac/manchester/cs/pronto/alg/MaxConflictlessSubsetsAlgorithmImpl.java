/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Mar 24, 2008
 */
public class MaxConflictlessSubsetsAlgorithmImpl implements MaxConflictlessSubsetsAlgorithm {

	protected HittingSetsAlgorithmImpl m_hsAlg = new HittingSetsAlgorithmImpl();
	
	public <T> Set<Set<T>> compute(Set<T> set, Collection<Set<T>> conflictSets) {
		
		/*
		 * We first compute all hitting sets
		 */
		Set<Set<T>> hsSets = m_hsAlg.compute( conflictSets );
		
		if (hsSets.isEmpty()) return Collections.singleton(set);
		/*
		 * Next we select the preferred hitting sets
		 */
		leavePreferredHittingSets(hsSets);
		/*
		 * Finally we select max conflictless subsets
		 */
		return selectMaxConflictlessSubsets(set, hsSets);
	}
	
	/*
	 * Simple implementation - leave those that are minimal in terms of cardinality
	 */
	protected <T> void leavePreferredHittingSets(Set<Set<T>> hsSets) {
		
		/*
		 * Naive implementation - two iterations over the set. Yes, I know it can be faster
		 */
		HashSet<Set<T>> toBeRemoved = new HashSet<Set<T>>();
		int cardinality = Integer.MAX_VALUE;
		
		for (Set<T> hsSet : hsSets) {
			
			cardinality = (hsSet.size() < cardinality) ? hsSet.size() : cardinality;
		}
		
		for (Set<T> hsSet : hsSets) {
			
			if (hsSet.size() > cardinality) {
				
				toBeRemoved.add( hsSet );
			}
		}
		
		hsSets.removeAll( toBeRemoved );
	}
	
	protected <T> Set<Set<T>> selectMaxConflictlessSubsets(Set<T> set, Collection<Set<T>> hsSets) {
		
		HashSet<Set<T>> results = new HashSet<Set<T>>(hsSets.size());		
		
		for (Set<T> hsSet : hsSets) {
			
			HashSet<T> maxSet = new HashSet<T>(set);
			
			maxSet.removeAll( hsSet );
			results.add( maxSet );
		}
		
		return results;
	}

}
