package uk.ac.manchester.cs.pronto.alg;


import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

public class MinHittingSetsAlgorithmImplTest {

	@Test
	public void testAddConflictSet1() {

		DumbHittingSetAlgImpl<Integer> alg = new DumbHittingSetAlgImpl<Integer>();
		Set<Set<Integer>> hittingSets = null;

		hittingSets = alg.addConflictSet( new HashSet<Integer>( Arrays.asList(2,4,5) ));
		hittingSets = alg.addConflictSet( new HashSet<Integer>( Arrays.asList(1,2,3) ));		
		hittingSets = alg.addConflictSet( new HashSet<Integer>( Arrays.asList(1,3,5) ));
		hittingSets = alg.addConflictSet( new HashSet<Integer>( Arrays.asList(2,4,6) ));
		hittingSets = alg.addConflictSet( new HashSet<Integer>( Arrays.asList(2,4) ));
		hittingSets = alg.addConflictSet( new HashSet<Integer>( Arrays.asList(2,3,5) ));		
		hittingSets = alg.addConflictSet( new HashSet<Integer>( Arrays.asList(1,6) ));		
		
		System.out.println(hittingSets);
		
		assertEquals(6, hittingSets.size());
	}
	
	@Test
	public void testIterative() {

		IterativeHittingSetAlgorithm<Integer> alg = new IterativeMIPBasedHittingSetAlgorithm<Integer>();
		Set<Set<Integer>> testSets = new HashSet<Set<Integer>>();
		int cnt = 0;
		
		testSets.add( new HashSet<Integer>( Arrays.asList(2,4,5) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(1,2,3) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(1,3,5) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(2,4,6) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(2,4) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(2,3,5) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(1,6) ));
		
		alg.setSets( testSets );
		
		while (alg.hasNext()) {
			
			Set<Integer> hSet = alg.next();
			
			System.out.println(hSet);
			cnt++;
		}
		
		assertEquals(6, cnt);
	}	
	
	@Test
	public void testIterative2() {

		IterativeHittingSetAlgorithm<Integer> alg = new IterativeMIPBasedHittingSetAlgorithm<Integer>();
		Set<Set<Integer>> testSets = new HashSet<Set<Integer>>();
		int cnt = 0;
		
		testSets.add( new HashSet<Integer>( Arrays.asList(1,2,3) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(1,3,5) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(2,4,6 ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(2,3,5 ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(4,6) ));
		
		alg.setSets( testSets );
		
		while (alg.hasNext()) {
			
			Set<Integer> hSet = alg.next();
			
			System.out.println(hSet);
			cnt++;
		}
		
		assertEquals(8, cnt);
	}	
	
	
	@Test
	public void testCompute1() {

		HittingSetsAlgorithm<Integer> alg = new HittingSetsAlgorithmImpl<Integer>();
		Set<Set<Integer>> testSets = new HashSet<Set<Integer>>();
		
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(2), new Integer(4), new Integer(5) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(1), new Integer(2), new Integer(3) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(1), new Integer(3), new Integer(5) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(2), new Integer(4), new Integer(6) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(2), new Integer(4) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(2), new Integer(3), new Integer(5) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(1), new Integer(6) ) ));
		
		Set<Set<Integer>> hittingSets = alg.compute( testSets );
		
		System.out.println(hittingSets);
		
