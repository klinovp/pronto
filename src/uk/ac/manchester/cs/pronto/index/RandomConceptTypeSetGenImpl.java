/**
 * 
 */
package uk.ac.manchester.cs.pronto.index;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.graph.Node;
import uk.ac.manchester.cs.graph.Tree;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.cache.IndexCache;
import uk.ac.manchester.cs.pronto.util.SetUtils;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Generates random subset of concept types. Number of concept types has to be specified.
 * This class must not be used to generate all concept types
 */
public class RandomConceptTypeSetGenImpl implements IndexSetGenerator {

	//private Logger	m_logger = Logger.getLogger( RandomConceptTypeSetGenImpl.class );
	
	private int m_cTypeNumber = 0;
	private Random m_rnd = new Random();
	
	private int s_sat = 0;
	private int s_unsat = 0;
	private long m_total = 0;
	private long m_sat = 0;
	
	private final OWLDataFactory m_factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
	private final OWLReasonerFactory m_reasonerFactory = new PelletReasonerFactory();
	
	@Override
	public IndexSet generate(PTBox ptbox) {
		
		Set<OWLClass> probSig = getProbabilisticSignature(ptbox.getDefaultConstraints());
		//We need to create an OWLReasoner based on the KB instance
		OWLReasoner reasoner = m_reasonerFactory.createReasoner( ptbox.getClassicalOntology() );
		Map<OWLClass, Set<OWLClass>> subsumerMap = new HashMap<OWLClass, Set<OWLClass>>();
		Map<OWLClass, Set<OWLClass>> disjointMap = new HashMap<OWLClass, Set<OWLClass>>();
		
		//reasoner.precomputeInferences( new InferenceType[] {InferenceType.CLASS_HIERARCHY} );
		//Uncomment after Pellet's bug #485 has been fixed
		//http://clark-parsia.trac.cvsdude.com/pellet-devel/ticket/485
		//reasoner.prepareReasoner();
		computeMatrices( reasoner, probSig, subsumerMap, disjointMap );
		
		System.out.println( "Matrices computed" );
		
		return new ConceptTypeSet(generateConceptTypes( probSig, reasoner, m_cTypeNumber, subsumerMap, disjointMap ));
	}	
	
	
	public Set<ConceptType> generateConceptTypes(	Set<OWLClass> atoms,
													OWLReasoner reasoner,
													int cTypeNumber,
													Map<OWLClass, Set<OWLClass>> subsumers,
													Map<OWLClass, Set<OWLClass>> disjoints) {

		Set<ConceptType> cTypes = new HashSet<>(cTypeNumber);
		//Initialize the tree
		Tree<ConceptTypeNode> cTypeTree = new Tree<>(new ConceptTypeNode(new ConceptType(), 0));
		int duplicates = -1;
		
		while (cTypes.size() <  cTypeNumber && !cTypeTree.getRoot().getObject().isClosed() && duplicates < cTypes.size()) {
			//Do a random run down the tree
			Node<ConceptTypeNode> node = cTypeTree.getRoot();
			m_total = System.currentTimeMillis();
			m_sat = 0;
			
			for (OWLClass atom : atoms) {
				//Current node shouldn't ever be closed. Each open node should have at least
				//one open child. Thus if the root is open, then there should be a sequence of
				//open nodes all the way down
				if (node.isLeaf()) {
					//Need to expand the node
					expandNode(node, atom, reasoner, subsumers, disjoints);
				}
				
				node = pickRandomOpenChild( node, reasoner );
			}
			
			if (!cTypes.add( node.getObject().getConceptType() )) {
				duplicates++;
			}
			
			System.out.println( "Concept type #" + cTypes.size());// + ": " + node.getObject().getConceptType().getConjunctiveExpr() );
			System.out.println( "Total time: " + (System.currentTimeMillis() - m_total) + ", SAT time: " + m_sat);
			//FIXME It may be possible to generate the same concept type twice
			//but the chances are very low if the tree is large
		}
		
		System.out.println( "SAT: " + s_sat );
		System.out.println( "UNSAT: " + s_unsat );
		
		return cTypes;
	}
	

