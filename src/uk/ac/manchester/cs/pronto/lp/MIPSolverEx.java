/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

/**
 * Extended interface for solvers that support time-outs, re-optimization, etc.
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface MIPSolverEx extends MIPSolver {

	public void setTimeout(long timeout);
	public long getTimeout();
	public void setMIPGap(double gap);
	public boolean supportsSolutionPool();
	public void setSolutionPoolSize(int size);
	public int getSolutionPoolSize(int size);
	public double[][] getSolutions();
	public double[] getObjValues();
}
