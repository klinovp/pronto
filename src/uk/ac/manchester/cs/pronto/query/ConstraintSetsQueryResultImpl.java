/**
 * 
 */
package uk.ac.manchester.cs.pronto.query;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;

/**
 * Represents a set of constraint sets as a result to a probabilistic query
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class ConstraintSetsQueryResultImpl implements PQueryResult {

	private Set<Set<ConditionalConstraint>> m_ccSets = null;
	
	public ConstraintSetsQueryResultImpl(Set<Set<ConditionalConstraint>> sets) {
		
		m_ccSets = sets;
	}
	
	/**
	 * @param writer
	 * @throws IOException
	 */
	@Override
	public void serialize(Writer writer) throws IOException {
	
		writer.write( m_ccSets.toString() );
	}
	
	public Set<Set<ConditionalConstraint>> getConstraintSets() {
		
		return m_ccSets;
	}

	@Override
	public String toString() {
		
		return m_ccSets.toString();
	}

	
}
