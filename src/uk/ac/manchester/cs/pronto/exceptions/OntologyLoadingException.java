/**
 * 
 */
package uk.ac.manchester.cs.pronto.exceptions;

import org.apache.commons.lang.exception.NestableException;

/**
 * <p>Title: OntologyLoadingException</p>
 * 
 * <p>Description: 
 *  This exception is thrown when something bad happens during loading of a
 *  probabilistic knowledge base
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class OntologyLoadingException extends NestableException {

	public String m_msg;

	public OntologyLoadingException(Throwable e) {
		super(e);
	}
	
	public OntologyLoadingException(Throwable e, String message) {
		super(e);
		m_msg = message;
	}
	
	public String getMessage() {
		return m_msg;
	}
	
}
