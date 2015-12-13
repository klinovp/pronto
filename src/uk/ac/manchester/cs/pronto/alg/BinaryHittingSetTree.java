/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.ac.manchester.cs.graph.Node;
import uk.ac.manchester.cs.graph.Tree;


/**
 * Keeps binary tree structure used to compute hitting sets. The main purpose is to be able to
 * add update the existing trees when adding ne conflict sets
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class BinaryHittingSetTree<T> {

	private Tree<HittingSetObject<T>> m_tree = null;
	
	protected  BinaryHittingSetTree(Tree<HittingSetObject<T>> tree) {
		
		m_tree = tree;
	}
	
	public Tree<HittingSetObject<T>> getTree() {
		
		return m_tree;
	}
	
/*	public void addConflictSet(Set<T> conflictSet) {
		//First update the tree
		addConflictSet(m_tree.getRoot(), conflictSet);
		
		collectHittingSets();
	}*/
	
	public Set<Set<T>> getHittingSets() {
		
		return m_tree.getRoot().getObject().getMinimalHittingSets();
	}
/*
 * This is a buggy and complicated implementation of the "update tree" operation that inserts
 * a new conflict set into the tree. I don't know if it can be faster than a more straightforward
 * procedure (see HittingSetUtils.updateHittingSets) but it might. So I decided to keep it
 * commented out for the moment.
 * 
 * 
 */	
/*	protected void addConflictSet(Node<HittingSetObject<T>> node, Set<T> conflictSet) {
		
		HittingSetObject<T> hsNode = node.getObject();
		
		if (!conflictSet.isEmpty()) {
			
			Set<Set<T>> nodeConflictSets = hsNode.getConflictSets();
			
			nodeConflictSets.add( conflictSet );
			hsNode.setMinimalHittingSets( hsNode.getElement() != null 
											? Collections.singleton( 
													Collections.singleton( hsNode.getElement() ))
											: new HashSet<Set<T>>());
			//Propagate changed to children
			if (!node.isLeaf()) {

				Set<T> copy = new HashSet<T>(conflictSet);
				Node<HittingSetObject<T>> leftChild = node.getChildAt( 0 );
				Node<HittingSetObject<T>> rightChild = node.getChildAt( 1 );
				T splitElement = leftChild.getObject().getElement();
				
				if (copy.contains( splitElement )) {
					//Should go to the left kid
					copy.remove( splitElement );
					addConflictSet( leftChild, copy );
					
				} else {
					//Should go to the right kid
					addConflictSet( rightChild, copy );
				}
				
			} else {
				//This means that the new conflict set contains elements which are not in the
				//previous sets. Not a good case because the tree has to grow.
				addNodes( node );
			}
			
			hsNode.setChanged( true );
		}
	}*/

	protected void addNodes(Node<HittingSetObject<T>> node) {
		
		if( node.getObject().getConflictSets().size() > 0 ) {
			/*
			 * Pick some element
			 */
			T element = pickSome( node.getObject() );
			HittingSetObject<T> left = createLeft( node, element );
			HittingSetObject<T> right = createRight( node, element );
			/*
			 * Add nodes in depth-first manner
			 */
			node.addChild( left );
			node.addChild( right );

			addNodes( node.getChildAt( 0 ) );
			addNodes( node.getChildAt( 1 ) );
		}
	}
	
	
	protected T pickSome(HittingSetObject<T> object) {
		/*
		 * For simplicity, take just the first element of the first set
		 */
		return object.getConflictSets().iterator().next().iterator().next();
	}
	
	protected HSObject<T> createLeft(Node<? extends HittingSetObject<T>> node, T element) {
		
		/*
		 * Select those sets that contain the element
		 */
		return new HSObject<T>(node.getObject().leftSubset( element ), element);
	}

	protected HSObject<T> createRight(Node<? extends HittingSetObject<T>> node, T element) {
		
		return new HSObject<T>(node.getObject().rightSubset( element ), null);
	}	
	
	
	protected void collectHittingSets() {
		
		collectHittingSets( m_tree.getRoot() );
	}

	/*
	 * Collects all hitting sets by traversing the tree and minimizing sets at each node
	 */
	protected void collectHittingSets(Node<? extends HittingSetObject<T>> node) {
		
		HittingSetObject<T> hsNode = node.getObject();
		
		if (!node.isLeaf() && hsNode.isChanged()) {
			
			Node<? extends HittingSetObject<T>> left = node.getChildAt( 0 );
			Node<? extends HittingSetObject<T>> right = node.getChildAt( 1 );
			
			collectHittingSets(left);
			collectHittingSets(right);
			
			Set<Set<T>> hittingSets = new HashSet<Set<T>>(hsNode.getMinimalHittingSets());			
			Set<Set<T>> leftHittingSet = left.getObject().getMinimalHittingSets(); 
			Set<Set<T>> rightHittingSet = right.getObject().getMinimalHittingSets();
			
			if (rightHittingSet.isEmpty()) {
				
				hittingSets.addAll( leftHittingSet );
				
			} else {
				
				for(Set<T> leftSet : leftHittingSet) {
					
					for (Set<T> rightSet : rightHittingSet) {
						
						HashSet<T> newSet = new HashSet<T>(leftSet);
						
						newSet.addAll( rightSet );
						//Check that this new set is minimal (can be added)
						//It can also expunge some already present  set
						HittingSetUtils.addToHittingSets(hittingSets, newSet);
					}
				}				
			}
			
			hsNode.setMinimalHittingSets( hittingSets );
			//Mark that the node contains an up-to-date set of minimal hitting sets
			hsNode.setChanged( false );			
		}  
	}


	@Override
	public String toString() {
		
		return m_tree == null ? "null" : m_tree.toString();
	}
}


