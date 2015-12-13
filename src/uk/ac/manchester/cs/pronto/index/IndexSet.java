/**
 * 
 */
package uk.ac.manchester.cs.pronto.index;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import aterm.ATermAppl;

/**
 * <p>Title: IndexSet</p>
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
public interface IndexSet {
	/**
	 * @return all index terms
	 */
	public Collection<? extends IndexTerm> getTerms();
	/**
	 * Retrieves all index terms that are subsumed by the given term 
	 * w.r.t to the knowledge base
	 */
	public Collection<? extends IndexTerm> getSubsumedTerms(ATermAppl term);
	
	public IndexSet clone();
	
	public void dump(Writer writer) throws IOException;	
	
	public void clear();
	
	public boolean isEmpty();
	
}
