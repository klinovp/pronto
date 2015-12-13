/**
 * 
 */
package uk.ac.manchester.cs.pronto.cache;

import java.util.List;
import java.util.Map;

import org.mindswap.pellet.utils.Bool;

import aterm.ATermAppl;

/**
 * <p>Title: IndexCache</p>
 * 
 * <p>Description: 
 *  Stores the known results of satisfiability, disjointness and subsumption
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public interface IndexCache {

	
	public Bool isKnownSatisfiable(ATermAppl term);
	
	public void rememberSatisfiable(ATermAppl term, Bool sat);
	
	public Bool isKnownSubClassOf(ATermAppl expr, ATermAppl term);

	public Bool isKnownDisjoint(ATermAppl expr, ATermAppl term);
	
	public void rememberSubClassOf(ATermAppl expr, ATermAppl term, Bool subclass);
	
	public void rememberDisjoint(ATermAppl expr, ATermAppl term, Bool disjoint);

	public void useSubsumptionMatrix(Map<ATermAppl, List<ATermAppl>> matrix);
	public void useDisjointnessMatrix(Map<ATermAppl, List<ATermAppl>> matrix);
	
	public void clear();
}
