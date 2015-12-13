/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * 24 Sep 2009
 */
public class DumbHittingSetAlgImpl<T> implements HittingSetsAlgorithm<T> {

	private HSStructure<T> m_hsStructure = null;
	/* 
	 */
	@Override
	public Set<Set<T>> compute(Collection<Set<T>> cSets) {

		Set<Set<T>> conflictSets = new HashSet<Set<T>>(cSets); 
		
		if (conflictSets.size() == 1) {
			
			m_hsStructure = new HSStructure<T>(conflictSets.iterator().next());
			
		} else {
			
			Set<T> set = conflictSets.iterator().next();
			
			conflictSets.remove( set );
			compute(conflictSets);
			m_hsStructure.updateStructure(set);
		}
		
		return m_hsStructure.getHittingSets();
	}

	public Set<Set<T>> addConflictSet(Set<T> conflictSet) {
		
		if (m_hsStructure == null) {
			
			return compute( Collections.singleton( conflictSet ) );
			
		} else {
			
			m_hsStructure.updateStructure(conflictSet);
			
			return m_hsStructure.getHittingSets();
		}
	}
}

/*
 * Maps elements to sets of sets that contain them
 */
class HSStructure<T> {
	
	private Map<T, Set<Set<T>>> m_setMap = new HashMap<T, Set<Set<T>>>();
	
	HSStructure() {}
	
	HSStructure(Collection<Set<T>> sets) {
		
		for (Set<T> set : sets) addSet(set);
	}
	
	HSStructure(Set<T> set) {
		
		for (T element : set) {
			
			m_setMap.put( element, Collections.singleton(Collections.singleton(element) ));
		}
	}
	
	void addSet(Set<T> set) {
		
		for (T element : set) addSet( element, set );
	}
	
	private void addSet(T element, Set<T> set) {
		
		Set<Set<T>> containers = m_setMap.get( element );
		
		containers = (containers == null) ? new HashSet<Set<T>>() : containers;
		containers.add( set );
		m_setMap.put( element, containers );
	}
	
	Set<Set<T>> getHittingSets() {
		
		Set<Set<T>> minHSets = new HashSet<Set<T>>();
		
		for (Set<Set<T>> containers : m_setMap.values()) minHSets.addAll( containers );
		
		return minHSets;
	}
	
	/*
	 * Returns all sets that contain given elements
	 */
	Set<Set<T>> getSetsForElements(Set<T> set, boolean remove) {
		
		Set<Set<T>> sets = new HashSet<Set<T>>();
		
		for (T element : set) {
			
			Set<Set<T>> containers = remove ? m_setMap.get( element ) : m_setMap.get( element );
			
			if (containers != null) {
				
				sets.addAll( containers );	
			}
		}
		
		return sets;
	}
	
	
	void updateStructure(Set<T> set) {
	
		Map<T, Set<Set<T>>> newStructure = new HashMap<T, Set<Set<T>>>(m_setMap.size());
		//These hitting sets do not require updates
		Set<Set<T>> newHittingSets = getSetsForElements( set, true );
		//These do
		Set<Set<T>> toBeUpdated = getHittingSets();
		
		toBeUpdated.removeAll( newHittingSets );

		//Now update others and drop any non-minimal ones
		for (T element : set) {
			
			for (Set<T> hSet : toBeUpdated) {
				
				Set<T> newHS = new HashSet<T>(hSet.size() + 1);
				
				newHS.addAll( hSet );
				newHS.add( element );
				
				HittingSetUtils.addToHittingSets( newHittingSets, newHS );
			}
		}
		
		m_setMap = newStructure;
		
		for (Set<T> newHS : newHittingSets) addSet(newHS);
	}	
	

	
}
