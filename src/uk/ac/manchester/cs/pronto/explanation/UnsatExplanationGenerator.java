/**
 * 
 */
package uk.ac.manchester.cs.pronto.explanation;

import java.util.Set;

import aterm.ATermAppl;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Discovers all explanations of conjunctive class expression unsatisfiability
 */
public interface UnsatExplanationGenerator {
	
	public Set<Set<ATermAppl>> computeExplanations(Set<ATermAppl> conjuncts, SATChecker checker);
	public void setTimeLimit(long limit);
}
