/**
 * 
 */
package uk.ac.manchester.cs.pronto.index;

import aterm.ATermAppl;

/**
 * <p>Title: IndexTerm</p>
 * 
 * <p>Description: 
 *  index term = aterm + index in the index set
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public interface IndexTerm {

	public ATermAppl getTerm();
	public void setTerm(ATermAppl term);
	public int getIndex();
	public void setIndex(int index);
	public IndexTerm clone();
}
