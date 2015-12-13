package uk.ac.manchester.cs.pronto.constraints;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;
import org.mindswap.pellet.utils.ATermUtils;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.ProbKnowledgeBase;
import uk.ac.manchester.cs.pronto.ProntoMainTestSuite;
import uk.ac.manchester.cs.pronto.io.KBStandaloneLoader;

public class KBBasedSetInclusionQualifierTest extends TestCase {

	private static final String	FILE_PREFIX	= "file:" + ProntoMainTestSuite.BASE + "constraints/";
	private static final String	URI_PREFIX	= ProntoMainTestSuite.URI_PREFIX;
	
	
	@Test
	public void testIncludes() throws Exception {
		
		ProbKnowledgeBase pkb = new KBStandaloneLoader().load(FILE_PREFIX + "test_incl_qualifier_1.xml" );
		Set<ConditionalConstraint> ccSet1 = new HashSet<ConditionalConstraint>();
		Set<ConditionalConstraint> ccSet2 = new HashSet<ConditionalConstraint>();
		CCSetInclusionQualifier q = new KBBasedCCSetInclusionQualifier(pkb.getPTBox().getClassicalKnowledgeBase());
		
		ccSet1.add( new ConditionalConstraint( ATermUtils.makeTermAppl( URI_PREFIX + "Penguin" ),
				ATermUtils.makeTermAppl(  URI_PREFIX + "FlyingObject" ) , 0.0, 0.05 ) );
		ccSet1.add( new ConditionalConstraint( ATermUtils.TOP,
				ATermUtils.makeTermAppl(  URI_PREFIX + "Penguin" ), 1, 1 ) );
		ccSet1.add( new ConditionalConstraint( ATermUtils.TOP,
				ATermUtils.makeTermAppl( URI_PREFIX + "Penguin" ) , 0, 1 ) );
		
		
		ccSet2.add( new ConditionalConstraint( ATermUtils.makeTermAppl(  URI_PREFIX + "Penguin" ),
				ATermUtils.makeTermAppl( URI_PREFIX + "FlyingObject" ), 0.0, 0.05 ) );
		ccSet2.add( new ConditionalConstraint( ATermUtils.TOP,
				ATermUtils.makeTermAppl( URI_PREFIX + "ArcticPenguin" ), 1, 1 ) );
		
		assertTrue(q.includes( ccSet2, ccSet1 ));
	}

}
