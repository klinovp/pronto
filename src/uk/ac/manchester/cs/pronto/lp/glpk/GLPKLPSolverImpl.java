/** * 
 */
package uk.ac.manchester.cs.pronto.lp.glpk;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.lp.LPSolver;
import uk.ac.manchester.cs.pronto.util.NumberUtils;

/**
 * Implementation based on the GNU Linear Programming Kit (GLPK)
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class GLPKLPSolverImpl implements LPSolver {
	
	private Logger	m_logger = Logger.getLogger( GLPKLPSolverImpl.class );
	private glp_prob m_model = null;
	private glp_smcp m_simplexCtrl = new glp_smcp();
	private boolean m_complete = false;
	private int[] m_colStats = null;
	private int[] m_rowStats = null;
	
	static {		
		try
		{
		    // Try to load Linux library
			System.loadLibrary("glpk_java"); 
		}
		catch (UnsatisfiedLinkError e)
		{
			// Try to load Windows library
			System.loadLibrary("glpk_4_57_java");
		} 
	}

	public GLPKLPSolverImpl() {
		
		GLPK.glp_init_smcp( m_simplexCtrl );
	}
	
	public GLPKLPSolverImpl(final int varNumber) {
		
		this();
		//TODO Add variables here
	}	
	
	/**
	 * @param column
	 * @throws CPException
	 */
	@Override
	public int addColumn(double[] column, String name) throws CPException {
		
		int index = GLPK.glp_add_cols( m_model, 1 );
		//Add objective coefficient
		GLPK.glp_set_obj_coef( m_model, index, column[0] );
		//Set column's coefficients
		GLPKUtils.setColumnCoefficients( m_model, index, column );
		
		if (name != null) GLPK.glp_set_col_name( m_model, index, name );
		
		return index;
	}

	/**
	 * @param coeffs
	 * @param rhs
	 * @param type
	 * @throws CPException
	 */
	@Override
	public int addRow(double[] row, double rhs, LPSolver.ROW_TYPE type, String name) throws CPException {
		
		int index = GLPK.glp_add_rows( m_model, 1 );
		
		GLPKUtils.setRowCoefficients( m_model, index, row, rhs, type );
		
		if (name != null) {
			
			GLPK.glp_set_row_name( m_model, index, name );
		}

		m_simplexCtrl.setPresolve( GLPKConstants.GLP_ON );
		
		return index;
	}

	/**
	 * @return Solution
	 */
	@Override
	public double[] getAssignment() {
		
		double[] solution = new double[GLPK.glp_get_num_cols( m_model )];
		
		for (int i = 0; i < solution.length; i++) {
			
			solution[i] = GLPK.glp_get_col_prim(m_model, i + 1);
		}
		
		return solution;
	}

	/**
	 * @return
	 */
	@Override
	public double[] getDuals() {
		
		double[] duals = null;
		
		if( GLPK.glp_get_prim_stat( m_model ) == GLPKConstants.GLP_FEAS ) {

			duals = new double[GLPK.glp_get_num_rows( m_model )];

			for( int i = 1; i <= duals.length; i++ ) {

				duals[i - 1] = NumberUtils.round( GLPK.glp_get_row_dual( m_model, i ) );
			}
		} 		
		
		return duals;
	}

	/**
	 * Solves the model using the simplex method by default.
	 */
	@Override
	public STATUS solveLP() {
		
		m_simplexCtrl.setMeth( GLPKConstants.GLP_PRIMAL );
		m_simplexCtrl.setTm_lim( 3000 );
		//GLPK.glp_write_lp(m_model, null, "C:\\kl\\tmp\\test.lp" );
		
		GLPK.glp_simplex(m_model, m_simplexCtrl);
		//TODO Rethink perturbation (see commented stuff below) later
		
/*		if (resFlag == GLPKConstants.GLP_ETMLIM) {
			//This means that the model is too hard to solve, most probably due to high
			//primal degeneracy. We perturb it by relaxing lower (=0) column bounds,
			//solve the perturbed version and then try to *re-optimize* the original version 
			m_logger.info( "LP model is too hard, we will perturb it and solve that version first" );

			perturb(m_model, -1E-06);
			m_simplexCtrl.setTm_lim( 300000 );
			m_logger.info( "Solving perturbed instance..." );
			resFlag = GLPK.glp_simplex(m_model, m_simplexCtrl);
			m_logger.info( "... done, restoring the original instance" );
			
			//saveBasis();
			
			perturb(m_model, 0d);
			
			//restoreBasis();
			
			if( resFlag == 0 ) {

				m_logger.info( "Solving the original instance..." );

				m_simplexCtrl.setPresolve( GLPKConstants.GLP_OFF );
				m_simplexCtrl.setMeth( GLPKConstants.GLP_DUAL );
				resFlag = GLPK.glp_simplex( m_model, m_simplexCtrl );
				m_logger.info( resFlag == 0	? "... done!" : "... it didn't work, fail :(" );
				
			} else m_logger.info( "Perturbed instance is no easier, fail" );
		}
		
		if (resFlag != 0) {

			writeLP( "C:///kl//tmp//bad.glpk" );
			//There's no hope...
			throw new CPException("GLPK simplex solver returned an error: " + resFlag, null);
		}*/
		
		return getStatus();
	}

