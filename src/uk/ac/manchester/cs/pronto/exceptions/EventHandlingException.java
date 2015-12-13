/**
 * 
 */
package uk.ac.manchester.cs.pronto.exceptions;

import org.apache.commons.lang.exception.NestableException;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Aug 30, 2009
 */
@SuppressWarnings("serial")
public class EventHandlingException extends NestableException {

	public String m_msg;
	
	public EventHandlingException(Throwable e) {
		
		super(e);
	}
	
	public EventHandlingException(Throwable e, String message) {
		
		super(e);
		m_msg = message;
	}
	
	public String getMessage() {
		
		return m_msg;
	}
}	