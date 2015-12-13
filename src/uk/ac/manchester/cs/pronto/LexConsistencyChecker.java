/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.events.EVENT_TYPES;
import uk.ac.manchester.cs.pronto.events.ReasoningEventHandler;
import uk.ac.manchester.cs.pronto.lp.CCSetAnalyzerImpl2;
import uk.ac.manchester.cs.pronto.zpartition.ZPartition;
import uk.ac.manchester.cs.pronto.zpartition.ZPartitioner;
import uk.ac.manchester.cs.pronto.zpartition.ZPartitionerCGImpl;
import uk.ac.manchester.cs.pronto.zpartition.ZPartitionerImpl;

/**
 * <p>Title: LexConsistencyChecker</p>
 * 
 * <p>Description: 
 *  Provides consistency checking services to the main reasoner. 
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class LexConsistencyChecker implements ConsistencyChecker {

	Logger s_logger = Logger.getLogger(LexConsistencyChecker.class);	
	
	protected PSATSolver	m_solver;
	protected ZPartitioner	m_zper;
	//Event handlers map
	private Map<EVENT_TYPES, List<ReasoningEventHandler>> m_eventHandlers = new HashMap<EVENT_TYPES, List<ReasoningEventHandler>>();
	
	public LexConsistencyChecker(PSATSolver solver) {
		
		m_solver = solver;
		
		m_zper = Constants.USE_CG_ZPARTITIONER
			? new ZPartitionerCGImpl(new CCSetAnalyzerImpl2() )
			: new ZPartitionerImpl(m_solver );
	}

	public void setEventHandlers(Map<EVENT_TYPES, List<ReasoningEventHandler>> handlersMap) {
		
		m_eventHandlers = handlersMap;
		// Propagate event handlers down to the z-partitioner	
		m_zper.setEventHandlers( m_eventHandlers );			
	}
	
	public int isConsistent(ProbKnowledgeBase pkb) {

		PTBox ptbox = pkb.getPTBox();

		ZPartition zp = decideGenericConsistency( ptbox );

		if( null == zp ) {

			s_logger.warn( "PKB is g-inconsistent" );
			
			return PKB_G_INCONSISTENT;
		}

		if( !isAConsistent( pkb ) ) {
			
			return PKB_A_INCONSISTENT;
		}

		return PKB_CONSISTENT;
	}

	/**
	 * Here we compute z-partition
	 * 
	 * @param ptbox
	 * @return
	 */
	public ZPartition decideGenericConsistency(PTBox ptbox) {

		long t = System.currentTimeMillis();
		
		if( !m_solver.isPTBoxSatisfiable( ptbox ) ) {
			// if PTBox is unsatisfiable, it's trivially inconsistent
			return null;

		} else {

			s_logger.info( "ptbox satisfiable, determined in " + (System.currentTimeMillis() - t)
					+ " ms for " + ptbox.getDefaultConstraints().size() + " constraints" );

			return m_zper.partition( ptbox );
		}
	}

	/**
	 * Decides consistency of one specific PABox. It is more frequently used
	 * than consistency for all individuals.
	 * 
	 * @param individual
	 * @return
	 */
	public boolean isAConsistent(ProbKnowledgeBase pkb, ATermAppl individual) {
		
		PTBox ptbox = pkb.getPTBoxForIndividual( individual );
		
		return m_solver.isPTBoxSatisfiable( ptbox );
	}
	
	/**
	 * Checks satisfiability of TBox combined with CC's for any probabilistic
	 * individual
	 */
	public boolean isAConsistent(ProbKnowledgeBase pkb) {

		/*
		 * TODO
		 * Question: if KB is unsatisfiable for _some_ probabilistic individual,
		 * do we want to know for which one? Probably, yes. So, we can store it
		 * somewhere
		 */
		for (ATermAppl individual : pkb.getPABox().getProbabilisticIndividuals()) {
			
			if (!m_solver.isPTBoxSatisfiable( pkb.getPTBoxForIndividual( individual ) )) {
				
				s_logger.warn( "PKB inconsistent for individual " + individual );
				
				return false;
			}
		}
		
		return true;
	}

	/*---------------------------------------------------------------------------------------------
	 * Telemetry
	 *---------------------------------------------------------------------------------------------
	 */
	
	@Override
	public String getMeasure(String measure) {
		
		return m_zper.getMeasure( measure );
	}

	@Override
	public Collection<String> getMeasureNames() {
		
		return m_zper.getMeasureNames();
	}

	@Override
	public boolean isMeasureSupported(String measure) {
		
		return m_zper.isMeasureSupported( measure );
	}

	@Override
	public void resetMeasure(String measure) {
		
		m_zper.resetMeasure( measure );
	}

	@Override
	public void resetMeasures() {
		
		m_zper.resetMeasures();
	}
	
	
}
