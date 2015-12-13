/**
 * 
 */
package uk.ac.manchester.cs.pronto.explanation;

import java.util.Collection;

import aterm.ATermAppl;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface SATChecker {

	public boolean isSatisfiable(Collection<ATermAppl> conjuncts);
	public boolean isSatisfiable(ATermAppl term);
}
