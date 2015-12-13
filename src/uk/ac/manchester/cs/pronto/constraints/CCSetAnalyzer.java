/**
 * 
 */
package uk.ac.manchester.cs.pronto.constraints;

import java.util.Map;
import java.util.Set;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.benchmark.Telemetry;

/**
 * 
 * Finds minimal unsatisfiable and maximal satisfiable sets of conditional
 * constraints
 * 
 * @author Pavel Klinov
 * 
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface CCSetAnalyzer extends Telemetry {

	/**
	 * Finds all minimal unsat sets of PTBox constraints. The subsets are unsat
	 * given the set of tough constraints
	 * 
	 * @param toughConstraints
	 * @param ptbox
	 * @return
	 */
	public Set<Set<ConditionalConstraint>> getMinimalUnsatSubsets(
			Set<ConditionalConstraint> toughConstraints,
			PTBox ptbox);

	/**
	 * Finds just some minimal unsatisfiable subset of PTBox constraints
	 * 
	 * @param toughConstraints
	 * @param ptbox
	 * @return
	 */
	public Set<ConditionalConstraint> getMinimalUnsatSubset(
			Set<ConditionalConstraint> toughConstraints,
			PTBox ptbox);
	
	
	/**
	 * Finds all maximal sat subsets of PTBox constraints. The subsets are sat
	 * given the set of tough constraints
	 * 
	 * @param toughConstraints
	 * @param ptbox
	 * @return
	 */
	public Set<Set<ConditionalConstraint>> getMaximalSatSubsets(
			Set<ConditionalConstraint> toughConstraints,
			PTBox ptbox);

	/**
	 * Finds just some maximal satisfiable subset of PTBox constraints
	 * 
	 * @param toughConstraints
	 * @param ptbox
	 * @return
	 */
	public Set<Set<ConditionalConstraint>> getMaximalSatSubset(
			Set<ConditionalConstraint> toughConstraints,
			PTBox ptbox);

	public Map<Set<ConditionalConstraint>, Set<Set<ConditionalConstraint>>>
			getAllConflictSets(	Set<Set<ConditionalConstraint>> strictConstraints,
								PTBox ptbox);	
}
