/**
 * 
 */
package uk.ac.manchester.cs.pronto.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * <p>Title: SetUtils</p>
 * 
 * <p>Description: 
 *  TODO Merge with org.mindswap.pellet.utils.SetUtils
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class SetUtils {

	public static void main(String[] args) {

		kSubsets(Arrays.asList(new Integer[] {1, 2, 3, 4, 5}), 3);		
	}
	
	/**
	 * Compute all unordered subsets of the length k. 
	 * 
	 * @param list
	 * @param k
	 * @return
	 */
	public static <T> Set<Set<T>> kSubsets(List<T> list, int k) {
		
		long max = numOfSubsets( list.size(), k );
		Set<Set<T>> result = new HashSet<Set<T>>((int)max);		

		for( int index = 0; index < max; index++ )
		{
			result.add(subset( list, k, index ));
		}

		return result;
	}
	
	
	public static long numOfSubsets(int size, int k) {
		
		if( (0 == k) || (size == k) )	{
			
			return 1;
		}

		return numOfSubsets( size - 1, k - 1  ) + numOfSubsets( size - 1, k );
	}
	
	
	public static <T> Set<T> subset(List<T> list, int k, long index) {

		int size = list.size();
		Set<T> result = new HashSet<T>();
		
		for( int slotValue = 1; slotValue <= size; slotValue++ )
		{
			if( 0 == k) {
				break;
			}

			long threshold = numOfSubsets( size - slotValue, k - 1 );

			if( index < threshold )
			{
				//System.out.print( list.get(slotValue - 1) + "\t" );
				result.add(list.get(slotValue - 1));
				k--;
			}
			else if( index >= threshold )
			{
				index = index - threshold;
			}
		}

		//System.out.println();
		return result;
	}
	
	public static <T> void printAll(List<T> list, int k)
	{

		long max = numOfSubsets( list.size(), k );

		for( int index = 0; index < max; index++ )
		{
			subset( list, k, index );
		}
	}
	
	/*
	 * Computes intersection of a collection of sets
	 */
	public static <T> Set<T> intersection(Collection<Set<T>> sets) {
		
		Set<T> result = new HashSet<T>();
		result.addAll( sets.iterator().next());
		
		for (Set<T> set : sets) {
			
			result.retainAll( set );
		}
		
		return result;
	}
	
	/*
	 * Randomly picks an element from the set
	 */
	public static <T> T pickRandomElement(Set<T> set) {
		
		int rnd = (int) (Math.random() * (set.size() - 1));
		Iterator<T> iter = set.iterator();
		T result = iter.next();
		
		for (; rnd > 0; rnd--) {
			
			result = iter.next();
		}
		
		return result;
	}
	
	public static <T> Set<T> pickRandomSubset(Set<T> set, int size) {

		Random rnd = new Random();
		List<T> list = new ArrayList<T>(set);
		/*
		 * This might be inefficient if binomial coefficients get very large
		 */
		return SetUtils.subset( list, size, rnd.nextInt( biCoeff(set.size(), size) ) );
	}
	
	/*
	 * Simpler version
	 */
	public static <T> Set<T> pickRandomSubset2(Set<T> set, int size) {

		Random rnd = new Random();
		Set<T> subset = new HashSet<T>(size);
		List<T> list = new ArrayList<T>(set);
		int index = 0;
		
		if (size >= set.size()) {
			
			return set;
		}
		
		for (int i = 0; i < size; i++) {
			
			index = rnd.nextInt(list.size());
			
			subset.add( list.remove( index ) );
		}
		
		return subset;
	}
	
	/*
	 * WARNING: the function does not check that there exist that many subsets
	 * of the given size. Checking would require computing of a binomial coefficient
	 * which might be slow. Other approximations might be used (see Stirling approximation
	 * for example).
	 * Currently the function stops after stumbling over a number of duplicates
	 */
	public static <T> Set<Set<T>> pickRandomSubsets(Set<T> set, int size, int number) {
		
		Set<Set<T>> subsets = new HashSet<Set<T>>(number);
		int dups = 0;
		
		if (size > set.size()) {
			
			return null;
		}
		
		while (subsets.size() < number && dups < set.size()) {

			if (!subsets.add( pickRandomSubset2(set, size) )) {
				
				dups++;
				
			} else {
				
				dups = 0;
			}
		}
		
		return subsets;
	}
	
	
	/*
	 * Computes binomial coefficient (n,k)
	 */
	public static int biCoeff(int n, int k) {

		if( (0 == k) || (n == k) )	{
			
			return 1;
		}

		return biCoeff( n - 1, k - 1  ) + biCoeff( n - 1, k );
	}
	
	/**
	 * Checks if two collections have a non-empty intersection
	 */
	public static <T> boolean intersects( Collection<T> collection1, Collection<T> collection2 ) {
		
		for (T elem : collection1)	if (collection2.contains( elem )) return true;
		
        return false;
    }	
	
	public static <T> Set<? extends T> intersection(Collection<? extends T> collection1
										, Collection<? extends T> collection2) {
		
		HashSet<T> s = new HashSet<T>(collection1);
		
		s.retainAll( collection2 );
		
		return s;
	}
	
	/*
	 * Assuming that A is a smaller than B, this method computes A - B
	 */
	public static <T> Set<T> setDifference(Set<T> setA, Set<T> setB) {
		
		HashSet<T> s = new HashSet<T>();
		
		for (T element : setA) {
			
			if (!setB.contains( element )) s.add( element );
		}
		
		return s;
	}
	
	public static <T> Set<T> flatten(Set<Set<T>> sets) {
		
		HashSet<T> s = new HashSet<T>(sets.size() * 2);
		
		for (Set<T> set : sets) s.addAll( set );
		
		return s;
	}
}
