package uk.ac.manchester.cs.pronto;

import junit.framework.TestCase;

import org.mindswap.pellet.utils.ATermUtils;

import uk.ac.manchester.cs.pronto.io.KBStandaloneLoader;

/**
 * <p>Title: ProntoLexicographicReasonerTest</p>
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
public class ProntoLexicographicReasonerTest extends TestCase {

	private static final String	FILE_PREFIX	= "file:" + ProntoMainTestSuite.BASE + "lex_reasoner/";
	private static final String	URI_PREFIX	= ProntoMainTestSuite.URI_PREFIX;

	/*
	 * Check simple inheritance with overriding
	 */
	public void testTightLexicographicEntailment1() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load(FILE_PREFIX + "test_tlexent_1.xml" );
		BasicLexicographicReasoner reasoner = new BasicLexicographicReasoner(
				new PSATSolverImpl() );
		ConditionalConstraint apfCC = reasoner
				.tightLexicographicEntailment(
						pkb.getPTBox(),
						null,
						ATermUtils.makeTermAppl( URI_PREFIX + "ArcticPenguin" ),
						ATermUtils.makeTermAppl( URI_PREFIX + "FlyingObject" ) );

		assertEquals( 0.0, apfCC.getLowerBound(), 0.0001 ); 
		assertEquals( 0.05, apfCC.getUpperBound(), 0.0001 );

	}

	/*
	 * Check that if KB |= C \in D then KB |=(lex) (C|D)[1,1]
	 */
	public void testTightLexicographicEntailment2() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlexent_1.xml" );
		BasicLexicographicReasoner reasoner = new BasicLexicographicReasoner(
				new PSATSolverImpl() );
		ConditionalConstraint apfCC = reasoner
				.tightLexicographicEntailment( pkb.getPTBox(), null, ATermUtils
						.makeTermAppl( URI_PREFIX + "ArcticPenguin" ),
						ATermUtils.makeTermAppl( URI_PREFIX + "Bird" ) );

		assertEquals( 1.0, apfCC.getLowerBound(), 0.0001 );
		assertEquals( 1.0, apfCC.getUpperBound(), 0.0001 );
	}

	/*
	 * Check complete ignorance ( KB |=(lex) (C|D)[0,1] )
	 */
	public void testTightLexicographicEntailment3() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlexent_1.xml" );
		BasicLexicographicReasoner reasoner = new BasicLexicographicReasoner(
				new PSATSolverImpl() );
		ConditionalConstraint apfCC = reasoner
				.tightLexicographicEntailment(
						pkb.getPTBox(),
						null,
						ATermUtils.makeTermAppl( URI_PREFIX + "ArcticPenguin" ),
						ATermUtils.makeTermAppl( URI_PREFIX + "WingedObject" ) );

		assertEquals( 0.0, apfCC.getLowerBound(), 0.0001 );
		assertEquals( 1.0, apfCC.getUpperBound(), 0.0001 );

	}

	/*
	 * Check the case of (D|C)[l_1,u_1] and (E|D)[l_2,u_2]
	 */
	public void testTightLexicographicEntailment4() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlexent_2.xml" );
		BasicLexicographicReasoner reasoner = new BasicLexicographicReasoner(
				new PSATSolverImpl() );
		ConditionalConstraint wfCC = reasoner.tightLexicographicEntailment( pkb.getPTBox(), null,
				ATermUtils.makeTermAppl( URI_PREFIX + "WingedObject" ),
				ATermUtils.makeTermAppl( URI_PREFIX + "FlyingObject" ) );

		assertEquals( 0.63, wfCC.getLowerBound(), 0.0001 );
		assertEquals( 0.965, wfCC.getUpperBound(), 0.0001 );

	}

	/*
	 * Check inheritance and overriding simultaneously
	 */
	public void testTightLexicographicEntailment5() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlexent_3.xml" );
		ProntoReasoner reasoner = new BasicLexicographicReasoner( new PSATSolverImpl() );
		ConditionalConstraint awfCC = reasoner.subsumptionEntailment( pkb, ATermUtils
				.makeTermAppl( URI_PREFIX + "ArcticPenguin" ),
				ATermUtils.makeTermAppl( URI_PREFIX + "WingedFlying" ) );

		assertEquals( 0.1, awfCC.getLowerBound(), 0.0001 );
		assertEquals( 0.5, awfCC.getUpperBound(), 0.0001 );

	}

	/*
	 * Check simple instance checking
	 */
	public void testTightLexicographicEntailment6() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlexent_5.xml" );
		ProntoReasoner reasoner = new BasicLexicographicReasoner( new PSATSolverImpl() );

		ConditionalConstraint tweetyCC = reasoner.membershipEntailment( pkb, ATermUtils
				.makeTermAppl( URI_PREFIX + "Tweety" ), ATermUtils
				.makeTermAppl( URI_PREFIX + "FlyingObject" ) );

		assertEquals( 0.28, tweetyCC.getLowerBound(), 0.0001 );
		assertEquals( 0.72, tweetyCC.getUpperBound(), 0.0001 );
	}

	/*
	 * Check advanced instance checking
	 */
	public void testTightLexicographicEntailment7() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlexent_4.xml" );
		ProntoReasoner reasoner = new BasicLexicographicReasoner( new PSATSolverImpl() );

		ConditionalConstraint lewisCC = reasoner.membershipEntailment( pkb, ATermUtils
				.makeTermAppl( URI_PREFIX + "Lewis" ), ATermUtils
				.makeTermAppl( URI_PREFIX + "WingedFlying" ) );

/*		ConditionalConstraint awfCC = reasoner.subsumptionEntailment( pkb, ATermUtils
				.makeTermAppl( URI_PREFIX + "ArcticPenguin" ),
				ATermUtils.makeTermAppl( URI_PREFIX + "WingedFlying" ) );
*/		
/*		ConditionalConstraint apfCC = reasoner.subsumptionEntailment( pkb, ATermUtils
				.makeTermAppl( URI_PREFIX + "ArcticPenguin" ),
				ATermUtils.makeTermAppl( URI_PREFIX + "FlyingObject" ) );
*/		
		
/*		ConditionalConstraint lewisF = reasoner.membershipEntailment( pkb, ATermUtils
				.makeTermAppl( URI_PREFIX + "Lewis" ), ATermUtils
				.makeTermAppl( URI_PREFIX + "FlyingObject" ) );

		ConditionalConstraint lewisW = reasoner.membershipEntailment( pkb, ATermUtils
				.makeTermAppl( URI_PREFIX + "Lewis" ), ATermUtils
				.makeTermAppl( URI_PREFIX + "WingedObject" ) );
*/		
		
		assertEquals( 0.03, lewisCC.getLowerBound(), 0.0001 );
		assertEquals( 0.46, lewisCC.getUpperBound(), 0.0001 );

	}
	
	/**
	 * Membership entailment is supposed to check PABox consistency for that
	 * individual. And it must fail in this case
	 * @throws Exception
	 */
	public void testNotAConsistent() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_membership_1.xml" );
		ProntoReasoner reasoner = new BasicLexicographicReasoner( new PSATSolverImpl() );
		boolean result = false;

		try {
			reasoner.membershipEntailment( pkb, ATermUtils
					.makeTermAppl( URI_PREFIX + "Lewis" ), ATermUtils
					.makeTermAppl( URI_PREFIX + "Penguin" ) );
		} catch( RuntimeException e ) {
			result = true;
		}

		assertTrue(result);
	}
}
