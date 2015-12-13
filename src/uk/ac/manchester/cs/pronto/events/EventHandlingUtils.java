/**
 * 
 */
package uk.ac.manchester.cs.pronto.events;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.pronto.exceptions.EventHandlingException;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Aug 30, 2009
 */
public class EventHandlingUtils {

	static Logger s_logger = Logger.getLogger(EventHandlingUtils.class);
	
	public static void fireEvent(	ReasoningEvent event,
									Map<EVENT_TYPES, List<ReasoningEventHandler>> handlersMap) {
		
		List<ReasoningEventHandler> handlers = handlersMap.get( event.getType() );
		
		if( handlers != null ) {

			for( ReasoningEventHandler handler : handlers ) {

				try {

					handler.handleEvent( event );

				} catch( EventHandlingException e ) {

					s_logger.error( "Error during handling of event: " + event );
					s_logger.error( e );
					// Is it fatal or not. By default it's not
				}
			}
		}
	}
}
