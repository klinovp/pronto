package uk.ac.manchester.cs.pronto.events;

import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.manchester.cs.pronto.PTBoxSuppData;
import uk.ac.manchester.cs.pronto.ProntoMainTestSuite;

public class BeforeComputeConflictGraphEventHandlerImplTest {

	private static final String	FILE_PREFIX	= ProntoMainTestSuite.BASE + "io/";	
	

	@Test
	public void testHandleEvent1() throws Exception {	
		
		BeforeComputeConflictGraphEventHandlerImpl handler = new BeforeComputeConflictGraphEventHandlerImpl();
		PTBoxSuppData suppData = new PTBoxSuppData();
		ReasoningEvent.SimpleEventImpl cgEvent = new ReasoningEvent.SimpleEventImpl(
						EVENT_TYPES.BEFORE_CONFLICT_GRAPH_COMPUTED );
						cgEvent.setParameters( new Object[] { "test_event", suppData } );
						
		handler.setFileLocation( FILE_PREFIX );
		handler.handleEvent( cgEvent );
		
		assertNotNull(suppData.getConflictGraph());
	}

}
