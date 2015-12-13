/**
 * 
 */
package uk.ac.manchester.cs.pronto.benchmark;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Nov 19, 2010
 */
public class TelemetryUtils {

	public static String getMeasuresAsString(Telemetry tProvider) {
		
		String str = "";
		String sep = System.getProperty( "line.separator" );
		
		for (String name : tProvider.getMeasureNames()) {
			
			str += name + ": " + tProvider.getMeasure( name ) + sep;
		}
		
		return str;
	}
}
