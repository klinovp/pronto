/**
 * 
 */
package uk.ac.manchester.cs.pronto.index;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.ProbKnowledgeBase;
import uk.ac.manchester.cs.pronto.io.KBStandaloneLoader;
import uk.ac.manchester.cs.pronto.ProntoMainTestSuite;


/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class IndexSetGeneratorTest {

	private static final String	FILE_PREFIX	= "file:" + ProntoMainTestSuite.BASE + "index/";
	
	private IndexSetGenerator	m_sGen	= new RandomConceptTypeSetGenImpl();

	@Test
	public void testGenerate1() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load(FILE_PREFIX + "test_index_set_4.xml" );
		
		m_sGen.setTermNumber( 10 );
		
		IndexSet indexSet = m_sGen.generate( pkb.getPTBox() );
	
		assertEquals(6, indexSet.getTerms().size());
	}

	public void testFindSubsumed(IndexSet iSet, IndexSetGenerator gen, ATermAppl term, int expected) throws Exception {

		Collection<? extends IndexTerm> subsumed = iSet.getSubsumedTerms( term );

		for( IndexTerm t : subsumed ) {
			
			System.out.println( t );
		}

		assertEquals( expected, subsumed.size() );
	}

}

