/**
 * 
 */
package uk.ac.manchester.cs.pronto.benchmark;

import java.util.Collection;

/**
 * This interface should be implemented by classes which can expose some of its 
 * internal behavior measures, for example, performance metrics 
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface Telemetry {

	public void resetMeasures();
	public void resetMeasure(String measure);
	public String getMeasure(String measure);
	public Collection<String> getMeasureNames();
	public boolean isMeasureSupported(String measure);
}