class HSObject<T> implements HittingSetObject<T> {
	
	protected Set<Set<T>> m_conflictSets;
	protected Set<Set<T>> m_hsSets;
	protected T m_element = null;
	protected boolean m_changed = true;
	
	HSObject(Set<Set<T>> sets, T element) {
		
		m_conflictSets = sets;
		m_element = element;
		m_hsSets = new HashSet<Set<T>>();
		
		if (element != null) m_hsSets.add( Collections.singleton( element ));
	}
	
	public Set<Set<T>> leftSubset(T element) {
		
		Set<Set<T>> leftSets = new HashSet<Set<T>>();
		
		for (Set<T> set : m_conflictSets ) {
			
			if (set.contains( element )) {
				
				HashSet<T> leftSubset = new HashSet<T>(set);
				
				leftSubset.remove( element );
				
				if (leftSubset.size() > 0) {
					
					leftSets.add( leftSubset );
				}
			}
		}
		
		return leftSets;
	}
	
	
	public Set<Set<T>> rightSubset(T element) {
		
		Set<Set<T>> rightSets = new HashSet<Set<T>>();
		
		for (Set<T> set : m_conflictSets ) {
			
			if (!set.contains( element )) {
				
				rightSets.add( set );
			}
		}
		
		return rightSets;
	}
	
	public Set<Set<T>> getConflictSets() {
		
		return m_conflictSets;
	}
	
	public void setConflictSets(Set<Set<T>> conflictSets) {
		
		m_conflictSets = conflictSets;
	}
	
	public Set<Set<T>> getMinimalHittingSets() {
		
		return m_hsSets;
	}
	
	public void setMinimalHittingSets(Set<Set<T>> hittingSets) {
		
		m_hsSets = hittingSets;
	}
	
	public T getElement() {
		
		return m_element;
	}
	
	public String toString() {
		
		return m_element + ": <" + m_conflictSets + "> [" + m_hsSets + "]";
	}

	public boolean isChanged() {
		
		return m_changed;
	}

	public void setChanged(boolean changed) {
		
		this.m_changed = changed;
	}
	
	
}