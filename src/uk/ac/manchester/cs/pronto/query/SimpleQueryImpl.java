/**
 * 
 */
package uk.ac.manchester.cs.pronto.query;

import java.util.List;

import uk.ac.manchester.cs.pronto.exceptions.QueryProcessingException;

/**
 * <p>Title: SimpleQueryImpl</p>
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
public class SimpleQueryImpl implements PQuery {

	private int m_type;
	
	public SimpleQueryImpl(int type) {
		
		m_type = type;
	}
	
	public int getQueryType() {

		return m_type;
	}

	public List<?> getQueryParameters() {

		return null;
	}

	public void deserialize(String params, String separator) throws QueryProcessingException {}

	public String toString() {
		
		if(SATISFIABILITY_QUERY == getQueryType() ) {
			return "psat";
		}
		else if(CONSISTENCY_QUERY == getQueryType() ) {
			return "consistency";

		}
		else if(UNSAT_SUBSETS_QUERY == getQueryType() ) {
			return "unsat_subsets";
		}
		else {
			return "Unknown query type";
		}
	}
	
}
