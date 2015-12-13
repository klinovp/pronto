package uk.ac.manchester.cs.pronto.index;

import java.util.Collection;
import static org.junit.Assert.*;

import org.junit.Test;
import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.ProbKnowledgeBase;
import uk.ac.manchester.cs.pronto.io.KBStandaloneLoader;
import uk.ac.manchester.cs.pronto.ProntoMainTestSuite;

/**
 *
 */
public class SimpleIndexSetImplTest {

	private static final String	FILE_PREFIX	= "file:" + ProntoMainTestSuite.BASE + "index/";
	private static final String	URI_PREFIX	= ProntoMainTestSuite.URI_PREFIX;	
	
	private IndexSetGenerator	m_sGen	= new ConceptTypeSetGeneratorImpl();

	@Test
	public void testFindSubsumed1() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load(FILE_PREFIX + "test_index_set_1.xml" );
		
		testFindSubsumed( pkb.getPTBox(), m_sGen, ATermUtils.makeNot(ATermUtils.makeTermAppl( URI_PREFIX + "WingedObject" )), 6 );
	}

	@Test
	public void testFindSubsumed2() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load(FILE_PREFIX + "test_index_set_1.xml" );
		
		testFindSubsumed( pkb.getPTBox(), m_sGen,
				ATermUtils.makeNot( ATermUtils.makeTermAppl(URI_PREFIX + "Penguin" )), 8 );
	}
	
	@Test
	public void testFindSubsumed3() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load(FILE_PREFIX + "test_index_set_1.xml" );
		
		testFindSubsumed( pkb.getPTBox(), m_sGen,
				ATermUtils.makeAnd( 
						ATermUtils.makeNot( ATermUtils.makeTermAppl(URI_PREFIX + "Bird" )),
						ATermUtils.makeNot( ATermUtils.makeTermAppl(URI_PREFIX + "Penguin" ))), 4 );
	}
	
	@Test
	public void testFindSubsumed4() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load(FILE_PREFIX + "test_index_set_1.xml" );
		
		testFindSubsumed( pkb.getPTBox(), m_sGen, ATermUtils.makeTermAppl(URI_PREFIX + "FlyingObject" ), 6 );
	}	
	
	public void testFindSubsumed(PTBox ptbox, IndexSetGenerator gen, ATermAppl term, int expected) throws Exception {

		IndexSet indexSet = gen.generate( ptbox );
		
		Collection<? extends IndexTerm> subsumed = indexSet.getSubsumedTerms( term );

		assertEquals( expected, subsumed.size() );
	}

}
