/**
 * 
 */
package uk.ac.manchester.cs.pronto.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Apr 29, 2009
 */
public class CollectionUtils {

	/*
	 * Generates the collection of all permutations of domain values (whatever they are)
	 */
	public static <T> Collection<List<T>> combinations(List<Collection<T>> domains) {
		
		List<List<T>> perms = new ArrayList<List<T>>();
		
		perms.add( new ArrayList<T>() );
		addCombination(perms, domains, 0);
		
		return perms;		
	}
	
	
	private static <T> void addCombination(List<List<T>> perms, List<Collection<T>> domains, int i) {
		
		if( i < domains.size() ) {
			
			for( int permIndex = 0;  permIndex < perms.size(); ) {
				
				List<T> perm = perms.get( permIndex );
				
				perms.remove( permIndex );
				
				if (domains.get( i ).isEmpty()) {
					
					perms.clear();
					return;
				}
				
				for (T domValue : domains.get( i )) {
					
					List<T> newPerm = new ArrayList<T>( perm );
					
					newPerm.add(domValue);
					perms.add( permIndex++, newPerm );
				}
			}
			
			addCombination(perms, domains, i + 1);
		}
	}	
}
