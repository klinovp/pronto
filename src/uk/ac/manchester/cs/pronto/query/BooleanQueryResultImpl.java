/**
 * 
 */
package uk.ac.manchester.cs.pronto.query;

import java.io.IOException;
import java.io.Writer;

import uk.ac.manchester.cs.pronto.ProbabilisticExplanation;

/**
 * <p>Title: BooleanQueryResultImpl</p>
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
public class BooleanQueryResultImpl implements PQueryResult {

	private boolean m_result = false;
	
	public BooleanQueryResultImpl(boolean result) {
		m_result = result;
	}
	
	public void serialize(Writer writer) throws IOException {
		writer.write(toString());
	}
	
	public String toString() {
		return String.valueOf(m_result);
	}

	public ProbabilisticExplanation getExplanation() {
		
		return null;
	}
	
}
