/**
 * 
 */
package uk.ac.manchester.cs.pronto.events;

import uk.ac.manchester.cs.pronto.exceptions.EventHandlingException;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Aug 30, 2009
 */
public interface ReasoningEventHandler {

	public void handleEvent(ReasoningEvent event) throws EventHandlingException;
}
