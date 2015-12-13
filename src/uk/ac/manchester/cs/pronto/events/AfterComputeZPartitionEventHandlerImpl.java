/**
 * 
 */
package uk.ac.manchester.cs.pronto.events;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.pronto.Constants;
import uk.ac.manchester.cs.pronto.exceptions.EventHandlingException;
import uk.ac.manchester.cs.pronto.io.ZPartitionXMLSerializerImpl;
import uk.ac.manchester.cs.pronto.zpartition.ZPartition;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class AfterComputeZPartitionEventHandlerImpl implements ReasoningEventHandler {

	Logger s_logger = Logger.getLogger(AfterComputeZPartitionEventHandlerImpl.class);
	
	private ZPartitionXMLSerializerImpl m_serializer = null;
	
	public AfterComputeZPartitionEventHandlerImpl(){}
	
	public AfterComputeZPartitionEventHandlerImpl(ZPartitionXMLSerializerImpl serializer) {
		
		m_serializer = serializer;
	}	
	
	/**
	 * @param event
	 * @throws EventHandlingException
	 */
	@Override
	public void handleEvent(ReasoningEvent event) throws EventHandlingException {
		
		ZPartition zp = (ZPartition) event.getParameters()[0];
		
		if (null != zp) {
			//Handle the event, serialize the graph
			try {
				
				if (m_serializer == null) {
					
					m_serializer = createDefaultSerializer((String)event.getParameters()[1]);
				}
				
				m_serializer.serialize( zp );
				
			} catch( IOException e ) {
				// Misconfiguration
				s_logger.error( "Event handler misconfigured", e );
				
				throw new EventHandlingException(e);
				
			} catch (RuntimeException e) {
				
				s_logger.error( "Error during z-partition serialization: " + e );
				
				throw new EventHandlingException(e);
			}
		} else {
			//Do nothing, may only write to the log
			s_logger.info( "No z-partition to serialize" );
		}
	}
	
	private ZPartitionXMLSerializerImpl createDefaultSerializer(String id) throws IOException {

		File dir = new File(Constants.SERIALIZATION_FOLDER_PREFIX);
		String fName = Constants.SERIALIZATION_FOLDER_PREFIX + "zp_" + id;
		
		fName = fName.endsWith( ".xml" ) ? fName : fName + ".xml";
		
		File file = new File( fName );

		dir.mkdirs();
		
		if (file.exists()) file.delete();
		
		file.createNewFile();

		return new ZPartitionXMLSerializerImpl( new FileWriter( file ) );
	}
}