/**
 * 
 */
package uk.ac.manchester.cs.pronto.constraints;

import java.util.Set;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;

/**
 * 
 * Manages information about conflicts between subsets of conditional constraints
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface ConflictGraph {
	
	public void addConflict(Set<ConditionalConstraint> ccSet, Set<ConditionalConstraint> conflictsSet);
	public void addConflicts(Set<ConditionalConstraint> ccSet, Set<Set<ConditionalConstraint>> conflictsSets);

	/**
	 * Returns all subsets that are in conflicts with the set according to the information
	 * stored in the graph
	 * 
	 * @param ccSubset
	 * @return
	 */
	public Set<Set<ConditionalConstraint>> getConflictSets(Set<ConditionalConstraint> ccSubset);
	
	/**
	 * Returns all subsets that are not in conflict with ccSet
	 * 
	 * @param ccSet
	 * @return
	 */
	public Set<Set<ConditionalConstraint>> getSetsNotUnderConflict(Set<ConditionalConstraint> ccSet);	
	
	public Set<Set<ConditionalConstraint>> getConstraintSets();
	public ConflictGraph clone();
}
