/**
 * 
 */
package uk.ac.manchester.cs.pronto.exceptions;


/**
 * <p>Title: QueryProcessingException</p>
 * 
 * <p>Description: 
 *  This exception is thrown when something bad happens during processing of a
 *  probabilistic task
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class QueryProcessingException extends TaskProcessingException {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5129515279103813125L;

	public QueryProcessingException(Throwable e) {
		super( e );
	}
	
	public QueryProcessingException(Throwable e, String str) {
		
		super( e, str );
	}
	
}
