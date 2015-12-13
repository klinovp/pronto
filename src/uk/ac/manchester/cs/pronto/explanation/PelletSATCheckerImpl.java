/**
 * 
 */
package uk.ac.manchester.cs.pronto.explanation;

import java.util.Collection;

import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Solves SAT using underlying Pellet's KnowledgeBase class
 */
public class PelletSATCheckerImpl implements SATChecker {

	private KnowledgeBase m_kb = null;
	
	public PelletSATCheckerImpl(KnowledgeBase kb) {
		
		m_kb = kb;
	}
	/**
	 * @param conjuncts
	 * @return
	 */
	@Override
	public boolean isSatisfiable(Collection<ATermAppl> conjuncts) {
		
		return m_kb.isSatisfiable( ATermUtils.makeAnd(ATermUtils.makeList(conjuncts)) );
	}

	/**
	 * @param term
	 * @return
	 */
	@Override
	public boolean isSatisfiable(ATermAppl term) {
		
		return m_kb.isSatisfiable( term );
	}
	
	public KnowledgeBase getKB() {
		
		return m_kb;
	}

}
