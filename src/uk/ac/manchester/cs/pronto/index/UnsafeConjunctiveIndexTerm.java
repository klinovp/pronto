/**
 * 
 */
package uk.ac.manchester.cs.pronto.index;

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
 * Same as ConjunctiveIndexTerm but it provides a direct access to its stored
 * conjunctive expression (which is unsafe because the calling code is now
 * responsible for synchronization between the cached expression and the lists
 * of conjuncts).
 * 
 * Should only be used as the last option to improve performance
 */
public class UnsafeConjunctiveIndexTerm implements IndexTerm {

	private Set<ATerm> m_posConjuncts = new HashSet<ATerm>();
	private Set<ATerm> m_negConjuncts = new HashSet<ATerm>();
	//Current conjunctive expression (cached for the sake of performance)
	private ATermAppl m_conjExpr = ATermUtils.TOP;
	private int m_index = -1;

	public UnsafeConjunctiveIndexTerm() {
		
		m_posConjuncts.add(ATermUtils.TOP);
		m_negConjuncts.add(ATermUtils.BOTTOM);
	}
	
	private UnsafeConjunctiveIndexTerm(Set<ATerm> pos, Set<ATerm> neg, int index) {
		
		m_posConjuncts = new HashSet<ATerm>(pos);
		m_negConjuncts = new HashSet<ATerm>(neg);
		m_index = index;
		update();
	}
	
	public boolean positiveConjunct(ATermAppl term) {
		
		boolean added = m_posConjuncts.add(term);
		
		if (added) {
			
			m_conjExpr = ATermUtils.makeAnd( m_conjExpr, term );
		}
		
		return added; 
	}

	public boolean negativeConjunct(ATermAppl term) {

		ATermAppl notTerm = ATermUtils.makeNot( term );
		boolean added = m_negConjuncts.add(term);
		
		if (added) {
		
			m_conjExpr = ATermUtils.makeAnd( m_conjExpr, notTerm );
		}
		
		return added;
	}
	
	public void removePositive(ATermAppl term) {
		
		m_posConjuncts.remove( term );
		//Can't really remove the last conjunct from an ATerm, so just clear
		//it and hope that the caller will update the expression
		m_conjExpr = ATermUtils.TOP;
	}
	
	public void removeNegative(ATermAppl term) {
		
		m_negConjuncts.remove( term );
		m_conjExpr = ATermUtils.TOP;
	}
	
	@Override
	public ATermAppl getTerm() {
		
		//Simply return the cached value w/o even normalizing it
		return m_conjExpr;
	}

	@Override
	public void setTerm(ATermAppl term) {
		
		m_conjExpr = term;
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
	
	public ConjunctiveIndexTerm toConjunctiveIndexTerm(boolean safe) {
		
		if (!safe) {
			
			return new ConjunctiveIndexTerm(new HashSet<ATerm>(m_posConjuncts),
											new HashSet<ATerm>(m_negConjuncts),
											ATermUtils.normalize( m_conjExpr ),
											getIndex());
		} else {
			
			return new ConjunctiveIndexTerm(new HashSet<ATerm>(m_posConjuncts),
											new HashSet<ATerm>(m_negConjuncts),
											getIndex());
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

	@Override
	public int getIndex() {
		
		return m_index;
	}

	@Override
	public void setIndex(int index) {
		
		m_index = index;
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
	
	@Override
	public IndexTerm clone() {
		
		return new UnsafeConjunctiveIndexTerm(m_posConjuncts, m_negConjuncts, m_index);
	}
}
