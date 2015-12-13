/**
 * 
 */
package uk.ac.manchester.cs.pronto.query;

import java.io.IOException;
import java.io.Writer;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.ProbabilisticExplanation;

/**
 * <p>Title: SingleConstraintQueryResultImpl</p>
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
public class SingleConstraintQueryResultImpl implements PQueryResult {

	private ConditionalConstraint m_result;
	private ATermAppl m_individ;
	private ProbabilisticExplanation m_explanation;
	
	public SingleConstraintQueryResultImpl(ATermAppl object, ConditionalConstraint cc) {
		
		m_individ = object;
		m_result = cc;
	}
	
	public void setExplanation(ProbabilisticExplanation explanation) {
		m_explanation = explanation;
	}
	
	public void serialize(Writer writer) throws IOException {

		writer.write(toString());
	}

	public ConditionalConstraint getResult() {
		return m_result;
	}
	
	public String toString() {
		
		String result = "";
		
		if (null != m_individ) {
			
			result = m_individ.toString() + ":" + m_result.getConclusion() + m_result.getUncertaintyInterval();
			
		} else {
			result = null == m_result ? "Empty" : m_result.toString();
		}
			
		return result;  
	}
	
	public ProbabilisticExplanation getExplanation() {
		
		return m_explanation;
	}
	
}
