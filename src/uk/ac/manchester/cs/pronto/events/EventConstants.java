/**
 * 
 */
package uk.ac.manchester.cs.pronto.events;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Aug 31, 2009
 */
public class EventConstants {

	public static final Map<EVENT_TYPES, Boolean> S_EVENT_CFG;
	
	static {
		
		S_EVENT_CFG = new HashMap<EVENT_TYPES, Boolean>();
		
		S_EVENT_CFG.put( EVENT_TYPES.BEFORE_CONFLICT_GRAPH_COMPUTED, true );
		S_EVENT_CFG.put( EVENT_TYPES.AFTER_CONFLICT_GRAPH_COMPUTED, true );
		S_EVENT_CFG.put( EVENT_TYPES.BEFORE_ZPARTITION_COMPUTED, true );
		S_EVENT_CFG.put( EVENT_TYPES.AFTER_ZPARTITION_COMPUTED, true );
	}
}
