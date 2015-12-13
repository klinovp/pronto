/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;

/**
 * <p>Title: PABoxImpl</p>
 * 
 * <p>Description: 
 *          the powerset of conditional constraints
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class PABoxImpl implements PABox {

	private Map<ATermAppl, Set<ConditionalConstraint>>	m_concreteCC;

	public PABoxImpl() {}
	
	public PABoxImpl(Map<ATermAppl, Set<ConditionalConstraint>> ccMap) {
		
		m_concreteCC = ccMap;		
	}
	
	public void setConstraintsMap(Map<ATermAppl, Set<ConditionalConstraint>> ccMap) {

		m_concreteCC = ccMap;
	}

	public Map<ATermAppl, Set<ConditionalConstraint>> getConstraintsMap() {

		return m_concreteCC;
	}

	public Set<ATermAppl> getProbabilisticIndividuals() {

		return null == m_concreteCC
			? null
			: m_concreteCC.keySet();
	}

	public Set<ConditionalConstraint> getConstraintsForIndividual(URI individual) {

		return getConstraintsForIndividual( ATermUtils.makeTermAppl( individual.toString() ) );
	}

	public Set<ConditionalConstraint> getConstraintsForIndividual(ATermAppl individual) {

		return null == m_concreteCC
			? null
			: m_concreteCC.get( individual );
	}

	public void setConstraintsForIndividual(ATermAppl individual, Set<ConditionalConstraint> ccSet) {

		if( null == m_concreteCC ) {
			m_concreteCC = new HashMap<ATermAppl, Set<ConditionalConstraint>>();
		}

		m_concreteCC.put( individual, ccSet );
	}

	public void addConstraintForIndividual(ATermAppl individual, ConditionalConstraint cc) {

		Set<ConditionalConstraint> ccSet = null;

		if( null == m_concreteCC ) {
			m_concreteCC = new HashMap<ATermAppl, Set<ConditionalConstraint>>( 1 );
		}

		if( null == (ccSet = m_concreteCC.get( individual )) ) {

			ccSet = new HashSet<ConditionalConstraint>( 1 );
		}

		ccSet.add( cc );
		m_concreteCC.put( individual, ccSet );
	}

	public void addConstraintsForIndividual(ATermAppl individual, Set<ConditionalConstraint> ccSet) {

		Set<ConditionalConstraint> existingSet = null;

		if( null != ccSet ) {
			if( null == m_concreteCC ) {
				m_concreteCC = new HashMap<ATermAppl, Set<ConditionalConstraint>>( 1 );
			}

			if( null == (existingSet = m_concreteCC.get( individual )) ) {

				existingSet = new HashSet<ConditionalConstraint>( ccSet.size() );
			}
		}

		existingSet.addAll( ccSet );
		m_concreteCC.put( individual, ccSet );
	}

	public boolean probabilisticIndividualExists(ATermAppl individual) {

		return m_concreteCC.containsKey( individual );
	}

	public void removeProbabilisticIndividual(ATermAppl individual) {
		
		if (m_concreteCC.containsKey(individual)) {
			
			m_concreteCC.remove( individual );
		}
	}
	
	
}
