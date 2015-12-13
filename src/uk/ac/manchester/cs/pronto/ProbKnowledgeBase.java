/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.Set;

import org.mindswap.pellet.KnowledgeBase;
import org.semanticweb.owlapi.model.OWLOntology;

import aterm.ATermAppl;


/**
 * <p>Title: ProbKnowledgeBase</p>
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
public class ProbKnowledgeBase {

	protected PTBox m_ptbox;
	protected PABox m_pabox;
	protected boolean m_bPreprocessed = false;
	
	public PTBox getPTBox() {
		
		return m_ptbox;
	}
	
	public void setPTBox(PTBox ptbox) {
		
		m_ptbox = ptbox;
	}
	
	public PABox getPABox() {
		
		return m_pabox;
	}
	
	public void setPABox(PABox pabox) {
		
		m_pabox = pabox;
	}
	
	public PTBox getPTBoxForIndividual(ATermAppl individual) {
		
		Set<ConditionalConstraint> concreteCC = m_pabox.getConstraintsForIndividual(individual);
		PTBox ptbox = null;
		
		if (null != concreteCC) {
			
			ptbox = newPTBox(m_ptbox.getClassicalKnowledgeBase(), m_ptbox.getClassicalOntology(), concreteCC);
			
			ptbox.getSupplementalData().setCache( m_ptbox.getSupplementalData().getCache() );
		}
		
		return ptbox;		
	}
	
	protected PTBox newPTBox(KnowledgeBase kb, OWLOntology ontology, Set<ConditionalConstraint> ccSet) {
		
		return new PTBoxImpl(kb, ontology, ccSet);
	}
	
	
	public void preprocess() {
		
		if( !m_bPreprocessed ) {
			
			m_ptbox.preprocess();
		}
		
		m_bPreprocessed = true;
	}
	
	protected void addClass(ATermAppl newClass, ATermAppl expr) {
		
		KnowledgeBase kb = m_ptbox.getClassicalKnowledgeBase();
		
		if (null != expr) {
			
			kb.addClass( newClass );
			kb.addEquivalentClass( newClass, expr );
		}
	}
}
