package uk.ac.manchester.cs.pronto.alg;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class MaxConflictlessSubsetsAlgorithmTest extends TestCase {

	public void testCompute1() {

		Set<Integer> set = new HashSet<Integer>(  );
		
		for (int i = 0; i < 20; i++) {
			set.add( new Integer(i) );
		}
		
		Set<Set<Integer>> testSets = new HashSet<Set<Integer>>();
		
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(1), new Integer(2), new Integer(3) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(1), new Integer(3), new Integer(5) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(2), new Integer(4), new Integer(6) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(2), new Integer(3), new Integer(5) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(4), new Integer(6) ) ));
		
		Set<Set<Integer>> maxSets = new MaxConflictlessSubsetsAlgorithmImpl().compute( set, testSets );
		
		assertEquals(2, maxSets.size());
		assertEquals(18, maxSets.iterator().next().size());
	}
	
	public void testCompute2() {

		Set<Integer> set = new HashSet<Integer>( Arrays.asList(1,2,3) );
		Set<Integer> conflictSet = Collections.emptySet(); 
		
		Set<Set<Integer>> maxSets = new MaxConflictlessSubsetsAlgorithmImpl().compute( set, Collections.singleton(conflictSet) );
		
		assertEquals(1, maxSets.size());
		assertEquals(3, maxSets.iterator().next().size());
	}	

}
