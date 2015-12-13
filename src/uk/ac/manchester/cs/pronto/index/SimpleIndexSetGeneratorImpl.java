package uk.ac.manchester.cs.pronto.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.cache.IndexCache;

/**
 * <p>Title: SimpleIndexSetGeneratorImpl</p>
 * 
 * <p>Description: 
 *  Straightforward implementation - generates satisfiable index terms from the
 *  set of conditional constraints
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class SimpleIndexSetGeneratorImpl implements IndexSetGenerator {

	private IndexCache m_cache;
	private int m_limit = -1;
	
	public void setTermNumber(int limit) {
		
		m_limit = limit;
	}
	
	public void useCache(IndexCache cache) {
		
		m_cache = cache;
	}
	
	public IndexCache getCache() {
		
		return m_cache;
	}
	
	public IndexSet generate(PTBox ptbox) {

		Set<ConjunctiveIndexTerm> termSet = null;
		SimpleIndexSetImpl indexSet = new SimpleIndexSetImpl();
		/*
		 * Now we will iterate through the collection and traverse a depth-first
		 * tree in order to create LP variables. Each node represents the
		 * decision how we map the constraint (D|C)[l,u] to the set {(D and C),
		 * (not D and C), (not C} See the Lukasiewicz  P-SHOIN paper for the
		 * definition of the index set
		 */
		if( null == ptbox.getDefaultConstraints() ) {

			termSet = Collections.singleton( new ConjunctiveIndexTerm() );
			
		} else {
			
			Set<ATermAppl> probSig = getProbabilisticSignature(ptbox.getDefaultConstraints());
			UnsafeConjunctiveIndexTerm indexTerm = new UnsafeConjunctiveIndexTerm();
			/*
			 * We proceed recursively in depth-first manner. Each level = index
			 * of the conditional. We prune branches when elements of the index
			 * set are inconsistent with the given TBox
			 */
			termSet = new HashSet<ConjunctiveIndexTerm>( ptbox.getDefaultConstraints().size() );
			
			terms( 	indexTerm,
					ptbox.getClassicalKnowledgeBase(),
					new ArrayList<ATermAppl>(probSig),
					0,
					termSet,
					new Random(),
					false);
		}

		indexSet.setConjunctiveTerms( termSet );
		//FIXME remove		
		System.out.println("Index set generated!");

		return indexSet;
	}


	/*
	 * Generates index terms in a depth-first manner pruning the obviously
	 * unsatisfiable branches
	 */
	protected boolean terms(	UnsafeConjunctiveIndexTerm indexTerm,
								KnowledgeBase kb,
								List<ATermAppl> atomList,
								int level,
								Set<ConjunctiveIndexTerm> termSet,
								Random rnd,
								boolean satNeeded) {
		
		ATermAppl currExpr = ATermUtils.normalize(indexTerm.getTerm());
		
		if (m_limit >= 0 && termSet.size() >= m_limit) {
			//Stop here, we've got enough terms
			return true;
		}
		
		if(!satNeeded || kb.isSatisfiable( currExpr ))  {
			/*
			 * Move on
			 */
			if( level < atomList.size() ) {

				if (rnd.nextBoolean()) {
					
					boolean posSAT = positiveBranch(termSet, atomList, kb, indexTerm, level, rnd, true);
					
					negativeBranch(termSet, atomList, kb, indexTerm, level, rnd, posSAT);
					
				} else {

					boolean negSAT = negativeBranch(termSet, atomList, kb, indexTerm, level, rnd, true);
					
					positiveBranch(termSet, atomList, kb, indexTerm, level, rnd, negSAT);
				}
			}
			else {
				
				termSet.add( indexTerm.toConjunctiveIndexTerm( false ) );
				//FIXME remove
				System.out.println(termSet.size());
			}
			
			return true;
			
		} else {
			
			return false;
		}
	}
	
	private boolean positiveBranch(	Set<ConjunctiveIndexTerm> termSet,
									List<ATermAppl> atomList,
									KnowledgeBase kb,
									UnsafeConjunctiveIndexTerm indexTerm,
									int level,
									Random rnd,
									boolean satNeeded) {
		
		boolean result = false;
		ATermAppl atom = atomList.get( level );
		//Conjunction with a positive literal
		if (!indexTerm.getNegativeConjuncts().contains( atom )) {

			ATermAppl exprCopy = indexTerm.getTerm();
			
			indexTerm.positiveConjunct( atom );
			result = terms(	indexTerm, kb, atomList, level + 1,	termSet, rnd, satNeeded);
			
			indexTerm.removePositive( atom );
			indexTerm.setTerm( exprCopy );
		}

		return result;
	}

	private boolean negativeBranch(Set<ConjunctiveIndexTerm> termSet,
									List<ATermAppl> atomList,
									KnowledgeBase kb,
									UnsafeConjunctiveIndexTerm indexTerm,
									int level,
									Random rnd,
									boolean satNeeded) {
		
		boolean result = false;
		ATermAppl atom = atomList.get( level );
		//Conjunction with a negative literal
		if (!indexTerm.getPositiveConjuncts().contains( atom )) {
			
			ATermAppl exprCopy = indexTerm.getTerm();
			
			indexTerm.negativeConjunct( atom );
			result = terms(	indexTerm, kb, atomList, level + 1,	termSet, rnd, satNeeded);
			
			indexTerm.removeNegative( atom );
			indexTerm.setTerm( exprCopy );
		}
		
		return result;
	}

	private Set<ATermAppl> getProbabilisticSignature(Set<ConditionalConstraint> ccSet) {
		
		Set<ATermAppl> sig = new HashSet<ATermAppl>(ccSet.size() * 2);
		
		for (ConditionalConstraint cc : ccSet) {
			
			sig.add( cc.getEvidence() );
			sig.add( cc.getConclusion() );
		}
		
		return sig;
	}	
}
