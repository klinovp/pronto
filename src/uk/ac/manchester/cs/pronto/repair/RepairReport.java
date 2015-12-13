/**
 * 
 */
package uk.ac.manchester.cs.pronto.repair;

import java.util.Set;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class RepairReport {

	private Set<ConditionalConstraint> m_repair = null;
	private Set<Set<ConditionalConstraint>> m_conflicts = null;
	
	protected RepairReport(Set<Set<ConditionalConstraint>> conflicts, Set<ConditionalConstraint> repair) {
		
		m_repair = repair;
		m_conflicts = conflicts;
	}
	
	public Set<ConditionalConstraint> getRepair() {
		
		return m_repair;
	}
	
	public Set<Set<ConditionalConstraint>> getConflicts() {
		
		return m_conflicts;
	}
}