/*	private void perturb(glp_prob model, double pertValue) {

		int colNum = GLPK.glp_get_num_cols(model);
		
		for (int i = 1; i <= colNum; i++) {
			
			int type = GLPK.glp_get_col_type( model, i );
			double upper = GLPK.glp_get_col_ub( model, i );
			
			GLPK.glp_set_col_bnds(model, i, type, pertValue, upper);
		}
	}*/
	
	/**
	 * @param max
	 * @throws CPException
	 */
	@Override
	public void setMaximize(boolean max) throws CPException {
		
		GLPK.glp_set_obj_dir(m_model, max ? GLPKConstants.GLP_MAX : GLPKConstants.GLP_MIN);
	}

	/**
	 * @param objective
	 * @throws CPException
	 */
	@Override
	public void setObjective(double[] objective) throws CPException {
		
		for (int i = 0; i < getColumnNumber(); i++) {			
			GLPK.glp_set_obj_coef( m_model, i + 1, i < objective.length ? objective[i] : 0d );
		}
	}

	protected void setModel(glp_prob model) {
		
		m_model = model;
		GLPK.glp_create_index( m_model );
		m_colStats = null;
		m_rowStats = null;
	}
	
	protected glp_prob getModel() {
		
		return m_model;
	}
	
	public void readMPS(String filename) throws IOException, CPException {
		
		if (m_model == null) m_model = GLPK.glp_create_prob();
		
		int result = GLPK.glp_read_mps( m_model, GLPKConstants.GLP_MPS_FILE, null, filename );
		//int result = GLPK.glp_read_lp( m_model, null, filename );
		
		if (result != 0) {
			
			throw new CPException("Error reading model in the MPS format: " + result, null);
		}
	}

	public void writeMPS(String filename) throws IOException {
		
		GLPK.glp_write_mps( m_model, GLPKConstants.GLP_MPS_FILE, null, filename );
	}	
	
	
	protected glp_smcp getSimplexCtrl() {
		
		return m_simplexCtrl;
	}
	
	public void dispose() {
		
		if (m_model != null) {
		
			GLPK.glp_delete_prob( m_model );
			m_model = null;
			m_colStats = null;
			m_rowStats = null;
		}
	}


	@Override
	public void removeRow(int index) throws CPException {
		
		SWIGTYPE_p_int ind = GLPK.new_intArray( 1 );
		
		GLPK.intArray_setitem( ind, 1, index );
		GLPK.glp_del_rows( m_model, 1, ind );
		
		GLPK.delete_intArray( ind );
		
		m_simplexCtrl.setPresolve( GLPKConstants.GLP_ON );
	}


	@Override
	public void removeRow(String name) throws CPException {
		
		int index = GLPK.glp_find_row( m_model, name );
		
		if (index > 0) {
			
			m_logger.debug( "Removing row " + name + " with index " + index );
			
			removeRow( index );
			
		} else {
			
			m_logger.debug( "Row " + name + " not found, removal skipped" );
		}
	}


	@Override
	public double getColumnReducedCost(int index) {
		
		return GLPK.glp_get_col_dual( m_model, index );
	}


	@Override
	public boolean getMaximize() {
		
		return GLPK.glp_get_obj_dir( m_model ) == GLPKConstants.GLP_MAX;
	}


	@Override
	public double getObjectiveCoeff(int index) {
		
		return GLPK.glp_get_obj_coef( m_model, index );
	}


	@Override
	public void removeColumn(int index) throws CPException {
		
		SWIGTYPE_p_int ind = GLPK.new_intArray( 1 );
		
		GLPK.intArray_setitem( ind, 1, index );
		GLPK.glp_del_cols( m_model, 1, ind );		
		
		GLPK.delete_intArray( ind );
		
		m_simplexCtrl.setPresolve( GLPKConstants.GLP_ON );
	}


	@Override
	public void removeColumn(String name) throws CPException {
		
		int index = GLPK.glp_find_col( m_model, name );
		
		if (index > 0) {
			
			m_logger.debug( "Removing column " + name + " with index " + index );
			
			removeColumn( index );
			
		} else {
			
			m_logger.debug( "Column " + name + " not found, removal skipped" );
		}		
	}


	@Override
	public void removeColumns(int[] indexes) throws CPException {
		
		SWIGTYPE_p_int ind = GLPK.new_intArray( indexes.length + 1 );
		
		for (int i = 0; i < indexes.length; i++) {
		
			GLPK.intArray_setitem( ind, i + 1, indexes[i] );
		}
		
		GLPK.glp_del_cols( m_model, indexes.length, ind );	
		GLPK.delete_intArray( ind );
		m_simplexCtrl.setPresolve( GLPKConstants.GLP_ON );
	}

	@Override
	public int getColumnNumber() {
		
		return GLPK.glp_get_num_cols( m_model );
	}


	@Override
	public int getRowNumber() {
		
		return GLPK.glp_get_num_rows( m_model );
	}


	@Override
	public int getFirstColumnIndex() {
		
		return 1;
	}
	
	@Override
	public int getFirstRowIndex() {
		
		return 1;
	}


	@Override
	public void setVariableUpperBound(int varIndex, double bound) {

		if (bound <= 0d) {
			
			GLPK.glp_set_col_bnds( m_model, varIndex, GLPKConstants.GLP_FX, 0d, 0d );
			
		} else if (bound == Double.POSITIVE_INFINITY) {
			
			GLPK.glp_set_col_bnds( m_model, varIndex, GLPKConstants.GLP_LO, 0d, bound );
			
		} else {
			
			GLPK.glp_set_col_bnds( m_model, varIndex, GLPKConstants.GLP_DB, 0d, bound );
		}
	}
	
	@Override
	public void setVariableLowerBound(int varIndex, double lb) {

		int type = GLPK.glp_get_col_type( m_model, varIndex );
		double ub = GLPK.glp_get_col_ub( m_model, varIndex );
		
		if (lb == Double.NEGATIVE_INFINITY) {

			type = (type == GLPKConstants.GLP_FX || type == GLPKConstants.GLP_UP)
													? GLPKConstants.GLP_UP : GLPKConstants.GLP_FR;
			
		} else {
			
			type = (type == GLPKConstants.GLP_FX || type == GLPKConstants.GLP_UP)
													? GLPKConstants.GLP_FX : GLPKConstants.GLP_LO;
		}
		
		GLPK.glp_set_col_bnds( m_model, varIndex, type, lb, ub );		
	}	
	
	
	public void writeLP(String filename) {
		
		GLPK.glp_write_lp( m_model, null, filename );
	}

	public void readLP(String filename) {
		
		GLPK.glp_read_lp( m_model, null, filename );
		m_colStats = null;
		m_rowStats = null;
	}
	
	@Override
	public String getColumnName(int index) {
		
		return GLPK.glp_get_col_name( m_model, index );
	}


	@Override
	public String getRowName(int index) {
		
		return GLPK.glp_get_row_name( m_model, index );
	}


	@Override
	public void setRowUpperBound(int index, double bound) throws CPException {

		GLPK.glp_set_row_bnds( m_model, index, GLPKConstants.GLP_UP, 0, bound );
	}


	@Override
	public void removeRows(int[] indexes) throws CPException {

		SWIGTYPE_p_int ind = GLPK.new_intArray( indexes.length + 1 );
		
		for (int i = 0; i < indexes.length; i++) {
		
			GLPK.intArray_setitem( ind, i + 1, indexes[i] );
		}
		
		GLPK.glp_del_rows( m_model, indexes.length, ind );	
		GLPK.delete_intArray( ind );
		m_simplexCtrl.setPresolve( GLPKConstants.GLP_ON );
		m_colStats = null;
		m_rowStats = null;
	}
	
	
	public boolean testVarAssignment(double[] assignment) {
		
		SWIGTYPE_p_int varIndexes = GLPK.new_intArray( GLPK.glp_get_num_cols( m_model ) + 1 );
		SWIGTYPE_p_double row = GLPK.new_doubleArray( GLPK.glp_get_num_cols( m_model ) + 1 );
		boolean result = true;
		
		/*try {
			writeLP( "C:\\kl\\tmp\\test.lp" );
		} catch( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//Check it row by row
		for (int rowIndex = 1; result && (rowIndex <= GLPK.glp_get_num_rows( m_model )); rowIndex++) {
			
			int len = GLPK.glp_get_mat_row( m_model, rowIndex, varIndexes, row );
			int type = GLPK.glp_get_row_type( m_model, rowIndex );
			double lb = GLPK.glp_get_row_lb( m_model, rowIndex );
			double ub = GLPK.glp_get_row_ub( m_model, rowIndex );
			//Check the inequality
			if (len > 0) result &= checkRow(row, varIndexes, len, type, lb, ub, assignment);
		}
		
		GLPK.delete_intArray( varIndexes );
		GLPK.delete_doubleArray( row );
		
		return result;
	}


	private boolean checkRow(	SWIGTYPE_p_double row,
								SWIGTYPE_p_int varIndexes,
								int len,
								int type,
								double lb,
								double ub,
								double[] assignment) {
		
		//Compute left hand-side first
		double lhs = 0d;
		boolean result = false;
		
		for (int i = 1; i <= len; i++) {
			
			int varInd = GLPK.intArray_getitem(varIndexes, i);
			double coeff = GLPK.doubleArray_getitem(row, i);
			
			lhs += coeff * assignment[varInd - 1];
		}
		
		if (type == GLPKConstants.GLP_FX) {
			
			result = NumberUtils.equal(lhs, lb);
		}
		
		if (type == GLPKConstants.GLP_LO) {
			
			result = !NumberUtils.greater(lb, lhs);
		}
		
		if (type == GLPKConstants.GLP_UP) {
			
			result = !NumberUtils.greater(lhs, ub);
		}		
		
		return result;
	}
	
	public void initLPInstance(int colNum) {
		
		if (colNum < 0) {
			
			m_logger.fatal( Thread.currentThread().getStackTrace() );
			
			throw new IllegalArgumentException("Negative number of variables: " + colNum);
			
		} else {
		
			m_model = GLPK.glp_create_prob();
			m_colStats = null;
			m_rowStats = null;
			
			if (colNum > 0) GLPK.glp_add_cols( m_model, colNum );
			
			for( int i = 1; i <= colNum; i++ ) {
	
				GLPK.glp_set_col_bnds( m_model, i, GLPKConstants.GLP_LO, 0, Double.MAX_VALUE );
			}		
		}
	}
	
	public void setObjectiveCoeff(int index, double coeff) throws CPException {
		
		GLPK.glp_set_obj_coef( m_model, index, coeff );
	}

	@Override
	public boolean isComplete() {
		
		return m_complete;
	}

	@Override
	public void setComplete(boolean complete) {
		
		m_complete = complete;
	}
	
	public void setRowBounds(int rowIndex, double lBound, double uBound) {
		
		if (lBound == Double.NEGATIVE_INFINITY) {
			
			if (uBound == Double.POSITIVE_INFINITY) {
				//Set the row free
				GLPK.glp_set_row_bnds( m_model, rowIndex, GLPKConstants.GLP_FR, 0, 0 );
				
			} else {
				//Upper bound
				GLPK.glp_set_row_bnds( m_model, rowIndex, GLPKConstants.GLP_UP, 0, uBound );
			}
		} else {
			
			if (uBound == Double.POSITIVE_INFINITY) {
				//Lower bound
				GLPK.glp_set_row_bnds( m_model, rowIndex, GLPKConstants.GLP_LO, lBound, 0 );
				
			} else {
				//Double bound
				GLPK.glp_set_row_bnds( m_model, rowIndex, GLPKConstants.GLP_DB, lBound, uBound );
			}			
		}
	}
	
	public boolean isColumnBasic(int colIndex) {
		
		return GLPK.glp_get_col_stat( m_model, colIndex ) == GLPKConstants.GLP_BS;
	}

	@Override
	public void restoreBasis() {
		
		if (m_colStats != null && m_rowStats != null && m_model != null) {
			
			for (int i = 0; i < m_colStats.length; i++) {
				
				GLPK.glp_set_col_stat( m_model, i + 1, m_colStats[i] );
			}
			
			for (int i = 0; i < m_rowStats.length; i++) {
				
				GLPK.glp_set_row_stat( m_model, i + 1, m_rowStats[i] );
			}			
		}
	}

	@Override
	public void saveBasis() {
		
		if (m_model != null) {
			
			m_colStats = new int[GLPK.glp_get_num_cols( m_model )];
			m_rowStats = new int[GLPK.glp_get_num_rows( m_model )];
			
			for (int i = 0; i < m_colStats.length; i++) {
				
				m_colStats[i] = GLPK.glp_get_col_stat( m_model, i + 1 );
			}
			
			for (int i = 0; i < m_rowStats.length; i++) {
				
				m_rowStats[i] = GLPK.glp_get_row_stat( m_model, i + 1 );
			}			
		}
	}

	@Override
	public double getObjValue() {
		
		return GLPK.glp_get_obj_val( m_model );
	}

	@Override
	public STATUS getStatus() {

		int status = GLPK.glp_get_status( m_model );		
		
		if (status == GLPKConstants.GLP_OPT) return STATUS.OPTIMAL;
		if (status == GLPKConstants.GLP_FEAS) return STATUS.FEASIBLE;
		if (status == GLPKConstants.GLP_NOFEAS) return STATUS.INFEASIBLE;

		return STATUS.UNDEFINED;
	}
}
