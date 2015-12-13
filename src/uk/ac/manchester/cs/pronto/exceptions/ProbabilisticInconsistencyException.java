/**
 * 
 */
package uk.ac.manchester.cs.pronto.exceptions;

import org.mindswap.pellet.exceptions.InconsistentOntologyException;

/**
 * <p>Title: ProbabilisticInconsistencyException</p>
 * 
 * <p>Description: 
 *  Thrown if the probabilistic ontology is inconsistent, i.e. conditional constraints
 *  are in a irresolvable conflict
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class ProbabilisticInconsistencyException extends InconsistentOntologyException {

	public String m_msg;

	public ProbabilisticInconsistencyException() {}	
	
	public ProbabilisticInconsistencyException(String message) {

		m_msg = message;
	}
	
	public String getMessage() {
		return m_msg;
	}

}
