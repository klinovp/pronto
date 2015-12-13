/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.benchmark.Telemetry;


/**
 * <p>Title: PSATSolver</p>
 * 
 * <p>Description: 
 *  Interface for classes that can check satisfiability of probabilistic
 *  DL knowledge bases
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public interface PSATSolver extends Telemetry {

	public boolean isPTBoxSatisfiable(PTBox ptbox);
	public ConditionalConstraint tightLogicalEntailment(PTBox ptbox, ATermAppl concept);
	public double computeLowerProbability(PTBox ptbox, ATermAppl concept);
	public double computeUpperProbability(PTBox ptbox, ATermAppl concept);
}
