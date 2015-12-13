/**
 * 
 */
package uk.ac.manchester.cs.pronto.benchmark;

/**
 * Internal interface to propagate measures across reasoning tasks
 * Should not be exposed to users (users can only read measures)
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * 22 Aug 2010
 */
public interface TelemetryEx extends Telemetry {

	public void setMeasure(String name, String measure);
}
