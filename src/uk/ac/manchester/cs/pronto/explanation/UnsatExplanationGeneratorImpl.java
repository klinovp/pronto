/**
 * 
 */
package uk.ac.manchester.cs.pronto.explanation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.alg.HittingSetsAlgorithm;
import uk.ac.manchester.cs.pronto.alg.HittingSetsAlgorithmImpl;

/**
 * Finds all justifications of inconsistency of a conjunctive class expression
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class UnsatExplanationGeneratorImpl implements UnsatExplanationGenerator {

	
	private Logger	m_logger = Logger.getLogger( UnsatExplanationGeneratorImpl.class );	
	protected long m_timeLimit = Long.MAX_VALUE;
	protected ContractionStrategy m_contrStrategy = new DivideAndConquerStrategyImpl();
	
	public void setTimeLimit(long limit) {
		
		m_timeLimit = limit;
	}
	
	/**
	 * Finds all minimal inconsistent sets of conjuncts
	 * 
	 * @param expression Conjunctive class expression
	 * @param kb TBox
	 * @return Minimal inconsistent sets of conjuncts (justifications)
	 */
	public Set<Set<ATermAppl>> computeExplanations(Set<ATermAppl> conjuncts, SATChecker checker) {
		
		if (!checker.isSatisfiable( conjuncts )) {
		
			long ts = System.currentTimeMillis();
			Set<Set<ATermAppl>> explSets = new HashSet<Set<ATermAppl>>();
			// All the work is done here
			ts = System.currentTimeMillis();

			computeAllExplanations( conjuncts,
									explSets,
									new HashSet<ATermAppl>(),
									new HittingSetsAlgorithmImpl<ATermAppl>(),
									checker,
									ts);

			return explSets;
			
		} else return null; 
	}
	
	
	private void computeAllExplanations(Set<ATermAppl> conjuncts,
										Set<Set<ATermAppl>> explSets,
										Set<ATermAppl> explanation,
										HittingSetsAlgorithm<ATermAppl> hsAlg,
										SATChecker checker,
										long startTime) {
		
		if ((System.currentTimeMillis() - startTime > m_timeLimit) && !explSets.isEmpty()) {
			//Oops, we're out of time!
			return;
		}
		/*
		 * Strategy: compute all minimal hitting sets, remove them from the list
		 * of conjuncts and find more explanation sets. Then repeat until no
		 * explanations are found
		 */
		boolean moreExplanations = false;
		List<ATermAppl> conjunctsCopy = new ArrayList<ATermAppl>(conjuncts);
		Set<Set<ATermAppl>> explHS = hsAlg.addConflictSet( explanation );
		//Set<Set<ATermAppl>> explHS = hsAlg.compute( explSets );
		
		if (explHS.isEmpty()) explHS.add( new HashSet<ATermAppl>(1) );//Stupid
		
		for (Set<ATermAppl> hs : explHS) {
			
			if (System.currentTimeMillis() - startTime > m_timeLimit) {
				
				m_logger.warn( "Hitting set size when we stop: " + explHS.size() );
				
				return;
			}
			
			//Remove from the initial list of conjuncts
			conjunctsCopy.removeAll( hs );
			
			if (null != (explanation = m_contrStrategy.prune( conjunctsCopy, checker ))) {
				
				/*s_logger.debug( "Justification number " + explSets.size() +
								" of length " + explanation.size() + 
								" found in " + (System.currentTimeMillis() - ts) + " ms");*/
				
				explSets.add( explanation );
				moreExplanations = true;
				break;
			}
			
			conjunctsCopy.addAll( hs );
		}
		//Repeat until no more explanations are found or time threshold is reached
		if (moreExplanations) {
			
			computeAllExplanations(conjuncts, explSets, explanation, hsAlg, checker, startTime);
		}
	}	
}
