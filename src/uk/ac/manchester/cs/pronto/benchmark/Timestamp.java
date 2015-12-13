package uk.ac.manchester.cs.pronto.benchmark;

import uk.ac.manchester.cs.pronto.cache.IndexCacheUtils;

/**
 * The class collects information about cache hit ratio at given timestamp.
 * WARNING class directly uses static members of IndexCacheUtils (subject to refactor)
 * 
 * @author pavel
 *
 */
public class Timestamp {

	private long m_hitsTotal = 0;
	private long m_missesTotal = 0;
	
	private long m_hitsLast = 0;
	private long m_missesLast = 0;
	
	private long m_hitsCurr = 0;
	private long m_missesCurr = 0;
	
	
	protected void reset() {
		
		m_hitsTotal = m_missesTotal = m_hitsLast = m_missesLast = 0;
	}
	
	protected void start() {
		
		m_hitsCurr = IndexCacheUtils.s_cache_hit;
		m_missesCurr = IndexCacheUtils.s_cache_miss;
		
		m_hitsLast = m_missesLast = 0;
	}
	
	/*
	 * Collect statistics
	 */
	protected void stop() {
		
		//Statistics since the last start
		m_hitsLast = IndexCacheUtils.s_cache_hit - m_hitsCurr;
		m_missesLast = IndexCacheUtils.s_cache_miss - m_missesCurr;
		//Update totals
		m_hitsTotal += m_hitsLast;
		m_missesTotal += m_missesLast;
	}
	
	
	public long hitsTotal() {
		return m_hitsTotal;
	}
	
	public long hitsLast() {
		return m_hitsLast;
	}
	
	public long missesTotal() {
		return m_missesTotal;
	}
	
	public long missesLast() {
		return m_missesLast;
	}
	
}
