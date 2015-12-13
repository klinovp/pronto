package uk.ac.manchester.cs.pronto.lp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;
import org.mindswap.pellet.utils.ATermUtils;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.ProbKnowledgeBase;
import uk.ac.manchester.cs.pronto.constraints.CCSetAnalyzer;
import uk.ac.manchester.cs.pronto.io.KBStandaloneLoader;
import uk.ac.manchester.cs.pronto.ProntoMainTestSuite;
import uk.ac.manchester.cs.pronto.util.CCUtils;

public class CCSetAnalyzerTest {

	private static final String	FILE_PREFIX	= "file:" + ProntoMainTestSuite.BASE + "lp/";
	private static final String	URI_PREFIX	= ProntoMainTestSuite.URI_PREFIX;

	private CCSetAnalyzer m_ccAnalyzer = new CCSetAnalyzerImpl2(); 
	
	public CCSetAnalyzerTest() {}

	@Test
	public void testFirstNonToleratingSubset() throws Exception {
	
		ProbKnowledgeBase kb = new KBStandaloneLoader().load( FILE_PREFIX + "test_min_unsat_1.xml" );
		PTBox ptbox = kb.getPTBox();
		ConditionalConstraint cc = new ConditionalConstraint( ATermUtils.makeTermAppl( URI_PREFIX + "Penguin" ),
				ATermUtils.makeTermAppl( URI_PREFIX + "FlyingObject" ), 0, 0.05 );
		Set<ConditionalConstraint> toughConstraints = new HashSet<ConditionalConstraint>(2);
		
		toughConstraints.add(cc);
		toughConstraints.add(CCUtils.conceptVerificationConstraint(cc.getEvidence()));
		
		Set<ConditionalConstraint> ntSet = m_ccAnalyzer.getMinimalUnsatSubset(toughConstraints, ptbox);
		
		System.out.println(ntSet);
		
		assertNotNull( ntSet );
		assertEquals(1, ntSet.size());
		assertEquals(new ConditionalConstraint( ATermUtils.makeTermAppl( URI_PREFIX + "Bird" ),
				ATermUtils.makeTermAppl( URI_PREFIX + "FlyingObject" ), 0.9, 0.95 ), ntSet.iterator().next());
	}
	
	@Test
	public void testAllNonToleratingSubsets1() throws Exception {
		
		ProbKnowledgeBase kb = new KBStandaloneLoader().load( FILE_PREFIX + "test_min_unsat_2.xml" );
		PTBox ptbox = kb.getPTBox();
		Set<ConditionalConstraint> toughConstraints = new HashSet<ConditionalConstraint>(1);
		
		toughConstraints.add(CCUtils.conceptVerificationConstraint(ATermUtils.makeTermAppl( URI_PREFIX + "Penguin" )));
		
		Set<Set<ConditionalConstraint>> ntSets = m_ccAnalyzer.getMinimalUnsatSubsets(toughConstraints, ptbox);

		System.out.println(ntSets);		
		
		assertNotNull( ntSets );
		assertEquals(2, ntSets.size());
		
		Iterator<Set<ConditionalConstraint>> iter = ntSets.iterator(); 
		Set<ConditionalConstraint> first = iter.next();
		Set<ConditionalConstraint> second = iter.next();
		
		assertTrue((first.size() == 2 && second.size() == 3) || (second.size() == 2 && first.size() == 3));
	}
	
	@Test
	public void testAllNonToleratingSubsets2() throws Exception {
		
		ProbKnowledgeBase kb = new KBStandaloneLoader().load( FILE_PREFIX + "test_cg_1.xml" );
		PTBox ptbox = kb.getPTBox();
		Set<ConditionalConstraint> toughConstraints = new HashSet<ConditionalConstraint>(2);

		toughConstraints.add(
				new ConditionalConstraint(
						ATermUtils.makeTermAppl( URI_PREFIX + "Penguin" ),
						ATermUtils.makeTermAppl( URI_PREFIX + "FlyingObject" ),
						0.4, 0.4 ));
		toughConstraints.add(CCUtils.conceptVerificationConstraint(ATermUtils.makeTermAppl( URI_PREFIX + "Penguin" )));
		
		Set<Set<ConditionalConstraint>> ntSets = m_ccAnalyzer.getMinimalUnsatSubsets( toughConstraints, ptbox );
		
		assertNotNull( ntSets );
		assertEquals(1, ntSets.size());
		assertEquals(1, ntSets.iterator().next().size());
		System.out.println( ntSets );
	}
	
	@Test
	public void testAllNonToleratingSubsets3() throws Exception {
		
		ProbKnowledgeBase kb = new KBStandaloneLoader().load( FILE_PREFIX + "test_cg_2.xml" );
		PTBox ptbox = kb.getPTBox();
		Set<ConditionalConstraint> toughConstraints = new HashSet<ConditionalConstraint>(2);

		toughConstraints.add(
				new ConditionalConstraint(
						ATermUtils.makeTermAppl( URI_PREFIX + "ArcticPenguin" ),
						ATermUtils.makeTermAppl( URI_PREFIX + "WingedObject" ),
						0.7, 0.7 ));
		toughConstraints.add(CCUtils.conceptVerificationConstraint(ATermUtils.makeTermAppl( URI_PREFIX + "ArcticPenguin" )));
		
		Set<Set<ConditionalConstraint>> ntSets = m_ccAnalyzer.getMinimalUnsatSubsets( toughConstraints, ptbox );

		System.out.println( ntSets );
		assertTrue(ntSets.size() == 2);
		
		Iterator<Set<ConditionalConstraint>> iter = ntSets.iterator(); 
		Set<ConditionalConstraint> first = iter.next();
		Set<ConditionalConstraint> second = iter.next();		
		
		assertTrue((first.size() == 1 && second.size() == 2) || (second.size() == 1 && first.size() == 2));
	}	
	
	
	/*@Test
	public void testMinimalUnsatSubsets() throws Exception {
	
		ProbKnowledgeBase kb = new KBStandaloneLoader().load( "file:///C:/kl/kb/real/sao_500_SAT_1.owl" );
		PTBox ptbox = kb.getPTBox();
		Set<ConditionalConstraint> toughConstraints = new HashSet<ConditionalConstraint>();
		Set<ConditionalConstraint> unsatSet = m_ccAnalyzer.getMinimalUnsatSubset(toughConstraints, ptbox);
		
		for (ConditionalConstraint cc : unsatSet) {
			
			System.out.println( ptbox.translateConstraint( cc ));	
		}
	}*/	
}
