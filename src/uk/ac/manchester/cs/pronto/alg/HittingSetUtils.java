/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class HittingSetUtils {

	/*
	 * Updates hitting sets given a new conflict set
	 */
	public static <T> Set<Set<T>> updateHittingSets(Set<Set<T>> hSets, Set<T> conflictSet) {
		
		Set<Set<T>> newHittingSets = new HashSet<Set<T>>(hSets.size() * 1/2);
		//First get those hitting sets which do not require updates
		for (T element : conflictSet) {
			
			for (Set<T> hSet : hSets) {
				
				if (hSet.contains( element )) {
					
					newHittingSets.add( hSet );
				}
			}
			
			hSets.removeAll( newHittingSets );
		}
		//Now update others and drop any non-minimal ones
		for (T element : conflictSet) {
			
			for (Set<T> hSet : hSets) {
				
				Set<T> newHS = new HashSet<T>(hSet.size() + 1);
				
				newHS.addAll( hSet );
				newHS.add( element );
				
				addToHittingSets( newHittingSets, newHS );
			}
		}
		
		return newHittingSets;
	}
	
	protected static <T> void addToHittingSets(Set<Set<T>> hittingSets, Set<T> newSet) {
		
		Set<Set<T>> tmpSet = new HashSet<Set<T>>();
		
		for (Set<T> hs : hittingSets) {
			
			if (hs.containsAll( newSet )) {
				//One of the existing hitting sets is not minimal. Mark it for removal.
				tmpSet.add( hs );
			} 
			//Check if the new set is minimal. If not - exit immediately
			if (newSet.containsAll( hs )) return;
		}
		
		hittingSets.add( newSet );
		hittingSets.removeAll( tmpSet );
	}
}
