/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.ac.manchester.cs.graph.Tree;


/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Mar 23, 2008
 */
public class HittingSetsAlgorithmImpl<T> implements HittingSetsAlgorithm<T> {

	private BinaryHittingSetTree<T> m_hsTree = null; 
	private Set<Set<T>> m_hittingSets = null;;

	/* 
	 */
	public Set<Set<T>> compute(Collection<Set<T>> conflictSets) {

		if( conflictSets.isEmpty() || conflictSets.contains( Collections.emptySet() ) ) {

			return new HashSet<Set<T>>();
		}
		else {

			m_hsTree = new BinaryHittingSetTree<T>( generateHSTree( conflictSets ) );

			m_hsTree.addNodes( m_hsTree.getTree().getRoot() );
			m_hsTree.collectHittingSets();

			return m_hittingSets = m_hsTree.getHittingSets();
		}
	}
	
	public Set<Set<T>> addConflictSet(Set<T> conflictSet) {
		
		if (m_hsTree == null) {
			
			return compute(Collections.singleton(conflictSet));
			
		} else {
			
			return m_hittingSets = HittingSetUtils.updateHittingSets( m_hittingSets, conflictSet );			
		}
	}

	public BinaryHittingSetTree<T> getHittingSetTree() {
		
		return m_hsTree;
	}
	/**
	 * Generates binary hitting set tree
	 * 
	 * @param <T>
	 * @param conflictSets
	 * @return
	 */
	protected Tree<HittingSetObject<T>> generateHSTree(Collection<Set<T>> conflictSets) {
		
		HashSet<Set<T>> initialSets = new HashSet<Set<T>>(conflictSets);
		
		minimizeSets(initialSets);
		/*
		 * First, we create the tree
		 */
		Tree<HittingSetObject<T>> hsTree = new Tree<HittingSetObject<T>>(new HSObject<T>(initialSets, null));;
		
		return hsTree;
	}

	/**
	 * Removes non-minimal sets
	 */
	protected void minimizeSets(Set<Set<T>> conflictSets) {

		if( conflictSets != null ) {

			Set<Set<T>> tmpSet = new HashSet<Set<T>>( conflictSets.size() );

			do {
				tmpSet.clear();

				for( Set<T> iSet : conflictSets ) {

					for( Set<T> jSet : conflictSets ) {

						if( iSet != jSet && iSet.containsAll( jSet ) ) {

							tmpSet.add( iSet );
						}
					}
				}

				conflictSets.removeAll( tmpSet );

			} while( tmpSet.size() > 0 );
		}
	}
}
