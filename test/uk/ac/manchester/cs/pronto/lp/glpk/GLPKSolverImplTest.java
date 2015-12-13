package uk.ac.manchester.cs.pronto.lp.glpk;

import org.gnu.glpk.GLPK;
import org.junit.Test;
import org.gnu.glpk.glp_prob;

public class GLPKSolverImplTest {

	
	@Test
	public void testSolveMIP() throws Exception {

		GLPKMIPSolverImpl solver = new GLPKMIPSolverImpl();
		glp_prob model = GLPK.glp_create_prob();
		
		GLPK.glp_read_prob( model, 0, "C:///kl//tmp//hard.glpk" );
		
		solver.setModel( model );
		
		solver.solveMIP();
		
		System.out.println( "Status: " + solver.getStatus() );
		System.out.println( "Objective value: " + solver.getObjValue() );
	}	
}