		assertEquals(6, hittingSets.size());
	}

	
	@Test
	public void testCompute2() {

		HittingSetsAlgorithm<Integer> alg = new DumbHittingSetAlgImpl<Integer>();
		Set<Set<Integer>> testSets = new HashSet<Set<Integer>>();
		
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(1), new Integer(2), new Integer(3) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(1), new Integer(3), new Integer(5) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(2), new Integer(4), new Integer(6) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(2), new Integer(3), new Integer(5) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(4), new Integer(6) ) ));
		
		Set<Set<Integer>> hittingSets = alg.compute( testSets );
		
		System.out.println(hittingSets);
		
		assertEquals(8, hittingSets.size());
	}
	
	@Test
	public void testCompute3() {

		HittingSetsAlgorithmImpl<Integer> alg = new HittingSetsAlgorithmImpl<Integer>();
		Set<Set<Integer>> testSets = new HashSet<Set<Integer>>();
		
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(39) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(20), new Integer(3) ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(new Integer(22), new Integer(3) ) ));
		
		Set<Set<Integer>> hittingSets = alg.compute( testSets );
		
		System.out.println(hittingSets);
		
		assertEquals(2, hittingSets.size());
	}
	
	@Test
	public void testCompute4() {

		//HittingSetsAlgorithm<Integer> alg = new HittingSetsAlgorithmImpl<Integer>();
		HittingSetsAlgorithm<Integer> alg = new DumbHittingSetAlgImpl<Integer>();
		Set<Set<Integer>> testSets = new HashSet<Set<Integer>>();
		
		testSets.add( new HashSet<Integer>( Arrays.asList(1, 2, 3 ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(3, 4, 5 ) ));
		testSets.add( new HashSet<Integer>( Arrays.asList(2, 4 ) ));
		
		Set<Set<Integer>> hittingSets = alg.compute( testSets );
		
		hittingSets = alg.addConflictSet( new HashSet<Integer>( Arrays.asList(1, 5 ) ));
		
		System.out.println(hittingSets);
		
		//assertEquals(4, hittingSets.size());
	}	
	
	@Test
	public void testEvaluationTest() {
		
		Set<Set<Integer>> conflictSets1 = new HashSet<Set<Integer>>();
		Set<Set<Integer>> conflictSets2 = new HashSet<Set<Integer>>();
		Random rndElement = new Random();
		Random rndSetSize = new Random();
		int setNumberLimit = 30;
		int setSizeLimit = 5;
		int elementLimit = 30;
		
		for (int i = 0; i < setNumberLimit; i++) {
			//Generate random conflict set
			Set<Integer> conflictSet = null;
			
			do {
				//Make sure we don't generate conflict sets which are subsets of already 
				//generated ones 
				conflictSet = generateRandomSet(rndSetSize.nextInt( setSizeLimit ) + 2,
																		rndElement, elementLimit);				
			} while (isContained( conflictSets1, conflictSet ));
				
			conflictSets1.add( conflictSet );
			conflictSets2.add( new HashSet<Integer>(conflictSet) );
		}

		long ts = System.currentTimeMillis();
		
		Set<Set<Integer>> hittingSets = new DumbHittingSetAlgImpl<Integer>().compute( conflictSets1 );
		
		System.out.println( "Dumb alg:");
		System.out.println( "Size: " + hittingSets.size() +  ", generated in: " + (System.currentTimeMillis() - ts) );
		
		ts = System.currentTimeMillis();
		
		Set<Set<Integer>> hittingSets2 = new HittingSetsAlgorithmImpl<Integer>().compute( conflictSets2 );
		
		System.out.println( "Normal alg:");
		System.out.println( "Size: " + hittingSets2.size() +  ", generated in: " + (System.currentTimeMillis() - ts) );
		
	}
	
	@Test
	public void testIncrementalEvaluation() {

		HittingSetsAlgorithmImpl<Integer> normalAlg = new HittingSetsAlgorithmImpl<Integer>();
		DumbHittingSetAlgImpl<Integer> dumbAlg = new DumbHittingSetAlgImpl<Integer>();
		Set<Set<Integer>> conflictSets1 = new HashSet<Set<Integer>>();
		Set<Set<Integer>> conflictSets2 = new HashSet<Set<Integer>>();
		//We generate random conflict sets of integers and incrementally increase their number
		//to make sure that the number of hitting sets never decreases
		Random rndElement = new Random();
		Random rndSetSize = new Random();
		int setNumberLimit = 60;
		int setSizeLimit = 4;
		int elementLimit = 50;
		
		for (int i = 0; i < setNumberLimit; i++) {
			//Generate random conflict set
			Set<Integer> conflictSet = null;
			
			do {
				//Make sure we don't generate conflict sets which are subsets of already 
				//generated ones 
				conflictSet = generateRandomSet(rndSetSize.nextInt( setSizeLimit ) + 2,
																		rndElement, elementLimit);				
			} while (isContained( conflictSets1, conflictSet ));
				
			conflictSets1.add( conflictSet );
			conflictSets2.add( conflictSet );
			
			long ts = System.currentTimeMillis();
			
			Set<Set<Integer>> hittingSets = dumbAlg.addConflictSet( new HashSet<Integer>(conflictSet) );
			
			System.out.println( "Dumb alg, " + conflictSets1.size() + " sets:");
			System.out.println( "Size: " + hittingSets.size() +  ", generated in: " + (System.currentTimeMillis() - ts) );
			
			ts = System.currentTimeMillis();
			
			hittingSets = normalAlg.addConflictSet( new HashSet<Integer>(conflictSet) );
			
			System.out.println( "Normal alg, " + conflictSets2.size() + " sets:");
			System.out.println( "Size: " + hittingSets.size() +  ", generated in: " + (System.currentTimeMillis() - ts) );
		}
	}
	
	private void analyzeIt(	Set<Set<Integer>> prevHSSets,
							Set<Set<Integer>> currHSSets,
							Set<Set<Integer>> prevConflictSets,
							Set<Set<Integer>> conflictSets,
							Set<Integer> lastConflictSet,
							BinaryHittingSetTree<Integer> hsTree) {
		//We know that lastConflictSet is a new set, it's neither contained in  prevConflictSets
		//nor a subset of any previous conflict sets. First we check if it has some new element(s) 
		if ( findNewElement( prevConflictSets, lastConflictSet ) >= 0) {
			
			System.out.println("+++ There are new elements but still fewer hitting sets! +++");
		}
		//Next we compare previous and current hitting sets to see what's missing
		Set<Set<Integer>> missingHSSets = findMissingHittingSets(prevHSSets, currHSSets);
		
		if (!missingHSSets.isEmpty()) {
			//This means that some hitting sets which were minimal before are not parts of
			//any new minimal hitting sets. The only way how this can happen is if they are 
			//no longer considered minimal. Thus we try to find out why.
			for (Set<Integer> missingHS : missingHSSets) {
				
				Set<Set<Integer>> newMinimalHSets = findNewMinimalHittingSets(currHSSets, missingHS);
				
				if( newMinimalHSets.isEmpty() ) {
					//Interesting. Find out why they are no longer hitting sets 
					//(not just minimal hitting sets)
					Set<Set<Integer>> nonhitSets = findNonHitSets(conflictSets, missingHS);
					/*
					 * !!!I UNDERSTAND!!!
					 * Consider the example: we had conflict sets {1,2,3} and {3,4,5}
					 * {1,5} is one of the minimal hitting sets
					 * then we add new conflict set {2,4}
					 * and it's no longer a hitting set! 
					 */
					System.out.println( nonhitSets + ", " + lastConflictSet );
					
				} else {
					//We can't really get here (unless there are bugs)
					for( Set<Integer> newMinimalHSet : newMinimalHSets ) {
						// They should also be minimal hitting sets before, shouldn't they?
						Set<Set<Integer>> nonhitSets = findNonHitSets(prevConflictSets, newMinimalHSet);
						
						System.out.println( nonhitSets );
					}
				}
			}
		}
	}

	private Set<Set<Integer>> findNonHitSets(Set<Set<Integer>> conflictSets, Set<Integer> hSet) {
		
		Set<Set<Integer>> nonHitSets = new HashSet<Set<Integer>>();
		
		for (Set<Integer> conflictSet : conflictSets) {
			
			boolean hit = false;
			
			for (int hsElement : hSet) {
				
				if (conflictSet.contains( hsElement )) {
					
					hit = true;
					break;//Set is hit
				}
			}
			
			if (!hit) nonHitSets.add( conflictSet );
		}
		
		return nonHitSets;
	}
	
	private Set<Set<Integer>> findNewMinimalHittingSets(Set<Set<Integer>> currHSSets,
														Set<Integer> missingHS) {
		
		Set<Set<Integer>> newMinHSets = new HashSet<Set<Integer>>();
		
		for (Set<Integer> hSet : currHSSets) {
			
			if (missingHS.containsAll( hSet )) newMinHSets.add( hSet );
		}
		
		return newMinHSets;
	}
	
	private Set<Set<Integer>> findMissingHittingSets(	Set<Set<Integer>> prevHSSets,
														Set<Set<Integer>> currHSSets) {
		
		Set<Set<Integer>> missingHSSets = new HashSet<Set<Integer>>();
		
		for (Set<Integer> prevHS : prevHSSets) {
			
			if (!isContained(currHSSets, prevHS)) {

				missingHSSets.add( prevHS );
			}
		}
		
		return missingHSSets;
	}
	
	private int findNewElement(Set<Set<Integer>> prevSets, Set<Integer> lastSet) {
		
		for (int element : lastSet) {
			
			boolean isNew = true;
			
			for (Set<Integer> set : prevSets) {
				
				if (set.contains( element )) {
					
					isNew = false;
					break;
				}
			}
			
			if (isNew) return element;
		}
		
		return -1;
	}
	
	private boolean isContained(Set<Set<Integer>> sets, Set<Integer> set) {
		
		for (Set<Integer> conflictSet : sets) {
			
			if (conflictSet.containsAll( set )) {
				
				return true;
			}
		}
		
		return false;
	}
	
	private Set<Integer> generateRandomSet(int setSize, Random rnd, int elemLimit) {
		
		Set<Integer> set = new HashSet<Integer>(setSize);
		
		for (int i = 0; i < setSize; i++) {
			
			set.add( rnd.nextInt( elemLimit ) );
		}
		
		return set;
	}
}
