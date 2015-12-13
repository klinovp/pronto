/**
 * 
 */
package uk.ac.manchester.cs.pronto.exceptions;

import org.apache.commons.lang.exception.NestableException;

/**
 * <p>Title: CPException</p>
 * 
 * <p>Description: 
 *  This exception is thrown when something bad happens during solving a
 *  constraint programming problem. It's aimed at being a wrapper around
 *  various specific exceptions  thrown by different CP solvers
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class CPException extends NestableException {

	public CPException(Throwable e) {
		super(e);
	}
	
	public CPException(String msg, Throwable e) {
		
		super(msg, e);
	}
	
}
