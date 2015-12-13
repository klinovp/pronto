package uk.ac.manchester.cs.pronto.index;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.cache.IndexCache;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * 4 Jun 2010
 */
public class ConceptTypeSetGeneratorImpl implements IndexSetGenerator {

	private final OWLDataFactory m_factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
	private final OWLReasonerFactory m_reasonerFactory = new PelletReasonerFactory();
	private Random m_rnd = new Random();
	private int m_limit = -1;

	
	@Override
	public IndexSet generate(PTBox ptbox) {

		Set<OWLClass> probSig = getProbabilisticSignature(ptbox.getDefaultConstraints());
		//We need to create an OWLReasoner based on the KB instance
		OWLReasoner reasoner = m_reasonerFactory.createReasoner( ptbox.getClassicalOntology() );
		
		reasoner.precomputeInferences( new InferenceType[] {InferenceType.CLASS_HIERARCHY} );
		
		return new ConceptTypeSet(generateConceptTypes( new ArrayList<OWLClass>(probSig), reasoner, m_limit ));
	}	
	
	private Set<OWLClass> getProbabilisticSignature(Set<ConditionalConstraint> defaultConstraints) {

		Set<OWLClass> sig = new HashSet<OWLClass>();
		
		for (ConditionalConstraint cc : defaultConstraints)  {
			
			sig.add( m_factory.getOWLClass( IRI.create(cc.getEvidence().toString()) ) );
			sig.add( m_factory.getOWLClass( IRI.create(cc.getConclusion().toString()) ) );
		}
		
		return sig;
	}

	public Set<ConceptType> generateConceptTypes(List<OWLClass> atoms, OWLReasoner reasoner, int conceptTypeNumber) {
		
		Set<ConceptType> ConceptTypes = new HashSet<ConceptType>(Math.max( 0, conceptTypeNumber));

		generate(ConceptTypes, atoms, reasoner, conceptTypeNumber, new ConceptType(), 0, false);
		
		return ConceptTypes;
	}
	/*
	 * Recursive function
	 */
	private boolean generate(	Set<ConceptType> conceptTypes,
								List<OWLClass> atoms,
								OWLReasoner reasoner,
								int conceptTypeNumber,
								ConceptType partialConceptType,
								int atomIndex,
								boolean satNeeded) {

		OWLObjectIntersectionOf exprCopy = partialConceptType.getConjunctiveExpr();
		
		if ((m_limit >= 0 && conceptTypes.size() >= conceptTypeNumber)) return true;
		
		if (!satNeeded || reasoner.isSatisfiable( exprCopy )) {
			
			if (atomIndex < atoms.size()) {
			
				if (m_rnd.nextBoolean()) {
					
					boolean posSAT = positiveBranch(conceptTypes, atoms, reasoner, conceptTypeNumber, partialConceptType, atomIndex, true);
					negativeBranch(conceptTypes, atoms, reasoner, conceptTypeNumber, partialConceptType, atomIndex, posSAT);
					
				} else {
					
					boolean negSAT = negativeBranch(conceptTypes, atoms, reasoner, conceptTypeNumber, partialConceptType, atomIndex, true);
					positiveBranch(conceptTypes, atoms, reasoner, conceptTypeNumber, partialConceptType, atomIndex, negSAT);
				}
			
			} else {

				conceptTypes.add( partialConceptType.clone() );
			}
			
			return true;
			
		} else {
			
			return false;
		}
	}

	private boolean negativeBranch(Set<ConceptType> conceptTypes,
									List<OWLClass> atoms,
									OWLReasoner reasoner,
									int conceptTypeNumber,
									ConceptType partialConceptType,
									int atomIndex,
									boolean satNeeded) {
		
		boolean result = false;
		OWLClass nextAtom = atoms.get( atomIndex );
		OWLObjectIntersectionOf exprCopy = partialConceptType.getConjunctiveExpr();
		//Conjunction with a negative literal
		if (!partialConceptType.containsPositive( nextAtom )) {
			
			partialConceptType.addNegative( nextAtom );
			
			result = generate(	conceptTypes,
								atoms,
								reasoner,
								conceptTypeNumber,
								partialConceptType,
								atomIndex + 1,
								satNeeded);
			
			partialConceptType.removeNegative( nextAtom );
			partialConceptType.setConjunctiveExpr( exprCopy );
		}
		
		return result;
	}
	
	private boolean positiveBranch(	Set<ConceptType> conceptTypes,
									List<OWLClass> atoms,
									OWLReasoner reasoner,
									int conceptTypeNumber,
									ConceptType partialConceptType,
									int atomIndex,
									boolean satNeeded) {

		boolean result = false;
		OWLClass nextAtom = atoms.get( atomIndex );
		OWLObjectIntersectionOf exprCopy = partialConceptType.getConjunctiveExpr();
		// Conjunction with a positive literal
		if (!partialConceptType.containsNegative( nextAtom )) {
			
			partialConceptType.addPositive( nextAtom );
			
			result = generate(	conceptTypes,
								atoms,
								reasoner,
								conceptTypeNumber,
								partialConceptType,
								atomIndex + 1,
								satNeeded);
			
			partialConceptType.removePositive( nextAtom );
			partialConceptType.setConjunctiveExpr( exprCopy );
		}		
		
		return result;
	}

	@Override
	public void useCache(IndexCache cache) {}	
	
	public void setTermNumber(int size) {
		
		m_limit = size;
	}
}