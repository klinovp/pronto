package uk.ac.manchester.cs.pronto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mindswap.pellet.utils.ATermUtils;

import uk.ac.manchester.cs.pronto.benchmark.TelemetryUtils;
import uk.ac.manchester.cs.pronto.io.KBStandaloneLoader;

public class HSOptimizedLexReasonerTest {
	
	private static final String	FILE_PREFIX	= "file:" + ProntoMainTestSuite.BASE + "lex_reasoner/";
	private static final String	URI_PREFIX	= ProntoMainTestSuite.URI_PREFIX;

	/*
	 * Check simple inheritance with overriding
	 */
	@Test
	public void testTightLexicographicEntailment1() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load(FILE_PREFIX + "test_tlexent_1.xml" );
		BasicLexicographicReasoner reasoner = new HSOptimizedLexReasoner( new PSATSolverImpl() );
		
		Pronto.setDefaultEventHandlers( reasoner );
		
		ConditionalConstraint apfCC = reasoner.tightLexicographicEntailment(
						pkb.getPTBox(),
						null,
						ATermUtils.makeTermAppl( URI_PREFIX + "ArcticPenguin" ),
						ATermUtils.makeTermAppl( URI_PREFIX + "FlyingObject" ) );

		System.out.println(TelemetryUtils.getMeasuresAsString(reasoner ));
		
		assertEquals( 0.0, apfCC.getLowerBound(), 0.0001 ); 
		assertEquals( 0.05, apfCC.getUpperBound(), 0.0001 );
	}

	/*
	 * Check that if KB |= C \in D then KB |=(lex) (C|D)[1,1]
	 */
	@Test
	public void testTightLexicographicEntailment2() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlexent_1.xml" );
		BasicLexicographicReasoner reasoner = new HSOptimizedLexReasoner( new PSATSolverImpl() );
		ConditionalConstraint apfCC = reasoner.tightLexicographicEntailment(
						pkb.getPTBox(),
						null,
						ATermUtils.makeTermAppl( URI_PREFIX + "ArcticPenguin" ),
						ATermUtils.makeTermAppl( URI_PREFIX + "Bird" ) );

		System.out.println(TelemetryUtils.getMeasuresAsString( reasoner ));
		
		assertEquals( 1.0, apfCC.getLowerBound(), 0.0001 );
		assertEquals( 1.0, apfCC.getUpperBound(), 0.0001 );
	}

	/*
	 * Check complete ignorance ( KB |=(lex) (C|D)[0,1] )
	 */
	@Test
	public void testTightLexicographicEntailment3() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlexent_1.xml" );
		BasicLexicographicReasoner reasoner = new HSOptimizedLexReasoner( new PSATSolverImpl() );
		ConditionalConstraint apfCC = reasoner.tightLexicographicEntailment(
						pkb.getPTBox(),
						null,
						ATermUtils.makeTermAppl( URI_PREFIX + "ArcticPenguin" ),
						ATermUtils.makeTermAppl( URI_PREFIX + "WingedObject" ) );

		System.out.println(TelemetryUtils.getMeasuresAsString( reasoner ));
		
		assertEquals( 0.0, apfCC.getLowerBound(), 0.0001 );
		assertEquals( 1.0, apfCC.getUpperBound(), 0.0001 );
	}

	/*
	 * Check the case of (D|C)[l_1,u_1] and (E|D)[l_2,u_2]
	 */
	@Test
	public void testTightLexicographicEntailment4() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlexent_2.xml" );
		BasicLexicographicReasoner reasoner = new HSOptimizedLexReasoner( new PSATSolverImpl() );
		ConditionalConstraint wfCC = reasoner.tightLexicographicEntailment( pkb.getPTBox(), null,
				ATermUtils.makeTermAppl( URI_PREFIX + "WingedObject" ),
				ATermUtils.makeTermAppl( URI_PREFIX + "FlyingObject" ) );

		System.out.println(TelemetryUtils.getMeasuresAsString( reasoner ));
		
		assertEquals( 0.63, wfCC.getLowerBound(), 0.0001 );
		assertEquals( 0.965, wfCC.getUpperBound(), 0.0001 );
	}

	/*
	 * Check inheritance and overriding simultaneously
	 */
	@Test
	public void testTightLexicographicEntailment5() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlexent_3.xml" );
		ProntoReasoner reasoner = new HSOptimizedLexReasoner( new PSATSolverImpl() );
		ConditionalConstraint awfCC = reasoner.subsumptionEntailment(
				pkb,
				ATermUtils.makeTermAppl( URI_PREFIX + "ArcticPenguin" ),
				ATermUtils.makeTermAppl( URI_PREFIX + "WingedFlying" ) );

		System.out.println(TelemetryUtils.getMeasuresAsString( reasoner ));
		
		assertEquals( 0.1, awfCC.getLowerBound(), 0.0001 );
		assertEquals( 0.5, awfCC.getUpperBound(), 0.0001 );

	}

	/*
	 * Check simple instance checking
	 */
	@Test
	public void testTightLexicographicEntailment6() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlexent_5.xml" );
		ProntoReasoner reasoner = new HSOptimizedLexReasoner( new PSATSolverImpl() );

		ConditionalConstraint tweetyCC = reasoner.membershipEntailment( 
				pkb,
				ATermUtils.makeTermAppl( URI_PREFIX + "Tweety" ),
				ATermUtils.makeTermAppl( URI_PREFIX + "FlyingObject" ) );

		System.out.println(TelemetryUtils.getMeasuresAsString( reasoner ));
		
		assertEquals( 0.28, tweetyCC.getLowerBound(), 0.0001 );
		assertEquals( 0.72, tweetyCC.getUpperBound(), 0.0001 );
	}

	/*
	 * Check advanced instance checking
	 */
	@Test
	public void testTightLexicographicEntailment7() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlexent_4.xml" );
		ProntoReasoner reasoner = new HSOptimizedLexReasoner( new PSATSolverImpl() );

		ConditionalConstraint lewisCC = reasoner.membershipEntailment( 
				pkb,
				ATermUtils.makeTermAppl( URI_PREFIX + "Lewis" ),
				ATermUtils.makeTermAppl( URI_PREFIX + "WingedFlying" ) );

		System.out.println(TelemetryUtils.getMeasuresAsString( reasoner ));
		
		assertEquals( 0.03, lewisCC.getLowerBound(), 0.0001 );
		assertEquals( 0.46, lewisCC.getUpperBound(), 0.0001 );
	}
	
	/**
	 * Membership entailment is supposed to check PABox consistency for that
	 * individual. And it must fail in this case
	 * @throws Exception
	 */
	@Test
	public void testNotAConsistent() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_membership_1.xml" );
		ProntoReasoner reasoner = new HSOptimizedLexReasoner( new PSATSolverImpl() );
		boolean result = false;

		try {
			
			reasoner.membershipEntailment( 
					pkb,
					ATermUtils.makeTermAppl( URI_PREFIX + "Lewis" ),
					ATermUtils.makeTermAppl( URI_PREFIX + "Penguin" ) );
			
		} catch( RuntimeException e ) {
			
			result = true;
		}

		System.out.println(TelemetryUtils.getMeasuresAsString( reasoner ));
		
		assertTrue(result);
	}

	
