/**
 * 
 */
package uk.ac.manchester.cs.pronto.io;

import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.pronto.ProbKnowledgeBase;
import uk.ac.manchester.cs.pronto.exceptions.OntologyLoadingException;

/**
 * <p>Title: ProntoLoader</p>
 * 
 * <p>Description: 
 *  Interface for probabilistic DL KB loaders
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public interface ProntoLoader {
	
	public ProbKnowledgeBase load(String uri) throws OntologyLoadingException;
	public ProbKnowledgeBase load(OWLOntology ontology) throws OntologyLoadingException;
	
	public String getClassicalOntologyURI();
}
