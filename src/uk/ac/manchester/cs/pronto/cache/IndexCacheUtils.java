/**
 * 
 */
package uk.ac.manchester.cs.pronto.cache;

import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.utils.ATermUtils;
import org.mindswap.pellet.utils.Bool;

import aterm.ATermAppl;
import aterm.ATermList;

/**
 * <p>Title: IndexCacheUtils</p>
 * 
 * <p>Description: 
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class IndexCacheUtils {

	public static long s_cache_hit = 0;
	public static long s_cache_miss = 0;
	
	public static boolean isSatisfiable(IndexCache cache, KnowledgeBase kb, ATermAppl term) {

		if (null == cache) return kb.isSatisfiable( term );
		
		Bool result = cache.isKnownSatisfiable( term );
		
		if (result.isKnown()) {
			
			s_cache_hit++;
			
			return result.isTrue();
			
		} else {
			
			boolean value = kb.isSatisfiable( term );
			
			cache.rememberSatisfiable( term, Bool.create( value ) );
			
			s_cache_miss++;
			
			return value;
		}
	}
	
	public static boolean isSubClassOf(IndexCache cache, KnowledgeBase kb, ATermAppl expr, ATermAppl term) {
		
		if (null == cache) return kb.isSubClassOf( expr, term );	
		
		Bool known = cache.isKnownSubClassOf( expr, term );
		boolean result = false;
		
		if (known.isKnown()) {
			
			s_cache_hit++;
			
			result = known.isTrue();
			
		} else {
			/*
			 * TODO move to the cache
			 */
			known = cache.isKnownSatisfiable( ATermUtils.makeAnd(expr, ATermUtils.negate(term)) );
			
			if( known.isKnown() ) {

				s_cache_hit++;
				cache.rememberSubClassOf( expr, term, known.not() );
				result = known.isFalse();
				
			} else {
				
				result = kb.isSubClassOf( expr, term );

				cache.rememberSubClassOf( expr, term, Bool.create( result ) );

				s_cache_miss++;
			}
		}
		
		if (result && !ATermUtils.BOTTOM.isEqual( expr )) {
			cache.rememberDisjoint( expr, term, Bool.FALSE );
		}
		
		if (result) {		
			/*
			 * term is always a conjunction of literals. 
			 * So we use the fact that if T \in (A \and B)
			 * then T \notin ~A and T \notin ~B
			 */
			if( term.getAFun() == ATermUtils.ANDFUN ) {

				for( ATermList conjuncts = (ATermList) term.getArgument( 0 ); !conjuncts.isEmpty(); conjuncts = conjuncts
						.getNext() ) {

					cache.rememberSubClassOf( expr,
							ATermUtils.negate( (ATermAppl)conjuncts.getFirst() ), Bool.FALSE );
				}
			}
		}
		
		return result;
	}

	
	public static boolean isDisjoint(IndexCache cache, KnowledgeBase kb, ATermAppl expr, ATermAppl term) {
		
		assertNotNull( cache );		
		Bool known = cache.isKnownDisjoint( expr, term );
		boolean result = false;
		
		if (known.isKnown()) {
			
			s_cache_hit++;
			
			result = known.isTrue();
			
		} else {
			
			/*
			 * TODO move to the cache
			 */			
			known = cache.isKnownSatisfiable( ATermUtils.makeAnd(expr, term) );
			
			if( known.isKnown() ) {

				s_cache_hit++;
				cache.rememberDisjoint( expr, term, known.not() );
				result = known.isFalse();
				
			} else {

				result = kb.isDisjointClass( expr, term );
				
				cache.rememberDisjoint( expr, term, Bool.create( result ) );

				s_cache_miss++;
			}
		}
		

		if (result && !ATermUtils.BOTTOM.isEqual( expr )) {
			cache.rememberSubClassOf( expr, term, Bool.FALSE );
		}
		
		return result;
	}
	
	private static void assertNotNull(IndexCache cache) {
		
		if (null == cache) throw new RuntimeException("Index cache not created");
	}
}
