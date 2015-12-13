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
import uk.ac.manchester.cs.pronto.constraints.ConflictGraph;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class ConflictGraphXMLSerializerImpl implements ConflictGraphSerializer {

	private XMLWriter m_xmlWriter = null;
	private CCSerializer m_ccSerializer = null;
	
	public ConflictGraphXMLSerializerImpl(Writer writer) {
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
	 * @param cg Conflict graph to be serialized
	 * @throws IOException
	 */
	@Override
	public void serialize(ConflictGraph cg) throws IOException {
		
		m_xmlWriter.startDocument( Names.CONFLICT_GRAPH_ROOT_ELEMENT_NAME );
		
		for (Set<ConditionalConstraint> ccSet : cg.getConstraintSets()) {
			
			m_xmlWriter.writeStartElement( Names.CONFLICT_MAPPING_ELEMENT_NAME );
			m_xmlWriter.writeStartElement( Names.CC_SET_ELEMENT_NAME );
			serializeConstraintSet( ccSet );
			m_xmlWriter.writeEndElement();
			//Now serialize conflict sets
			for (Set<ConditionalConstraint> conflictSet : cg.getConflictSets( ccSet )) {

				m_xmlWriter.writeStartElement( Names.CONFLICT_SET_ELEMENT_NAME );
				serializeConstraintSet( conflictSet );
				m_xmlWriter.writeEndElement();
			}
			
			m_xmlWriter.writeEndElement();
		}
		
		m_xmlWriter.endDocument();
	}

	private void serializeConstraintSet(Set<ConditionalConstraint> ccSet) throws IOException {
		
		for (ConditionalConstraint cc : ccSet) m_ccSerializer.serialize( cc );
	}
}
