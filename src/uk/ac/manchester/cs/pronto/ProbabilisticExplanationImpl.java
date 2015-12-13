package uk.ac.manchester.cs.pronto;

import java.util.Set;

import aterm.ATermAppl;

/**
 * <p>Title: ProbabilisticExplanationImpl</p>
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
public class ProbabilisticExplanationImpl implements ProbabilisticExplanation {

	private PTBox m_ptbox;
	private ConditionalConstraint m_cc;
	private ATermAppl m_individual;
	private Set<Set<ConditionalConstraint>> m_lowerExplSet;
	private Set<Set<ConditionalConstraint>> m_upperExplSet;
	
	protected ProbabilisticExplanationImpl(PTBox ptbox, ConditionalConstraint cc,
			Set<Set<ConditionalConstraint>> lowerExplSet, Set<Set<ConditionalConstraint>> upperExplSet) {

		m_ptbox = ptbox;
		m_cc = cc;
		m_lowerExplSet = lowerExplSet;
		m_upperExplSet = upperExplSet;
	}
	
	public Set<ATermAppl> getClassicalExplanationSet() {
		/*
		 * Normally the method should return the minimal set of ontology axioms
		 * that support the inference when combined with probabilistic axioms.
		 * This is not yet implemented
		 */
		throw new UnsupportedOperationException();
	}

	public PTBox getPTBox() {

		return m_ptbox;
	}

	public Set<Set<ConditionalConstraint>> getLowerBoundExplanationSet() {

		return m_lowerExplSet;
	}

	public Set<Set<ConditionalConstraint>> getUpperBoundExplanationSet() {

		return m_upperExplSet;
	}
	
	public ConditionalConstraint getEntailment() {
		
		return m_cc;
	}
	
	protected void setLowerBoundExplanationSet(Set<Set<ConditionalConstraint>> subset) {
		
		m_lowerExplSet = subset;
	}

	protected void setUpperBoundExplanationSet(Set<Set<ConditionalConstraint>> subset) {
		
		m_upperExplSet = subset;
	}
	
	public String toString() {
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append( "Explaining the " + ((null == m_individual)
			? "generic"
			: "concrete") + " constraint " + m_cc.toString() + ":" );
		buffer.append( System.getProperty("line.separator") );
		
		if (null != m_lowerExplSet && m_lowerExplSet.size() > 0) {
			buffer.append( "Lower bound is because of: " + System.getProperty("line.separator"));
			buffer.append(m_lowerExplSet);
		}

		if (null != m_upperExplSet && m_upperExplSet.size() > 0) {
			buffer.append( System.getProperty("line.separator") );
			buffer.append( "Upper bound is because of: " + System.getProperty("line.separator"));
			buffer.append(m_upperExplSet);
		}
		
		return buffer.toString();
	}
}
