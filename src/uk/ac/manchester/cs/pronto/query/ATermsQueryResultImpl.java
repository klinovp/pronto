package uk.ac.manchester.cs.pronto.query;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.ProbabilisticExplanation;

/**
 * <p>Title: ATermsQueryResultImpl</p>
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
public class ATermsQueryResultImpl implements PQueryResult {

	private Set<ATermAppl> m_aterms; 
	
	public ATermsQueryResultImpl(Set<ATermAppl> aterms) {
		
		m_aterms = aterms;
	}
	
	public void serialize(Writer writer) throws IOException {

		writer.write(toString());
	}
	
	public String toString() {
		
		String result = "";
		
		if (null != m_aterms) {

			for (Iterator<ATermAppl> iter = m_aterms.iterator(); iter.hasNext();) {
				result += iter.next().toString() + System.getProperty("line.separator");
			}
		}
		
		return result;
	}
	
	public ProbabilisticExplanation getExplanation() {
		
		return null;
	}
}
