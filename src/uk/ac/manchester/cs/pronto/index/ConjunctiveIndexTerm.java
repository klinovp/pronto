/**
 * 
 */
package uk.ac.manchester.cs.pronto.index;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Explicitly stores positive and negative conjuncts to avoid dumb SAT tests.
 * Note that conjuncts need not be atomic.
 */
public class ConjunctiveIndexTerm implements IndexTerm {

	private Set<ATerm> m_posConjuncts = new HashSet<ATerm>();
	private Set<ATerm> m_negConjuncts = new HashSet<ATerm>();
	//Current conjunctive expression (cached for the sake of performance)
	private ATermAppl m_conjExpr = ATermUtils.TOP;
	private int m_index = -1;

	public ConjunctiveIndexTerm() {}
	
	public ConjunctiveIndexTerm(Collection<ATerm> posConjuncts, Collection<ATerm> negConjuncts, int index) {
		
		m_posConjuncts.addAll( posConjuncts );
		m_negConjuncts.addAll( negConjuncts );
		m_index = index;
		update();
	}
	
	public ConjunctiveIndexTerm(ATermAppl aterm) {
		
		conjunct(aterm);
	}
	
	public ConjunctiveIndexTerm(ConjunctiveIndexTerm term) {

		m_posConjuncts.addAll( term.getPositiveConjuncts() );
		m_negConjuncts.addAll( term.getNegativeConjuncts() );
		update();
	}	
	/*
	 * Should only be used by UnsafeConjunctiveIndexTerm for unsafe conversion
	 */
	protected ConjunctiveIndexTerm(	Set<ATerm> posConjuncts,
									Set<ATerm> negConjuncts,
									ATermAppl expr,
									int index) {
		
		m_posConjuncts = posConjuncts;
		m_negConjuncts = negConjuncts;
		m_conjExpr = expr;
		setIndex( index );
	} 
	/*
	 * Updates the cached conjunctive expression
	 */
	private void update() {

		m_conjExpr = ATermUtils.TOP;
		
		if (m_posConjuncts != null){
			
			for (ATerm term : m_posConjuncts) {
				
				m_conjExpr = ATermUtils.makeAnd( m_conjExpr, term );
			}
		}
		
		if (m_negConjuncts != null){
			
			for (ATerm term : m_negConjuncts) {
				
				m_conjExpr = ATermUtils.makeAnd( m_conjExpr, ATermUtils.makeNot( term ) );
			}
		}		
	}
	
	public void conjunct(ATermAppl term) {
		
		term = ATermUtils.normalize( term );
		
		if (ATermUtils.isNot( term )) {
			
			negativeConjunct(term );
			
		} else if (ATermUtils.isAnd( term )) {
			//Conjunct with a list of conjuncts
			for (ATermList list = (ATermList) term.getArgument( 0 );
														!list.isEmpty(); list = list.getNext()) {
				//Add primitives in recursive manner
				conjunct( (ATermAppl) list.getFirst() );
			}
			
		} else {
			
			positiveConjunct(term);
		}
	}
	
	protected void positiveConjunct(ATermAppl term) {
		
		m_posConjuncts.add(term);
		m_conjExpr = ATermUtils.makeAnd( m_conjExpr, term );
	}

	protected void negativeConjunct(ATermAppl term) {
		
		if (ATermUtils.isNot( term )) {
		
			m_negConjuncts.add(term.getArgument( 0 ));
			m_conjExpr = ATermUtils.makeAnd( m_conjExpr, term );
		}
	}
	
	
	
	public void conjunct(Collection<ATermAppl> list) {
		
		for (ATermAppl conjunct : list) {
			
			conjunct(conjunct);
		}
	}
	

	@Override
	public ATermAppl getTerm() {
		
		//Simply return the cached value
		m_conjExpr = ATermUtils.normalize( m_conjExpr );
		
		return m_conjExpr;
	}

	@Override
	public void setTerm(ATermAppl term) {
		
		m_posConjuncts.clear();
		m_negConjuncts.clear();
		
		conjunct( term );
	}
	
	public Set<ATerm> getPositiveConjuncts() {
		
		return m_posConjuncts;
	}

	public Set<ATerm> getNegativeConjuncts() {
		
		return m_negConjuncts;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof ConjunctiveIndexTerm) {
			
			return ((ConjunctiveIndexTerm)obj).getTerm().isEqual( getTerm() );
			
		} else {
			
			return false;
		}
	}

	@Override
	public int hashCode() {
		
		return m_posConjuncts.hashCode();
	}
	
	public String toString() {
		
		return getTerm().toString();
	}
	
	/**
	 * It is important to pass normalized terms
	 */
	public boolean isStructurallySubsumedBy(ATermAppl term) {
		
		boolean result = false;
	
		if (ATermUtils.isAnd( term )) {
			//Recursively process each conjunct 
			ATermList list = (ATermList) term.getArgument( 0 );
			
			result = true;
			
			while( !list.isEmpty() && result) {
				
				ATermAppl arg = (ATermAppl) list.getFirst();
				
				result &= isStructurallySubsumedBy(arg);
				list = list.getNext();
			}
			
		} else if (ATermUtils.isNot( term )) {
			
			if (m_negConjuncts.contains( term.getArgument( 0 ) )) {
				
				return true;
			}
			
		}  else	if (m_posConjuncts.contains( term ) || term.equals( ATermUtils.TOP )) {
				
				result = true;
		}
	
		return result;
	}

	public int getIndex() {
		
		return m_index;
	}

	@Override
	public void setIndex(int index) {
		
		m_index = index;
	}
	
	@Override
	public IndexTerm clone() {
		
		return new ConjunctiveIndexTerm(m_posConjuncts, m_negConjuncts, m_index);
	}	
}
