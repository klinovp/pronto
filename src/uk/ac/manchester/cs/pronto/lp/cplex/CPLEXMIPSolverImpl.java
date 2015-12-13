/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp.cplex;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.lp.MIPSolver;
import uk.ac.manchester.cs.pronto.lp.MIPSolverEx;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class CPLEXMIPSolverImpl extends CPLEXLPSolverImpl implements MIPSolverEx {

	private Logger	m_logger	= Logger.getLogger( CPLEXMIPSolverImpl.class );
	
	/**
	 * @return
	 */
	@Override
	public long getTimeout() {
		
		try {
			
			return (long)getModel().getParam( IloCplex.DoubleParam.TiLim );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param gap
	 */
	@Override
	public void setMIPGap(double gap) {
		
		try {
			
			getModel().setParam( IloCplex.DoubleParam.EpGap, gap );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param timeout
	 */
	@Override
	public void setTimeout(long timeout) {
		
		try {
			
			getModel().setParam( IloCplex.DoubleParam.TiLim, timeout / 1000 );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param colNum
	 * @param varTypes
	 */
	@Override
	public void initMIPInstance(VAR_TYPE[] varTypes) {
		
		try {
			
			dispose();
			initModel();
			initVariables( varTypes );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException(e);
		}
	}

	private IloNumVarType getCPLEXvarType(VAR_TYPE type) {

		switch(type) {
		
		case GENERAL: return IloNumVarType.Float;
		case INTEGER: return IloNumVarType.Int;
		case BINARY: return IloNumVarType.Bool;
		
		}

		throw new RuntimeException("Unknown variable type");
	}

	/**
	 * @return
	 * @throws CPException
	 */
	@Override
	public STATUS solveMIP() {
		
		IloCplex model = getModel();
		
		try {

			//writeLP( "test_mip.lp" );
			model.solve();
			getModel().setParam( IloCplex.IntParam.PopulateLim, 500 );
			getModel().setParam( IloCplex.IntParam.SolnPoolIntensity, 4 );
			getModel().setParam( IloCplex.IntParam.SolnPoolReplace, 2 );
			getModel().setParam( IloCplex.DoubleParam.EpGap, 0.9 );
			getModel().setParam( IloCplex.DoubleParam.TiLim, 1 );
			model.populate();
			getModel().setParam( IloCplex.DoubleParam.EpGap, 0d );
			getModel().setParam( IloCplex.DoubleParam.TiLim, 1000 );
			
			return getStatus();
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException( e );
		}
	}

	public MIPSolver clone() {
		
		throw new RuntimeException("Currently not supported");
	}

	protected void initVariables(VAR_TYPE[] types) throws IloException {
		
		for (int i = 0; i < types.length; i++) {
			
			addVar( getModel().numVar( 0d, 1d, getCPLEXvarType(types[i]) ) );
		}
	}

	@Override
	public int getSolutionPoolSize(int size) {
		
		try {
			
			return getModel().getParam( IloCplex.IntParam.SolnPoolCapacity  );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException(e);
		}
	}

	@Override
	public double[][] getSolutions() {
		
		int solNum = getModel().getSolnPoolNsolns();
		double[][] solutions = new double[solNum][];
		IloNumVar[] vars = getVarArray();
		
		try {
			
			for (int i = 0; i < solNum; i++) {
				
				double[] solution = new double[vars.length];
				
				for (int j = 0; j < vars.length; j++) {
					
					solution[j] = getModel().getValue( vars[j], i );
				}
				
				solutions[i] = solution;
			}
		} catch( UnknownObjectException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException(e);
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException(e);
		}
		
		return solutions;
	}
	
	public double[] getObjValues() {
	
		int solNum = getModel().getSolnPoolNsolns();
		double[] values = new double[solNum];
		
		try {
			
			for (int i = 0; i < solNum; i++) {
				
				values[i] = getModel().getObjValue( i );
			}
		}  catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException(e);
		}
		
		return values;		
		
	}

	@Override
	public void setSolutionPoolSize(int size) {
		
		try {
			
			getModel().setParam( IloCplex.IntParam.SolnPoolCapacity, size );
			
		} catch( IloException e ) {
			
			m_logger.fatal( e );
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean supportsSolutionPool() {

		return true;
	}
}
