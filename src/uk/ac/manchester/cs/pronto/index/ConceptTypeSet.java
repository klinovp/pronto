/**
 * 
 */
package uk.ac.manchester.cs.pronto.index;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import aterm.ATermAppl;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * 4 Jun 2010
 */
public class ConceptTypeSet implements IndexSet {

	private Set<ConceptType> m_ctSet = null;
	
	protected ConceptTypeSet(Set<ConceptType> ctSet) {
		
		int i = 0;
		
		m_ctSet = ctSet;
		
		for (ConceptType ct : ctSet) {
			
			ct.setIndex( i++ );
		}
	}
	
	@Override
	public void clear() {

		m_ctSet.clear();
	}

	@Override
	public void dump(Writer writer) throws IOException {
		// TODO Implement a better printing
		writer.write( m_ctSet.toString() );
	}

	@Override
	public Collection<? extends IndexTerm> getSubsumedTerms(ATermAppl term) {
		// Well, it's a pain in the ass because we need to translate ATerm to OWL API
		Set<ConceptType> subsumed = new HashSet<ConceptType>();

		for( ConceptType cType : m_ctSet ) {

			if( cType.isStructurallySubsumedBy( term ) ) {

				subsumed.add( cType );
			}
		}

		return subsumed;
	}

	@Override
	public Collection<? extends IndexTerm> getTerms() {

		return m_ctSet;
	}

	@Override
	public boolean isEmpty() {
		
		return m_ctSet == null || m_ctSet.isEmpty();
	}

	public IndexSet clone() {

		return new ConceptTypeSet( m_ctSet );
	}		
}
