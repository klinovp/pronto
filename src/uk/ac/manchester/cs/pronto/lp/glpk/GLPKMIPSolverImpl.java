/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp.glpk;

import org.apache.log4j.Logger;
import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;

import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.lp.MIPSolver;
import uk.ac.manchester.cs.pronto.lp.MIPSolverEx;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * 5 Oct 2009
 */
public class GLPKMIPSolverImpl extends GLPKLPSolverImpl implements MIPSolverEx {

	private Logger	m_logger = Logger.getLogger( GLPKMIPSolverImpl.class );
	private glp_iocp m_mipCtrl = new glp_iocp();
	
	public GLPKMIPSolverImpl() {
		
		super();
		GLPK.glp_init_iocp( m_mipCtrl );
		m_mipCtrl.setPresolve( GLPKConstants.GLP_OFF );
	}
	
	protected GLPKMIPSolverImpl(glp_prob model) {
		
		this();
		setModel( model );
		m_mipCtrl.setPresolve( GLPKConstants.GLP_OFF );
	}
	
	protected glp_iocp getMipCtrl() {
		
		return m_mipCtrl;
	}
	
	@Override
	/*
	 * The return value can be trusted only if the search has been completed (the caller
	 * needs to use the getMIPstatus method)
	 */
	public STATUS solveMIP() {
		
		/*String tmpFilename = "C://dump/test_mip.glpk";
		
		GLPK.glp_write_prob( getModel(), 0, tmpFilename );
		GLPK.glp_read_prob( getModel(), 0, tmpFilename );*/
		
		search( m_mipCtrl );
		
		return getStatus();
	}
	
	/*
	 * Runs search until time limit is reached
	 */
	protected void search(glp_iocp ctrl) {
		
		//long ts = System.currentTimeMillis();
		
		if (ctrl.getPresolve() == GLPKConstants.GLP_OFF) {
			
			super.getSimplexCtrl().setPresolve( GLPKConstants.GLP_OFF );
			GLPK.glp_simplex( getModel(), super.getSimplexCtrl() );
		}
		
		GLPK.glp_intopt(getModel(), ctrl);
		
		//GLPK.glp_write_prob( getModel(), 0, "C:///kl//tmp//test.glpk" );
		
		/*if (System.currentTimeMillis() - ts > 10000) {
			
			GLPK.glp_write_lp( getModel(), null, "C:///kl//tmp//" + "hard_" + (System.currentTimeMillis() - ts) + ".lp" );	
		}*/

		if (m_logger.isDebugEnabled()) {
			m_logger.debug("MIP size: " + GLPK.glp_get_num_cols(getModel()) + " x " + GLPK.glp_get_num_rows(getModel()));
		}
	}
	
	@Override
	public double[] getAssignment() {

		double[] solution = new double[GLPK.glp_get_num_cols( getModel() )];
		
		for (int i = 0; i < solution.length; i++) {
			
			solution[i] = GLPK.glp_mip_col_val(getModel(), i + 1);
		}
		
		return solution;
	}
	
	@Override
	public double[] getDuals() {
		//Duals are not provided for MIP
		return null;
	}	
	
	@Override
	public double getObjValue() {
		
		return GLPK.glp_mip_obj_val( getModel() );
	}

	@Override
	public double[] getObjective() {

		double[] objective = new double[GLPK.glp_get_num_cols( getModel() )];
		
		for (int i = 0; i < objective.length; i++) {
			
			objective[i] = GLPK.glp_get_obj_coef( getModel(), i + 1 );
		}
		
		return objective;
	}
	
	public MIPSolver clone() {
		
		return new GLPKMIPSolverImpl(GLPKUtils.copyMIPModel( getModel() ));
	}

	@Override
	public int addRow(double[] row, double rhs, ROW_TYPE type, String name) throws CPException {

		int index = super.addRow( row, rhs, type, name );
		
		//m_mipCtrl.setPresolve( GLPKConstants.GLP_OFF );
		
		return index;
	}

	@Override
	public STATUS getStatus() {

		int simplexStatus = GLPK.glp_get_status( getModel() );		
		int mipStatus = GLPK.glp_mip_status( getModel());
		
		if (mipStatus == GLPKConstants.GLP_OPT) return STATUS.OPTIMAL;
		if (mipStatus == GLPKConstants.GLP_FEAS) return STATUS.FEASIBLE;
		if (mipStatus == GLPKConstants.GLP_NOFEAS 
				|| simplexStatus == GLPKConstants.GLP_NOFEAS) return STATUS.INFEASIBLE;

		return STATUS.UNDEFINED;
	}

	@Override
	public long getTimeout() {
		
		return m_mipCtrl.getTm_lim();
	}

	@Override
	public void setTimeout(long timeout) {
		
		m_mipCtrl.setTm_lim( (int) timeout );
	}

	@Override
	public void setMIPGap(double gap) {
		
		m_mipCtrl.setMip_gap( gap );
	}
	
	public void initMIPInstance(MIPSolver.VAR_TYPE[] varTypes) {
		
		super.initLPInstance( varTypes.length);
		//The rest is to set proper column types
		for (int i = 0; i < varTypes.length; i++) {
			
			GLPK.glp_set_col_kind(getModel(), i+1,  GLPKUtils.genericColumnTypeToGLPKType(varTypes[i]));
		}		
	}

	@Override
	public int getSolutionPoolSize(int size) {

		return -1;
	}

	@Override
	public double[][] getSolutions() {
		
		return new double[][]{getAssignment()};
	}

	@Override
	public void setSolutionPoolSize(int size) {
		//Don't support it for now...
	}

	@Override
	public boolean supportsSolutionPool() {
		
		return false;
	}

	@Override
	public double[] getObjValues() {
		
		return new double[] {GLPK.glp_mip_obj_val( getModel() )};
	}
}