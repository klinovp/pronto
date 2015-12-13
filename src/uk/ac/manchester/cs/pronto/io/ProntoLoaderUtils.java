/**
 * 
 */
package uk.ac.manchester.cs.pronto.io;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.mindswap.pellet.utils.ATermUtils;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.Constants;
import uk.ac.manchester.cs.pronto.util.CCUtils;

/**
 * <p>Title: ProntoLoaderUtils</p>
 * 
 * <p>Description: 
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class ProntoLoaderUtils {

	static Logger		s_logger	= Logger.getLogger( ProntoLoaderUtils.class );	
	public static String	INTERVAL_SEPARATOR			= ";";	
	
	/**
	 * Loads conditional constraints from an ontology with annotated axioms
	 * 
	 * @param ontology
	 * @param signature 
	 * @param declAxioms Used to return declaration axioms for auto-generated class names
	 * @param iriPrefix IRI prefix for auto-generated class names
	 * @param raxList
	 * @return
	 */
	public static Set<ConditionalConstraint> loadDefaultConstraintsFromOWL(
			OWLOntology ontology,
			Map<String, OWLClassExpression> nameMap,
			Set<OWLEntity> signature,
			List<RemoveAxiom> raxList, String iriPrefix,
			OWLOntologyManager manager) {

		Set<ConditionalConstraint> ccSet = new HashSet<ConditionalConstraint>();
		//Begin with generic (default) subclass-of axioms
		for( OWLAxiom axiom : ontology.getAxioms( AxiomType.SUBCLASS_OF ) ) {
			for( OWLAnnotation annotation : axiom.getAnnotations() ) {

				if( Constants.CERTAINTY_ANNOTATION_URI.equals(annotation.getProperty().getIRI().toURI() ) ) {

					OWLSubClassOfAxiom sbAxiom = (OWLSubClassOfAxiom) axiom;
					String subClassIRI = generateClassName(sbAxiom.getSubClass(), nameMap, iriPrefix);
					String superClassIRI = generateClassName(sbAxiom.getSuperClass(), nameMap, iriPrefix);
					ConditionalConstraint cc = newConstraint(subClassIRI, superClassIRI, annotation.getValue().toString());

					signature.addAll( sbAxiom.getSubClass().getClassesInSignature() );
					signature.addAll( sbAxiom.getSuperClass().getClassesInSignature() );
					
					if( null != cc ) {

						ccSet.add( cc );

						if( null != raxList ) {
							raxList.add( new RemoveAxiom( ontology, axiom ) );
						}
					}
				}
			}
		}

		return ccSet;
	}

	/*
	 * Generates a class name for a class expression and add the corresponding
	 * ontology changes to the list ontologyChanges
	 */
	private static String generateClassName(	OWLClassExpression classExpr,
												Map<String, OWLClassExpression> nameMap,
												String iriPrefix) {

		if (!classExpr.isAnonymous()) {
			
			return classExpr.asOWLClass().getIRI().toString();
			
		} else {
			
			String className = iriPrefix + System.nanoTime();
			
			nameMap.put( className, classExpr );
			
			s_logger.debug( className + " = " + classExpr );
			
			return className;
		}
	}

	private static ConditionalConstraint newConstraint(	String evClass, String cnClass, String certainty) {

		double lower = 0, upper = 0;
		int langTagPos = certainty.indexOf( '@' );
		
		if (langTagPos >= 0) {
			
			certainty = certainty.substring( 0, langTagPos );
		}
		
		if (certainty.startsWith( "\"" ) && certainty.endsWith( "\"" )) {
			
			certainty = certainty.substring( 1, certainty.length() - 1 );
		}

		StringTokenizer st = new StringTokenizer( certainty, INTERVAL_SEPARATOR );		
		
		if( 2 != st.countTokens() ) {
			
			s_logger.error( "Invalid probability interval: " + certainty );
			
			return null;
		}
		else {

			try {

				lower = Double.parseDouble( st.nextToken() );
				upper = Double.parseDouble( st.nextToken() );
				
			} catch (NumberFormatException nfe) {

				s_logger.error( "Invalid probability interval: " + certainty, nfe );
				
				return null;
				
			} finally {
				
				if (lower > upper || lower < 0 || upper > 1) {
					
					s_logger.error( "Invalid probability interval: " + certainty );
					
					return null;
				}
			}
			
			return new ConditionalConstraint( 	CCUtils.uriToATerm( evClass ),
												CCUtils.uriToATerm( cnClass ),
												lower, upper );
		}
	}

	/*
	 * Load conditional constraints for all probabilistic individuals in the
	 * ontology. Simply put, for any probabilistic individual we store a set of
	 * conditional constraints
	 */
	public static Map<ATermAppl, Set<ConditionalConstraint>>
								loadConcreteConstraintsFromOWL(	OWLOntology ontology,
																Map<String, OWLClassExpression> nameMap,
																Set<OWLEntity> signature,
																List<RemoveAxiom> raxList,
																String iriPrefix,
																OWLOntologyManager manager) {

		Map<ATermAppl, Set<ConditionalConstraint>> ccMap = new HashMap<ATermAppl, Set<ConditionalConstraint>>();
		ConditionalConstraint cc = null;
		Set<ConditionalConstraint> ccSet = null;
		OWLDataFactory factory = manager.getOWLDataFactory();

		for( OWLAxiom axiom : ontology.getAxioms( AxiomType.CLASS_ASSERTION ) ) {
			
			for( OWLAnnotation annotation : axiom.getAnnotations() ) {

				if( Constants.CERTAINTY_ANNOTATION_URI.equals( annotation.getProperty().getIRI().toURI() ) ) {

					OWLClassAssertionAxiom caAxiom = (OWLClassAssertionAxiom) axiom;
					String classNameIRI = generateClassName(caAxiom.getClassExpression(), nameMap, iriPrefix);
					OWLIndividual individ = caAxiom.getIndividual();
					ATermAppl indTerm = ATermUtils.makeTermAppl( individ.asOWLNamedIndividual().getIRI().toURI().toString() );

					ccSet = (Set<ConditionalConstraint>) ccMap.get( indTerm );
					cc = newConstraint( factory.getOWLThing().getIRI().toString(), classNameIRI, annotation.getValue().toString() );
					signature.addAll( caAxiom.getClassExpression().getClassesInSignature() );

					if( null != cc ) {

						if( null == ccSet ) {

							ccSet = new HashSet<ConditionalConstraint>();
						}

						ccSet.add( cc );
						ccMap.put( indTerm, ccSet );

						if( null != raxList ) {

							raxList.add( new RemoveAxiom( ontology, axiom ) );
						}
					}
				}
			}
		}

		return ccMap;
	}
}
