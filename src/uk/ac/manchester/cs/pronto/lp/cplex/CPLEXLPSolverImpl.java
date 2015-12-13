/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp.cplex;

import ilog.concert.IloColumn;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloLinearNumExprIterator;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloObjective;
import ilog.concert.IloObjectiveSense;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.lp.LPSolver;
import uk.ac.manchester.cs.pronto.util.ArrayUtils;
import uk.ac.manchester.cs.pronto.util.NumberUtils;

/**
 * LP solver based on ILOG CPLEX
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class CPLEXLPSolverImpl implements LPSolver {

	private Logger	m_logger	= Logger.getLogger( CPLEXLPSolverImpl.class );

	private String m_basisFilename = "basis.bas";
	private IloCplex m_model = null;
	private Map<String, IloNumVar> m_nameVarMap = new HashMap<String, IloNumVar>();
	private List<IloNumVar> m_vars = new ArrayList<IloNumVar>();
	private Map<String, IloRange> m_nameRangeMap = new HashMap<String, IloRange>();
	private List<IloRange> m_ranges = new ArrayList<IloRange>();
	private double[] m_objCoeffs = new double[]{};
	private boolean	m_complete;
	
	public CPLEXLPSolverImpl() {
		
		try {
			
			initModel();
			
		} catch( IloException e ) {

			m_logger.fatal( e );
			throw new RuntimeException(e);
		}
	}
	
	public CPLEXLPSolverImpl(int varNum) {
		
		try {
			
			initModel();
			initVariables( varNum );
			
		} catch( IloException e ) {

			m_logger.fatal( e );
			throw new RuntimeException(e);
		}
	}
	
	protected IloCplex getModel() {
		
		return m_model;
	}
	
	public double[] getObjective() {
		
		return m_objCoeffs;
	}
	
	protected void initModel() throws IloException {
		
		m_model = new IloCplex();
		
		m_model.setParam( IloCplex.DoubleParam.EpRHS, PRECISION_THRESHOLD );
	}
	
	protected void initVariables(int varNum) throws IloException {
		
		for (int i = 0; i < varNum; i++) {
			
			addVar( m_model.numVar( 0d, 1d, IloNumVarType.Float ) );
		}
	}	
	
	/**
	 * @param column
	 * @param name
	 * @return Index of the newly added column
	 * @throws CPException
	 */
	@Override
	public int addColumn(double[] column, String name) throws CPException {
		
		try {
			
			IloObjective obj = m_model.getObjective();
			IloColumn col = m_model.column( obj, column[0] );
			int index = 1;
			
			for (IloRange range : m_ranges) {
				//Install the new column at every constraint
				col = col.and( m_model.column( range, column[index++] ) );
			}
			
			addVar( m_model.numVar(col, 0.0, 1.0, name));
			m_objCoeffs = ArrayUtils.add(m_objCoeffs, column[0]);//TODO This must be damn fucking slow!
			
			return getColumnNumber() - 1 + getFirstColumnIndex();
			
		} catch( IloException e ) {

			m_logger.fatal( e );
			
			throw new CPException(e);
		}
	}

	/**
	 * @param coeffs
	 * @param rhs
	 * @param type
	 * @param name
	 * @return
	 * @throws CPException
	 */
	@Override
	public int addRow(double[] coeffs, double rhs, ROW_TYPE type, String name) throws CPException {
		
		try {
			
			addRange(getVarArray(), coeffs, rhs, type, name);
			
			return getRowNumber() - 1 + getFirstRowIndex();
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new CPException(e);
		}
	}

	private void addRange(IloNumVar[] vars, double[] coeffs, double rhs, LPSolver.ROW_TYPE type, String name) throws IloException {
		
		IloNumExpr rangeExpr = m_model.scalProd( coeffs, vars );
		IloRange range = null;
		
		switch(type) {
		
		case EQUAL:
			
			range = m_model.addEq( rangeExpr, rhs, name );
			break;
			
		case GREATER_EQUAL:				

			range = m_model.addGe( rangeExpr, rhs, name );
			break;
			
		case LESS_EQUAL:				

			range = m_model.addLe( rangeExpr, rhs, name );
			break;
		}
		
		addRange( range );		
	}
	
	protected void addRange(IloNumVar[] vars, double[] coeffs, double lb, double ub, String name) throws IloException {
		
		IloNumExpr expr = m_model.scalProd( coeffs, vars );
		IloRange range = NumberUtils.equal( lb, ub, PRECISION_THRESHOLD )
			? m_model.addEq( expr, lb, name )
			: m_model.addRange( lb, expr, ub, name );

		addRange( range );		
	}	

	/**
	 * 
	 */
	@Override
	public void dispose() {
		
		try {

			if( m_model != null ) {

				removeAllRanges();
				removeAllVars();
				m_model.clearModel();
			}

		} catch( IloException e ) {

			m_logger.fatal( e );
			
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return
	 */
	@Override
	public double[] getAssignment() {
		
		try {
			
			//writeLP( "test.lp" );
			assertSolved();

			return m_model.getValues( getVarArray() ); 
			
		} catch( Exception e ) {
			
			m_logger.fatal( e );
			
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param index
	 * @return
	 */
	@Override
	public String getColumnName(int index) {
		
		return getVar(index).getName();
	}

	/**
	 * @return
	 */
	@Override
	public int getColumnNumber() {
		
		return m_vars.size();
	}

	/**
	 * @param index
	 * @return
	 */
	@Override
	public double getColumnReducedCost(int index) {
		
		try {
			
			return m_model.getReducedCost( getVar(index) );
			
		} catch( Exception e ) {
			
			m_logger.fatal( e );
			
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return
	 */
	@Override
	public double[] getDuals() {

		try {

			assertSolved();
			
			double[] duals = new double[m_ranges.size()];
			
			for (int i = 0; i < m_ranges.size(); i++) duals[i] = m_model.getDual( m_ranges.get(i) );
			
			return duals;

		} catch( Exception e ) {

			m_logger.fatal( e );

			throw new RuntimeException( e );
		}
	}

	/**
	 * @return
	 */
	@Override
	public int getFirstColumnIndex() {
		
		return 0;
	}

	/**
	 * @return
	 */
	@Override
	public int getFirstRowIndex() {
		
		return 0;
	}

	/**
	 * @return
	 */
	@Override
	public boolean getMaximize() {
		
		try {
			
			IloObjective obj = m_model.getObjective();
			
			return obj == null ? true : obj.getSense().equals( IloObjectiveSense.Maximize );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );

			throw new RuntimeException( e );
		}
	}

	/**
	 * @param index
	 * @return
	 */
	@Override
	public double getObjectiveCoeff(int index) {
		
		return m_objCoeffs[index];
	}

	/**
	 * @param index
	 * @return
	 */
	@Override
	public String getRowName(int index) {
		
		return getRange( index ).getName();
	}

	/**
	 * @return
	 */
	@Override
	public int getRowNumber() {
		
		return m_ranges.size();
	}

	/**
	 * @param colNum
	 */
	@Override
	public void initLPInstance(int colNum) {
		
		try {

			initModel();
			initVariables(colNum);
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException( e );
		}
	}

	/**
	 * @param colIndex
	 * @return
	 */
	@Override
	public boolean isColumnBasic(int colIndex) {
		
		try {
			
			return m_model.getBasisStatus( getVar( colIndex ) ).equals( IloCplex.BasisStatus.Basic );
			
		} catch( IloException e ) {

			m_logger.fatal( e );
			throw new RuntimeException( e );			
		}
	}

	/**
	 * @return
	 */
	@Override
	public boolean isComplete() {
		
		return m_complete;
	}

	/**
	 * @param index
	 * @throws CPException
	 */
	@Override
	public void removeColumn(int index) throws CPException {
		
		IloNumVar var = getVar( index );
		
		try {
			
			m_model.delete( var );
			removeVar( index );
			ArrayUtils.remove( m_objCoeffs, index );
			
		} catch( IloException e ) {

			m_logger.fatal( e );
			throw new CPException( e );
		}
	}

	/**
	 * @param name
	 * @throws CPException
	 */
	@Override
	public void removeColumn(String name) throws CPException {
		
		try {
			
			IloNumVar var = getVar( name );
			int index = getVarIndex( var );
			
			m_model.delete( var );
			removeVar( index );
			ArrayUtils.remove( m_objCoeffs, index );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new CPException( e );
		}
	}

	/**
	 * @param indexes
	 * @throws CPException
	 */
	@Override
	public void removeColumns(int[] indexes) throws CPException {
		
		int delCnt = 0;
		
		Arrays.sort( indexes );
		
		try {
			
			for (int index : indexes) {
				
				IloNumVar var = getVar( index - delCnt );
				
				m_model.delete( var );
				removeVar( index - delCnt );
				delCnt += 1;
			}
			
			m_objCoeffs = getVarCoefficients( (IloLinearNumExpr)m_model.getObjective().getExpr() );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new CPException( e );
		}
	}

	public void removeColumns(String namePrefix) {
		
		try {
			
			for (Iterator<IloNumVar> varIter = m_vars.iterator(); varIter.hasNext();) {
				
				IloNumVar var = varIter.next();
				String name = var.getName();
				
				if (name != null && name.startsWith( namePrefix )) {

					m_model.delete( var );
					m_nameVarMap.remove( var.getName() );
					varIter.remove();
				}
			}
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException( e );
		}
	}
	
	/**
	 * @param index
	 * @throws CPException
	 */
	@Override
	public void removeRow(int index) throws CPException {
		
		try {
			
			removeRange( index );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new CPException( e );
		}
	}

	/**
	 * @param name
	 * @throws CPException
	 */
	@Override
	public void removeRow(String name) throws CPException {
		
		try {
			
			IloRange range = getRange( name );
			
			removeRange( range );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new CPException( e );
		}		
	}

	/**
	 * @param indexes
	 * @throws CPException
	 */
	@Override
	public void removeRows(int[] indexes) throws CPException {
		
		int delCnt = 0;
		
		Arrays.sort( indexes );
		
		for (int index : indexes) {
			
			removeRow( index - delCnt );
			delCnt += 1;
		}
	}

	/**
	 * 
	 */
	@Override
	public void restoreBasis() {
		
		try {
			
			m_model.readBasis( m_basisFilename );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException( e );
		}
	}

	/**
	 * 
	 */
	@Override
	public void saveBasis() {
		
		try {
			
			m_model.writeBasis(m_basisFilename);
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException( e );
		}
	}

	/**
	 * @param complete
	 */
	@Override
	public void setComplete(boolean complete) {
		
		m_complete = complete;
	}

	/**
	 * @param max
	 * @throws CPException
	 */
	@Override
	public void setMaximize(boolean max) throws CPException {
		
		try {
			
			m_model.delete( m_model.getObjective() );
			setObjective( m_objCoeffs, max );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new CPException( e );
		}
	}

	private void setObjective(double[] coeffs, boolean max) throws IloException{
		
		if (coeffs.length == getColumnNumber()) {
			
			IloNumExpr objExpr = m_model.scalProd( coeffs, getVarArray() );

			if (coeffs != m_objCoeffs)	m_objCoeffs = ArrayUtils.clone( coeffs );
			
			if (max) {
				
				m_model.addMaximize( objExpr );
				
			} else  m_model.addMinimize( objExpr );
			
		} else {
			
			throw new IllegalArgumentException("Wrong number of objective coefficients");
		}		
	}
	
	/**
	 * @param objective
	 * @throws CPException
	 */
	@Override
	public void setObjective(double[] objective) throws CPException {
		
		try {
			
			IloObjective oldObj = m_model.getObjective();
			
			if (oldObj != null) m_model.delete( oldObj );
			
			setObjective( objective, getMaximize() );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new CPException( e );
		}
	}

	/**
	 * @param index
	 * @param coeff
	 * @throws CPException
	 */
	@Override
	public void setObjectiveCoeff(int index, double coeff) throws CPException {
		
		try {
			
			m_objCoeffs[index] = coeff;
			setObjective( m_objCoeffs, getMaximize() );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new CPException( e );
		}
	}

	/**
	 * @param rowIndex
	 * @param bound
	 * @param bound2
	 */
	@Override
	public void setRowBounds(int rowIndex, double lb, double ub) {
		
		try {
			
			IloRange range = getRange( rowIndex );
			
			range.setBounds( lb, ub );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException( e );
		}
	}

	/**
	 * @param index
	 * @param bound
	 * @throws CPException
	 */
	@Override
	public void setRowUpperBound(int index, double ub) throws CPException {
		
		try {
			
			IloRange range = getRange( index );
			
			range.setUB( ub );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new CPException( e );
		}		
	}

	/**
	 * @param varIndex
	 * @param bound
	 */
	@Override
	public void setVariableLowerBound(int varIndex, double lb) {
		
		try {
			
			IloNumVar var = getVar( varIndex );
			
			var.setLB( lb );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException( e );
		}
	}

	/**
	 * @param varIndex
	 * @param bound
	 */
	@Override
	public void setVariableUpperBound(int varIndex, double ub) {
		
		try {
			
			IloNumVar var = getVar( varIndex );
			
			var.setUB( ub );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException( e );
		}		
	}

	/**
	 * @return
	 * @throws CPException
	 */
	@Override
	public STATUS solveLP() {
		
		try {
			
			long ts = System.currentTimeMillis();
			
			//getModel().setParam( IloCplex.BooleanParam.PerInd, true );
			//writeLP("test_lp.lp");
			m_model.solve();
			
			ts = System.currentTimeMillis() - ts;
			
			return getStatus();

		} catch( IloException e ) {
			
			m_logger.fatal( e );
			writeLP("wrong_lp.lp");
			
			throw new RuntimeException( e );
		}
	}

	public void writeLP(String filename) {
		
		try {
			
			m_model.exportModel( filename );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			e.printStackTrace();
		}
	}
	
	protected void addVar(IloNumVar var) {
		
		m_nameVarMap.put( var.getName(), var );
		m_vars.add( var );
	}
	
	protected void removeVar(IloNumVar var) {
		
		m_vars.remove( var );
		m_nameVarMap.remove( var.getName() );
	}
	
	protected void removeVar(int index) {
		
		IloNumVar var = m_vars.get( index );
		
		m_vars.remove( index );
		m_nameVarMap.remove( var.getName() );
	}	
	
	protected IloNumVar[] getVarArray() {
		
		return m_vars.toArray( new IloNumVar[]{} );
	}
	
	protected int getVarNumber() {
		
		return m_vars.size();
	}
	
	protected void removeAllVars() {
		
		m_vars.clear();
		m_nameVarMap.clear();
	}
	
	protected IloNumVar getVar(int index) {
		
		return m_vars.get( index );
	}
	
	protected IloNumVar getVar(String name) {
		
		return m_nameVarMap.get( name );
	}
		
	protected int getVarIndex(IloNumVar var) {
		//TODO Keep the m_vars array sorted and use binary search here!
		return m_vars.indexOf( var );
	}
	
	
	protected void addRange(IloRange range) {
		
		m_nameRangeMap.put( range.getName(), range );
		m_ranges.add( range );
	}
	
	protected void removeRange(IloRange range) throws IloException  {
		
		m_model.delete( range );
		m_ranges.remove( range );
		m_nameRangeMap.remove( range.getName() );
	}
	
	protected void removeRange(int index) throws IloException {
		
		IloRange range = m_ranges.get( index );
		
		m_model.delete( range );
		m_ranges.remove( index );
		m_nameRangeMap.remove( range.getName() );
	}	
	
	protected void removeAllRanges() throws IloException {
		
		m_model.delete( this.getRangeArray() );
		m_ranges.clear();
		m_nameRangeMap.clear();
	}
	
	protected IloRange getRange(int index) {
		
		return m_ranges.get( index );
	}
	
	protected IloRange getRange(String name) {
		
		return m_nameRangeMap.get( name );
	}
		
	protected int getRangeIndex(IloRange range) {
		//TODO Keep the m_vars array sorted and use binary search here!
		return m_ranges.indexOf( range );
	}	
	
	protected IloRange[] getRangeArray() {
		
		return m_ranges.toArray( new IloRange[] {} );
	}
	
	protected List<IloRange> getRangeList() {
		
		return m_ranges;
	}	
	
	protected double[] getVarCoefficients(IloLinearNumExpr expr) {
		
		double[] coeffs = new double[m_vars.size()];
		
		for (IloLinearNumExprIterator iter = expr.linearIterator(); iter.hasNext(); ) {
			
			coeffs[getVarIndex( iter.nextNumVar() )] = iter.getValue();
		}
		
		return coeffs;
	}

	/*
	 * Solves the model if the solution is not available
	 */
	protected void assertSolved() throws IloException {
		
		m_model.solve();
	}
	
	public double getPrecision() {
		
		return PRECISION_THRESHOLD;
	}

	@Override
	public double getObjValue() {
		
		try {
			
			//assertSolved();
			
			return m_model.getObjValue();
			
		} catch( IloException e ) {

			m_logger.fatal( e );
			throw new RuntimeException( e );
		}
	}

	@Override
	public STATUS getStatus() {

		IloCplex.Status cplexStat = null;
		STATUS stat = null;
		
		try {
			
			//assertSolved();
			cplexStat = m_model.getStatus();
			
		} catch( IloException e ) {

			m_logger.fatal( e );
			throw new RuntimeException( e );
		}		

		if (cplexStat == IloCplex.Status.Optimal) {
		
			stat = STATUS.OPTIMAL;
			
		} else if (cplexStat == IloCplex.Status.Infeasible ) {
			
			stat = STATUS.INFEASIBLE;
			
		} else if (cplexStat == IloCplex.Status.Feasible ) {
			
			stat = STATUS.FEASIBLE;
			
		} else if (cplexStat == IloCplex.Status.Unknown ) {
			
			stat = STATUS.UNDEFINED;
			
		} else {
			
			stat = STATUS.OTHER;
		}
		
		return stat;
	}
}
