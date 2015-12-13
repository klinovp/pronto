package uk.ac.manchester.cs.pronto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mindswap.pellet.utils.ATermUtils;

import uk.ac.manchester.cs.pronto.io.KBStandaloneLoader;

/**
 * <p>Title: PSATSolverImplTest</p>
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
public class PSATSolverImplTest {

	private static final String	FILE_PREFIX	= "file:" + ProntoMainTestSuite.BASE + "psat/";
	private static final String	URI_PREFIX	= ProntoMainTestSuite.URI_PREFIX;
	
	/*
	 * Check satisfiability of an inconsistent KB (yes-yes, not a lapsus, this
	 * sort of things happens with P-SHOQ...)
	 */
	@Test
	public void testIsSatisfiable2() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load(FILE_PREFIX + "test_g_inconsistent.xml" );
		PSATSolverImpl solver = new PSATSolverImpl();
		boolean result = solver.isPTBoxSatisfiable( pkb.getPTBox() );

		assertTrue( result );
	}

	/*
	 * Complement of testTLogEnt3. Verifies that the constraint entailed by
	 * testTLogEnt3 is satisfied by the PTBox
	 */
	@Test
	public void testIsSatisfiable3() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_sat_2.xml" );
		PSATSolverImpl solver = new PSATSolverImpl();
		boolean result = solver.isPTBoxSatisfiable( pkb.getPTBox() );

		assertTrue( result );
	}

	/*
	 * Check the case of complete ignorance. If there nothing connecting
	 * concepts C and D, then (D|C)[1,1] (as well as (D|C)[0,0]) should NOT be
	 * in conflict with the KB
	 */
	@Test
	public void testIsSatisfiable4() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_sat_3.xml" );		
		PSATSolverImpl solver = new PSATSolverImpl();
		boolean result = solver.isPTBoxSatisfiable( pkb.getPTBox() );

		assertTrue( result );
	}

	/*
	 * Check that a:B and Pr(B)=0 are NOT in conflict.
	 */
	@Test
	public void testIsSatisfiable5() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_sat_4.xml" );		
		PSATSolverImpl solver = new PSATSolverImpl();
		boolean result = solver.isPTBoxSatisfiable( pkb.getPTBox() );

		assertTrue( result );
	}
	
	@Test
	public void testIsSatisfiable6() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_sat_5.xml" );
		PSATSolverImpl solver = new PSATSolverImpl();
		boolean result = solver.isPTBoxSatisfiable( pkb.getPTBox() );
		
		assertTrue( result );
	}
	
	/*
	 * Note that the KB would be satisfiable if (Bird|Thing)[l,u], l > 0 was
	 * removed. This corresponds to assigning the probability of 0 to Bird
	 */
	@Test
	public void testIsUnSatisfiable1() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_unsat_3.xml" );
		PSATSolverImpl solver = new PSATSolverImpl();
		boolean result = solver.isPTBoxSatisfiable( pkb.getPTBox() );

		assertFalse( result );
	}

	/*
	 * Complement with testTLogEnt3. Verifies that any constraint which is
	 * beyond the interval entailed by testTLogEnt3 is NOT satisfied by the
	 * PTBox
	 */
	@Test
	public void testIsUnsatisfiable2() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_unsat_2.xml" );		
		PSATSolverImpl solver = new PSATSolverImpl();
		boolean result = solver.isPTBoxSatisfiable( pkb.getPTBox() );

		assertFalse( result );
	}

	/*
	 * Vacuous. All that is explicitly asserted must be trivially entailed 
	 */
	@Test
	public void testTLogEnt1() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlogent_1.xml" );		
		PSATSolverImpl solver = new PSATSolverImpl();
		ConditionalConstraint birdCC = solver.tightLogicalEntailment( pkb.getPTBox(), ATermUtils
				.makeTermAppl( URI_PREFIX + "Bird" ) );

		assertEquals( 0.5, birdCC.getLowerBound(), 0.0001 );
		assertEquals( 0.7, birdCC.getUpperBound(), 0.0001 );
	}

	/*
	 * Same as previous one but satisfiability of Bird is asserted through the
	 * constraint on its subclass Penguin
	 * 
	 * NOTE This test will fail if a module is extracted for the probabilistic signature.
	 * This is so because the entailment class is not from the signature.
	 */
	@Test
	public void testTLogEnt2() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlogent_2.xml" );		
		PSATSolverImpl solver = new PSATSolverImpl();
		ConditionalConstraint birdCC = solver.tightLogicalEntailment( pkb.getPTBox(), ATermUtils
				.makeTermAppl( URI_PREFIX + "Bird" ) );

		assertEquals( 0.5, birdCC.getLowerBound(), 0.0001 );
		assertEquals( 1.0, birdCC.getUpperBound(), 0.0001 );
	}

	/*
	 * Compute satisfiability of WingedFlying knowing degrees of satisfiability
	 * of Winged and Flying separately
	 */
	@Test
	public void testTLogEnt3() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlogent_3.xml" );		
		PSATSolverImpl solver = new PSATSolverImpl();
		ConditionalConstraint wfCC = solver.tightLogicalEntailment( pkb.getPTBox(), ATermUtils
				.makeTermAppl( URI_PREFIX + "WingedFlying" ) );

		assertEquals( 0.3, wfCC.getLowerBound(), 0.0001 );
		assertEquals( 0.7, wfCC.getUpperBound(), 0.0001 );
	}

	/*
	 * Check entailment under complete ignorance
	 */
	@Test
	public void testTLogEnt4() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlogent_4.xml" );
		PSATSolverImpl solver = new PSATSolverImpl();
		ConditionalConstraint wfCC = solver.tightLogicalEntailment( pkb.getPTBox(), ATermUtils
				.makeTermAppl( URI_PREFIX + "Bird" ) );

		assertEquals( 0.0, wfCC.getLowerBound(), 0.0001 );
		assertEquals( 1.0, wfCC.getUpperBound(), 0.0001 );
	}

	@Test
	public void testTLogEnt5() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlogent_5.xml" );		
		PSATSolverImpl solver = new PSATSolverImpl();
		ConditionalConstraint wfCC = solver.tightLogicalEntailment( pkb.getPTBox(), ATermUtils
				.makeTermAppl( URI_PREFIX + "WingedFlying" ) );
		
		assertEquals( 0.23, wfCC.getLowerBound(), 0.0001 );
		assertEquals( 0.73, wfCC.getUpperBound(), 0.0001 );
	}
	
	@Test
	public void testTLogEnt7() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( FILE_PREFIX + "test_tlogent_7.xml" );		
		PSATSolverImpl solver = new PSATSolverImpl();
		ConditionalConstraint wfCC = solver.tightLogicalEntailment( pkb.getPTBox(), ATermUtils
				.makeTermAppl( URI_PREFIX + "WingedFlying" ) );
		
		assertEquals( 0.03, wfCC.getLowerBound(), 0.0001 );
		assertEquals( 0.46, wfCC.getUpperBound(), 0.0001 );
	}	

	@Test
	public void testIsUnSatisfiableWithoutUnconditionals1() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( "file:test_data/tmp/test_tmp_3.xml" );
		PSATSolverImpl solver = new PSATSolverImpl();
		boolean result = solver.isPTBoxSatisfiable( pkb.getPTBox() );

		assertFalse( result );
	}	
	
	@Test
	public void testIsUnSatisfiableWithoutUnconditionals2() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( "file:test_data/tmp/test_tmp_4.xml" );
		PSATSolverImpl solver = new PSATSolverImpl();
		boolean result = solver.isPTBoxSatisfiable( pkb.getPTBox() );

		assertFalse( result );
	}	
	

}
