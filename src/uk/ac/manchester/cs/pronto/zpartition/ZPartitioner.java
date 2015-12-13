/**
 * 
 */
package uk.ac.manchester.cs.pronto.zpartition;

import java.util.List;
import java.util.Map;

import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.benchmark.Telemetry;
import uk.ac.manchester.cs.pronto.events.EVENT_TYPES;
import uk.ac.manchester.cs.pronto.events.ReasoningEventHandler;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface ZPartitioner extends Telemetry {

	public ZPartition partition(PTBox ptbox);
	public void setEventHandlers(Map<EVENT_TYPES, List<ReasoningEventHandler>> handlersMap);
}
