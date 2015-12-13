/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.pronto.alg.IterativeHittingSetAlgorithm;
import uk.ac.manchester.cs.pronto.alg.IterativeMIPBasedHittingSetAlgorithm;
import uk.ac.manchester.cs.pronto.alg.MaxConflictlessSubsetsAlgorithm;
import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.alg.MaxConflictlessSubsetsAlgorithmImpl;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.PTBoxImpl;
import uk.ac.manchester.cs.pronto.constraints.CCSetAnalyzer;
import uk.ac.manchester.cs.pronto.events.EVENT_TYPES;
import uk.ac.manchester.cs.pronto.events.EventHandlerWithFeedback;
import uk.ac.manchester.cs.pronto.events.ReasoningEvent;
import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.exceptions.EventHandlingException;
import uk.ac.manchester.cs.pronto.util.NumberUtils;

/**
 * @author Pavel Klinov pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Mar 12, 2008
 */
public class CCSetAnalyzerImpl2 implements CCSetAnalyzer,
                                           EventHandlerWithFeedback<Set<ConditionalConstraint>> {

	public enum TELEMETRY {IIS_NUMBER, REPAIR_NUMBER};
	
	private Logger m_logger = Logger.getLogger(CCSetAnalyzerImpl2.class);
	private int m_iisLimit = 100;
	protected CGLPGeneratorImpl	m_generator = null;
	private Map<String, Double> m_teleMap = new HashMap<String, Double>();
	
	/*
	 * Used to compute maximal satisfiable subsets of constraints
	 */
	protected MaxConflictlessSubsetsAlgorithm m_mcsAlg = new MaxConflictlessSubsetsAlgorithmImpl();

	public CCSetAnalyzerImpl2() {
		
		resetMeasures();
	}
	
	public Set<Set<ConditionalConstraint>> getMaximalSatSubset(Set<ConditionalConstraint> toughConstraints, PTBox ptbox) {
		/*
		 * Not yet implemented. To be removed
		 */
		return null;
	}

	public Set<Set<ConditionalConstraint>> getMaximalSatSubsets(
										Set<ConditionalConstraint> toughConstraints, PTBox ptbox) {
		/*
		 * The strategy is to first find all minimal unsatisfiable subsets, then
		 * produce the minimal hitting sets and remove them from the PTBox
		 */
		Set<Set<ConditionalConstraint>> minUnsatSubsets = getMinimalUnsatSubsets( toughConstraints, ptbox );
		Set<Set<ConditionalConstraint>> maxSatSubsets = m_mcsAlg.compute( ptbox.getDefaultConstraints(), minUnsatSubsets );
		
		return maxSatSubsets;
	}

	/**
	 * Computes some minimal unsatisfiable subset
	 * 
	 * @return Minimal unsatisfiable subset of PTBox's constraint wrt toughConstraints or
	 * an empty set if PTBox is satisfiable wrt toughConstraints
	 */
	public Set<ConditionalConstraint> getMinimalUnsatSubset(
														Set<ConditionalConstraint> untouchCC,
														PTBox ptbox) {
		
		Set<ConditionalConstraint> emptySet = Collections.emptySet();
		untouchCC = untouchCC == null ? emptySet : untouchCC;
		m_generator = new StabilizedCGLPGeneratorImpl();
		//We need to find *some* minimal unsatisfiable subset of PTBox given tough
		//(i.e. non-removable) constraints.
		Set<ConditionalConstraint> added = ptbox.addDefaultConstraints( untouchCC );
		m_generator.getLPforPSAT( ptbox );//LP model is generated here
		Set<ConditionalConstraint> iiSet = findIIS( m_generator, ptbox );

		iiSet.removeAll( untouchCC );
		ptbox.removeDefaultConstraints( added );
		
		updateIntMetric( TELEMETRY.IIS_NUMBER, iiSet.isEmpty() ? 0 : 1 );
		
		return iiSet;
	}

	/**
	 * Finds all minimal unsatisfiable subsets
	 */
	public Set<Set<ConditionalConstraint>> getMinimalUnsatSubsets(
														Set<ConditionalConstraint> untouchConstraints,
														PTBox ptbox) {

		Set<Set<ConditionalConstraint>> iisSets = new HashSet<Set<ConditionalConstraint>>();
		Set<Set<ConditionalConstraint>> unsatSubsets = new HashSet<Set<ConditionalConstraint>>();
		Set<ConditionalConstraint> added = ptbox.addDefaultConstraints(untouchConstraints );

		m_generator = new StabilizedCGLPGeneratorImpl();
		CCAwareLPSolver psatLP = m_generator.getLPforPSAT( ptbox );
		//It's quite possible that the system is solvable
		if( LPUtils.isPSATLPinstanceSolvable( psatLP ) ) {

			m_logger.debug( "The system is solvable so there won't be any IISes" );

			unsatSubsets = Collections.emptySet();
		}
		else {
			
			Set<ConditionalConstraint> emptySet = Collections.emptySet();
			
			findAllIISes( 	untouchConstraints == null ? emptySet : untouchConstraints,
							m_generator,
							new IterativeMIPBasedHittingSetAlgorithm<ConditionalConstraint>(),
							iisSets,
							new HashSet<Set<ConditionalConstraint>>(),
							ptbox);

			for (Set<ConditionalConstraint> iis : iisSets) {
				//Check minimality
				addToIISes(unsatSubsets, iis);
			}
		}
		
		updateIntMetric( TELEMETRY.IIS_NUMBER, unsatSubsets.size() );
		
		ptbox.removeDefaultConstraints( added );

		return unsatSubsets;
	}
	
	
	
	private void addToIISes(Set<Set<ConditionalConstraint>> unsatSubsets, Set<ConditionalConstraint> iiSet) {
		
		boolean add = true;

		for( Set<ConditionalConstraint> unsatSet : unsatSubsets ) {

			if( iiSet.containsAll( unsatSet ) ) {
				// It's not minimal
				add = false;
				break;
			}

			if( unsatSet.containsAll( iiSet ) ) {
				// Some already added conflict is not minimal
				add = false;
				unsatSet.retainAll( iiSet );
				break;
			}
		}

		if( add ) unsatSubsets.add( iiSet );		
	}

	/*
	 * Basically, the method computes conflict graph
	 */
	public Map<Set<ConditionalConstraint>, Set<Set<ConditionalConstraint>>> getAllConflictSets(
			Set<Set<ConditionalConstraint>> strictConstraints, PTBox ptbox) {

		/*
		 * Naive implementation: it generates linear number of PSAT instances
		 */
		Map<Set<ConditionalConstraint>, Set<Set<ConditionalConstraint>>> resultMap = 
			new HashMap<Set<ConditionalConstraint>, Set<Set<ConditionalConstraint>>>();
		
		for (Set<ConditionalConstraint> ccSet : strictConstraints) {
			
			resultMap.put( ccSet, getMinimalUnsatSubsets( ccSet, ptbox ) );
		}

		return resultMap;
	}
	
	/**
	 * Finds all irreducible infeasible systems
	 */
	protected void findAllIISes(	Set<ConditionalConstraint> untouch,	
									CGLPGeneratorImpl lpGen,
									IterativeHittingSetAlgorithm<ConditionalConstraint> hsAlgo,
									Set<Set<ConditionalConstraint>> iisSets,
									Set<Set<ConditionalConstraint>> prevHitSets,
									PTBox ptbox){

		CCAwareLPSolver model = lpGen.getLPModel();
		Set<ConditionalConstraint> newIIS = null;	
		//Register itself as a handler of the column generation stalling events
		//lpGen.setPartialPSATEventHandler( this );
		
		if (iisSets.isEmpty()) {
			//That's probably the first time the method is called, so need to find the first IIS
			newIIS = findIIS( lpGen, ptbox );
			newIIS.removeAll( untouch );
			iisSets.add(newIIS);
			
			m_logger.info( "New IIS found: " + newIIS );
			
		} else if (iisSets.size() < m_iisLimit){
		
			int cnt = 0;
			
			hsAlgo.setSets( iisSets );
			
			while (hsAlgo.hasNext()) {
				
				Set<ConditionalConstraint> hSet = hsAlgo.next();
				//See if we tried this hitting set before (or its subset)
				//and it didn't lead to a new IIS
				if (!isSuperset(hSet, prevHitSets)) {
					
					pullOutHittingSet(model, hSet);
					newIIS  = findIIS( lpGen, ptbox );
					newIIS.removeAll( untouch );//Remove untouchable var indexes from the IIS
					restoreHittingSet(model, hSet);
					
					updateIntMetric( TELEMETRY.REPAIR_NUMBER, 1 );
					
					if (!newIIS.isEmpty()) {
						
						iisSets.add( newIIS );
						
						m_logger.info( "New IIS found: " + newIIS );
						m_logger.info( "Current number of IISes: " + iisSets.size() );
						
						break;
						
					} else {
						
						m_logger.debug( "Repair was successful" );
						prevHitSets.add( hSet );
					}
				}
				
				cnt += 1;
				m_logger.debug( "Checked " + cnt + " repairs, number of conflicts: " + iisSets.size() );
			}
		}
		
		if (newIIS != null && !newIIS.isEmpty()) {
			//New IIS has been found, so we need to repeat the whole thing
			findAllIISes( untouch, lpGen, hsAlgo, iisSets, prevHitSets, ptbox );
		}
	}
	
	/*
	 * Fixes the variables in the index set at zero
	 */
	private void pullOutHittingSet(	CCAwareLPSolver model, Set<ConditionalConstraint> hSet) {

		for (ConditionalConstraint cc : hSet) model.relaxRows( cc );
	}
	/*
	 * Sets the variables in the index set free
	 */
	private void restoreHittingSet(	CCAwareLPSolver model, Set<ConditionalConstraint> hSet) {

		for (ConditionalConstraint cc : hSet) model.enforceRows( cc );
	}

	/*
	 * Finds some IIS
	 */
	private Set<ConditionalConstraint> findIIS(	CGLPGeneratorImpl lpGen, PTBox ptbox) {
		
		Set<ConditionalConstraint> iis = new HashSet<ConditionalConstraint>();
		CCAwareLPSolver model = lpGen.getLPModel();
		//Try to generate more columns to see if the current system is infeasible
		if (isInfeasible( lpGen )) {
			
			m_logger.debug( "IIS candidate:" );
			
			for (int rowIndex : lpGen.getNonZeroIndexes()) {
				
				ConditionalConstraint cc = model.getConstraint( rowIndex );
				
				if (cc != null)	{
					
					m_logger.debug( cc );
					iis.add( cc );
				}
			}
			
			minimizeIS(iis, model, ptbox);
		}
		
		return iis;
	}

	/*
	 * Minimizes infeasible systems to get irreducible ones (IISes)
	 */
	private void minimizeIS(Set<ConditionalConstraint> iis,
							CCAwareLPSolver model,
							PTBox ptbox) {
		
		//Create a little PTBox for the corresponding constraints
		PTBox little = new PTBoxImpl(ptbox.getClassicalKnowledgeBase(), ptbox.getClassicalOntology(), iis);
		CGLPGeneratorImpl littleLPgen = new CGLPGeneratorImpl();
		CCAwareLPSolver littleLP = littleLPgen.getLPforPSAT( little );
		//Now start pulling the IS rows out
		//TODO Binary search should be better in case of high redundancy
		for (Iterator<ConditionalConstraint> iter = iis.iterator(); iter.hasNext();) {

			ConditionalConstraint cc = iter.next();
			
			littleLP.relaxRows( cc );
			
			m_logger.debug( "Freeing constraint: " + cc );			
			
			if (!isInfeasible( littleLPgen )) {
				
				m_logger.debug( "System now feasible, the constraint is essential" );
				//The rows for that constraints is essential for infeasibility,
				//so we'll keep at least one of them and restore their bounds 
				littleLP.enforceRows( cc );
				
			} else {
				
				m_logger.debug( "System still infeasible, constraint removed");
				
				iter.remove();
			}
		}
		//Free some native memory
		littleLP.dispose();
	}
	
	private boolean isInfeasible(CGLPGeneratorImpl lpGen) {
		
		try {
			
			double objValue = lpGen.generateColumns( lpGen.getColumnGenerator(), lpGen.getLPModel() );
			
			return !NumberUtils.equal(objValue, 1d );
			
		} catch( CPException e ) {
			
			m_logger.fatal( "Fatal error when trying to minimize an IS", e );
			
			throw new RuntimeException(e);
		}
	}
	
	private <T> boolean isSuperset(Set<T> hSet, Set<Set<T>> prevHitSets) {
		
		for (Set<T> set : prevHitSets) {
			
			if (hSet.containsAll( set )) return true;
		}
		
		return false;
	}
	
	/*
	 * This method can be called from within the column generation process to check if that 
	 * can be stopped early (if some subset of the linear system is infeasible)
	 */
	@Override
	public Set<ConditionalConstraint> handleEventWithFeedback(ReasoningEvent event) throws EventHandlingException {

		Set<ConditionalConstraint> conflict = new HashSet<ConditionalConstraint>();
		
		if (event.getType().equals(EVENT_TYPES.COL_GENERATION_STALLED )) {
		
			PTBox littlePTBox = (PTBox)event.getParameters()[0];
			StabilizedCGLPGeneratorImpl littleLPgen = new StabilizedCGLPGeneratorImpl();
			
			littleLPgen.setPartialPSATEventHandler( null );
			
			CCAwareLPSolver littleLP = littleLPgen.getLPforPSAT( littlePTBox );
			
			if (!LPUtils.isPSATLPinstanceSolvable( littleLP )) {
				
				//Get conflicting conditional constraints
				for (int varIndex : littleLPgen.getNonZeroIndexes() ){
					
					conflict.add( littleLP.getConstraint( varIndex ) );
				}
			}
			
			littleLP.dispose();//Free some native memory
		}
		
		return conflict;
	}

	@Override
	public void handleEvent(ReasoningEvent event) throws EventHandlingException {
		
		handleEventWithFeedback(event);
	}	
	
/* 
 * ============================================================================
 * Telemetry
 * ============================================================================
 */	

	private void updateIntMetric(TELEMETRY name, int num) {
		
		m_teleMap.put( name.toString(), num + m_teleMap.get( name.toString() ) );
	}
	
	@Override
	public String getMeasure(String measure) {

		Double metric = m_teleMap.get( measure ); 
		
		return null == metric ? null : metric.toString(); 
	}

	@Override
	public Collection<String> getMeasureNames() {

		return new HashSet<String>(m_teleMap.keySet());
	}

	@Override
	public boolean isMeasureSupported(String measure) {

		return m_teleMap.containsKey( measure );
	}

	@Override
	public void resetMeasure(String measure) {

		if (null != TELEMETRY.valueOf( measure )) {
			
			m_teleMap.put( measure.toString(), 0d );
		}		
	}

	@Override
	public void resetMeasures() {

		m_teleMap.put(TELEMETRY.IIS_NUMBER.toString(), 0d);
		m_teleMap.put(TELEMETRY.REPAIR_NUMBER.toString(), 0d);
	}
}
