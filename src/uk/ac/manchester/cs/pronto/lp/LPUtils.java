/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.pronto.Constants;
import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.lp.LPSolver.STATUS;
import uk.ac.manchester.cs.pronto.util.NumberUtils;


/**
 * 
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class LPUtils {

	private static Logger	s_logger = Logger.getLogger( LPUtils.class );
	
	public static boolean isPSATLPinstanceSolvable( LPSolver lp ) {
		//The solver should not re-optimize the model if it has been solved
		return getLPObjectiveValue(lp) > 1d - Constants.PROBABILITY_LOW_THRESHOLD;
	}
	
	public static double getLPObjectiveValue( LPSolver lp ) {
		//The solver should not re-optimize the model if it has been solved
		lp.solveLP();
		
		return lp.getObjValue();
	}	
	
	public static STATUS solveMIPwithTimeOut(	MIPSolverEx solver,
												long timeout,
												double threshold,
												boolean continueAfterTimeOut) throws CPException {

		STATUS status = null;
		double objValue = 0d;
		boolean max = solver.getMaximize();
		
		while (timeout < Integer.MAX_VALUE / 2) {
		
			if (timeout > 0) solver.setTimeout( timeout );
			
			s_logger.debug( "Solving MIP..." );
			
			status = solver.solveMIP();
			
			if (status == STATUS.OPTIMAL) {
				
				break;//Done
				
			} else if (status == STATUS.FEASIBLE ) {
				
				objValue = solver.getObjValue();
				//Need to check if the solution is good enough
				if ((max && NumberUtils.greater(objValue, threshold )) ||
				    (!max && NumberUtils.greater( threshold, objValue ))) {
				
					s_logger.debug( "MIP solution is approximate but good enough" );
					
					break;
				}
				
			} else if (status == STATUS.INFEASIBLE ) {
				
				s_logger.info( "MIP model infeasible" );
				
				throw new CPException("MIP model infeasible", null);
				
			} 
			
			if (!continueAfterTimeOut) {
				
				throw new CPException("MIP model is too hard", null);
				
			} else {
				
				s_logger.debug( "MIP solution not yet found or not good enough, restarting the search" );
				//Increase the timeout and try again
				timeout *= 2;
			}
		}
		
		return solver.getStatus();
	}
	/*
	 * Selects maxNumber columns with the highest (or lowest) reduced cost and removes them from the
	 * model. The method should be used to keep RMP small. 
	 */
	protected static int dropLPcolumns(	LPSolver solver,
										int maxNumber,
										boolean zeroCoeffsOnly) throws CPException {
		
		SortedMap<Double, Integer> colMap = new TreeMap<Double, Integer>();
		int colNum = solver.getColumnNumber();
		boolean isMin = !solver.getMaximize();
		
		for (int i = solver.getFirstColumnIndex(); i < colNum + solver.getFirstColumnIndex(); i++) {
			
			if (!zeroCoeffsOnly || NumberUtils.greater( solver.getObjectiveCoeff( i ), 0d)) {
			
				colMap.put( (isMin ? -1 : 1) * solver.getColumnReducedCost( i ), i );
			}
		}
		
		if( !colMap.isEmpty() ) {

			int ncs = Math.min( colMap.size(), maxNumber);
			int[] indexes = new int[ncs];
			int i = 0;
			
			for (int colIndex : colMap.values()) {
				
				if (i <= maxNumber) {
					
					indexes[i++] = colIndex;
					
				} else break;
			}
			
			solver.removeColumns( indexes );
			
			return ncs;
			
		} else return 0;
	}	
}
