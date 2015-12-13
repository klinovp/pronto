/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.constraints.CCSetAnalyzer;
import uk.ac.manchester.cs.pronto.events.EVENT_TYPES;
import uk.ac.manchester.cs.pronto.events.ReasoningEventHandler;
import uk.ac.manchester.cs.pronto.exceptions.ProbabilisticInconsistencyException;
import uk.ac.manchester.cs.pronto.lp.CCSetAnalyzerImpl2;
import uk.ac.manchester.cs.pronto.util.CCUtils;
import uk.ac.manchester.cs.pronto.util.NumberUtils;
import uk.ac.manchester.cs.pronto.util.SetUtils;
import uk.ac.manchester.cs.pronto.zpartition.ZPartition;

/**
 * <p>Title: BasicLexicographicReasoner</p>
 * 
 * <p>Description: 
 *  The main reasoner class exposed to users. It internally triggers services
 *  provided by PSAT solvers and consistency checkers to do probabilistic
 *  inferencing. It can be configured to use a different PSAT solver
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class BasicLexicographicReasoner implements ProntoReasoner {

	public enum TELEMETRY {LEX_MIN_MODELS_NUMBER, LEX_MIN_MODELS_TIME, TLOGENT_TIME};
	
	Logger s_logger = Logger.getLogger(this.getClass());
	
	protected PSATSolver			m_pSATsolver;
	protected ConsistencyChecker	m_consChecker;
	//Lexicographically minimal models can be cached; it's useful for optimization
	protected Map<ProbKnowledgeBase, LexReasoningCache> m_lexCache = new HashMap<ProbKnowledgeBase, LexReasoningCache>();
	private Set<Set<ConditionalConstraint>> m_lastMinSets;
	//Event handlers map
	private Map<EVENT_TYPES, List<ReasoningEventHandler>> m_eventHandlers = new HashMap<EVENT_TYPES, List<ReasoningEventHandler>>();
	private Map<String, Double> m_teleMap = new HashMap<String, Double>();

	public BasicLexicographicReasoner() {
		
		this(new PSATSolverImpl());
		initMeasures();
	}
	
	public void reset() {
		
		m_lexCache.clear();
		resetMeasures();
	}
	
	public BasicLexicographicReasoner(PSATSolver solver) {
		
		m_pSATsolver = solver;
		m_consChecker = new LexConsistencyChecker(solver);
		// Propagate event handlers to the consistency checker (so that it can fire its events)
		m_consChecker.setEventHandlers( m_eventHandlers );
		initMeasures();
	}

	public void setEventHandler(EVENT_TYPES type, ReasoningEventHandler handler) {
		
		List<ReasoningEventHandler> handlers = m_eventHandlers.get( type );
		
		handlers = handlers == null ? new ArrayList<ReasoningEventHandler>() : handlers;
		handlers.add( handler );
		
		m_eventHandlers.put( type, handlers );
	}
	
	protected Map<EVENT_TYPES, List<ReasoningEventHandler>> getEventHandlers() {
		
		return m_eventHandlers;
	}
	
	/**
	 * Delegates call to underlying p-sat solver
	 */
	public boolean isSatisfiable(PTBox ptbox) {
		
		return m_pSATsolver.isPTBoxSatisfiable(ptbox);
	}

	/**
	 * Delegates call to underlying consistency checker
	 */
	public int isConsistent(ProbKnowledgeBase pkb) {
		
		return m_consChecker.isConsistent(pkb);
	}
	
	
	protected Set<Set<ConditionalConstraint>> checkLexCache(ProbKnowledgeBase pkb, ATermAppl evidence) {
		
		LexReasoningCache cache = m_lexCache.get( pkb ); 
		
		if (null == cache) {
			
			return null;
			
		} else {
			
			return cache.getLexMinimalSubsets( evidence );
		}
	}
	
	protected void updateLexCache(ProbKnowledgeBase pkb, ATermAppl evidence) {
		
		LexReasoningCache cache = m_lexCache.get( pkb ); 
		
		if (null == cache) {
			
			cache = new LexReasoningCache();
		}
		
		cache.cacheLexMinimalSubsets( evidence, m_lastMinSets );
		m_lexCache.put( pkb, cache );
	}
	
	public ConditionalConstraint subsumptionEntailment(	ProbKnowledgeBase pkb,
														ATermAppl evidence,
														ATermAppl conclusion)
			throws ProbabilisticInconsistencyException {

		//Check lexicographic cache first
		Set<Set<ConditionalConstraint>> lexMinSets = null;
		ConditionalConstraint result = null;
		
		if (checkConcept( pkb, evidence) && checkConcept( pkb, conclusion )) {
			
			if (null != (lexMinSets = checkLexCache( pkb, evidence ))) {
				//We're lucky
				m_lastMinSets = lexMinSets;
				s_logger.info( "Lex cache hit: subsumption entailment, evidence: " + evidence);
				result = entailFromLexMinimalSets( pkb.getPTBox(), evidence, conclusion );
				
			} else {
				
				result = tightLexicographicEntailment( pkb.getPTBox(), null, evidence, conclusion );
				
				updateLexCache( pkb, evidence );
			}
			
		} else {
		
			return CCUtils.unsatisfiableConstraint( evidence, conclusion );
		}

		return result;
	}

	
	public ConditionalConstraint membershipEntailment(	ProbKnowledgeBase pkb,
														ATermAppl individual,
														ATermAppl concept)
														throws ProbabilisticInconsistencyException {
		//Check lexicographic cache first
		Set<Set<ConditionalConstraint>> lexMinSets = null;
		ConditionalConstraint result = null;
		
		if (checkIndividual( pkb, individual) && checkConcept( pkb, concept )) {
		
			if (null != (lexMinSets = checkLexCache( pkb, individual ))) {
				//We're lucky
				m_lastMinSets = lexMinSets;
				s_logger.info( "Lex cache hit: membership entailment, individual: " + individual);
				
				result = entailFromLexMinimalSets( pkb.getPTBox(), ATermUtils.TOP, concept );
				
			} else {
				
				if (!m_consChecker.isAConsistent( pkb, individual )) {
					
					s_logger.warn( "PABox is inconsistent for individual: " + individual.toString() );
					
					throw new ProbabilisticInconsistencyException("PABox is inconsistent for individual: " + individual.toString());
				}
				
				result = tightLexicographicEntailment(	
						pkb.getPTBox(),
						pkb.getPABox().getConstraintsForIndividual( individual ),
						ATermUtils.TOP,
						concept );
				
				updateLexCache( pkb, individual );
			}
			
			return result;
		
		} else return CCUtils.unsatisfiableConstraint( ATermUtils.TOP, concept );
	}
	
	/**
	 * Main reasoning method
	 * 
	 * @param kb
	 * @param evidence
	 * @param conclusion
	 * @return
	 */
	protected ConditionalConstraint tightLexicographicEntailment(	PTBox ptbox,
																	Set<ConditionalConstraint> ccSet,
																	ATermAppl evidence,
																	ATermAppl conclusion)
																	throws ProbabilisticInconsistencyException {

		LexReasoningData lrData = beforeComputingLexMinimalSets(ptbox, ccSet, evidence, conclusion);
		ZPartition zp = lrData.getZPartition();
		Set<Set<ConditionalConstraint>> lexMinSets = lrData.getLexMinimalSets();
		/*
		 * First check if the ontology is generally consistent
		 */
		if (null == zp) {
			
			s_logger.error( "Ontology is generally inconsistent" );
			
			throw new ProbabilisticInconsistencyException("Ontology is generally inconsistent");
		}
		
		if (null == lexMinSets) {
			/*
			 * This means that we can't start doing reasoning - ccSet is in
			 * conflict with the classical knowledge
			 */
			return CCUtils.unsatisfiableConstraint( evidence, conclusion );
		}
		/*
		 * No we can try find all the lex-minimal subsets of the default
		 * constraints from which we can logically entail the conclusion.
		 * ZPartition guarantees that we iterate through the partitions in the
		 * order of decreasing specificity.
		 */
		long ts = System.currentTimeMillis();
		
		for( Iterator<Set<ConditionalConstraint>> zpIter = zp.partitionIterator(); zpIter.hasNext(); ) {

			/*
			 * Update lexicographically minimal sets and move on
			 */
			updateLexMinimalSubsets( zpIter.next(), lrData);
		}
		/*
		 * Cache the lexicographically minimal sets
		 */
		m_lastMinSets = lrData.getLexMinimalSets();
		m_teleMap.put( TELEMETRY.LEX_MIN_MODELS_NUMBER.toString(), (double)lrData.getLexMinimalSets().size() );
		m_teleMap.put( TELEMETRY.LEX_MIN_MODELS_TIME.toString(), (double)(System.currentTimeMillis() - ts) );
		/*
		 * Now we are ready to compute the tight lexicographic entailment. We
		 * simply compute TLogEnt and minimize (resp. maximize) over all
		 * lexicographically minimal models (sincerely hoping that they aren't
		 * painfully many)
		 */
		return entailFromLexMinimalSets(lrData.getPTBbox(), evidence, conclusion);
	}

	/*
	 * Invoked before we start computing lexicographically minimal sets
	 */
	protected LexReasoningData beforeComputingLexMinimalSets(PTBox ptbox, Set<ConditionalConstraint> ccSet, ATermAppl evidence, ATermAppl conclusion) {

		LexReasoningData lrData = createLexReasoningData();
		Set<Set<ConditionalConstraint>> lexMinSets = new HashSet<Set<ConditionalConstraint>>();
		Set<ConditionalConstraint> initialLexMinSet = new HashSet<ConditionalConstraint>();
		ZPartition zp = null;
		PTBox ptClone = null;
		/*
		 * We can't lexicographically derive anything from an inconsistent
		 * PTBox, so check that first. Note that if z-partition is already
		 * there, it won't be re-created
		 */
		lrData.setZPartition( zp = m_consChecker.decideGenericConsistency( ptbox ) );
		
		s_logger.info("z-partition: " + System.getProperty( "line.separator" ) + zp);
		
		/*
		 * Now we have z-partition.
		 * One more thing before we start doing the hard stuff:
		 * check that the evidence is not in conflict with the given
		 * knowledge (otherwise the inference makes no sense)
		 */
		ptClone = ptbox.clone(false);

		if (ccSet != null) {
			
			initialLexMinSet.addAll( ccSet );
		}
		
		if (!ATermUtils.TOP.isEqual(evidence)) {
			
			initialLexMinSet.add( CCUtils.conceptVerificationConstraint( evidence ) );
		}
		
		ptClone.setDefaultConstraints( initialLexMinSet );

		if( m_pSATsolver.isPTBoxSatisfiable( ptClone ) ) {

			lexMinSets.add( initialLexMinSet );
			lrData.setLexMinimalSets( lexMinSets );
			ptClone.removeDefaultConstraints( initialLexMinSet );
			lrData.setPTBbox( ptClone );
		} 
		
		return lrData;
	}
	
	/*
	 * Given the next subset in the z-partition, the method updates current 
	 * lexicographically minimal sets. It can be overriden by subclasses
	 * (for example, for optimization purposes)
	 */
	protected void updateLexMinimalSubsets(Set<ConditionalConstraint> pjSubset, LexReasoningData lrData) {
		
		Set<Set<ConditionalConstraint>> currentSets = lrData.getLexMinimalSets();
		List<ConditionalConstraint> pjList = new ArrayList<ConditionalConstraint>( pjSubset );
		
		s_logger.debug("Finding satisfiable subsets");
		/*
		 * We name variables as in the Lukasiewicz paper for clarity
		 */
		int m = 0, n = pjSubset.size(), l = n;

		while( m < n ) {
			/*
			 * The idea is to find the max (w.r.t. set inclusion) subset of
			 * P_j which is satisfiable given ptbox and lexMinSets. On the
			 * first step we merely try to estimate the cardinality
			 * of such subset (if it exists) - m
			 */
			if( existsSatisfiableSubset( pjList, currentSets, lrData.getPTBbox()/*ptbox*/, l ) ) {
				m = l;
				l = n;
			}
			else {
				n = l - 1;
				l = n;
			}
		}
		/*
		 * Now we narrowed the search to the subsets of size m, so the rest
		 * is to find all such satisfiable subsets. This is much-much
		 * quicker than iterating over the entire powerset of P_j
		 */
		s_logger.debug("Found satisfiable subsets of size " + m);
		
		if( m > 0 ) {
			
			updateLexMinimalSubsets( pjList, lrData, m );
		}
	} 
	
	
	/**
	 * Finds a subset of P_j of cardinality k which is satisfiable given the
	 * PTBox and at least one of the previously computed subsets of P. Halts as
	 * soon as one of such subsets is found
	 * 
	 * @return
	 */
	private boolean existsSatisfiableSubset(List<ConditionalConstraint> pj,	Set<Set<ConditionalConstraint>> currentSets, PTBox ptbox, int k) { 
		/*
		 * Iterate through all subsets of P_j of cardinality k. We currently
		 * don't have any heuristics to do so intelligently
		 */
		Set<ConditionalConstraint> pjSubset = null;
		long t = 0;

		boolean result = false;

		for(int i = 0; i < SetUtils.numOfSubsets(pj.size(), k ) && !result; i++ ) {

			pjSubset = SetUtils.subset( pj, k, i );

			for( Set<ConditionalConstraint> ccSet : currentSets) {				
				/*
				 * TODO Prevent the regeneration of the index set
				 */
				PTBox clonedPtbox = ptbox.clone();

				clonedPtbox.reset();
				clonedPtbox.addDefaultConstraints( pjSubset );
				clonedPtbox.addDefaultConstraints( ccSet );
				
				t = System.currentTimeMillis();

				if (result = m_pSATsolver.isPTBoxSatisfiable( clonedPtbox )) {
					
					s_logger.debug("PSAT for " + clonedPtbox.getDefaultConstraints().size() + " solved in " + (System.currentTimeMillis() - t) + " ms");
					s_logger.debug("Checked " + (i+1) + " " + k + "-subsets before break");
					break;
					
				} else {
					
					s_logger.debug("PSAT for " + clonedPtbox.getDefaultConstraints().size() + " solved in " + (System.currentTimeMillis() - t) + " ms");
				}
			}
		}

		if (! result) {
			s_logger.debug("Checked " + SetUtils.numOfSubsets( pj.size(), k ) + " " + k + "-subsets without break");
		}
		
		return result;
	}

	/**
	 * 
	 */
	private void updateLexMinimalSubsets(List<ConditionalConstraint> pj, LexReasoningData lrData, int m) {	
	
		Set<Set<ConditionalConstraint>> currentSets = lrData.getLexMinimalSets();
		Set<ConditionalConstraint> pjSubset = null;
		// Can we estimate size to reduce the number of mallocs?
		Set<Set<ConditionalConstraint>> result = new HashSet<Set<ConditionalConstraint>>();
		
		if (m == pj.size() && 1 == currentSets.size()) {
		/*
		 * Trivial case - just need to find the whole subset (assuming that
		 * it is satisfiable given )
		 */
			Set<ConditionalConstraint> lexSet = currentSets.iterator().next();
			
			if (null != lexSet) {
				lexSet.addAll( pj );
			} else {
				lexSet = new HashSet<ConditionalConstraint>(pj);
			}
			
			result.add( lexSet );
			lrData.setLexMinimalSets( result );
			
			return;
		}
		/*
		 * Hard stuff - find those subsets of size m that are satisfiable given
		 * _some_ of the current lexicographically minimal sets
		 */
		for( int i = 0; i < SetUtils.numOfSubsets( pj.size(), m ); i++ ) {

			pjSubset = SetUtils.subset( pj, m, i );

			for( Set<ConditionalConstraint> lexSubset : currentSets) {

				/*
				 * It might be faster not to clone every time but simply remove
				 * constraints after checking SAT (for hash sets it should work
				 * quick enough)
				 */
				PTBox clonedPtbox = lrData.getPTBbox().clone();
				/*
				 * TODO Prevent the regeneration of the index set
				 */
				clonedPtbox.reset();
				clonedPtbox.addDefaultConstraints( pjSubset );
				clonedPtbox.addDefaultConstraints( lexSubset );

				if( m_pSATsolver.isPTBoxSatisfiable( clonedPtbox ) ) {

					result.add( clonedPtbox.getDefaultConstraints() );
				}
			}
		}

		lrData.setLexMinimalSets( result );
	}

	
	/*
	 * Once lexicographically minimal sets are computed, we can entail the result
	 * from them
	 */
	protected ConditionalConstraint entailFromLexMinimalSets(PTBox ptbox, ATermAppl evidence, ATermAppl conclusion) {
		
		double lower = 1, upper = 0;
		long ts = System.currentTimeMillis();
		boolean lMinReached = false;
		boolean uMaxReached = false;
		int cnt = 1;
		
		s_logger.info( "\n+++ " + m_lastMinSets.size() + " lex minimal models computed, starting TLogEnt +++ \n" );
		
		for( Set<ConditionalConstraint> lexSet : m_lastMinSets) {			

			PTBox clean = ptbox.clone(false);
			
			s_logger.info( "Processing " + (cnt++) + " lex min subset out of " + m_lastMinSets.size());
			
			clean.reset();
			clean.addDefaultConstraints( lexSet );
			
			if (!uMaxReached) {
				
				upper = Math.max( m_pSATsolver.computeUpperProbability( clean, conclusion ), upper);
				uMaxReached = NumberUtils.equal( upper, 1.0 );
			}
			if (!lMinReached) {
				
				lower = Math.min( m_pSATsolver.computeLowerProbability( clean, conclusion ), lower);
				lMinReached = NumberUtils.equal( lower, 0.0 );
			}
			
			s_logger.info( "Upper: " + upper + "(" + uMaxReached + "), lower: " + lower + "(" + lMinReached + ")" );
			
			if (lMinReached && uMaxReached) break;
		}

		m_teleMap.put( TELEMETRY.TLOGENT_TIME.toString(), (double)(System.currentTimeMillis() - ts) );		
		
		return new ConditionalConstraint( evidence, conclusion, lower, upper );
	}

	public Set<Set<ConditionalConstraint>> computeMinUnsatisfiableSubsets(PTBox ptbox) {
		
		CCSetAnalyzer analyzer = new CCSetAnalyzerImpl2();
		
		return analyzer.getMinimalUnsatSubsets( null, ptbox );
	}

	public Set<Set<ConditionalConstraint>> computeMinIncoherentSubsets(PTBox ptbox) {
		//TODO Implement
		throw new NotImplementedException();		
	}
	
	/*
	 * Asserts non-zero probability for all evidence classes
	 */
	protected Set<ConditionalConstraint> getCoherenceConstraints(PTBox ptbox, double threshold) {
		
		Set<ATermAppl> evidences = new HashSet<ATermAppl>();
		Set<ConditionalConstraint> coherenceCC = new HashSet<ConditionalConstraint>();
		
		for (ConditionalConstraint cc : ptbox.getDefaultConstraints()) evidences.add( cc.getEvidence() );
		
		evidences.remove( ATermUtils.TOP );
		
		for (ATermAppl evidence : evidences) {
			
			coherenceCC.add( new ConditionalConstraint(ATermUtils.TOP, evidence, threshold, 1d) );
		}
		
		return coherenceCC;
	}

	protected LexReasoningData createLexReasoningData() {
		
		return new LexReasoningData();
	}
	
	protected boolean checkConcept(ProbKnowledgeBase pkb, ATermAppl concept) {
		
		if (!pkb.getPTBox().getClassicalKnowledgeBase().isClass( concept )) {
			
			s_logger.error( "Undefined concept " + concept );
			
			return false;
			
		} else return true;
	}
	
	protected boolean checkIndividual(ProbKnowledgeBase pkb, ATermAppl individual) {
		
		if (!pkb.getPABox().getProbabilisticIndividuals().contains( individual )) {
			
			s_logger.error( "Undefined probabilistic individual " + individual );
			
			return false;
			
		} else return true;
	}
	
	class EntailmentRequest {
		
		ProbKnowledgeBase m_pkb;
		ConditionalConstraint m_cc;
		ATermAppl m_individual;
		
		EntailmentRequest(ProbKnowledgeBase pkb, ConditionalConstraint cc, ATermAppl individual) {
			
			m_pkb = pkb;
			m_cc = cc;
			m_individual = individual;
		}
	}
	
	/*
	 * Contains data accumulated during lexicographic entailment
	 */
	protected class LexReasoningData {
		
		private ZPartition m_zpartition;
		private PTBox m_ptbox;
		private Set<Set<ConditionalConstraint>> m_lexMinimalSets;
		
		public ZPartition getZPartition() {
			
			return m_zpartition;
		}
		public void setZPartition(ZPartition m_zpartition) {
			
			this.m_zpartition = m_zpartition;
		}
		public PTBox getPTBbox() {
			
			return m_ptbox;
		}
		public void setPTBbox(PTBox m_ptbox) {
			
			this.m_ptbox = m_ptbox;
		}
		public Set<Set<ConditionalConstraint>> getLexMinimalSets() {
			
			return m_lexMinimalSets;
		}
		public void setLexMinimalSets(Set<Set<ConditionalConstraint>> minimalSets) {
			
			m_lexMinimalSets = minimalSets;
		}
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

		if (m_teleMap.containsKey( measure )) {
			
			return m_teleMap.get( measure ).toString();
			
		} else {
			
			return m_pSATsolver.getMeasure( measure );
		}
	}

	@Override
	public Collection<String> getMeasureNames() {

		Collection<String> measures = new HashSet<String>(m_pSATsolver.getMeasureNames());
		
		measures.addAll( m_teleMap.keySet() );
		
		return measures;
	}

	@Override
	public void resetMeasure(String measure) {

		m_pSATsolver.resetMeasure( measure );
		
		if (m_teleMap.containsKey( measure )) m_teleMap.put( measure, 0d ); 
	}

	@Override
	public void resetMeasures() {

		m_pSATsolver.resetMeasures();
		initMeasures();
	}
	
	public void initMeasures() {

		m_teleMap.put( TELEMETRY.LEX_MIN_MODELS_NUMBER.toString(), 0d );
		m_teleMap.put( TELEMETRY.LEX_MIN_MODELS_TIME.toString(), 0d );
		m_teleMap.put( TELEMETRY.TLOGENT_TIME.toString(), 0d );		
	}	

	@Override
	public boolean isMeasureSupported(String measure) {

		return m_pSATsolver.isMeasureSupported( measure ) || m_teleMap.containsKey( measure );
	}
}