	private void expandNode(Node<ConceptTypeNode> node,
							OWLClass atom,
							OWLReasoner reasoner,
							Map<OWLClass, Set<OWLClass>> subsumerMap,
							Map<OWLClass, Set<OWLClass>> disjointMap) {
		
		ConceptTypeNode cTypeNode = node.getObject();
		int level = cTypeNode.getLevel();
		ConceptType currentCType = cTypeNode.getConceptType();
		ConceptType posChild = currentCType.clone();
		ConceptType negChild = currentCType.clone();
		Set<OWLClass> currSubsumers = cTypeNode.getSubsumers();
		Set<OWLClass> currDisjoints = cTypeNode.getDisjoints();
		Set<OWLClass> clazzSubsumers = subsumerMap.get( atom ); 
		Set<OWLClass> clazzDisjoints = disjointMap.get( atom );

		if (currDisjoints.contains( atom ) || SetUtils.intersects( currSubsumers, clazzDisjoints )) {
			//Positive branch is closed, negative is open
			negChild.addNegative( atom );

			Set<OWLClass> newSubsumers = new HashSet<OWLClass>(currSubsumers);
			Set<OWLClass> newDisjoints = new HashSet<OWLClass>(currDisjoints);
			
			newDisjoints.add( atom );
			
			ConceptTypeNode negNode = new ConceptTypeNode(negChild, level + 1, newSubsumers, newDisjoints);
			
			negNode.setStatus( STATUS.OPEN );
			node.addChild(negNode);
			
		} else if (currSubsumers.contains( atom ) /*|| SetUtils.intersects( currDisjoints, clazzSubsumers )*/) {
			//Positive branch is open, negative is closed
			posChild.addPositive( atom );
			
			Set<OWLClass> newSubsumers = new HashSet<OWLClass>(currSubsumers);
			Set<OWLClass> newDisjoints = new HashSet<OWLClass>(currDisjoints);
			
			newSubsumers.addAll( clazzSubsumers );
			newDisjoints.addAll( clazzDisjoints );
			
			ConceptTypeNode posNode = new ConceptTypeNode(posChild, level + 1, newSubsumers, newDisjoints);
			
			posNode.setStatus( STATUS.OPEN );
			node.addChild(posNode);			
			
		} else {
			//Both branches are unknown
			posChild.addPositive( atom );
			negChild.addNegative( atom );
			
			Set<OWLClass> posSubsumers = new HashSet<OWLClass>(currSubsumers);
			Set<OWLClass> posDisjoints = new HashSet<OWLClass>(currDisjoints);
			Set<OWLClass> negSubsumers = new HashSet<OWLClass>(currSubsumers);
			Set<OWLClass> negDisjoints = new HashSet<OWLClass>(currDisjoints);			

			posSubsumers.addAll( clazzSubsumers );
			posDisjoints.addAll( clazzDisjoints );	
			negDisjoints.add( atom );
		
			//Add children, each will have status 'UNKNOWN' for the moment (we do SAT checks lazily)
			node.addChild( new ConceptTypeNode(posChild, level + 1, posSubsumers, posDisjoints) );
			node.addChild( new ConceptTypeNode(negChild, level + 1, negSubsumers, negDisjoints) );			
		}
	}
	
