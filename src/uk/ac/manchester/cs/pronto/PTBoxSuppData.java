/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.cache.IndexCache;
import uk.ac.manchester.cs.pronto.constraints.ConflictGraph;
import uk.ac.manchester.cs.pronto.index.IndexSet;
import uk.ac.manchester.cs.pronto.zpartition.ZPartition;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class PTBoxSuppData {

	private static final Logger s_logger = Logger.getLogger(PTBoxSuppData.class);
	
	private IndexCache m_iCache;
	private IndexSet m_iSet;
	private ConflictGraph m_cGraph;
	private ZPartition m_zPartition;
	//Disjointness and subsumption matrices for classes appearing in conditional constraints
	private Map<ATermAppl, Set<ATermAppl>> m_disjointMap = new HashMap<ATermAppl, Set<ATermAppl>>();
	private Map<ATermAppl,Set<ATermAppl>> m_subsumerMap = new HashMap<ATermAppl, Set<ATermAppl>>();
	/**
	 * @return
	 */
	public IndexCache getCache() {
		return m_iCache;
	}
	
	public void setCache(IndexCache cache) {
		
		m_iCache = cache;
	}

	public ConflictGraph getConflictGraph() {
		return m_cGraph;
	}
	
	public void setConflictGraph(ConflictGraph cGraph) {
		
		m_cGraph = cGraph;
	}

	public IndexSet getIndexSet() {
		
		return m_iSet;
	}
	
	public void setIndexSet(IndexSet iSet) {
		
		m_iSet = iSet;
	}
	

	public ZPartition getZPartition() {
		
		return m_zPartition;
	}

	
	public void setZPartition(ZPartition zp) {
		
		m_zPartition = zp;
	}
	

	/**
	 * Does not clear the cache
	 */
	public void reset() {
		
		m_cGraph = null;
		m_iSet = null;
		m_zPartition = null;
	}
	
	public Map<ATermAppl, Set<ATermAppl>> getSubsumptionMatrix() {
		
		return m_subsumerMap;
	}
	
	public Map<ATermAppl, Set<ATermAppl>> getDisjointnessMatrix() {
		
		return m_disjointMap;
	}
	
	protected void computeMatrices(Set<ATermAppl> classSet, KnowledgeBase kb) {
		//Compute all the subsumption/disjointness relationships
		//It's slow, O(N^2)
		s_logger.debug( "Computing subsumption and disjointness matrices" );

		m_disjointMap.clear();
		m_subsumerMap.clear();
		
		for (ATermAppl clazz : classSet) {
			
			s_logger.debug( "Processing class: " + clazz );
			
			//ATermAppl notClazz =  ATermUtils.negate( clazz );
			Set<ATermAppl> posSubsumers = new HashSet<ATermAppl>();
			//Set<ATermAppl> negSubsumers = new HashSet<ATermAppl>();
			Set<ATermAppl> posDisjoints = new HashSet<ATermAppl>();
		
			//Subsumptions
			for (Set<ATermAppl> subsumers : kb.getSuperClasses( clazz )) posSubsumers.addAll( subsumers );
			//for (Set<ATermAppl> subsumers : kb.getSuperClasses( notClazz )) negSubsumers.addAll( subsumers );
			
			posSubsumers.remove( ATermUtils.TOP );
			//negSubsumers.remove( ATermUtils.TOP );
			posSubsumers.addAll( kb.getEquivalentClasses( clazz ) );
			//negSubsumers.addAll( kb.getEquivalentClasses( notClazz ) );
			//Disjointness
			for (Set<ATermAppl> disjSet : kb.getDisjointClasses( clazz )) posDisjoints.addAll( disjSet );
			
			posDisjoints.remove( ATermUtils.BOTTOM );
			m_subsumerMap.put( clazz, posSubsumers );
			//m_subsumerMap.put( notClazz, negSubsumers );
			m_disjointMap.put( clazz, posDisjoints );
			//negTotalLength += negSubsumers.size();
		}
		
		s_logger.debug( "Subsumption and disjointness matrices computed" );
	}
	
	public PTBoxSuppData clone() {
		
		PTBoxSuppData data = new PTBoxSuppData();
		/*
		 * we don't clone cache
		 */
		data.m_disjointMap = m_disjointMap;
		data.m_subsumerMap = m_subsumerMap;
		data.setCache( m_iCache );
		data.setConflictGraph( m_cGraph == null ? null : m_cGraph.clone() );
		data.setIndexSet( m_iSet == null ? null : m_iSet.clone() );
		data.setZPartition( m_zPartition == null ? null : m_zPartition.clone() );
		
		return data;
	}
	
	public void clearCache() {
		
		m_iCache.clear();
	}
}