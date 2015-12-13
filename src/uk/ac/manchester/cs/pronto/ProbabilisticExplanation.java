/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.Set;


import aterm.ATermAppl;

/**
 * <p>Title: ProbabilisticExplanation</p>
 * 
 * <p>Description: 
 *  Encompasses all the minimally sufficient information to explain a
 *  probabilistic entailment
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public interface ProbabilisticExplanation {

	public PTBox getPTBox();
	public Set<ATermAppl> getClassicalExplanationSet();
	public ConditionalConstraint getEntailment();
	public Set<Set<ConditionalConstraint>> getLowerBoundExplanationSet();
	public Set<Set<ConditionalConstraint>> getUpperBoundExplanationSet();
}
