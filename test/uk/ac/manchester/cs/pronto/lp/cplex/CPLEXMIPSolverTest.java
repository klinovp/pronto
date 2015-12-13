package uk.ac.manchester.cs.pronto.lp.cplex;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import uk.ac.manchester.cs.pronto.lp.LPSolver;
import uk.ac.manchester.cs.pronto.lp.MIPSolver;

public class CPLEXMIPSolverTest {

	private CPLEXMIPSolverImpl initMIP() throws Exception {
		
		CPLEXMIPSolverImpl model = new CPLEXMIPSolverImpl();
		
		model.initMIPInstance( new MIPSolver.VAR_TYPE[] { MIPSolver.VAR_TYPE.INTEGER, MIPSolver.VAR_TYPE.INTEGER} );
		
		model.addRow(new double[] {1, 2}, 3.0, LPSolver.ROW_TYPE.EQUAL, null );
		model.addRow( new double[] {3, 4}, 2.0, LPSolver.ROW_TYPE.GREATER_EQUAL, null );
		model.addRow( new double[] {-5, 6}, 1.0, LPSolver.ROW_TYPE.LESS_EQUAL, null );
		model.setObjective( new double[]{1,2} );
		model.setMaximize( true );
		
		return model;
	}	
	
	@Test
	public void testSetInitialMIPInstance() throws Exception {
		
		CPLEXMIPSolverImpl solver = initMIP();		
		
		solver.writeLP( "C://kl/tmp/test_mip.lp");
		
		System.out.println( solver.solveMIP() );
		System.out.println( Arrays.toString(solver.getAssignment()) );
	}

	@Test
	public void testAddRow() throws Exception {
		
		CPLEXMIPSolverImpl solver = initMIP();
		
		solver.addRow( new double[] {1, 2}, 3.0, LPSolver.ROW_TYPE.EQUAL, "new_row" );
		
		assertEquals(4, solver.getRowNumber());
		
		solver.writeLP( "C://kl/tmp/test_mip.lp");
	}

}
