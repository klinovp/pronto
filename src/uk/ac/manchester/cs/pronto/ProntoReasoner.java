/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.Set;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.benchmark.Telemetry;
import uk.ac.manchester.cs.pronto.events.EVENT_TYPES;
import uk.ac.manchester.cs.pronto.events.ReasoningEventHandler;
import uk.ac.manchester.cs.pronto.exceptions.ProbabilisticInconsistencyException;

/**
 * <p>Title: ProntoReasoner</p>
 * 
 * <p>Description: 
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public interface ProntoReasoner extends Telemetry {

	public ConditionalConstraint subsumptionEntailment(	ProbKnowledgeBase pkb,
														ATermAppl evidence,
														ATermAppl conclusion)
			throws ProbabilisticInconsistencyException;

	public ConditionalConstraint membershipEntailment(	ProbKnowledgeBase pkb,
														ATermAppl individual,
														ATermAppl concept)
														throws ProbabilisticInconsistencyException;

	public boolean isSatisfiable(PTBox ptbox);
	public int isConsistent(ProbKnowledgeBase pkb);
	public Set<Set<ConditionalConstraint>> computeMinUnsatisfiableSubsets(PTBox ptbox);
	/*
	 * FIXME Hacky-hacky-hacky!
	 * It computes the set of fragments which are unsatisfiable *if* some evidence classes
	 * have a non-zero probability.
	 */
	public Set<Set<ConditionalConstraint>> computeMinIncoherentSubsets(PTBox ptbox);
	public void setEventHandler(EVENT_TYPES type, ReasoningEventHandler handler);
}
