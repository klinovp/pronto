/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp.cplex;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.lp.CCAwareLPSolver;
import uk.ac.manchester.cs.pronto.lp.LPSolver;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class CCAwareCPLEXLPSolverImpl extends CPLEXLPSolverImpl implements CCAwareLPSolver {

	private Logger	m_logger = Logger.getLogger( CCAwareCPLEXLPSolverImpl.class );
	private final String L_ROW = "L_ROW_NORM";
	private final String U_ROW = "U_ROW_NORM";
	//private final String AUX_VAR = "AUX_VAR_";
	
	private Map<IloRange, ConditionalConstraint> m_rangeMap = new HashMap<IloRange, ConditionalConstraint>();
	
	@Override
	public void addRows(ConditionalConstraint cc, double[] lCoeffs, double[] uCoeffs)  throws CPException {
		
		String[] labels = getLabels(cc);
		int indexL = 0;
		int indexU = 0;
		
		//if (cc.isConditional()) {
			//Have to use 2 rows for conditional constraints
			indexL = addRow( lCoeffs, 0, LPSolver.ROW_TYPE.GREATER_EQUAL, labels[0] );
			indexU = addRow( uCoeffs, 0, LPSolver.ROW_TYPE.GREATER_EQUAL, labels[1] );
			
		/*} else {
			//TODO May have only a single double bounded row here
			indexL = addRow( lCoeffs, cc.getLowerBound(), LIInstance.GREATER_EQUAL, labels[0] );
			indexU = addRow( uCoeffs, cc.getUpperBound(), LIInstance.LESS_EQUAL, labels[1] );			
		}*/
		
		m_rangeMap.put( getRange(indexL), cc );
		m_rangeMap.put( getRange(indexU), cc );
	}

	/**
	 * @param cc
	 */
	@Override
	public void enforceRows(ConditionalConstraint cc) {
		
		String[] labels = getLabels(cc);
		
		try {
		
			//if (cc.isConditional()) {
				
				super.getRange( labels[0] ).setLB( 0d );
				super.getRange( labels[1] ).setLB( 0d );
				
			/*} else {
				
				super.getRange( labels[0] ).setBounds( cc.getLowerBound(), cc.getUpperBound() );
				super.getRange( labels[1] ).setBounds( cc.getLowerBound(), cc.getUpperBound() );
			}*/
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException( e );
		}
	}

	/**
	 * @param cc
	 */
	@Override
	public void relaxRows(ConditionalConstraint cc) {
		
		String[] labels = getLabels(cc);

		try {
			
			super.getRange( labels[0] ).setBounds( Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY );
			super.getRange( labels[1] ).setBounds( Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException( e );
		}		
	}

	private String[] getLabels(ConditionalConstraint cc) {
		
		return new String[] {"cc" + cc.getOrder() + "L", "cc" + cc.getOrder() + "U"};
	}

	@Override
	public double[] getConstraintLowerRow(ConditionalConstraint cc) {
		
		return getRangeCoefficients(getRange( getLabels(cc)[0] ));
	}

	@Override
	public double[] getConstraintUpperRow(ConditionalConstraint cc) {
		
		return getRangeCoefficients(getRange( getLabels(cc)[1] ));
	}
	
	private double[] getRangeCoefficients(IloRange range) {
		
		try {
			
			return super.getVarCoefficients( (IloLinearNumExpr)range.getExpr() );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException( e );
		}
	}

	@Override
	public ConditionalConstraint getConstraint(int rowIndex) {
		
		return m_rangeMap.get( getRange( rowIndex ) );
	}

	@Override
	protected void removeRange(IloRange range) throws IloException {
		
		if (range != null) {
			
			m_rangeMap.remove( range );
			super.removeRange( range );
		}
	}

	@Override
	protected void removeRange(int index) throws IloException {
		
		removeRange( getRange( index ) );
	}

	@Override
	public List<ConditionalConstraint> getConstraintList() {
		//There's some overhead here for avoiding duplicates
		Set<ConditionalConstraint> ccSet = new HashSet<ConditionalConstraint>();
		List<ConditionalConstraint> ccList = new ArrayList<ConditionalConstraint>();
		
		for (IloRange range : getRangeList()) {
			
			ConditionalConstraint cc = m_rangeMap.get( range );
			
			if (cc != null) {
				
				if (!ccSet.contains( cc )) {
			
					ccList.add( cc );
					ccSet.add( cc );
				}
			}
		}
		
		return ccList;
	}

	@Override
	public void setBoundingRows() {
		//The order is important for column generators
		try {

			double[] row = new double[getColumnNumber()];

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
		
		if( getRange( L_ROW ) == null ) {

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
		
		if( getRange( U_ROW ) == null ) {
			
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
		
		try {
			
			removeRange(getRange(L_ROW));
			removeRange(getRange(U_ROW));
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException( e );
		}
	}
//The code below is needed if unconditional constraints are handled in a special way
/*	public void addExtraVarsForFeasibility() {

		double[] columnCoeffs = new double[1 + getRowNumber()];
		int rowIndex = 1;
		//Auxiliary variables are negative in the objective function
		columnCoeffs[0] = -1d;
	
		try {
			
			for (IloRange range : getRangeList()) {
				//We add a new auxiliary variable to every row with non-zero lower bound				
				if (NumberUtils.greater(range.getLB(), 0d)) {

					columnCoeffs[rowIndex] = 1d;
					addColumn( columnCoeffs, AUX_VAR + range.getName() );
					columnCoeffs[rowIndex] = 0d;
				}
				
				rowIndex += 1;
			}
			//The last row is special, we need one extra variable here
			columnCoeffs[columnCoeffs.length - 1] = -1d;
			addColumn( columnCoeffs, AUX_VAR + "LAST" );
			
			//writeLP( "fuck.lp" );
			
		} catch( IloException e ) {

			m_logger.fatal( e );
			throw new RuntimeException(e);
			
		} catch( CPException e ) {

			m_logger.fatal( e );
			throw new RuntimeException(e);
		}
	}

	@Override
	public void removeExtraVars() {
		
		//writeLP( "fuck2.lp" );
		super.removeColumns( AUX_VAR );
		//writeLP( "fuck3.lp" );
	}*/	
}
