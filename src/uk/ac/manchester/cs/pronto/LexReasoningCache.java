/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import aterm.ATermAppl;

/**
 * <p>Title: LexReasoningCache</p>
 * 
 * <p>Description: 
 *  Keeps lexicographically minimal subsets that can be later reused
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class LexReasoningCache {

	private Map<ATermAppl, Set<Set<ConditionalConstraint>>> m_lexMap = new HashMap<ATermAppl, Set<Set<ConditionalConstraint>>>();
	
	public Set<Set<ConditionalConstraint>> getLexMinimalSubsets(ATermAppl evidence) {
		
		return m_lexMap.get( evidence );
	}
	
	public void cacheLexMinimalSubsets(ATermAppl evidence, Set<Set<ConditionalConstraint>> lexSubsets) {
		
		m_lexMap.put( evidence, lexSubsets );
	}
}