/*	@Test
	public void testTightLexicographicEntailmentTMP() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( "file:/C:/kl/manchester/pubs/2010/theses/pavel/KBs/Cadiag2/tlexent/repaired_1_PABOX_2.owl" );
		ProntoReasoner reasoner = Pronto.createReasoner();
		PTBox pabox = pkb.getPTBoxForIndividual( ATermUtils.makeTermAppl("http://www.cs.man.ac.uk/~klinovp/owlgen#individual") );

		ConditionalConstraint cc = reasoner.subsumptionEntailment( 
				pkb,
				ATermUtils.makeTermAppl( "http://www.owl.cs.manchester.ac.uk/cadiag.owl#S0383" ),
				ATermUtils.makeTermAppl( "http://www.owl.cs.manchester.ac.uk/cadiag.owl#D239" ) );


		System.out.println(cc);		
	}	*/
/*	
	@Test
	public void testTightLexicographicEntailmentTMP2() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( "http://www.cs.manchester.ac.uk/~klinovp/research/pshiq/sparrow_prob.rdf" );
		ProntoReasoner reasoner = new HSOptimizedLexReasoner( new PSATSolverImpl() );

		ConditionalConstraint tweetyFlies = reasoner.subsumptionEntailment( 
				pkb,
				ATermUtils.TOP,
				ATermUtils.makeTermAppl( URI_PREFIX + "FlyingObject_tweety" ) );

		assertEquals( 0.9, tweetyFlies.getLowerBound(), 0.0001 );
		assertEquals( 1, tweetyFlies.getUpperBound(), 0.0001 );
		
		ConditionalConstraint samDoesNotFly = reasoner.subsumptionEntailment( 
				pkb,
				ATermUtils.TOP,
				ATermUtils.makeTermAppl( URI_PREFIX + "FlyingObject_sam" ) );

		assertEquals( 0, samDoesNotFly.getLowerBound(), 0.0001 );
		assertEquals( 0.5, samDoesNotFly.getUpperBound(), 0.0001 );		
	}*/	
}
