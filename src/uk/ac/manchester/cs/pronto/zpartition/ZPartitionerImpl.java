/**
 * 
 */
package uk.ac.manchester.cs.pronto.zpartition;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PSATSolver;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.PTBoxSuppData;
import uk.ac.manchester.cs.pronto.events.EVENT_TYPES;
import uk.ac.manchester.cs.pronto.events.EventHandlingUtils;
import uk.ac.manchester.cs.pronto.events.ReasoningEvent;
import uk.ac.manchester.cs.pronto.events.ReasoningEventHandler;
import uk.ac.manchester.cs.pronto.util.CCUtils;

/**
 * <p>Title: ZPartitioner</p>
 * 
 * <p>Description: 
 *  Generates z-partition of a probabilistic TBox
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class ZPartitionerImpl implements ZPartitioner {

	Logger s_logger = Logger.getLogger(this.getClass());
	
	protected PSATSolver m_solver;
	//Event handlers map
	private Map<EVENT_TYPES, List<ReasoningEventHandler>> m_eventHandlers = new HashMap<EVENT_TYPES, List<ReasoningEventHandler>>();
	
	
	public ZPartitionerImpl(PSATSolver solver) {
		
		m_solver = solver;
	}
	
	public void setEventHandlers(Map<EVENT_TYPES, List<ReasoningEventHandler>> handlersMap) {
		
		m_eventHandlers = handlersMap;
	}
	
	/**
	 * Knowledge base is passed because some reasoning services are required to
	 * create z-partition. It would be more logical just to pass PTBox but then
	 * I would have to create some fake KB inside. I would prefer not to have any
	 * reasoning methods in KB class, but use an external reasoner instead.
	 * Oh, nothing can I control...
	 * 
	 * @param ptbox
	 */
	public ZPartition partition(PTBox ptbox) {
		
		Set<ConditionalConstraint> ccSet = new HashSet<ConditionalConstraint>(ptbox.getDefaultConstraints());
		Set<ConditionalConstraint> ccSubset = new HashSet<ConditionalConstraint>();
		Map<ATermAppl, Set<ConditionalConstraint>> evidenceToCCMap = null;
		ZPartitionImpl zp = new ZPartitionImpl();
		int index = 0;
		//First check if we can load the partition from disk
		fireBeforeComputeZPartitionEvent( ptbox );

		PTBoxSuppData suppData = ptbox.getSupplementalData();		
		
		if (suppData.getZPartition() != null) return suppData.getZPartition();//We're done
		
		//A copy of the ptbox so that we are free to add/remove constraints
		PTBox clone = ptbox.clone(false);
		long t = System.currentTimeMillis();
		
		do {
			
			int evIndex = 1;
			
			clone.setDefaultConstraints( ccSet );
			ccSubset.clear();
			//Find all distinct evidence classes for the current set of constraints
			evidenceToCCMap = getEvidenceToConstraintsMap(ccSet);
			
			for (ATermAppl evidence : evidenceToCCMap.keySet()) {
				
				Set<ConditionalConstraint> sameEvidenceSet = evidenceToCCMap.get( evidence );
				
				s_logger.debug( "Processing evidence " + evIndex + " out of " + evidenceToCCMap.size() );
				s_logger.debug( sameEvidenceSet.size() + " constraints with the same evidence " + evidence );
				
				if (isTolerated(evidence, clone)) {
					
					ccSubset.addAll(sameEvidenceSet);
				}
				
				s_logger.debug( "Current number of tolerated constraints: " + ccSubset.size() + " out of " + ccSet.size() );
				evIndex++;
			}
			
			if (!ccSubset.isEmpty()) {

				ccSet.removeAll( ccSubset );
				zp.add(ccSubset, index++); 
				s_logger.debug( "New subset added: " + ccSubset.size() + " constraints" );
				s_logger.debug( ccSet.size() + " constraints to go" );
			}
			
		} while (! (ccSubset.isEmpty() || ccSet.isEmpty()) );
		
		s_logger.info( "PTBox z-partitioned in: " + (System.currentTimeMillis() - t) + "ms");
		
		if (!ccSubset.isEmpty()) {
			
			suppData.setZPartition( zp );
			fireAfterZPartitionGraphEvent( ptbox );
			
			return zp;
			
		} else return null;
	}
	
	private Map<ATermAppl, Set<ConditionalConstraint>> getEvidenceToConstraintsMap(Set<ConditionalConstraint> ccSet) {

		Map<ATermAppl, Set<ConditionalConstraint>> evidenceToCCMap = new HashMap<ATermAppl, Set<ConditionalConstraint>>();		
		
		for (ConditionalConstraint cc : ccSet) {
			
			Set<ConditionalConstraint> sameEvSet = evidenceToCCMap.get( cc.getEvidence() );
			
			if (sameEvSet == null) {
				
				sameEvSet = new HashSet<ConditionalConstraint>();
			}
			
			sameEvSet.add( cc );
			evidenceToCCMap.put( cc.getEvidence(), sameEvSet );
		}
		
		return evidenceToCCMap;
	}

	/**
	 * Central method here: it determines if a PTBox remains satisfiable
	 * after adding a constraint of the form (C|T)(1;1) (i.e. whether the PTBox
	 * _tolerates_ all constraints with this evidence class)
	 */
	protected boolean isTolerated(ATermAppl evidence, PTBox ptbox) {
	
		boolean result = false;
		boolean evTop = evidence.isEqual( ATermUtils.TOP );
		boolean added = false;
		
		if (!evTop) {
		
			ConditionalConstraint evVerified = CCUtils.conceptVerificationConstraint(evidence);
			
			added = ptbox.addDefaultConstraint(evVerified);
			result = m_solver.isPTBoxSatisfiable(ptbox);
			
			if (added)	ptbox.removeDefaultConstraint(evVerified);
			
			return result;
			
		} else return true;
	}

	private String getOntologyID(String id) {
		
		String[] parts = id.split( "/" );
		
		return parts[parts.length - 1];
	}	
	
	protected void fireAfterZPartitionGraphEvent(PTBox ptbox) {

		PTBoxSuppData suppData = ptbox.getSupplementalData();
		ZPartition zp = suppData.getZPartition();
		ReasoningEvent.SimpleEventImpl zpEvent = new ReasoningEvent.SimpleEventImpl(EVENT_TYPES.AFTER_ZPARTITION_COMPUTED );

		zpEvent.setParameters( new Object[] {zp, getOntologyID( ptbox.getID() ) } );
		//Signal that z-partition has been computed (successfully or unsuccessfully)
		EventHandlingUtils.fireEvent( zpEvent, m_eventHandlers );
	}	
	
	//Invokes handlers that can help us by loading some previously computed z-partition
	protected void fireBeforeComputeZPartitionEvent(PTBox ptbox) {

		PTBoxSuppData suppData = ptbox.getSupplementalData();
		
		if (suppData.getZPartition() == null) {
			
			ReasoningEvent.SimpleEventImpl zpEvent = new ReasoningEvent.SimpleEventImpl(EVENT_TYPES.BEFORE_ZPARTITION_COMPUTED );
			zpEvent.setParameters( new Object[] {getOntologyID( ptbox.getID() ), suppData } );
			
			EventHandlingUtils.fireEvent( zpEvent, m_eventHandlers );
		}
	}	
	
	/*----------------------------------------------------------------------------------------------
	 *Telemetry
	 *---------------------------------------------------------------------------------------------- 
	 */
	public String getMeasure(String measure) {

		return m_solver.getMeasure( measure );  
	}

	public void resetMeasure(String measure) {
		
		m_solver.resetMeasure( measure );
	}

	public void resetMeasures() {

		m_solver.resetMeasures();
	}

	@Override
	public Collection<String> getMeasureNames() {
	
		return m_solver.getMeasureNames();
	}

	@Override
	public boolean isMeasureSupported(String measure) {
		
		return m_solver.isMeasureSupported( measure );
	}	
}
