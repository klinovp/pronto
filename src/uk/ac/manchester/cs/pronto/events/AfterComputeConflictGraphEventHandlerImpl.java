/**
 * 
 */
package uk.ac.manchester.cs.pronto.events;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.pronto.Constants;
import uk.ac.manchester.cs.pronto.constraints.ConflictGraph;
import uk.ac.manchester.cs.pronto.exceptions.EventHandlingException;
import uk.ac.manchester.cs.pronto.io.ConflictGraphSerializer;
import uk.ac.manchester.cs.pronto.io.ConflictGraphXMLSerializerImpl;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Aug 30, 2009
 */
public class AfterComputeConflictGraphEventHandlerImpl implements ReasoningEventHandler {

	Logger s_logger = Logger.getLogger(AfterComputeConflictGraphEventHandlerImpl.class);
	
	private ConflictGraphSerializer m_serializer = null;
	
	public AfterComputeConflictGraphEventHandlerImpl() {}
	
	public AfterComputeConflictGraphEventHandlerImpl(ConflictGraphSerializer serializer) {
		
		m_serializer = serializer;
	}
	/* 
	 * Handles the event by serializing the graph to disk
	 */
	@Override
	public void handleEvent(ReasoningEvent event) throws EventHandlingException {
		//Get conflict graph and see if it has been successfully computed
		ConflictGraph cg = (ConflictGraph) event.getParameters()[0];
		
		if (null != cg) {
			//Handle the event, serialize the graph
			try {
				
				if (m_serializer == null) {
					
					m_serializer = createDefaultSerializer((String)event.getParameters()[1]);
				}
				
				m_serializer.serialize( cg );
				
			} catch( IOException e ) {
				// Misconfiguration
				s_logger.error( "Event handler misconfigured", e );
				
				throw new EventHandlingException(e);
				
			} catch (RuntimeException e) {
				
				s_logger.error( "Error during conflict graph serialization: " + e );
				
				throw new EventHandlingException(e);
			}
		} else {
			//Do nothing, may only write to the log
			s_logger.info( "No conflict graph to serialize" );
		}
	}
	
	private ConflictGraphSerializer createDefaultSerializer(String id) throws IOException {

		File dir = new File(Constants.SERIALIZATION_FOLDER_PREFIX);
		String fName = Constants.SERIALIZATION_FOLDER_PREFIX + "cg_" + id;
		
		fName = fName.endsWith( ".xml" ) ? fName : fName + ".xml";
		
		File file = new File( fName );

		dir.mkdirs();
		
		if (file.exists()) file.delete();
		
		file.createNewFile();

		return new ConflictGraphXMLSerializerImpl(new FileWriter(file ) );
	}
}