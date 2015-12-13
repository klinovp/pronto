package uk.ac.manchester.cs.pronto.io;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.mindswap.pellet.utils.ATermUtils;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.ProbKnowledgeBase;
import uk.ac.manchester.cs.pronto.constraints.CCSetAnalyzer;
import uk.ac.manchester.cs.pronto.constraints.ConflictGraph;
import uk.ac.manchester.cs.pronto.constraints.ConflictGraphImpl;
import uk.ac.manchester.cs.pronto.constraints.KBBasedCCSetInclusionQualifier;
import uk.ac.manchester.cs.pronto.lp.CCSetAnalyzerImpl2;
import uk.ac.manchester.cs.pronto.ProntoMainTestSuite;
import uk.ac.manchester.cs.pronto.util.CCUtils;

public class ConflictGraphXMLSerializerImplTest {

	private static final String	FILE_PREFIX	= "file:" + ProntoMainTestSuite.BASE + "lp/";
	
	private ConflictGraph createConflictGraph(String ontologyURI) throws Exception {
		
		ProbKnowledgeBase kb = new KBStandaloneLoader().load(ontologyURI );
		PTBox ptbox = kb.getPTBox();
		ConflictGraph cGraph = new ConflictGraphImpl(
				new KBBasedCCSetInclusionQualifier(ptbox.getClassicalKnowledgeBase()));
		CCSetAnalyzer ccAnalyzer = new CCSetAnalyzerImpl2();
		//Now loop over the constrains to find conflicting sets
		for (ConditionalConstraint cc : new HashSet<ConditionalConstraint>(ptbox.getDefaultConstraints())) {
		
			Set<ConditionalConstraint> hardConstraints = new HashSet<ConditionalConstraint>(2);
			
			hardConstraints.add(cc);
			
			if (!ATermUtils.TOP.equals(cc.getEvidence())) {
				
				hardConstraints.add(CCUtils.conceptVerificationConstraint(cc.getEvidence()));
			}
			
			cGraph.addConflicts( hardConstraints, ccAnalyzer.getMinimalUnsatSubsets(hardConstraints, ptbox));
		}
		
		return cGraph;
	}
	
	@Test
	public void testSerialize() throws Exception {

		ConflictGraph cg = createConflictGraph(FILE_PREFIX + "test_cg_1.xml");
		StringWriter writer = new StringWriter();
		ConflictGraphXMLSerializerImpl serializer = new ConflictGraphXMLSerializerImpl(writer); 
		
		serializer.serialize( cg );
		writer.flush();
		
		System.out.println(writer.getBuffer().toString());
	}

	
}
