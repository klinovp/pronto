/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

import uk.ac.manchester.cs.pronto.Constants;
import uk.ac.manchester.cs.pronto.lp.cplex.CCAwareCPLEXLPSolverImpl;
import uk.ac.manchester.cs.pronto.lp.glpk.CCAwareGLPKSolver;

/**
 * @author Pavel Klinov
 *
 * pavel.klinov@gmail.com
 * 
 * Oct 5, 2011
 */
public class LPConfiguration {

	/**
	 * Creates an LP solver which maintains a mapping between constraints and inequalities
	 */
	public static CCAwareLPSolver createCCAwareLPSolver() {
		switch (Constants.LP_SOLVER) {
		
		case GLPK: return new CCAwareGLPKSolver();
		case CPLEX: return new CCAwareCPLEXLPSolverImpl();
		default: return new CCAwareGLPKSolver();
		}		
	}
}
