package uk.ac.manchester.cs.pronto.index;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mindswap.pellet.utils.ATermUtils;

public class ConjunctiveIndexTermTest {

	@Test
	public void testConjunctCollectionOfATermAppl() {
		
		ConjunctiveIndexTerm term = new ConjunctiveIndexTerm();
		
		term.conjunct( ATermUtils.makeTermAppl("test") );
		term.conjunct( ATermUtils.makeNot( ATermUtils.makeTermAppl("test") ));
		term.conjunct(	ATermUtils.makeNot(
							ATermUtils.makeAnd(
									ATermUtils.makeTermAppl("test"),
									ATermUtils.makeTermAppl("not_test"))));
		
		assertEquals( 1, term.getPositiveConjuncts().size() );
		assertEquals( 2, term.getNegativeConjuncts().size() );
	}

}
