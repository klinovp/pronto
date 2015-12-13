/**
 * 
 */
package uk.ac.manchester.cs.pronto.constraints;

import java.util.Set;

import org.mindswap.pellet.KnowledgeBase;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;

/**
 * Determines set inclusion by taking into account that some CC can be "subsumed"
 * by others. In the simplest case (A|T)[l,1] is subsumed by all constraints (B|T)[l,1] where
 * B is a subclass of A
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class KBBasedCCSetInclusionQualifier extends BasicCCSetInclusionQualifier {

	protected KnowledgeBase m_kb;
	
	public KBBasedCCSetInclusionQualifier(KnowledgeBase kb) {
		
		m_kb = kb;
	}
	
	/**
	 * Determines if ccSet1 includes (or subsumes) ccSet2.
	 * It means that all models satisfying ccSet1 also satisfy ccSet2 under given classical KB
	 * 
	 * @param ccSet1
	 * @param ccSet2
	 * @return
	 */
	public boolean includes(Set<ConditionalConstraint> ccSet1, Set<ConditionalConstraint> ccSet2) {
		
		if (super.includes( ccSet1, ccSet2 )) {
			
			return true;
			
		} else {
			
			for (ConditionalConstraint cc : ccSet2) {
				
				if (!constraintContained( cc, ccSet1 )) {
					
					return false;
				}
			}
		}
		
		return true;
	}

	protected boolean constraintContained(ConditionalConstraint cc, Set<ConditionalConstraint> ccSet) {
		
		for( ConditionalConstraint constraint : ccSet ) {
			/*
			 * Equality implies isStronger 
			 */
			if( isStronger( constraint, cc ) ) {

				return true;
			}
		}

		return false;
	}
	
	protected boolean isStronger(ConditionalConstraint cc1, ConditionalConstraint cc2) {
		/*
		 * (A|B)[l1,u1] is stronger than (C|B)[l2,u2] iff 
		 * Subclass(A,C) and [l1,u1] >= [l2,u2]
		 * Informally, it just means that the first constraint places a stronger restriction
		 * on probabilistic models
		 */
		if (!cc1.getEvidence().equals( cc2.getEvidence() )) {
			
			return false;
			
		} else {
			
			if (cc1.getUncertaintyInterval().compareTo( cc2.getUncertaintyInterval() ) > 0 
					&& m_kb.isSubClassOf( cc1.getConclusion(), cc2.getConclusion() )) {
				
				return true;
			}
		}
		
		return false;
	}
}
