/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Nov 6, 2008
 */
public enum TERTIARY_VALUE {
	
	TRUE,
	FALSE,
	UNKNOWN;
	
	public static TERTIARY_VALUE fromBoolean(boolean flag) {
		
		return flag ? TRUE : FALSE; 
	}

	public boolean isKnown() {
		
		return this != UNKNOWN;
	}
	
	public boolean isTrue() {
		
		return this == TRUE;
	}
}
