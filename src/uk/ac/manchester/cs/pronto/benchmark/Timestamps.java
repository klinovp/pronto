package uk.ac.manchester.cs.pronto.benchmark;

import java.util.HashMap;
import java.util.Map;

/**
 * Map of timestamps
 * 
 * @author pavel
 *
 */
public class Timestamps {

	private static Map<String, Timestamp> m_timestamps = new HashMap<String, Timestamp>();
	

	public static void start(String key) {
		
		Timestamp ts = m_timestamps.get( key );
		
		if (null == ts) {
			
			ts = new Timestamp();
		}
		
		ts.start();
		
		m_timestamps.put( key, ts );
	}
	
	/*
	 * Ask specific timestamp to collect statistics
	 */
	public static void stop(String key) {
		
		Timestamp ts = m_timestamps.get( key );
		
		if (null == ts) {
			
			ts = new Timestamp();
		}
		
		ts.stop();
		
		m_timestamps.put( key, ts );
	}
	
	public static void reset(String key) {
		
		Timestamp ts = m_timestamps.get( key );
		
		if (null == ts) {
			
			ts = new Timestamp();
		}
		
		ts.reset();
		
		m_timestamps.put( key, ts );
	}
	
	public static void resetAll() {
		
		for (Timestamp ts : m_timestamps.values()) {
			ts.reset();
		}
	}
	
	public static String printTotal(String key) {
		
		Timestamp ts = m_timestamps.get( key );
		
		if (null != ts) {
			
			return "Timestamp \"" + key + "\":" + System.getProperty( "line.separator" ) + 
				"Cache hits: " + ts.hitsTotal() + System.getProperty( "line.separator" ) +
				"Cache misses: " + ts.missesTotal() + System.getProperty( "line.separator" ) +
				"SAT tests total: " + (ts.hitsTotal() + ts.missesTotal()) + System.getProperty( "line.separator" );
		}
		
		return "";
	}
	
	public static String printLast(String key) {
		
		Timestamp ts = m_timestamps.get( key );
		
		if (null != ts) {
			
			return "Timestamp \"" + key + "\":" + System.getProperty( "line.separator" ) + 
				"Cache hits: " + ts.hitsLast() + System.getProperty( "line.separator" ) +
				"Cache misses: " + ts.missesLast() + System.getProperty( "line.separator" ) +
				"SAT tests total: " + (ts.hitsLast() + ts.missesLast()) + System.getProperty( "line.separator" );
		}
		
		return "";
	}
	
	
	public static String printTotals() {
		
		StringBuffer totals = new StringBuffer("");
		
		for (String key : m_timestamps.keySet()) {
			totals.append( printTotal(key) );
		}
	
		return totals.toString();
	}
}
