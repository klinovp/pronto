/**
 * 
 */
package uk.ac.manchester.cs.pronto.index;


import java.util.HashSet;
import java.util.Set;

import org.mindswap.pellet.utils.ATermUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * 4 Jun 2010
 */
public class ConceptType implements IndexTerm {

	private Set<OWLClass> m_posLiterals = new HashSet<OWLClass>();
	private Set<OWLClassExpression> m_negLiterals = new HashSet<OWLClassExpression>();
	private OWLObjectIntersectionOf m_conjExpr = null;
	private boolean m_changed = false;
	private int m_index = -1;
	private final OWLDataFactory m_factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
	
	public boolean addPositive(OWLClass literal) {
		
		m_changed |= m_posLiterals.add( literal );
		
		return m_changed;
	}
	
	public boolean addNegative(OWLClass literal) {
		
		m_changed |= m_negLiterals.add( literal.getObjectComplementOf() );
		
		return m_changed;
	}
	
	public void removePositive(OWLClass literal) {
		
		m_changed |= m_posLiterals.remove( literal );
	}
	
	public void removeNegative(OWLClass literal) {
		
		m_changed |= m_negLiterals.remove( literal.getObjectComplementOf() );
	}	
	
	public boolean containsPositive(OWLClass atom) {
		
		return m_posLiterals.contains( atom );
	}
	
	public boolean containsNegative(OWLClass atom) {
		
		return m_negLiterals.contains( atom.getObjectComplementOf() );
	}	
	
	public OWLObjectIntersectionOf getConjunctiveExpr() {
		
		if (m_changed) refresh();
		
		return m_conjExpr;
	}
	
	public void setConjunctiveExpr(OWLObjectIntersectionOf expr) {

		m_conjExpr = expr;
		m_changed = false;
	}	
	
	public Set<OWLClass> getPositiveLiterals() {
		
		return m_posLiterals;
	}
	
	public Set<OWLClassExpression> getNegativeLiterals() {
		
		return m_negLiterals;
	}	
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof ConceptType) {
			
			return ((ConceptType)obj).getPositiveLiterals().equals( getPositiveLiterals() );
			
		} else {
			
			return false;
		}
	}

	@Override
	public int hashCode() {
		
		return m_posLiterals.hashCode();
	}
	
	@Override
	public String toString() {
		
		return getPositiveLiterals().toString();
	}

	private void refresh() {
		
		Set<OWLClassExpression> conjuncts = new HashSet<OWLClassExpression>(m_posLiterals);
		
		conjuncts.addAll( m_negLiterals );
		
		if (conjuncts.isEmpty()) conjuncts.add( m_factory.getOWLThing() );
		
		m_conjExpr = m_factory.getOWLObjectIntersectionOf( conjuncts );
		m_changed = false;
	}
	
	public ConceptType clone() {
		
		ConceptType clon = new ConceptType();
		
		clon.m_posLiterals.addAll( m_posLiterals );
		clon.m_negLiterals.addAll( m_negLiterals );
		
		return clon;
	}

	@Override
	public ATermAppl getTerm() {
		// FIXME GET RID OF ATERMS!!!
		throw new RuntimeException("Not supported");
	}
	
	@Override
	public void setTerm(ATermAppl term) {
		// FIXME GET RID OF ATERMS!!!
		throw new RuntimeException("Not supported");
	}	

	@Override
	public int getIndex() {

		return m_index;
	}
	
	@Override
	public void setIndex(int index) {
		
		m_index = index;
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
			
			if (m_negLiterals.contains( atermToOWLClass(term.getArgument( 0 )).getObjectComplementOf() )) {
				
				return true;
			}
			
		}  else	if (m_posLiterals.contains( atermToOWLClass(term) ) || term.equals( ATermUtils.TOP )) {
				
				result = true;
		}
	
		return result;
	}
	
	private OWLClass atermToOWLClass(ATerm term) {
		
		return m_factory.getOWLClass( IRI.create(term.toString() ));
	}
}
