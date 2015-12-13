package uk.ac.manchester.cs.pronto.lp.cplex;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import uk.ac.manchester.cs.pronto.lp.LPSolver;

public class CPLEXLPSolverTest {

	private CPLEXLPSolverImpl initLP() throws Exception {
		
		CPLEXLPSolverImpl model = new CPLEXLPSolverImpl(2);
		
		model.addRow( new double[] {1.5, 2.5}, 1.0, LPSolver.ROW_TYPE.EQUAL, null );
		model.addRow( new double[] {3.5, 4.5}, 2.0, LPSolver.ROW_TYPE.GREATER_EQUAL, null );
		model.addRow( new double[] {5.5, 6.5}, 3.0, LPSolver.ROW_TYPE.LESS_EQUAL, null );
		model.setObjective( new double[] {1, 2} );
		model.setMaximize( true );
		
		return model;
	}
	
	@Test
	public void testSetInitialLPInstance() throws Exception {
		
		CPLEXLPSolverImpl solver = initLP();
		
		assertEquals(2, solver.getColumnNumber());
		assertEquals(3, solver.getRowNumber());
		
		solver.writeLP( "C://kl/tmp/test.lp");
		
		System.out.println( solver.solveLP() );
		System.out.println( Arrays.toString(solver.getAssignment()) );
	}

	@Test
	public void testAddColumn() throws Exception {
		
		CPLEXLPSolverImpl solver = initLP();
		
		solver.addColumn( new double[]{1,1,1,1}, "new_column" );
		
		assertEquals(3, solver.getColumnNumber());
		
		solver.writeLP( "C://kl/tmp/test.lp");
		
		System.out.println( solver.solveLP() );
		System.out.println( Arrays.toString(solver.getAssignment()) );
	}	
	
	@Test
	public void testRemoveColumn() throws Exception {
		
		CPLEXLPSolverImpl solver = initLP();
		
		solver.removeColumn( 1 );
		
		assertEquals(1, solver.getColumnNumber());
		
		solver.writeLP( "C://kl/tmp/test.lp");
	}	
	
	@Test
	public void testDuals() throws Exception {
		
		CPLEXLPSolverImpl solver = initLP();
		
		solver.solveLP();
		
		double[] duals = solver.getDuals();
		
		assertEquals(solver.getRowNumber(), duals.length);
		System.out.println( Arrays.toString(duals) );
	}	
}
