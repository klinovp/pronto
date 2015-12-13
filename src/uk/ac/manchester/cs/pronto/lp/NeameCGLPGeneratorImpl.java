/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

import java.util.List;

import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.util.NumberUtils;

/**
 * An extension of CGLPColumnGenerator that implement Neame's stabilization scheme
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class NeameCGLPGeneratorImpl extends CGLPGeneratorImpl {

	//Stabilization constants
	private final double m_alpha = 1d;
	private final double m_epsilon = 0.05;

	@Override
	protected double generateColumns(ColumnGeneratorEx gen, CCAwareLPSolver solver) throws CPException {
		
		List<double[]> columns = null;
		int cgIterCounter = 1;
		double[] accDuals = null;
		boolean dualsUpdated = false;
		boolean columnExists = true;
		double objValue = 0d;
		//Remove
		//Map<Integer, List<double[]>> prevColumns = new HashMap<Integer, List<double[]>>();
		
		//Here we start the program generation main loop. We will add
		//columns to RMP until there's no improving column.
		while( columnExists ) {
			// Solve RMP to obtain duals
			columnExists = false;
			//Temporarily turn off forgetting
			/*if (lpSize >= rmp.getLinearConstraints().getRightHandsides().length * 2 ) {
				//Forgetting: remove non-basic columns
				lpSize -= solver.shrinkLP(rmp.getLinearConstraints().getRightHandsides().length / 2);
			}*/
			
			double t = System.currentTimeMillis();
			
			s_logger.debug("Column generation step #" + (cgIterCounter));	
			solver.solveLP();
			objValue = solver.getObjValue();
			accDuals = null == accDuals ? new double[solver.getDuals().length] : accDuals;
			s_logger.debug( "RMP solved in: " + (System.currentTimeMillis() - t) + " ms");				
			
			do {
				//This loop is need get accDuals close enough to duals before we can safely
				//stop the column generation process
				dualsUpdated = updateDuals(accDuals, solver.getDuals(), m_epsilon * accDuals.length);
				//KEY STEP
				columns = gen.generateColumns( accDuals, solver.getMaximize() );

				s_logger.debug( columns.size() + " new column(s) generated in " +
										(System.currentTimeMillis() - t) + " ms");

				if( !columns.isEmpty() ) {
					
					columnExists = true;
					//The columns are added directly into the LP model, not via
					//any generic wrapper (like LPInstance)
					for (double[] column : columns)	{
						
						/*{//Remove
							int hash = Arrays.hashCode( column );
							List<double[]> prevCols = prevColumns.get( hash );

							if( prevCols != null ) {

								for( double[] prevCol : prevCols ) {

									if( Arrays.equals( column, prevCol ) ) {

										throw new RuntimeException(
												"We've seen this column before: " + Arrays.toString( column) + ", reduced cost: " +
												(column[1] - ArrayUtils.scalarProduct(ArrayUtils.subArray( column, 1, column.length - 1 ), accDuals)));
									}
								}
							}
							else {

								prevCols = new ArrayList<double[]>();
							}

							prevCols.add( column );
							prevColumns.put( hash, prevCols );
						}*/
						
						solver.addColumn( column, null );
					}
					
					cgIterCounter += 1;
				} 				
			} while (!columnExists && dualsUpdated);
		}
		
		return objValue;
	}

	/*
	 * Updates the dual vector according to the Neame's stabilization scheme:
	 * 
	 * d^k_mod = (1 - alpha) * d^{k-1}_mod + alpha * d^k
	 * 
	 * If the difference between d^k_mod and d^k becomes too small (e.g. as a result of several updates),
	 * the method copies d^k into d^k_mod and returns false. Otherwise it returns true. 
	 */
	private boolean updateDuals(double[] accDuals, double[] duals, double epsilon) {
		
		double norm = 0d;
		double dualSum = 0d;
		
		for (int i = 0; i < accDuals.length; i++) {
			
			accDuals[i] = (1.0 - m_alpha) * accDuals[i] + m_alpha * duals[i];
			norm += Math.abs( accDuals[i] - duals[i] );
			dualSum += duals[i];
		}
		
		if (norm < m_epsilon || NumberUtils.equal(dualSum, 0d)) {
			
			System.arraycopy( duals, 0, accDuals, 0, duals.length );
			
			if (norm < m_epsilon) {
				
				s_logger.debug( "Accumulated duals are equal to duals" );
				
			} else if (NumberUtils.equal(dualSum, 0d)) {
				
				s_logger.debug( "Duals are zeroes" );
			}
			
			return false;
			
		} else return true;
	}
	
	
}
