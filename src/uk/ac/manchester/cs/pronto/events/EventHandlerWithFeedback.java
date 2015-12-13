/**
 * 
 */
package uk.ac.manchester.cs.pronto.events;

import uk.ac.manchester.cs.pronto.exceptions.EventHandlingException;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface EventHandlerWithFeedback<T> extends ReasoningEventHandler {

	public T handleEventWithFeedback(ReasoningEvent event) throws EventHandlingException;
}
