/**
 * 
 */
package uk.ac.manchester.cs.pronto.repair;

import java.util.Set;

import uk.ac.manchester.cs.pronto.alg.IterativeHittingSetAlgorithm;
import uk.ac.manchester.cs.pronto.alg.IterativeMIPBasedHittingSetAlgorithm;
import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.constraints.CCSetAnalyzer;
import uk.ac.manchester.cs.pronto.lp.CCSetAnalyzerImpl2;

/**
 * Repairs a PTBox by randomly deleting minimal repairs
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * 8 Nov 2010
 */
public class RandomDeletionRepairer implements Repairer {

	private CCSetAnalyzer m_analyzer = new CCSetAnalyzerImpl2();
	private RepairReport m_report = null;
	
	public RandomDeletionRepairer() {}
	
	public RandomDeletionRepairer(CCSetAnalyzer analyzer) {
		
		m_analyzer = analyzer;
	}
	
	/* 
	 */
	@Override
	public PTBox repair(PTBox ptbox, Set<ConditionalConstraint> toughConstraints) {
		// Find minimal inconsistent sets
		Set<Set<ConditionalConstraint>> unsatSets = m_analyzer.getMinimalUnsatSubsets( toughConstraints, ptbox );
		IterativeHittingSetAlgorithm<ConditionalConstraint> alg = new IterativeMIPBasedHittingSetAlgorithm<ConditionalConstraint>();

		if( unsatSets == null || unsatSets.isEmpty() ) {
			//Nullify the report
			m_report = null;
			
			return ptbox;

		}
		else {
			// Compute some repair
			alg.setSets( unsatSets );

			PTBox repaired = ptbox.clone();
			Set<ConditionalConstraint> repair = alg.next();
			// Remove the repair
			repaired.removeDefaultConstraints( repair );
			//Save the report
			m_report = new RepairReport(unsatSets, repair);
			
			return repaired;
		}
	}
	
	public RepairReport getReport() {
		
		return m_report;
	}
}