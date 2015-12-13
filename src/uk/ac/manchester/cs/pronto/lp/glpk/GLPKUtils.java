/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp.glpk;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;

import uk.ac.manchester.cs.pronto.lp.LPSolver;
import uk.ac.manchester.cs.pronto.lp.MIPSolver;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class GLPKUtils {

	protected static int genericRowTypeToGLPKType(LPSolver.ROW_TYPE type) {

		switch ( type ) {

		case EQUAL:

			return GLPKConstants.GLP_FX;

		case GREATER_EQUAL:

			return GLPKConstants.GLP_LO;

		case LESS_EQUAL:

			return GLPKConstants.GLP_UP;
		}

		throw new RuntimeException( "Invalid row type" );
	}
	
	protected static int genericColumnTypeToGLPKType(MIPSolver.VAR_TYPE type) {

		switch (type) {
			
		case INTEGER: return GLPKConstants.GLP_IV;
			
		case BINARY: return GLPKConstants.GLP_BV;
		}

		return GLPKConstants.GLP_CV;
	}	
	
	
	protected static glp_prob copyMIPModel(glp_prob model) {
		
		glp_prob copy = GLPK.glp_create_prob();
		
		GLPK.glp_copy_prob( copy, model, GLPKConstants.GLP_ON );
		
		return copy;
	}
	
	
	protected static void setRowCoefficients(glp_prob model, int index, double[] row, double rhs, LPSolver.ROW_TYPE type) {
		
		SWIGTYPE_p_double newRow = GLPK.new_doubleArray( row.length + 1 );
		SWIGTYPE_p_int ind = GLPK.new_intArray( row.length + 1 );
		//Set the coefficients
		for (int i = 0; i < row.length; i++) {
			
			GLPK.doubleArray_setitem( newRow, i + 1, row[i] );
			GLPK.intArray_setitem( ind, i + 1, i + 1 );
		}
		
		GLPK.glp_set_mat_row( model, index, row.length, ind, newRow );
		GLPK.glp_set_row_bnds( model, index, GLPKUtils.genericRowTypeToGLPKType( type ), rhs, rhs );
		
		GLPK.delete_doubleArray( newRow );
		GLPK.delete_intArray( ind );		
	}
	
	protected static double[] getRowCoefficients(glp_prob model, int index) {
		
		int colNum = GLPK.glp_get_num_cols( model );
		SWIGTYPE_p_double coeffs = GLPK.new_doubleArray( colNum + 1 );
		SWIGTYPE_p_int indices = GLPK.new_intArray( colNum + 1 );
		int len = GLPK.glp_get_mat_row( model, index, indices, coeffs );
		double[] allCoeffs = new double[GLPK.glp_get_num_cols( model )];
		
		for (int i = 0; i < len; i++) {		
			int varIndex = GLPK.intArray_getitem( indices, i + 1 );
			
			allCoeffs[varIndex - 1] = GLPK.doubleArray_getitem( coeffs, i + 1 );
		}
		
		GLPK.delete_intArray( indices );
		GLPK.delete_doubleArray( coeffs );
		
		return allCoeffs;
	}
	
	protected static void setColumnCoefficients(glp_prob model, int index, double[] column) {
		
		SWIGTYPE_p_double col = GLPK.new_doubleArray( column.length );
		SWIGTYPE_p_int ind = GLPK.new_intArray( column.length );
		//Set the coefficients
		for (int i = 1; i < column.length; i++) {
			
			GLPK.doubleArray_setitem( col, i, column[i] );
			GLPK.intArray_setitem( ind, i, i );
		}
		
		GLPK.glp_set_mat_col( model, index, column.length - 1, ind, col );
		GLPK.glp_set_col_kind( model, index, GLPKConstants.GLP_CV );
		GLPK.glp_set_col_bnds( model, index, GLPKConstants.GLP_LO, 0, Double.MAX_VALUE );
		
		GLPK.delete_doubleArray( col );
		GLPK.delete_intArray( ind );
	}
}
