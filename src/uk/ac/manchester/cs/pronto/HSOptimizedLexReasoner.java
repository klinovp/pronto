/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.manchester.cs.pronto.benchmark.Telemetry;
import uk.ac.manchester.cs.pronto.constraints.CCSetAnalyzer;
import uk.ac.manchester.cs.pronto.lp.CCSetAnalyzerImpl2;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Mar 24, 2008
 */
public class HSOptimizedLexReasoner extends BasicLexicographicReasoner {

	private CCSetAnalyzer m_ccAnalyzer = new CCSetAnalyzerImpl2();
	private Telemetry[] m_teleSources = null;
	
	public HSOptimizedLexReasoner(PSATSolver solver) {
		
		super(solver);
		m_teleSources = new Telemetry[]{m_consChecker, m_ccAnalyzer};
	}
	
	@Override
	/**
	 * @param pjSubset Current subset in the z-partition
	 * @param lrData Stores z-partition, PTBox and other needed data
	 */
	protected void updateLexMinimalSubsets(	Set<ConditionalConstraint> pjSubset, LexReasoningData lrData) {

		PTBox ptbox = lrData.getPTBbox().clone(false);
		Set<Set<ConditionalConstraint>> minLexSets = lrData.getLexMinimalSets();
		Set<Set<ConditionalConstraint>> newLexSets = new HashSet<Set<ConditionalConstraint>>();
		Map<Set<ConditionalConstraint>, Set<Set<ConditionalConstraint>>> allLexSets = new HashMap<Set<ConditionalConstraint>, Set<Set<ConditionalConstraint>>>();
		int maxCardinality = 1;
		int cardinality = 0;
		
		ptbox.setDefaultConstraints( pjSubset );
		/*
		 * Need to find the maximal subsets of P_j that are satisfiable given
		 * some of current lex minimal sets
		 */
		for (Set<ConditionalConstraint> lexSet : minLexSets) {
			
			Set<Set<ConditionalConstraint>> maxSatSets = m_ccAnalyzer.getMaximalSatSubsets( lexSet, ptbox );
			
			if( maxSatSets.size() > 0 ) {
				//All maximal subsets are of the same cardinality
				cardinality = maxSatSets.iterator().next().size();

				if( cardinality > maxCardinality ) {

					allLexSets.clear();
				}

				if( cardinality >= maxCardinality ) {

					allLexSets.put( lexSet, maxSatSets );
					maxCardinality = cardinality;
				}
			}
		}
		/*
		 * Now update the minimal lexicographic subsets
		 */
		if( !allLexSets.isEmpty() ) {

			for( Set<ConditionalConstraint> lexSet : minLexSets ) {

				if( allLexSets.containsKey( lexSet ) ) {

					for( Set<ConditionalConstraint> satSubset : allLexSets.get( lexSet ) ) {

						Set<ConditionalConstraint> newLexSet = new HashSet<ConditionalConstraint>( lexSet );

						newLexSet.addAll( satSubset );
						newLexSets.add( newLexSet );
					}
				}
			}

			lrData.setLexMinimalSets( newLexSets );
		}
	}
	
	@Override
	public Set<Set<ConditionalConstraint>> computeMinIncoherentSubsets(PTBox ptbox) {
		
		Set<ConditionalConstraint> toughConstraints = super.getCoherenceConstraints( ptbox, Constants.COHERENCE_THRESHOLD );
		//TODO Get rid of extra PTBox preprocessing
		PTBox newPTBox = new PTBoxImpl(ptbox.getClassicalKnowledgeBase(), ptbox.getClassicalOntology(), ptbox.getDefaultConstraints());
		
		newPTBox.addDefaultConstraints( toughConstraints );
		newPTBox.preprocess();
		
		s_logger.info( "Coherence constraints:" );
		s_logger.info( toughConstraints );
		
		return m_ccAnalyzer.getMinimalUnsatSubsets(	toughConstraints, newPTBox );
	}

	@Override
	public Set<Set<ConditionalConstraint>> computeMinUnsatisfiableSubsets(PTBox ptbox) {
		
		return m_ccAnalyzer.getMinimalUnsatSubsets( null, ptbox );
	}

	/* 
	 * ============================================================================
	 * Telemetry
	 * ============================================================================
	 */	


	@Override
	public String getMeasure(String measure) {

		String superMeasure = super.getMeasure( measure );
		
		if (superMeasure == null) {
			//We add up measures from the ConsistencyChecker and CCAnalyzer
			//TODO Type casting to Double isn't nice, improve 
			Double total = 0d;
			
			for (Telemetry source : m_teleSources) {
				
				if (source.isMeasureSupported( measure )) total += Double.valueOf( source.getMeasure( measure ));
			}
			
			return total.toString();
			
		} else {
			
			return superMeasure;
		}
	}

	@Override
	public Collection<String> getMeasureNames() {

		Collection<String> measures = new HashSet<String>(super.getMeasureNames());
		
		measures.addAll( m_ccAnalyzer.getMeasureNames() );
		measures.addAll( m_consChecker.getMeasureNames() );
		
		return measures;
	}

	@Override
	public void resetMeasure(String measure) {
		// TODO Auto-generated method stub
		super.resetMeasure( measure );
		m_ccAnalyzer.resetMeasure(measure);
		m_consChecker.resetMeasure(measure);
	}

	@Override
	public void resetMeasures() {
		// TODO Auto-generated method stub
		super.resetMeasures();
		m_ccAnalyzer.resetMeasures();
		m_consChecker.resetMeasures();
	}

	@Override
	public boolean isMeasureSupported(String measure) {
		// TODO Auto-generated method stub
		return 	super.isMeasureSupported( measure )
				|| m_ccAnalyzer.isMeasureSupported( measure )
				|| m_consChecker.isMeasureSupported( measure );
	}
}
