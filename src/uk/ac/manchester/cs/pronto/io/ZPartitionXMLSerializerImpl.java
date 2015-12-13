/**
 * 
 */
package uk.ac.manchester.cs.pronto.io;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.coode.xml.XMLWriter;
import org.coode.xml.XMLWriterFactory;
import org.coode.xml.XMLWriterNamespaceManager;
import org.semanticweb.owlapi.vocab.Namespaces;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.Constants;
import uk.ac.manchester.cs.pronto.zpartition.ZPartition;

/**
 * Serializes z-partitions to XML
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class ZPartitionXMLSerializerImpl {

	private XMLWriter m_xmlWriter = null;
	private CCSerializer m_ccSerializer = null;
	
	public ZPartitionXMLSerializerImpl(Writer writer) {
		//Instantiate the COODE XML writer
		XMLWriterNamespaceManager nsm = new XMLWriterNamespaceManager(Constants.PRONTO_DEFAULT_URI);
		
        nsm.setPrefix("xsd", Namespaces.XSD.toString());
        nsm.setPrefix("rdf", Namespaces.RDF.toString());
        nsm.setPrefix("rdfs", Namespaces.RDFS.toString());
        nsm.setPrefix("xml", Namespaces.XML.toString());		
        nsm.setPrefix("pronto", Constants.PRONTO_DEFAULT_URI);
        
        m_xmlWriter = XMLWriterFactory.getInstance().createXMLWriter(writer, nsm, "");
        m_ccSerializer = new CCXMLSerializerImpl(m_xmlWriter);
	}
	
	/**
	 * @param zp z-partition to be serialized
	 * @throws IOException
	 */
	public void serialize(ZPartition zp) throws IOException {
		
		m_xmlWriter.startDocument( Names.ZPARTITION_ROOT_ELEMENT_NAME );
		
		for (java.util.Iterator<Set<ConditionalConstraint>> zIter = zp.partitionIterator(); zIter.hasNext();) {
			
			Set<ConditionalConstraint> ccSet = zIter.next();
			
			m_xmlWriter.writeStartElement( Names.CC_SET_ELEMENT_NAME );
			serializeConstraintSet( ccSet );
			m_xmlWriter.writeEndElement();
		}
		
		m_xmlWriter.endDocument();
	}

	private void serializeConstraintSet(Set<ConditionalConstraint> ccSet) throws IOException {
		
		for (ConditionalConstraint cc : ccSet) m_ccSerializer.serialize( cc );
	}	
}
