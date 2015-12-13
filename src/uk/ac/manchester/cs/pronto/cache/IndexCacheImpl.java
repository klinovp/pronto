/**
 * 
 */
package uk.ac.manchester.cs.pronto.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mindswap.pellet.utils.Bool;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.Constants;

/**
 * <p>Title: IndexCache</p>
 * 
 * <p>Description: 
 *  Stores the known results of satisfiability, disjointness and subsumption
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class IndexCacheImpl implements IndexCache {
	
	protected Map<ATermAppl, Map<ATermAppl, Bool>> m_subCache = new HashMap<ATermAppl, Map<ATermAppl, Bool>>();
	protected Map<ATermAppl, Map<ATermAppl, Bool>> m_disjCache = new HashMap<ATermAppl, Map<ATermAppl, Bool>>();
	protected Map<ATermAppl, Bool> m_satCache = new HashMap<ATermAppl, Bool>();
	/*
	 * We store subsumption relations between terms to improve cache hit ratio
	 * Subsumers - terms that subsume the key
	 * Subsumees - terms that are subsumed by the key
	 */
	protected Map<ATermAppl, List<ATermAppl>> m_subsumees;
	protected Map<ATermAppl, List<ATermAppl>> m_subsumers;
	/*
	 * We also store disjointness matrix
	 */
	protected Map<ATermAppl, List<ATermAppl>> m_disjoints;
	
	public Bool isKnownSatisfiable(ATermAppl term) {
		
		if (!Constants.USE_PRONTO_CACHE) {
			
			return Bool.UNKNOWN;
		}
		
		Bool known = m_satCache.get( term );
		
		return null == known ? Bool.UNKNOWN : known;
	}
	
	public void rememberSatisfiable(ATermAppl term, Bool sat) {
		
		m_satCache.put( term, sat );
		
	}
	
	public Bool isKnownSubClassOf(ATermAppl expr, ATermAppl term) {
		
		/*
		 * TODO move this check to the cache utils
		 */
		if (!Constants.USE_PRONTO_CACHE) {

			return Bool.UNKNOWN;
		}
		
		Bool result = cachedValue(expr, term, m_subCache);
		
		if (!result.isKnown()) {
			//Look if expr is a subclass of some subsumee
			result = isKnownSubclassOfSubsumee( expr, term );
		}

		if (!result.isKnown()) {
			//Look if expr is NOT a subclass of some subsumer
			result = isKnownNotSubclassOfSubsumer( expr, term );
		}
		
		if (!result.isKnown()) {		
			/*
			 * If the result is still unknown, try to use the disjointness matrix
			 * If C is a subclass of A and A is disjoint with B, then C
			 * is not a subclass of B
			 */
			result = isKnownDisjointWithSubsumer(expr, term);
		}
		
		return result;
	}

	
	/*
	 * Check if expr is a subclass of some subsumee (class that is subclass of term)
	 */
	protected Bool isKnownSubclassOfSubsumee(ATermAppl expr, ATermAppl term) {
	
		Bool result = Bool.UNKNOWN;
		List<ATermAppl> subsumees = m_subsumees.get( term );
		
		if( null != subsumees ) {
			for( ATermAppl subsumee : subsumees ) {

				if( (cachedValue( expr, subsumee, m_subCache )).isTrue() ) {

					rememberSubClassOf( expr, term, Bool.TRUE);
					
					return result;
				}
			}
		}
		
		return result;
	}

	/*
	 * Check if expr is NOT a subclass of some subsumer (class that is subclass of term)
	 */
	protected Bool isKnownNotSubclassOfSubsumer(ATermAppl expr, ATermAppl term) {
	
		Bool result = Bool.UNKNOWN;
		List<ATermAppl> subsumers = m_subsumers.get( term );
		
		if( null != subsumers ) {
			
			for( ATermAppl subsumer : subsumers ) {

				if( (cachedValue( expr, subsumer, m_subCache )).isFalse() ) {

					rememberSubClassOf( expr, term, Bool.FALSE);
					
					return result;
				}
			}
		}
		
		return result;
	}
	
	
	/*
	 * Check if expr is disjoint with some subsumer (class that is superclass of term)
	 */
	protected Bool isKnownDisjointWithSubsumer(ATermAppl expr, ATermAppl term) {

		Bool result = Bool.UNKNOWN;
		List<ATermAppl> disjoints = m_disjoints.get( term );
		
		if( null != disjoints ) {

			for( ATermAppl disjoint : disjoints ) {

				if( (cachedValue( expr, disjoint, m_subCache )).isTrue() ) {

					rememberSubClassOf( expr, term, Bool.FALSE );
					rememberDisjoint( expr, term, Bool.TRUE );

					return Bool.FALSE;
				}
			}
		}
		
		return result;
	}
	
	
	public Bool isKnownDisjoint(ATermAppl expr, ATermAppl term) {
		
		if (!Constants.USE_PRONTO_CACHE) {

			return Bool.UNKNOWN;
		}
		
		Bool result = cachedValue(expr, term, m_disjCache);
		
		if (!result.isKnown()) {
			//Look if expr is disjoint with some subsumer
			List<ATermAppl> subsumers = m_subsumers.get( term );			
			
			if( null != subsumers ) {
				
				for( ATermAppl subsumer : subsumers ) {

					//if( (result = isKnownDisjoint( expr, subsumer )).isTrue() ) {
					if( (result = cachedValue( expr, subsumer, m_disjCache )).isTrue() ) {

						rememberDisjoint( expr, term, Bool.TRUE);

						return result;
					}
				}
			}
			
			/*
			 * If the result is still unknown, try to use the disjointness matrix
			 * If C is a subclass of A and A is disjoint with B, then C
			 * is disjoint with B
			 */
			List<ATermAppl> disjoints = m_disjoints.get( term );
			
			if( null != disjoints ) {

				for( ATermAppl disjoint : disjoints ) {

					if( (result = cachedValue( expr, disjoint, m_subCache )).isTrue() ) {

						rememberSubClassOf( expr, term, Bool.FALSE );
						rememberDisjoint( expr, term, Bool.TRUE );

						return Bool.TRUE;
					}
				}

				result = Bool.UNKNOWN;
			}			
		}
		
		return result;
	}
	
	protected Bool cachedValue(ATermAppl expr, ATermAppl term, Map<ATermAppl, Map<ATermAppl, Bool>> cache) {
		
		Map<ATermAppl, Bool> known = cache.get( term );
		
		if (null == known) {
			
			return Bool.UNKNOWN;
			
		} else {
			
			Bool value = known.get( expr );
			
			return null == value ? Bool.UNKNOWN : value;
		}
	}
	
	protected void cacheValue(ATermAppl expr, ATermAppl term, Map<ATermAppl, Map<ATermAppl, Bool>> cache, Bool value) {
		
		Map<ATermAppl, Bool> known = cache.get( term );
		
		if (null == known) {
			
			known = new HashMap<ATermAppl, Bool>(1);
		}			
		
		known.put( expr, value );
		cache.put( term, known );
	}

	
	public void rememberSubClassOf(ATermAppl expr, ATermAppl term, Bool subclass) {
		
		cacheValue(expr, term, m_subCache, subclass);
	}
	
	public void rememberDisjoint(ATermAppl expr, ATermAppl term, Bool disjoint) {
		
		cacheValue(expr, term, m_disjCache, disjoint);
	}		
	
	
	public void useSubsumptionMatrix(Map<ATermAppl, List<ATermAppl>> matrix) {
		
		m_subsumees = new HashMap<ATermAppl, List<ATermAppl>>(matrix.size());
		m_subsumers = matrix;
		
		for (ATermAppl term : m_subsumers.keySet()) {
			
			for (ATermAppl subsumer : m_subsumers.get( term )) {
				
				List<ATermAppl> subsumees = m_subsumees.get( subsumer );
				
				if (null == subsumees) {
					subsumees = new ArrayList<ATermAppl>();
				}
				
				subsumees.add( term );
				m_subsumees.put( subsumer, subsumees );
			}
		}
	}
	
	public void useDisjointnessMatrix(Map<ATermAppl, List<ATermAppl>> matrix) {
		
		m_disjoints = matrix;
	}
	
	public void clear() {
		
		m_disjCache.clear();
		m_satCache.clear();
		m_subCache.clear();
		m_disjoints.clear();
		m_subsumees.clear();
		m_subsumers.clear();
	}
}