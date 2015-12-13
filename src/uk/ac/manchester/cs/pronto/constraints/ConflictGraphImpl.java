/**
 * 
 */
package uk.ac.manchester.cs.pronto.constraints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;

/**
 * @author Pavel Klinov pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class ConflictGraphImpl implements ConflictGraph {

	protected Map<Set<ConditionalConstraint>, Set<Set<ConditionalConstraint>>>	m_cg
					= new HashMap<Set<ConditionalConstraint>, Set<Set<ConditionalConstraint>>>();
	//FIXME Most probably we don't need it
	protected CCSetInclusionQualifier m_qualifier = null;

	public ConflictGraphImpl() {
		
		m_qualifier = new BasicCCSetInclusionQualifier();
	}
	
	public ConflictGraphImpl(CCSetInclusionQualifier qualifier) {
		
		m_qualifier = qualifier;
	}
	
	public ConflictGraphImpl(CCSetInclusionQualifier qualifier, Map<Set<ConditionalConstraint>, Set<Set<ConditionalConstraint>>> graph) {
		
		this(qualifier);
		
		m_cg.putAll( graph );
	}
 
	public Set<Set<ConditionalConstraint>> getConstraintSets() {
		
		return m_cg.keySet();
	}
	/**
	 * @param cc
	 * @param ccSet
	 */
	public void addConflict(Set<ConditionalConstraint> ccSet, Set<ConditionalConstraint> conflictsSet) {
		
		Set<Set<ConditionalConstraint>> subsumers = getSubsumers(ccSet);
	
		add(ccSet, conflictsSet);
		/*
		 * We add conflicts sets not only to ccSet but also to all subsets that subsume
		 */
		for (Set<ConditionalConstraint> subsumer : subsumers) {
			
			add(subsumer, conflictsSet);
		}
	}

	/**
	 * @param cc
	 * @param ccSet
	 */
	public void addConflicts(Set<ConditionalConstraint> ccSet, Set<Set<ConditionalConstraint>> conflictsSets) {

		if( conflictsSets.size() == 0 ) {

			add( ccSet, new HashSet<ConditionalConstraint>() );
			
		} else {

			for( Set<ConditionalConstraint> conflictSet : conflictsSets ) {
				addConflict( ccSet, conflictSet );
			}
		}
	}

	/**
	 * @param cc
	 * @return
	 */
	public Set<Set<ConditionalConstraint>> getConflictSets(Set<ConditionalConstraint> ccSet) {

		/*
		 * Find all key subsets that are subsumed by ccSet
		 */
		Set<Set<ConditionalConstraint>> conflicts = new HashSet<Set<ConditionalConstraint>>();
		
		for (Set<ConditionalConstraint> subsumee : getSubsumees( ccSet )) {
			
			conflicts.addAll( m_cg.get( subsumee ) );
		}
		
		return conflicts;
	}

	/*
	 * Returns all keys that subsume ccSet
	 */
	protected Set<Set<ConditionalConstraint>> getSubsumers(Set<ConditionalConstraint> ccSet) {

		Set<Set<ConditionalConstraint>> subsumers = new HashSet<Set<ConditionalConstraint>>();
		
		for (Set<ConditionalConstraint> someSet : m_cg.keySet()) {
			
			if ( m_qualifier.includes( someSet, ccSet )) {
				
				subsumers.add( someSet );
			}
		}
		
		return subsumers;
	}
	
	/*
	 * Returns all keys that are subsumed by ccSet
	 */
	protected Set<Set<ConditionalConstraint>> getSubsumees(Set<ConditionalConstraint> ccSet) {

		Set<Set<ConditionalConstraint>> subsumees = new HashSet<Set<ConditionalConstraint>>();
		
		for (Set<ConditionalConstraint> someSet : m_cg.keySet()) {
			
			if (m_qualifier.includes( ccSet, someSet )) {
				
				subsumees.add( someSet );
			}
		}
		
		return subsumees;
	}	
	
	/*
	 * Simple adding of a new mapping to the graph
	 */
	protected void add(Set<ConditionalConstraint> ccSet, Set<ConditionalConstraint> conflictsSet) {
		
		Set<Set<ConditionalConstraint>> conflicts = m_cg.get( ccSet );
		
		conflicts = null == conflicts ? new HashSet<Set<ConditionalConstraint>>() : conflicts;
		
		if (null != conflictsSet && !conflicts.contains( conflictsSet )) {
			conflicts.add( conflictsSet );
		}
		
		m_cg.put( ccSet, conflicts );
	}
	
	/**
	 * Returns all subsets that are in conflict with some subsets of ccSet
	 * 
	 * @param ccSet
	 * @return
	 */
	public Set<Set<ConditionalConstraint>> getSetsUnderConflicts(Set<ConditionalConstraint> ccSet) {
		
		return getSetsUnderConflicts(ccSet, false);
	}
	
	/**
	 * Returns all subsets that are in conflict _only_ with some subsets of ccSet
	 */
	public Set<Set<ConditionalConstraint>> getSetsUnderConflicts(Set<ConditionalConstraint> ccSet, boolean only) {
		
		Set<Set<ConditionalConstraint>> result = new HashSet<Set<ConditionalConstraint>>();
		
		/*
		 * We can naively iterate over all keys to see if any of them is in 
		 * conflicts. 
		 * TODO see if a bidirectional map can do better here
		 */
		for (Set<ConditionalConstraint> key : m_cg.keySet()) {
			
			int totalConflicts = 0;
			int subsumedConflicts = 0;
			
			for (Set<ConditionalConstraint> conflictSet : m_cg.get( key )) {
				
				totalConflicts++;
				
				if ((conflictSet.size() == 0 && (null == ccSet || ccSet.size() == 0))
						|| (conflictSet.size() > 0 && (null != ccSet && ccSet.containsAll( conflictSet )))) {
					subsumedConflicts++;
				}
			}
			
			if (subsumedConflicts > 0) {
				
				if (!only || (subsumedConflicts == totalConflicts)) {
					result.add( key );
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Returns all key sets whose conflict sets are not contained in ccSet
	 * 
	 * @param ccSet
	 * @return
	 */
	public Set<Set<ConditionalConstraint>> getSetsNotUnderConflict(Set<ConditionalConstraint> ccSet) {
		
		Set<Set<ConditionalConstraint>> result = new HashSet<Set<ConditionalConstraint>>();
		
		for (Set<ConditionalConstraint> keySet : m_cg.keySet()) {
			
			if (!isUnderConflict(keySet, ccSet)) {
				
				result.add( keySet );
			}
		}
		
		return result;
	}
	
	/**
	 * Checks if a set is under conflict with ccSet. In other words: is there at least one conflict
	 * set in the graph that is included in ccSet 
	 * 
	 * @param set
	 * @param ccSet
	 * @return
	 */
	public boolean isUnderConflict(Set<ConditionalConstraint> set, Set<ConditionalConstraint> ccSet) {
		
		Set<Set<ConditionalConstraint>> conflictSets = m_cg.get( set );
		
		if( null != conflictSets ) {
			
			for( Set<ConditionalConstraint> conflictSet : conflictSets ) {
				
				if ((null != conflictSet && conflictSet.size() > 0) && ccSet.containsAll( conflictSet )) {
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		
		for (Set<ConditionalConstraint> keySet : m_cg.keySet()) {
			
			sb.append( "+++Subset+++" ).append( System.getProperty( "line.separator" ) );
			sb.append( keySet.toString() ).append( System.getProperty( "line.separator" ) );
			sb.append( "+++Conflicting subsets+++" ).append( System.getProperty( "line.separator" ) );
			
			for (Set<ConditionalConstraint> valueSet : m_cg.get( keySet )) {
				
				sb.append( "		" + valueSet.toString() ).append( System.getProperty( "line.separator" ) );
			}
		}
		
		return sb.toString();
	}
	
	public ConflictGraph clone() {
		
		ConflictGraphImpl cGraph = new ConflictGraphImpl(m_qualifier);
		
		if (null != m_cg) {
			cGraph.m_cg = new HashMap<Set<ConditionalConstraint>, Set<Set<ConditionalConstraint>>>(m_cg);
		}
		
		return cGraph;
	}
}
