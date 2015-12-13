/**
 * 
 */
package uk.ac.manchester.cs.pronto.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.pronto.Constants;
import uk.ac.manchester.cs.pronto.PTBoxSuppData;
import uk.ac.manchester.cs.pronto.constraints.ConflictGraph;
import uk.ac.manchester.cs.pronto.exceptions.EventHandlingException;
import uk.ac.manchester.cs.pronto.io.ConflictGraphDeserializer;
import uk.ac.manchester.cs.pronto.io.ConflictGraphDeserializerSAXImpl;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Aug 31, 2009
 */
public class BeforeComputeConflictGraphEventHandlerImpl implements ReasoningEventHandler {

	Logger s_logger = Logger.getLogger(BeforeComputeConflictGraphEventHandlerImpl.class);
	
	private String m_location = null;
	private ConflictGraphDeserializer m_deserializer = null;
	
	public BeforeComputeConflictGraphEventHandlerImpl() {}
	
	public BeforeComputeConflictGraphEventHandlerImpl(ConflictGraphDeserializer deserializer) {
		
		m_deserializer = deserializer;
	}

	public void setFileLocation(String path) {
		
		m_location = path;
	}
	
	/* 
	 */
	@Override
	public void handleEvent(ReasoningEvent event) throws EventHandlingException {

		String ontologyID = (String)event.getParameters()[0];
		PTBoxSuppData suppData = (PTBoxSuppData)event.getParameters()[1];
		ConflictGraph cg = null;
		
		ontologyID = "cg_" + (ontologyID.endsWith( ".xml" ) ? ontologyID : ontologyID + ".xml");
		
		try {
			File file = new File( m_location == null 
									? Constants.SERIALIZATION_FOLDER_PREFIX + ontologyID
									:  m_location + ontologyID);
			ConflictGraphDeserializer deserializer = m_deserializer == null 
													? createDefaultDeserializer()
													: m_deserializer;
													
			cg = deserializer.deserialize( new FileReader(file) );
			
		} catch( FileNotFoundException e ) {
			
			s_logger.error( "Can't find serialized conflict graph: ", e );
			
			throw new EventHandlingException(e);
			
		} catch( IOException e ) {
			
			s_logger.error( "Can't deserialize conflict graph: ", e );
			
			throw new EventHandlingException(e);
		}
		
		suppData.setConflictGraph( cg );
	}

	private ConflictGraphDeserializer createDefaultDeserializer() throws IOException {

		return new ConflictGraphDeserializerSAXImpl();
	}	
}
