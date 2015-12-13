package uk.ac.manchester.cs.pronto.events;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import uk.ac.manchester.cs.pronto.ProbKnowledgeBase;
import uk.ac.manchester.cs.pronto.io.KBStandaloneLoader;
import uk.ac.manchester.cs.pronto.lp.CCSetAnalyzerImpl2;
import uk.ac.manchester.cs.pronto.ProntoMainTestSuite;
import uk.ac.manchester.cs.pronto.zpartition.ZPartitioner;
import uk.ac.manchester.cs.pronto.zpartition.ZPartitionerCGImpl;

public class AfterComputeConflictGraphEventHandlerImplTest {

	private static final String	FILE_PREFIX	= "file:" + ProntoMainTestSuite.BASE + "zpartition/";
	
	@Test
	public void testHandleEvent1() throws Exception {
		
		ProbKnowledgeBase kb = new KBStandaloneLoader().load(FILE_PREFIX + "test_zp_1.xml" );
		ZPartitioner zper = new ZPartitionerCGImpl(new CCSetAnalyzerImpl2() );
		ReasoningEventHandler handler = new AfterComputeConflictGraphEventHandlerImpl();
		Map<EVENT_TYPES, List<ReasoningEventHandler>> handlerMap = 
			new HashMap<EVENT_TYPES, List<ReasoningEventHandler>>();
		
		handlerMap.put( EVENT_TYPES.AFTER_CONFLICT_GRAPH_COMPUTED, Arrays.asList(handler) );
		zper.setEventHandlers( handlerMap );
		
		zper.partition( kb.getPTBox() );
		//Check that something was serialized 
	}

}
