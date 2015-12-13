/**
 * 
 */
package uk.ac.manchester.cs.pronto.index;

import java.util.ArrayList;
import java.util.List;

import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.cache.IndexCache;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Oct 18, 2008
 */
public class TestIndexSetGenerator implements IndexSetGenerator {

	private static final String	URI_PREFIX	= "file:test_prefix#";
	
	String[][] m_terms = new String[][] {
			
			{URI_PREFIX + "Bird", URI_PREFIX + "Penguin"},
			{URI_PREFIX + "Bird", "n" + URI_PREFIX + "Penguin"}
	};

	public IndexSet generate(PTBox ptbox) {
		/*
		 * Return some fake predefined IS
		 */
		List<ATermAppl> indexTerms = new ArrayList<ATermAppl>();
		
		for (int i = 0; i < m_terms.length; i++) {
			
			ATermAppl[] conjuncts = new ATermAppl[m_terms[i].length];
			
			for (int j = 0; j < m_terms[i].length; j++) {
				
				if (m_terms[i][j].startsWith( "n" )) {
					
					conjuncts[j] = ATermUtils.negate( ATermUtils.makeTermAppl(m_terms[i][j].substring( 1 )) );
					
				} else {
				
					conjuncts[j] = ATermUtils.makeTermAppl(m_terms[i][j]);
				}
			}
			
			indexTerms.add( ATermUtils.makeAnd( ATermUtils.makeList( conjuncts )));
		}
		
		SimpleIndexSetImpl is = new SimpleIndexSetImpl();
		
		is.setTerms( indexTerms );
		
		return is;
	}

	public void useCache(IndexCache cache) {}

	@Override
	public void setTermNumber(int size) {
		// TODO Auto-generated method stub
		
	}

	
	
}
