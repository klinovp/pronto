/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.utils.ATermUtils;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.cache.IndexCacheImpl;
import uk.ac.manchester.cs.pronto.util.CCUtils;

/**
 * <p>Title: PTBoxImpl</p>
 * 
 * <p>Description: 
 *          constraints
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class PTBoxImpl implements PTBox {

	Logger								s_logger		= Logger.getLogger( PTBoxImpl.class );

	private String						m_id;
	private KnowledgeBase				m_kb;
	private Set<ConditionalConstraint>	m_defaultCC;

	protected boolean					m_bSatisfiable	= false;
	protected boolean					m_bChanged		= true;
	protected boolean					m_bPreprocessed	= false;

	protected PTBoxSuppData				m_suppData		= new PTBoxSuppData();
	//First step towards migrating to a pure OWL API solution
	protected OWLOntology				m_ontology		= null;
	
	private Map<String, OWLClassExpression> m_nameMap = null;
	private Map<ATermAppl, ATermAppl> m_nameMapAterm = new HashMap<ATermAppl, ATermAppl>();

	public PTBoxImpl() {}
	
	//TODO Get rid of KnowledgeBase here (and move to the OWL API completely)
	public PTBoxImpl(KnowledgeBase kb, OWLOntology ontology, Collection<ConditionalConstraint> ccSet) {
		
		m_kb = kb;
		m_ontology = ontology;

		if (null != ccSet) {
			
			m_defaultCC = new HashSet<ConditionalConstraint>(ccSet);
		}
	}	

	public String getID() {
		
		return m_id;
	}
	
	public void setID(String id) {
		
		m_id = id;
	}	
	
	public KnowledgeBase getClassicalKnowledgeBase() {
		
		return m_kb;
	}

	public void setClassicalKnowledgeBase(KnowledgeBase kb) {

		m_kb = kb;
		m_bPreprocessed = false;
		preprocess();
	}
	
	public Set<ConditionalConstraint> getDefaultConstraints() {
		return m_defaultCC;
	}

	public void setDefaultConstraints(Set<ConditionalConstraint> ccSet) {

		if (null != ccSet) {
			
			m_defaultCC = new HashSet<ConditionalConstraint>(ccSet);
		}
	}

	public boolean addDefaultConstraint(ConditionalConstraint cc) {
		
		boolean changed = false;
		
		if (null == m_defaultCC) {
			
			m_defaultCC = new HashSet<ConditionalConstraint>();
		}
		
		if (null != cc && !m_defaultCC.contains( cc )) {
			
			changed = m_defaultCC.add(cc);
		}
		
		if (changed) {
			setChanged( true );
		}
		
		return changed;
	}
	
	public Set<ConditionalConstraint> addDefaultConstraints(Set<ConditionalConstraint> ccSet) {

		Set<ConditionalConstraint> added = new HashSet<ConditionalConstraint>();
		
		if( null != ccSet ) {

			for( ConditionalConstraint cc : ccSet ) {

				if ( addDefaultConstraint( cc ) ) added.add( cc );
			}
		}
		
		return added;
	}
	
	public void removeDefaultConstraint(ConditionalConstraint cc) {

		if (null != m_defaultCC) {
			
			if (m_defaultCC.remove(cc)) {
				setChanged(true);
			}
		}
	}

	public void removeDefaultConstraints(Collection<ConditionalConstraint> ccSet) {

		if (null != m_defaultCC && null != ccSet) {
			
			if (m_defaultCC.removeAll(ccSet)) {
				setChanged(true);	
			}
		}
	}	
	
	protected PTBoxImpl newPTBox() {
		
		return new PTBoxImpl();
	}
	
	protected void copyFrom(PTBoxImpl ptbox, boolean copyConstraints) {
		
		m_kb = ptbox.getClassicalKnowledgeBase();
		
		if (copyConstraints && null != ptbox.getDefaultConstraints()) {
			
			m_defaultCC = new HashSet<ConditionalConstraint>(ptbox.getDefaultConstraints());
		}
		
		setChanged(ptbox.getChanged());
		
		m_suppData = ptbox.getSupplementalData().clone();
		m_ontology = ptbox.getClassicalOntology();
	}
	
	public PTBoxImpl clone() {
		
		return clone(true);
	}
	
	public PTBoxImpl clone(boolean copyConstraints) {

		PTBoxImpl ptbox = newPTBox();
		
		ptbox.copyFrom(this, copyConstraints);
		
		return ptbox;
	}
	
	public void reset() {}
	
	public boolean getChanged() {
		
		return m_bChanged;
	}
	
	public void setChanged(boolean changed) {
		
		m_bChanged = changed;
	}
	
	public void preprocess() {
		
		if (!m_bPreprocessed && null != m_defaultCC && null != m_kb) {
		
			Set<ATermAppl> sig = getProbabilisticSignature(m_defaultCC);

			enrichSignature(sig, m_nameMap, true, m_kb);
			m_kb.classify();			
			/*
			 * Verify that all classes in constraints are correct
			 */
			verifyConstraints();
			/*
			 * Initialize supplemental data, in particular the cache
			 */
			m_suppData.computeMatrices( sig, m_kb );
			m_suppData.setCache( new IndexCacheImpl() );
			m_bPreprocessed = true;
		}
	}
	
	private Set<ATermAppl> getProbabilisticSignature(	Set<ConditionalConstraint> ccSet) {
		
		Set<ATermAppl> classSet = new HashSet<ATermAppl>(ccSet.size() * 2);

		for (ConditionalConstraint cc : ccSet) {
			
			if (!cc.getEvidence().equals( ATermUtils.TOP )) classSet.add( cc.getEvidence() );
			
			classSet.add( cc.getConclusion() );
		}		
		
		return classSet;
	}
	/*
	 * Enriches signature by adding classes that appear in abbreviated expressions
	 * Autogenerated names are optionally removed
	 */
	private void enrichSignature(	Set<ATermAppl> classSet,
									Map<String, OWLClassExpression> nameMap,
									boolean removeAutoGenNames,
									KnowledgeBase kb) {

		nameMap = nameMap == null ? new HashMap<String, OWLClassExpression>() : nameMap;
		
		if( removeAutoGenNames ) {
			//Remove auto generated names from the signature
			for( Iterator<ATermAppl> classIter = classSet.iterator(); classIter.hasNext(); ) {

				ATermAppl clazz = classIter.next();

				if( nameMap.containsKey( clazz.toString() ) ) classIter.remove();
			}
		}
		//Add classes that appear in abbreviated expressions
		for (String name : nameMap.keySet()) {
			
			OWLClassExpression expr = nameMap.get( name );
			
			switch (expr.getClassExpressionType()) {
			
			case OBJECT_UNION_OF:
				
			case OBJECT_INTERSECTION_OF:
				
				OWLNaryBooleanClassExpression nary = (OWLNaryBooleanClassExpression) expr;
				
				for (OWLClassExpression exprPart : nary.getOperands()) {
					
					classSet.add( convertToATerm(exprPart, kb) );
				}
				
			default:
					
				classSet.add( convertToATerm(expr, kb) );
			}
		}
	}	
	
	private ATermAppl convertToATerm(OWLClassExpression expr, KnowledgeBase kb) {

		return expr.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF
			? CCUtils.classExprToATerm( expr.getComplementNNF(), kb )
			: CCUtils.classExprToATerm( expr, kb );
	}
	
	public PTBoxSuppData getSupplementalData() {
		
		return m_suppData;
	}
	
	private void verifyConstraints() {
		
		for (ConditionalConstraint cc : getDefaultConstraints()) {
			
			if (cc.getLowerBound() < 0 || cc.getLowerBound() > 1
					|| cc.getUpperBound() < 0 || cc.getUpperBound() > 1) {
				
				s_logger.fatal( "Invalid probability interval: " + cc );
				/*
				 * Is it okay to throw unchecked exception here? I usually don't
				 * like them but in this case there's nothing that can be done
				 * by caller to handle that.
				 */
				throw new RuntimeException("Invalid constraint: " + cc);
			}
			
			validateClass(cc.getConclusion());		
			validateClass(cc.getEvidence());
		}
	}
	
	private void validateClass(ATermAppl clazz) {
		
		if (ATermUtils.isPrimitive( clazz ) && !m_kb.getAllClasses().contains( clazz )) {
		
			s_logger.error( "Class " + clazz + " is not declared" );
		}			
	}
	
	public OWLOntology getClassicalOntology() {
		
		return m_ontology;
	}
	
	public void setClassicalOntology(OWLOntology ontology) {
		
		m_ontology = ontology;
	}
	
	@Override
	public Map<String, OWLClassExpression> getAutoGeneratedClasses() {
		
		return m_nameMap;
	}
	
	public Map<ATermAppl, ATermAppl> getATermNameExpressionMap() {
		
		return m_nameMapAterm;
	}
	
	@Override
	public void setAutoGeneratedClasses(Map<String, OWLClassExpression> nameMap) {
		
		m_nameMap = nameMap;
		//Convert to ATerms (FIXME get rid of it soon)
		m_nameMapAterm.clear();
		
		for (Map.Entry<String, OWLClassExpression> nameEntry : nameMap.entrySet()) {
			
			m_nameMapAterm.put( ATermUtils.makeTermAppl( nameEntry.getKey() ), CCUtils.classExprToATerm( nameEntry.getValue(), m_kb ) );
		}
	}
	/*
	 * Translates auto generated class names (if any)
	 */
	public ConditionalConstraint translateConstraint(ConditionalConstraint cc) {
	
		ATermAppl evidence = translateClass(cc.getEvidence());
		ATermAppl conclusion = translateClass(cc.getConclusion());
		
		if (evidence != cc.getEvidence() || conclusion != cc.getConclusion()) {
			
			return new ConditionalConstraint(evidence, conclusion, cc.getLowerBound(), cc.getUpperBound());
			
		} else return cc;
	}
	
	
	private ATermAppl translateClass(ATermAppl clazz) {

		if (m_nameMap != null && m_nameMap.containsKey( clazz.toString() )) {
			//Auto generated class name, need to look up its definition in the KB
			Collection<ATermAppl> axioms = m_kb.getTBox().getAxioms( clazz );
	
			if (axioms.size() != 1) {
				//Auto generated names should appear exactly once (in the definition)
				String msg = "Zero or multiple definitions for auto generated class " + clazz;
				s_logger.error( msg );
				
				throw new RuntimeException(msg);
				
			} else {
				
				return (ATermAppl)axioms.iterator().next().getArgument( 1 );
			}
			
		} else return clazz;

	}
}
