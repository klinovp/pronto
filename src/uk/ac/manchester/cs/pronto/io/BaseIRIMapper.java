/**
 * 
 */
package uk.ac.manchester.cs.pronto.io;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * 19 Aug 2010
 */
public class BaseIRIMapper implements OWLOntologyIRIMapper {

	private String m_baseIRI = null;
	
	public BaseIRIMapper(String baseIRI) {
		
		m_baseIRI = baseIRI;
	}
	
	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
	
		if (ontologyIRI != null && ontologyIRI.toString().startsWith("local:")) {
			
			String str = ontologyIRI.toString();
			
			return IRI.create( m_baseIRI + "/" + str.substring( str.indexOf( "local:" ) + 6 ) );
			
		} else {
			
			return null;
		}
	}
}
