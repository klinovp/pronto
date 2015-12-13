/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.lp.CCAwareLPSolver;
import uk.ac.manchester.cs.pronto.lp.LPGenerator;
import uk.ac.manchester.cs.pronto.lp.LPUtils;
import uk.ac.manchester.cs.pronto.lp.StabilizedCGLPGeneratorImpl;

/**
 * @author Pavel Klinov
 * 
 *  pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 *  
 *  Sep 19, 2008
 *         
 */
public class PSATSolverImpl implements PSATSolver {

	private static Logger	s_logger	= Logger.getLogger( PSATSolverImpl.class );

	private LPGenerator		m_lpGen		= new StabilizedCGLPGeneratorImpl();
	private PTBox			m_lastPTBox = null;

	public PSATSolverImpl() {}

	public boolean isPTBoxSatisfiable(PTBox ptbox) {

		s_logger.info( "+++ PSAT started +++" );
		s_logger.info( ptbox.getDefaultConstraints().size() + " probabilistic statements" );
		m_lpGen.setInitialLP( null );
		
		if( !ptbox.getClassicalKnowledgeBase().isConsistent() ) {
			//If classical KB is inconsistent, then the probabilistic one can't be either
			return false;
			
		} else if( null != ptbox.getDefaultConstraints()
				&& ptbox.getDefaultConstraints().size() > 0 ) {

			CCAwareLPSolver psatLP = m_lpGen.getLPforPSAT( ptbox );
			boolean result = LPUtils.isPSATLPinstanceSolvable( psatLP );
			
			s_logger.info( "+++ PSAT finished, result: " + result + " +++" );
			//Cleanup
			psatLP.dispose();
			
			return result;
			
		} else {
			//No constraints so it's vacuously satisfiable
			return true;
		}
	}

	public ConditionalConstraint tightLogicalEntailment(PTBox ptbox, ATermAppl concept) {
		
		s_logger.info( "+++ TLogEnt started +++" );
		
		double l = 1.0, u = 0.0;
		
		m_lpGen.setInitialLP( null );
		
		CCAwareLPSolver upperLP = m_lpGen.getUpperLPforTLogEnt( ptbox, concept );		
		
		u = LPUtils.getLPObjectiveValue( upperLP );
		
		CCAwareLPSolver lowerLP = m_lpGen.getLowerLPforTLogEnt( ptbox, concept );
		
		l = LPUtils.getLPObjectiveValue( lowerLP );
		
		s_logger.info( "+++ TLogEnt finished +++" );

		return new ConditionalConstraint(ATermUtils.TOP, concept, l, u);
	}	
	
	@Override
	public double computeLowerProbability(PTBox ptbox, ATermAppl concept) {
		
		if (ptbox != m_lastPTBox) m_lpGen.setInitialLP( null );//Hack
		
		CCAwareLPSolver lp = m_lpGen.getLowerLPforTLogEnt( ptbox, concept );
		
		m_lastPTBox = ptbox;
		
		return LPUtils.getLPObjectiveValue( lp );
	}

	@Override
	public double computeUpperProbability(PTBox ptbox, ATermAppl concept) {
		
		if (ptbox != m_lastPTBox) m_lpGen.setInitialLP( null );//Hack
		
		CCAwareLPSolver lp = m_lpGen.getUpperLPforTLogEnt( ptbox, concept );
		
		m_lastPTBox = ptbox;
		
		return LPUtils.getLPObjectiveValue( lp );
	}	
	

	/*
	 * =============================================================================
	 * 
	 * Telemetry getters and setters
	 * 
	 * =============================================================================
	 */	
	
	
	@Override
	public String getMeasure(String measure) {

		return m_lpGen.getMeasure( measure );
	}

	@Override
	public Collection<String> getMeasureNames() {

		return m_lpGen.getMeasureNames();
	}

	@Override
	public void resetMeasure(String measure) {

		m_lpGen.resetMeasure( measure );
	}

	@Override
	public void resetMeasures() {
		
		m_lpGen.resetMeasures();
	}

	@Override
	public boolean isMeasureSupported(String measure) {

		return m_lpGen.isMeasureSupported( measure );
	}
}
