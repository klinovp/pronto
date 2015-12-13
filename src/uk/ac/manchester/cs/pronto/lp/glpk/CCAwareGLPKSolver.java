/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp.glpk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.lp.CCAwareLPSolver;
import uk.ac.manchester.cs.pronto.lp.LPSolver;

/**
 * @author Pavel Klinov
 *
 * pavel.klinov@gmail.com
 * 
 * Sep 19, 2011
 */
public class CCAwareGLPKSolver extends GLPKLPSolverImpl implements CCAwareLPSolver {

	private Logger	m_logger = Logger.getLogger( CCAwareGLPKSolver.class );
	private final String L_ROW = "L_ROW_NORM";
	private final String U_ROW = "U_ROW_NORM";
	private final String L_ROW_SUFFIX = "_L";
	private final String U_ROW_SUFFIX = "_U";
	
	private Map<Integer, ConditionalConstraint> m_ccIndex = new HashMap<Integer, ConditionalConstraint>();
	
	private String getLowerRowLabel(ConditionalConstraint cc) {
		return cc.getOrder() + L_ROW_SUFFIX;
	}
	
	private String getUpperRowLabel(ConditionalConstraint cc) {
		return cc.getOrder() + U_ROW_SUFFIX;
	}
	
	private void assertIndex() {
		
		GLPK.glp_create_index( getModel() );
	}
	
	@Override
	public void addRows(ConditionalConstraint cc, double[] lCoeffs, double[] uCoeffs) throws CPException {

		assertIndex();
		addRow(lCoeffs, 0, LPSolver.ROW_TYPE.GREATER_EQUAL, getLowerRowLabel(cc) );
		addRow( uCoeffs, 0, LPSolver.ROW_TYPE.GREATER_EQUAL, getUpperRowLabel(cc) );
		m_ccIndex.put( cc.getOrder(), cc );
	}

	@Override
	public void enforceRows(ConditionalConstraint cc) {
		
		enforceRow(GLPK.glp_find_row( getModel(), getLowerRowLabel( cc ) ));
		enforceRow(GLPK.glp_find_row( getModel(), getUpperRowLabel( cc ) ));
	}

	private void enforceRow(int rowIndex) {

		if (rowIndex > 0) {
			GLPK.glp_set_row_bnds( getModel(), rowIndex, GLPKConstants.GLP_LO, 0d, Double.POSITIVE_INFINITY );
		}
	}
	
	private void relaxRow(int rowIndex) {

		if (rowIndex > 0) {
			GLPK.glp_set_row_bnds( getModel(), rowIndex, GLPKConstants.GLP_FR, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY );
		}
	}	

	@Override
	public ConditionalConstraint getConstraint(int rowIndex) {

		String label = GLPK.glp_get_row_name( getModel(), rowIndex + getFirstRowIndex() );
		int suffIndex = -1;
		
		if (label != null && ((suffIndex = label.lastIndexOf( '_' )) >= 0)) {
			
			String id = label.substring( 0, suffIndex );
			
			try {
				
				int ccID = Integer.valueOf( id );
				
				return m_ccIndex.get( ccID );
				
			} catch( NumberFormatException e ) {
				
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public List<ConditionalConstraint> getConstraintList() {
		//There's some overhead here for avoiding duplicates
		Set<ConditionalConstraint> ccSet = new HashSet<ConditionalConstraint>();
		List<ConditionalConstraint> ccList = new ArrayList<ConditionalConstraint>();
		
		for (int i = 0; i < getRowNumber(); i++) {
			
			ConditionalConstraint cc = getConstraint( i );
			
			if (cc != null && !ccSet.contains( cc )) {
				
				ccList.add( cc );
				ccSet.add( cc );
			}
		}
		
		return ccList;
	}

	@Override
	public double[] getConstraintLowerRow(ConditionalConstraint cc) {	
		assertIndex();
		glp_prob model = getModel();
		
		int index = GLPK.glp_find_row( model, getLowerRowLabel( cc ) );
		
		if (index <= 0) {
			return null;
		}
		else {
			double[] coeffs = GLPKUtils.getRowCoefficients( model, index );
			
			return coeffs;
		}
	}

	@Override
	public double[] getConstraintUpperRow(ConditionalConstraint cc) {
		assertIndex();
		int index = GLPK.glp_find_row( getModel(), getUpperRowLabel( cc ) );
		
		return index > 0 ? GLPKUtils.getRowCoefficients( getModel(), index ) : null;
	}

	@Override
	public void relaxRows(ConditionalConstraint cc) {
		assertIndex();
		relaxRow(GLPK.glp_find_row( getModel(), getLowerRowLabel( cc ) ));
		relaxRow(GLPK.glp_find_row( getModel(), getUpperRowLabel( cc ) ));		
	}

	@Override
	public void removeColumns(String namePrefix) {
		assertIndex();
		
		SWIGTYPE_p_int delIndex = GLPK.new_intArray( getColumnNumber() );
		int ind = 1;
		
		for (int i = getFirstColumnIndex(); i <= getColumnNumber(); i++) {
			
			String colName = GLPK.glp_get_col_name( getModel(), i );
			
			if (colName != null && colName.startsWith( namePrefix )) {
				GLPK.intArray_setitem( delIndex, ind++, i );
			}
		}
				
		if (ind > 1) {
			GLPK.glp_del_cols( getModel(), ind - 1, delIndex );	
		}
		
		GLPK.delete_intArray( delIndex );
		getSimplexCtrl().setPresolve( GLPKConstants.GLP_ON );
	}

	@Override
	public void setBoundingRows() {
		//The order is important for column generators
		double[] row = new double[getColumnNumber()];
		
		try {
			Arrays.fill( row, -1d );
			addRow( row, -1d, LPSolver.ROW_TYPE.GREATER_EQUAL, U_ROW );	
			Arrays.fill( row, 1d );
			addRow( row, 1d, LPSolver.ROW_TYPE.GREATER_EQUAL, L_ROW );
		} catch( CPException e ) {

			m_logger.fatal( e );
			throw new RuntimeException( e );
		}
	}

	@Override
	public void setLowerBoundingRow() {
		assertIndex();
		
		if (GLPK.glp_find_row( getModel(), L_ROW ) <= 0) {
			
			try {
				
				double[] row = new double[getColumnNumber()];

				Arrays.fill( row, 1d );
				addRow( row, 1d, LPSolver.ROW_TYPE.GREATER_EQUAL, L_ROW );
			} catch( CPException e ) {

				m_logger.fatal( e );
				throw new RuntimeException( e );
			}
		}
	}

	@Override
	public void setUpperBoundingRow() {	
		assertIndex();
		
		if (GLPK.glp_find_row( getModel(), U_ROW ) <= 0) {
			
			try {
				
				double[] row = new double[getColumnNumber()];

				Arrays.fill( row, -1d );
				addRow( row, -1d, LPSolver.ROW_TYPE.GREATER_EQUAL, U_ROW );
			} catch( CPException e ) {

				m_logger.fatal( e );
				throw new RuntimeException( e );
			}
		}
	}

	@Override
	public void unsetBoundingRows() {

		assertIndex();
		
		try {
			
			removeRow( L_ROW );
			removeRow( U_ROW );
			
		} catch( CPException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException( e );
		}
	}
}