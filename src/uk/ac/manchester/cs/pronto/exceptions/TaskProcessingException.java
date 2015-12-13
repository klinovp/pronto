/**
 * 
 */
package uk.ac.manchester.cs.pronto.exceptions;

import org.apache.commons.lang.exception.NestableException;

/**
 * <p>Title: QueryProcessingException</p>
 * 
 * <p>Description: 
 *  This exception is thrown when something bad happens during processing of a
 *  probabilistic query
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class TaskProcessingException extends NestableException {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -4452350664290542687L;
	public String m_msg;
	
	public TaskProcessingException(Throwable e) {
		super(e);
	}
	
	public TaskProcessingException(Throwable e, String message) {
		
		super(e);
		m_msg = message;
	}
	
	public String getMessage() {
		return m_msg;
	}
	
}
