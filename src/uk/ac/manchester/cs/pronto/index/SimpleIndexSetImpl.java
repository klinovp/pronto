package uk.ac.manchester.cs.pronto.index;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;

public class SimpleIndexSetImpl implements IndexSet {

	protected Set<IndexTerm> m_terms = new HashSet<IndexTerm>();
	
	@Override
	public Set<IndexTerm> getSubsumedTerms(ATermAppl term) {
	
		Set<IndexTerm> result = new HashSet<IndexTerm>();
		
		term = ATermUtils.normalize( term );
		
		for (IndexTerm iTerm : getTerms()) {
			
			ConjunctiveIndexTerm indexTerm = (ConjunctiveIndexTerm) iTerm;
			
			if (indexTerm.isStructurallySubsumedBy( term )) {
				
				result.add( indexTerm );
			}
		}
		
		return result;
	}

	public void setTerms(Collection<ATermAppl> terms) {
	
		int index = 0;
		
		clear();
		
		for (ATermAppl term : terms) {
			
			IndexTerm indexTerm = new ConjunctiveIndexTerm(term);
			
			indexTerm.setIndex( index++ );
			addIndexTerm( indexTerm );
		}
	}
	
	public void setConjunctiveTerms(Set<ConjunctiveIndexTerm> terms) {
		
		int index = 0;
		
		clear();
		
		for (ConjunctiveIndexTerm term : terms) {
			
			term.setIndex( index++ );
			addIndexTerm( term );
		}
	}
	
    protected void addIndexTerm(IndexTerm term) {

        m_terms.add( term );
    }	
	
    public Set<IndexTerm> getTerms() {

        return m_terms;
    }
    
    public void setIndexTerms(List<? extends IndexTerm> terms) {

        int index = 0;
        m_terms = new HashSet<IndexTerm>(terms);

        if (null != terms && 0 < terms.size()) {

                for (IndexTerm term : m_terms) {

                        term.setIndex( index++ );
                }
        }
    }
    
    public void dump(Writer writer) throws IOException {

		for( IndexTerm term : m_terms ) {
			
			writer.write( term.toString() + System.getProperty( "line.separator" ) );
		}
	}

	public void clear() {

		if( m_terms != null ) {

			m_terms.clear();
		}
	}

	public boolean isEmpty() {

		return null == m_terms || m_terms.size() == 0;
	}   
	
	 protected void copyFrom(IndexSet iSet) {

         m_terms = new HashSet<IndexTerm>(iSet.getTerms());
 }

	public IndexSet clone() {

		SimpleIndexSetImpl iSet = new SimpleIndexSetImpl( );

		iSet.copyFrom( this );

		return iSet;
	}	
}
