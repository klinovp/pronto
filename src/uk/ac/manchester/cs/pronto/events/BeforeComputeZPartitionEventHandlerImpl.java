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
import uk.ac.manchester.cs.pronto.exceptions.EventHandlingException;
import uk.ac.manchester.cs.pronto.io.ZPartitionDeserializerSAXImpl;
import uk.ac.manchester.cs.pronto.zpartition.ZPartition;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * 16 Nov 2010
 */
public class BeforeComputeZPartitionEventHandlerImpl implements ReasoningEventHandler {

	Logger s_logger = Logger.getLogger(BeforeComputeConflictGraphEventHandlerImpl.class);
	
	private String m_location = null;
	private ZPartitionDeserializerSAXImpl m_deserializer = null;
	
	public BeforeComputeZPartitionEventHandlerImpl() {}

	public void setFileLocation(String path) {
		
		m_location = path;
	}
	
	/* 
	 */
	@Override
	public void handleEvent(ReasoningEvent event) throws EventHandlingException {

		String ontologyID = (String)event.getParameters()[0];
		PTBoxSuppData suppData = (PTBoxSuppData)event.getParameters()[1];
		ZPartition zp = null;
		
		ontologyID = "zp_" + (ontologyID.endsWith( ".xml" ) ? ontologyID : ontologyID + ".xml");
		
		try {
			File file = new File( m_location == null 
									? Constants.SERIALIZATION_FOLDER_PREFIX + ontologyID
									:  m_location + ontologyID);
			ZPartitionDeserializerSAXImpl deserializer = m_deserializer == null 
													? createDefaultDeserializer()
													: m_deserializer;
													
			zp = deserializer.deserialize( new FileReader(file) );
			
		} catch( FileNotFoundException e ) {
			
			s_logger.info( "Serialized z-partition not found: ", e );
			
		} catch( IOException e ) {
			
			s_logger.error( "Can't deserialize z-partition: ", e );
			
			throw new EventHandlingException(e);
		}
		
		suppData.setZPartition( zp );
	}

	private ZPartitionDeserializerSAXImpl createDefaultDeserializer() throws IOException {

		return new ZPartitionDeserializerSAXImpl();
	}	
}
