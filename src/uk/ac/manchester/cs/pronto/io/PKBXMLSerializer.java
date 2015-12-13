/**
 * 
 */
package uk.ac.manchester.cs.pronto.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.mindswap.pellet.KnowledgeBase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import aterm.ATermAppl;

import com.clarkparsia.pellet.owlapiv3.ConceptConverter;
import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.Constants;
import uk.ac.manchester.cs.pronto.PABox;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.ProbKnowledgeBase;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Serializes PTBox and PABox constraints in OWL/XML format.
 * Returns the result as string
 * 
 */
public class PKBXMLSerializer implements PKBSerializer {

	public static final int OWL_XML_FORMAT = 0;
	public static final int RDF_XML_FORMAT = 1;
	
	protected URI m_uri = null;
	protected OWLOntologyFormat m_format = new OWLXMLOntologyFormat();
	
	public void setOutputFormat(int format) {
		
		/*
		 * TODO use enums
		 */
		switch (format) {
		
		case OWL_XML_FORMAT:
			
			m_format = new OWLXMLOntologyFormat();
			break;
			
		case RDF_XML_FORMAT:
			
			m_format = new RDFXMLOntologyFormat();
			break;
			
		default:
			
			throw new RuntimeException("Unsupported output format");
		}
	}
	
	public void setOntologyURI(String uri) {
	
		m_uri = URI.create(uri);
	}
	
	private OWLOntology buildOWLOntology(ProbKnowledgeBase pkb, OWLOntologyManager manager) {
		
		OWLOntology ontology = null;
		
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		try {
			
			OWLImportsDeclaration importAxiom = factory.getOWLImportsDeclaration(pkb.getPTBox().getClassicalOntology().getOntologyID().getOntologyIRI() );			

			ontology = manager.createOntology( IRI.create(m_uri) );
			manager.applyChange( new AddImport(ontology, importAxiom));
			
			
		} catch( OWLOntologyCreationException e ) {

			e.printStackTrace();
			/*
			 * TODO throw out some checked exception
			 */
			return null;
			
		} catch( OWLOntologyChangeException e ) {

			e.printStackTrace();
			/*
			 * TODO throw out some checked exception
			 */
			return null;
		}
		
		addPTBoxConstraints(ontology, pkb.getPTBox(), manager, factory);
		
		if (pkb.getPABox() != null) {
			
			addPABoxConstraints(ontology, pkb.getPABox(), pkb.getPTBox().getClassicalKnowledgeBase(), manager, factory);	
		}
		
		return ontology;
	}
	

	public void serializeToFile(ProbKnowledgeBase pkb, File file) throws IOException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = buildOWLOntology( pkb, manager );
		
		try {
			
			manager.saveOntology( ontology, m_format, IRI.create(file.toURI())  );
			
		} catch( UnknownOWLOntologyException e ) {

			e.printStackTrace();
			
		} catch( OWLOntologyStorageException e ) {

			e.printStackTrace();
		}
		
	}

	public String serialize(ProbKnowledgeBase pkb) throws IOException {

		StringDocumentTarget strTarget = new StringDocumentTarget();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = buildOWLOntology( pkb, manager );
		
		try {
			
			manager.saveOntology( ontology, m_format, strTarget  );
			
		} catch( UnknownOWLOntologyException e ) {

			e.printStackTrace();
			
			return "";
			
		} catch( OWLOntologyStorageException e ) {

			e.printStackTrace();
			
			return "";
		}
		
		return strTarget.toString();
	}
	
	protected void addPTBoxConstraints(OWLOntology ontology, PTBox ptbox,
											OWLOntologyManager manager, OWLDataFactory  factory) {
		
		ConceptConverter converter = new ConceptConverter(ptbox.getClassicalKnowledgeBase(), factory); 
		
		for (ConditionalConstraint cc : ptbox.getDefaultConstraints()) {

			OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty( IRI.create(Constants.CERTAINTY_ANNOTATION_URI ));
			OWLAnnotationValue annValue = factory.getOWLStringLiteral( cc.getLowerBound() + ";" + cc.getUpperBound() );
			OWLAnnotation annotation = factory.getOWLAnnotation( annProp, annValue );	
			OWLClassExpression clsEv = (OWLClassExpression)converter.convert( cc.getEvidence() );
			OWLClassExpression clsCn = (OWLClassExpression)converter.convert( cc.getConclusion() );
			OWLAxiom axiom = factory.getOWLSubClassOfAxiom( clsEv, clsCn, Collections.singleton( annotation ) );
			
			try {
				
				manager.applyChange( new AddAxiom(ontology, axiom) );
			
			} catch( OWLOntologyChangeException e ) {
				
				e.printStackTrace();
			}
		}
	}
	
	protected void addPABoxConstraints(OWLOntology ontology, PABox pabox, KnowledgeBase kb,
											OWLOntologyManager manager, OWLDataFactory  factory) {
		
		ConceptConverter converter = new ConceptConverter(kb, factory);
		
		for (Map.Entry<ATermAppl, Set<ConditionalConstraint>> entry : pabox.getConstraintsMap().entrySet()) {
			
			for (ConditionalConstraint cc : entry.getValue()) {

				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty( IRI.create( Constants.CERTAINTY_ANNOTATION_URI ));
				OWLAnnotationValue annValue = factory.getOWLStringLiteral( cc.getLowerBound() + ";" + cc.getUpperBound() );
				OWLAnnotation annotation = factory.getOWLAnnotation( annProp, annValue );	
				OWLIndividual indiv = factory.getOWLNamedIndividual( IRI.create( entry.getKey().getName()) );
				OWLClassExpression clsCn = (OWLClassExpression)converter.convert( cc.getConclusion() );
				OWLAxiom axiom = factory.getOWLClassAssertionAxiom( clsCn, indiv, Collections.singleton( annotation ) );
				
				try {
					
					manager.applyChange( new AddAxiom(ontology, axiom) );
					
				} catch( OWLOntologyChangeException e ) {
					
					e.printStackTrace();
				}
			}
		}
	}
	
	
}
