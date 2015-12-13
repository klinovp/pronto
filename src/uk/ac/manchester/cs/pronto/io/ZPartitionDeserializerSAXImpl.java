/**
 * 
 */
package uk.ac.manchester.cs.pronto.io;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.zpartition.ZPartition;
import uk.ac.manchester.cs.pronto.zpartition.ZPartitionImpl;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * 16 Nov 2010
 */
public class ZPartitionDeserializerSAXImpl {

	private Logger	m_logger = Logger.getLogger( ZPartitionDeserializerSAXImpl.class );

	protected static enum HANDLER_STATE {INITIAL, SET_CONCLUSION, SET_EVIDENCE, CONSTRAINT_SET};

	public ZPartition deserialize(Reader reader) throws IOException {

		try {

			XMLReader parser = XMLReaderFactory.createXMLReader();
			Handler handler = new Handler();

			parser.setContentHandler( handler );
			parser.parse( new InputSource( reader ) );

			return handler.getZPartition();

		} catch( SAXException e ) {

			m_logger.error( e );

			return null;
		}
	}

	class Handler implements ContentHandler {

		private ZPartitionImpl m_zp			= null;
		private Set<ConditionalConstraint>	m_ccSet			= null;
		private HANDLER_STATE				m_state			= HANDLER_STATE.INITIAL;
		private StringBuilder				m_buffer		= new StringBuilder();
		private String						m_evidenceURI	= null;
		private String						m_conclusionURI	= null;
		private int							m_rank			= 0;//Specificity of the current constraint set
		private double[]					m_probInterval	= new double[2];

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {

			switch ( m_state ) {

			case SET_CONCLUSION:

				m_buffer.append( ch, start, length );
				break;

			case SET_EVIDENCE:

				m_buffer.append( ch, start, length );
				break;
			}
		}

		@Override
		public void endDocument() throws SAXException {
		}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {

			name = uri + name;
			
			if( Names.CC_ELEMENT_NAME.equals( name ) ) {

				m_ccSet.add( new ConditionalConstraint( m_evidenceURI, m_conclusionURI,	m_probInterval[0], m_probInterval[1] ) );

			} else if (Names.CC_SET_ELEMENT_NAME.equals( name ) ) {
				
				m_zp.add( m_ccSet, m_rank );
				
			} else if( Names.EVIDENCE_ELEMENT_NAME.equals( name ) ) {

				m_evidenceURI = m_buffer.toString();
				m_buffer.setLength( 0 );
				m_state = HANDLER_STATE.INITIAL;

			} else if( Names.CONCLUSION_ELEMENT_NAME.equals( name ) ) {

				m_conclusionURI = m_buffer.toString();
				m_buffer.setLength( 0 );
				m_state = HANDLER_STATE.INITIAL;
			}
		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {}

		@Override
		public void setDocumentLocator(Locator locator) {}

		@Override
		public void skippedEntity(String name) throws SAXException {}

		@Override
		public void startDocument() throws SAXException {}

		@Override
		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {

			name = uri + name;
			
			if( Names.ZPARTITION_ROOT_ELEMENT_NAME.equals( name ) ) {

				m_zp = new ZPartitionImpl();
				m_rank = 0;

			} else if( Names.CC_SET_ELEMENT_NAME.equals( name ) ) {

				m_ccSet = new HashSet<ConditionalConstraint>();
				m_rank--;

			} else if( Names.EVIDENCE_ELEMENT_NAME.equals( name ) ) {

				m_state = HANDLER_STATE.SET_EVIDENCE;

			} else if( Names.CONCLUSION_ELEMENT_NAME.equals( name ) ) {

				m_state = HANDLER_STATE.SET_CONCLUSION;

			} else if( Names.PROBABILITY_ELEMENT_NAME.equals( name ) ) {

				m_probInterval[0] = Double.valueOf( atts.getValue( Names.LOWER_PROBABILITY_ATTR_NAME ) );
				m_probInterval[1] = Double.valueOf( atts.getValue( Names.UPPER_PROBABILITY_ATTR_NAME ) );
			}
		}

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {}

		protected ZPartition getZPartition() {

			return m_zp;
		}
	}
}