	/*
	 * Tries to randomly choose an open child while removing unsatisfiable ones
	 */
	private Node<ConceptTypeNode> pickRandomOpenChild(Node<ConceptTypeNode> node, OWLReasoner reasoner) {
		
		List<Node<ConceptTypeNode>> children = node.getChildren();
		Node<ConceptTypeNode> rndChild = null;
		int index = 0;
		
		while(!children.isEmpty() && rndChild == null) {
			
			Node<ConceptTypeNode> child = children.get( index = m_rnd.nextInt( children.size() ) );
			
			switch (child.getObject().getStatus()) {
			
			case OPEN: 
				//Great. We found it.
				rndChild = child;
				break;
			
			case CLOSED: 
				//Some previously closed node (we may even remove it)
				//children.remove( index );
				break;
			
			case UNKNOWN: 
				//Need to check SAT to find out if its open or closed
				//TODO May use cache or class hierarchy here
				long ts = System.currentTimeMillis();
				
				if (reasoner.isSatisfiable( child.getObject().getConceptType().getConjunctiveExpr() )) { 
					
					child.getObject().setStatus( STATUS.OPEN );
					rndChild = child;
					s_sat++;
					
				} else {
					//Prune
					children.remove( index );
					//If the other child is still UNKNOWN, mark it as open
					//this may save a SAT check later
					if (children.size() == 1) {
						
						child = children.get( 0 );
						
						if (child.getObject().getStatus().equals( STATUS.UNKNOWN )) {
							
							child.getObject().setStatus( STATUS.OPEN );
						}
						
					} else {
						
						throw new RuntimeException("Invalid tree!");
					}
					s_unsat++;
				}
				
				m_sat += (System.currentTimeMillis() - ts);
			}
		}
		
		return rndChild;
	}


	@Override
	public void setTermNumber(int size) {
		
		m_cTypeNumber = size;
	}


	@Override
	public void useCache(IndexCache cache) {}
	
	
	private Set<OWLClass> getProbabilisticSignature(Set<ConditionalConstraint> defaultConstraints) {

		Set<OWLClass> sig = new HashSet<OWLClass>();
		
		for (ConditionalConstraint cc : defaultConstraints)  {
			
			sig.add( m_factory.getOWLClass( IRI.create(cc.getEvidence().toString()) ) );
			sig.add( m_factory.getOWLClass( IRI.create(cc.getConclusion().toString()) ) );
		}
		
		return sig;
	}
	
	private void computeMatrices(	OWLReasoner reasoner,
									Set<OWLClass> classes,
									Map<OWLClass, Set<OWLClass>> subsumers,
									Map<OWLClass, Set<OWLClass>> disjoints) {
		
		
		for (OWLClass clazz : classes) {
			subsumers.put( clazz, reasoner.getSuperClasses( clazz, false ).getFlattened() );
			disjoints.put( clazz, reasoner.getDisjointClasses( clazz ).getFlattened() );
		}
	}
}

class ConceptTypeNode {
	
	private ConceptType m_cType = null;
	private Set<OWLClass> m_subsumers = new HashSet<OWLClass>();
	private Set<OWLClass> m_disjoints = new HashSet<OWLClass>();
	private STATUS m_status = STATUS.UNKNOWN;
	private int m_level = 0;
	
	ConceptTypeNode(ConceptType cType, int level) {
		
		m_level = level;
		m_cType = cType;
	}
	
	ConceptTypeNode(ConceptType cType, int level, Set<OWLClass> subsumers, Set<OWLClass> disjoints) {
		
		this(cType, level);
		
		m_subsumers = subsumers;
		m_disjoints = disjoints;
	}	
	
	boolean isClosed() {
		
		return m_status == STATUS.CLOSED;
	}
	
	boolean isOpen() {
		
		return m_status == STATUS.OPEN;
	}	
	
	boolean isKnown() {
		
		return m_status != STATUS.UNKNOWN;
	}	
	
	STATUS getStatus() {
		
		return m_status;
	}
	
	void setStatus(STATUS status) {
		
		m_status = status;
	}
	
	ConceptType getConceptType() {
		
		return m_cType;
	}
	
	int getLevel() {
		
		return m_level;
	}
	
	public String toString() {
		
		return m_status + ": " + m_cType.getConjunctiveExpr();
	}
	
	public Set<OWLClass> getSubsumers() {
		
		return m_subsumers;
	}
	
	public Set<OWLClass> getDisjoints() {
		
		return m_disjoints;
	}	
}

enum STATUS {OPEN, CLOSED, UNKNOWN}