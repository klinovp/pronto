/**
 * 
 */
package uk.ac.manchester.cs.pronto.query;

import java.util.List;

import uk.ac.manchester.cs.pronto.exceptions.QueryProcessingException;

/**
 * <p>Title: PQuery</p>
 * 
 * <p>Description: 
 *  Probabilistic query
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public interface PQuery {

	public static final int ENTAILMENT_QUERY = 1;
	public static final int CONSISTENCY_QUERY = 2;
	public static final int SATISFIABILITY_QUERY = 3;
	public static final int UNSAT_SUBSETS_QUERY = 4;
	public static final int INCOHERENT_SUBSETS_QUERY = 5;	
	
	public int getQueryType();
	public List<?> getQueryParameters();
	public void deserialize(String params, String separator) throws QueryProcessingException;	
}

