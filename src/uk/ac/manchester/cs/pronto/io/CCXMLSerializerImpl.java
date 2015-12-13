/**
 * 
 */
package uk.ac.manchester.cs.pronto.io;

import java.io.IOException;
import java.io.Writer;

import org.coode.xml.XMLWriter;
import org.coode.xml.XMLWriterFactory;
import org.coode.xml.XMLWriterNamespaceManager;
import org.semanticweb.owlapi.vocab.Namespaces;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.Constants;
import uk.ac.manchester.cs.pronto.util.CCUtils;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * This class using OWL API (namely org.coode.xml.* classes)
 */
public class CCXMLSerializerImpl implements CCSerializer {

	private XMLWriter m_xmlWriter = null;
	
	public CCXMLSerializerImpl(Writer writer) {
		//Instantiate the COODE XML writer
		XMLWriterNamespaceManager nsm = new XMLWriterNamespaceManager(Constants.PRONTO_DEFAULT_URI);
		
        nsm.setPrefix("xsd", Namespaces.XSD.toString());
        nsm.setPrefix("rdf", Namespaces.RDF.toString());
        nsm.setPrefix("rdfs", Namespaces.RDFS.toString());
        nsm.setPrefix("xml", Namespaces.XML.toString());		
        nsm.setPrefix("pronto", Constants.PRONTO_DEFAULT_URI);
        
        m_xmlWriter = XMLWriterFactory.getInstance().createXMLWriter(writer, nsm, null);
	}
	
	protected CCXMLSerializerImpl(XMLWriter writer) {
		
		m_xmlWriter = writer;
	}
	
	/**
	 * @param cc
	 * @param writer
	 * @throws IOException
	 */
	@Override
	public void serialize(ConditionalConstraint cc) throws IOException {
		
		//OWLXMLObjectRenderer render = new OWLXMLObjectRenderer(m_xmlWriter);
		//It is assumed that the document has already started so we may just write the element
		m_xmlWriter.writeStartElement( Names.CC_ELEMENT_NAME );
		//TODO We should serialize class expressions properly (say, in OWL/XML format)
			m_xmlWriter.writeStartElement( Names.EVIDENCE_ELEMENT_NAME );
				m_xmlWriter.writeTextContent( CCUtils.aTermToString(cc.getEvidence()) );
			m_xmlWriter.writeEndElement();
			m_xmlWriter.writeStartElement( Names.CONCLUSION_ELEMENT_NAME );
				m_xmlWriter.writeTextContent( CCUtils.aTermToString(cc.getConclusion()) );
			m_xmlWriter.writeEndElement();			
			m_xmlWriter.writeStartElement( Names.PROBABILITY_ELEMENT_NAME );
				m_xmlWriter.writeAttribute( Names.LOWER_PROBABILITY_ATTR_NAME, String.valueOf( cc.getLowerBound() ) );
				m_xmlWriter.writeAttribute( Names.UPPER_PROBABILITY_ATTR_NAME, String.valueOf( cc.getUpperBound() ) );
			m_xmlWriter.writeEndElement();		
		m_xmlWriter.writeEndElement();		
	}
}
