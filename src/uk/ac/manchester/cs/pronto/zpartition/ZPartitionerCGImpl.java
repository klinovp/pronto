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

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.PTBoxSuppData;
import uk.ac.manchester.cs.pronto.constraints.CCSetAnalyzer;
import uk.ac.manchester.cs.pronto.constraints.ConflictGraph;
import uk.ac.manchester.cs.pronto.constraints.ConflictGraphImpl;
import uk.ac.manchester.cs.pronto.constraints.KBBasedCCSetInclusionQualifier;
import uk.ac.manchester.cs.pronto.events.EVENT_TYPES;
import uk.ac.manchester.cs.pronto.events.EventHandlingUtils;
import uk.ac.manchester.cs.pronto.events.ReasoningEvent;
import uk.ac.manchester.cs.pronto.events.ReasoningEventHandler;
import uk.ac.manchester.cs.pronto.util.CCUtils;

/**
 * Implementation of ZPartitioner based on Conflict Graphs
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class ZPartitionerCGImpl implements ZPartitioner {

	static Logger s_logger = Logger.getLogger(ZPartitionerCGImpl.class);	
	
	private CCSetAnalyzer m_ccAnalyser = null;
	//Event handlers map
	private Map<EVENT_TYPES, List<ReasoningEventHandler>> m_eventHandlers = new HashMap<EVENT_TYPES, List<ReasoningEventHandler>>();
	
	public ZPartitionerCGImpl(CCSetAnalyzer analyzer) {
		
		m_ccAnalyser = analyzer;
	}

	public void setEventHandlers(Map<EVENT_TYPES, List<ReasoningEventHandler>> handlersMap) {
		
		m_eventHandlers = handlersMap;
	}
	
	/**
	 * @param ptbox
	 * @return
	 */
	public ZPartition partition(PTBox ptbox) {
		
		PTBoxSuppData suppData = ptbox.getSupplementalData();
		ConflictGraph cGraph = null;
		Map<ATermAppl, Set<ConditionalConstraint>> evidenceToCCMap = getEvidenceToConstraintsMap(ptbox.getDefaultConstraints());		
		
		fireBeforeComputeConflictGraphEvent(ptbox);
		// First see, if conflict graph already exists
		if( (cGraph = suppData.getConflictGraph()) == null ) {
			// Oh well, seems that we've got some hard job to do
			PTBox clone = ptbox.clone();
			int evIndex = 1;

			cGraph = new ConflictGraphImpl(new KBBasedCCSetInclusionQualifier( ptbox.getClassicalKnowledgeBase() ) );
			// Now loop over the evidence classes to find conflicting sets of constraints
			for( ATermAppl evidence : evidenceToCCMap.keySet() ) {

				Set<ConditionalConstraint> sameEvidenceSet = evidenceToCCMap.get( evidence );
				long ts = System.currentTimeMillis();
				// Here we're going to generate a linear number of the Diagnosis problem instances
				s_logger.debug( "Processing evidence " + evIndex + " out of " + evidenceToCCMap.size() );
				s_logger.debug( sameEvidenceSet.size() + " constraints with the same evidence " + evidence );

				addNonToleratingSubsets(CCUtils.conceptVerificationConstraint(evidence ), cGraph, clone );

				s_logger.debug( "Done in " + (System.currentTimeMillis() - ts) + " ms" );
				evIndex++;
			}
			// Now we have conflict graph. The rest is to build z-partition
			suppData.setConflictGraph( cGraph );
			// Notify event handlers
			fireAfterComputeConflictGraphEvent( ptbox );
		}
		//Save z-partition for future use
		suppData.setZPartition( partition(evidenceToCCMap, ptbox.getDefaultConstraints(), cGraph) );
		
		return suppData.getZPartition();
	}
	
	
	private void addNonToleratingSubsets(ConditionalConstraint cc, ConflictGraph cGraph, PTBox ptbox) {

		Set<ConditionalConstraint> hardConstraints = new HashSet<ConditionalConstraint>(1);
		Set<Set<ConditionalConstraint>> ntSets = null;
		
		hardConstraints.add(cc);
		/*
		 * All the interesting stuff runs inside this thing
		 */
		ntSets = m_ccAnalyser.getMinimalUnsatSubsets(hardConstraints, ptbox);
		
		cGraph.addConflicts( hardConstraints, ntSets);
	}
	
	
	/**
	 * Creates z-partition by examining the conflict graph
	 * @param ptbox
	 * @param cGraph
	 * @return
	 */
	protected ZPartition partition(	Map<ATermAppl, Set<ConditionalConstraint>> evidenceToCCMap,
									Set<ConditionalConstraint> allConstraints,
									ConflictGraph cGraph) {
		
		ZPartition zp = new ZPartitionImpl();
		
		Set<ConditionalConstraint> current = new HashSet<ConditionalConstraint>(allConstraints);
		Set<ConditionalConstraint> next = null;
		Set<ConditionalConstraint> added = new HashSet<ConditionalConstraint>();
		
		do {
			
			next = addPartition(cGraph, current, evidenceToCCMap);

			next.removeAll( added );
			
			if (next.size() != 0) {
			
				zp.add( next );
				current.removeAll( next );
				added.addAll( next );
			}
			
		} while (0 != next.size() && current.size() > 0);
		
		return current.size() == 0 ? zp : null;
	}

	/*
	 * Finds all constraints in the graph that are not tolerated _only_ by any subset of
	 * current.
	 */
	private Set<ConditionalConstraint> addPartition(ConflictGraph cGraph,
													Set<ConditionalConstraint> current,
													Map<ATermAppl, Set<ConditionalConstraint>> evidenceToCCMap) {
		
		Set<ConditionalConstraint> next = new HashSet<ConditionalConstraint>();

		for (Set<ConditionalConstraint> evidenceSingleton : cGraph.getSetsNotUnderConflict( current )) {
			//Get all constraint with this evidence class
			next.addAll( evidenceToCCMap.get( evidenceSingleton.iterator().next().getConclusion() ) );
		}
		
		return next;
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
	
	
	private String getOntologyID(String id) {
		
		String[] parts = id.split( "/" );
		
		return parts[parts.length - 1];
	}
	
	protected void fireAfterComputeConflictGraphEvent(PTBox ptbox) {

		PTBoxSuppData suppData = ptbox.getSupplementalData();
		ConflictGraph cg = suppData.getConflictGraph();
		
		ReasoningEvent.SimpleEventImpl cgEvent = new ReasoningEvent.SimpleEventImpl(
														EVENT_TYPES.AFTER_CONFLICT_GRAPH_COMPUTED );

		cgEvent.setParameters( new Object[] { cg, getOntologyID(ptbox.getID()) } );
		// Signal that conflict graph has been computed (successfully or unsuccessfully)
		EventHandlingUtils.fireEvent(cgEvent, m_eventHandlers );
	}
	//Invokes handlers that can help us by loading some previously computed
	//conflict graph
	protected void fireBeforeComputeConflictGraphEvent(PTBox ptbox) {

		PTBoxSuppData suppData = ptbox.getSupplementalData();
		ConflictGraph cg = suppData.getConflictGraph();
		
		if (cg == null) {
			
			ReasoningEvent.SimpleEventImpl cgEvent = new ReasoningEvent.SimpleEventImpl(
															EVENT_TYPES.BEFORE_CONFLICT_GRAPH_COMPUTED );
			cgEvent.setParameters( new Object[] {getOntologyID(ptbox.getID()), suppData} );
			
			EventHandlingUtils.fireEvent( cgEvent, m_eventHandlers );
		}
	}
	
	/*----------------------------------------------------------------------------------------------
	 *Telemetry
	 *---------------------------------------------------------------------------------------------- 
	 */
	public String getMeasure(String measure) {

		return m_ccAnalyser.getMeasure( measure );  
	}

	public void resetMeasure(String measure) {
		
		m_ccAnalyser.resetMeasure( measure );
	}

	public void resetMeasures() {

		m_ccAnalyser.resetMeasures();
	}

	@Override
	public Collection<String> getMeasureNames() {
	
		return m_ccAnalyser.getMeasureNames();
	}

	@Override
	public boolean isMeasureSupported(String measure) {
		
		return m_ccAnalyser.isMeasureSupported( measure );
	}
}
