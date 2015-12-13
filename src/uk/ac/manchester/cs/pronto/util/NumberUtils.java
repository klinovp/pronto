package uk.ac.manchester.cs.pronto.util;

import java.math.BigDecimal;
import java.math.MathContext;

import uk.ac.manchester.cs.pronto.Constants;

/**
 * Encapsulates various helper methods for handling probability values (possibly intervals)
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * May 5, 2008
 */
public class NumberUtils {
	
	public static double roundProbability(double value) {
		
		value = value < Constants.PROBABILITY_LOW_THRESHOLD ? 0 : value;
		
		return BigDecimal.valueOf(value).round(new MathContext((int) (-1 * Math.log10(Constants.PROBABILITY_LOW_THRESHOLD)))).doubleValue();
		
	}

    public static double round(double val) {

		return round(val, Constants.PRECISION);
	}
	
	
    public static double round(double val, int places) {

		long factor = (long) Math.pow( 10, places );

		val = val * factor;

		long tmp = Math.round( val );

		return (double) tmp / factor;
	}

    public static boolean equal(double val1, double val2, double epsilon) {
    	
    	return Math.abs( val1 - val2 ) < epsilon;
    }

    public static boolean equal(double val1, double val2) {
    	
    	return equal(val1, val2, Constants.PROBABILITY_LOW_THRESHOLD);
    }
    
    public static boolean greater(double val1, double val2) {
    	
    	return val1 - val2 > Constants.PROBABILITY_LOW_THRESHOLD;
    }
    
    public static int compare(double val1, double val2) {
    	
    	if (val1 - val2 > Constants.PROBABILITY_LOW_THRESHOLD) return 1;
    	
    	if (val1 - val2 < Constants.PROBABILITY_LOW_THRESHOLD) return -1;
    	
    	return 0;
    }
}


