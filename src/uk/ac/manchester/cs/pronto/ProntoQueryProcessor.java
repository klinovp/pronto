/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.Collection;

import org.mindswap.pellet.KnowledgeBase;

import uk.ac.manchester.cs.pronto.benchmark.Telemetry;
import uk.ac.manchester.cs.pronto.exceptions.ProbabilisticInconsistencyException;
import uk.ac.manchester.cs.pronto.exceptions.QueryProcessingException;
import uk.ac.manchester.cs.pronto.query.BooleanQueryResultImpl;
import uk.ac.manchester.cs.pronto.query.ConstraintSetsQueryResultImpl;
import uk.ac.manchester.cs.pronto.query.EntailmentQueryImpl;
import uk.ac.manchester.cs.pronto.query.PQuery;
import uk.ac.manchester.cs.pronto.query.PQueryResult;
import uk.ac.manchester.cs.pronto.query.SingleConstraintQueryResultImpl;

/**
 * @author Pavel Klinov
 */
public class ProntoQueryProcessor implements Telemetry {

	protected ProntoReasoner	m_reasoner;
	protected ProbKnowledgeBase	m_pkb;
	protected boolean m_explain = false;

	public ProntoQueryProcessor(ProntoReasoner reasoner) {
		
		m_reasoner = reasoner;
	}

	public void setExplanationsRequired(boolean required) {
		
		m_explain = required;
	}
	
	public ProntoReasoner getReasoner() {
		return m_reasoner;
	}

	public ProbKnowledgeBase getKnowledgeBase() {
		return m_pkb;
	}

	public void setProbKnowledgeBase(ProbKnowledgeBase pkb) {
		
		m_pkb = pkb;
	}

	/*
	 * FIXME Rewrite this horrible IF statement
	 */
	public PQueryResult process(PQuery query) throws QueryProcessingException {

		try {

			if( PQuery.ENTAILMENT_QUERY == query.getQueryType() ) {

				EntailmentQueryImpl eQuery = (EntailmentQueryImpl) query;
				/*
				 * Need to differentiate between generic and concrete queries
				 */
				KnowledgeBase kb = m_pkb.getPTBox().getClassicalKnowledgeBase();
				SingleConstraintQueryResultImpl result = null;
				
				if( !kb.isClass( eQuery.getConclusion() ) ) {

					throw new QueryProcessingException( null,
									"Unrecognized conclusion parameter in the entailment query" );
				}

				if( kb.isClass( eQuery.getEvidence() ) ) {

					result = new SingleConstraintQueryResultImpl(
															null,
															m_reasoner.subsumptionEntailment(
																	m_pkb,
																	eQuery.getEvidence(),
																	eQuery.getConclusion() ) );

				} else if( null != m_pkb.getPTBoxForIndividual( eQuery.getEvidence() ) ) {

					result = new SingleConstraintQueryResultImpl(
															eQuery.getEvidence(),
															m_reasoner.membershipEntailment(
																	m_pkb,
																	eQuery.getEvidence(),
																	eQuery.getConclusion() ) );
					
				} else {

					throw new QueryProcessingException( null, "Unrecognized evidence parameter in the entailment query: " + eQuery.getEvidence() );
				}
				
				return result;
			}
			else if( PQuery.SATISFIABILITY_QUERY == query.getQueryType() ) {

				return new BooleanQueryResultImpl(m_reasoner.isSatisfiable(m_pkb.getPTBox() ) );

			} else if( PQuery.CONSISTENCY_QUERY == query.getQueryType() ) {
				/*
				 * Strictly speaking the result is not boolean - we distinguish
				 * between g-inconsistent and just inconsistent ontologies. To
				 * be fixed.
				 */
				return new BooleanQueryResultImpl(
							m_reasoner.isConsistent( m_pkb ) == ConsistencyChecker.PKB_CONSISTENT );

			} else if( PQuery.UNSAT_SUBSETS_QUERY == query.getQueryType() ) {
				
				return new ConstraintSetsQueryResultImpl(
									m_reasoner.computeMinUnsatisfiableSubsets( m_pkb.getPTBox() ));
				
			}  else if( PQuery.INCOHERENT_SUBSETS_QUERY == query.getQueryType() ) {
				
				return new ConstraintSetsQueryResultImpl(
									m_reasoner.computeMinIncoherentSubsets( m_pkb.getPTBox() ));
			}

		} catch( ProbabilisticInconsistencyException pie ) {

			throw new QueryProcessingException( pie );
		} 

		throw new QueryProcessingException( null, "Query type " + query.getQueryType() + " is not (yet) supported" );
	}

	@Override
	public String getMeasure(String measure) {

		return m_reasoner.getMeasure( measure );
	}

	@Override
	public Collection<String> getMeasureNames() {

		return m_reasoner.getMeasureNames();
	}

	@Override
	public boolean isMeasureSupported(String measure) {

		return m_reasoner.isMeasureSupported( measure );
	}

	@Override
	public void resetMeasure(String measure) {

		m_reasoner.resetMeasure( measure );
	}

	@Override
	public void resetMeasures() {

		m_reasoner.resetMeasures();
	}
}
