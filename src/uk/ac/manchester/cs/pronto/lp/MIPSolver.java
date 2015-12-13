/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;


/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * May 30, 2009
 * 
 * Interface for mixed integer programming solvers
 */
public interface MIPSolver extends LPSolver {

	public enum VAR_TYPE {GENERAL, INTEGER, BINARY};
	
	public void initMIPInstance(VAR_TYPE[] varTypes);
	public STATUS solveMIP();
	public double[] getObjective();
	public MIPSolver clone();
}
