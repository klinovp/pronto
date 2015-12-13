/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.lp.LPSolver;
import uk.ac.manchester.cs.pronto.lp.MIPSolver;
import uk.ac.manchester.cs.pronto.lp.MIPSolverEx;
import uk.ac.manchester.cs.pronto.lp.glpk.GLPKMIPSolverImpl;
import uk.ac.manchester.cs.pronto.util.NumberUtils;

/**
 * Iterative algorithm which uses an MIP solver to compute minimal hitting sets
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class IterativeMIPBasedHittingSetAlgorithm<T> implements IterativeHittingSetAlgorithm<T> {

	private Set<Set<T>> m_sets = null;
	private Set<T> m_nextHS = null;
	private MIPSolverEx m_model = null;
	private Map<T, Integer> m_orderMap = new HashMap<T, Integer>();
	private Boolean m_hasNext = null;
	

	protected void prepare() {
		
		LPSolver.STATUS status = null;
		
		if (m_sets == null || m_sets.isEmpty()) throw new RuntimeException("Sets are not set");
		
		try {
			
			if (null == m_model) {
				
				m_model = createMIPmodel(m_sets, m_orderMap);
			} 
			
			status = m_model.solveMIP();
			
		} catch( CPException e ) {
			//TODO add logging
			throw new RuntimeException(e);
		}
		
		if (status == LPSolver.STATUS.OPTIMAL) {
		
			m_nextHS = extractSolution(m_model, m_orderMap);
			m_hasNext = true;
			
		} else if (status == LPSolver.STATUS.INFEASIBLE) {
			//No solution exists
			m_hasNext = false;
			
		} else {
			// Should never happen. TODO add logging
			throw new RuntimeException("MIP model can't be solved");
		}
	}
	
	/**
	 * @param sets
	 */
	@Override
	public void setSets(Collection<Set<T>> sets) {
		
		int order = 0;
		
		if (m_model != null) m_model.dispose(); 
		
		m_sets = new HashSet<Set<T>>(sets);
		m_model = null;
		m_nextHS = null;
		m_hasNext = null;
		m_orderMap.clear();
		
		for (Set<T> set : sets) {
			
			for (T element : set) {
				
				if (!m_orderMap.containsKey( element )) {
					
					m_orderMap.put( element, order++ );
				}
			}
		}
	}	
	
	public boolean hasNext() {
		
		if (m_hasNext == null) {
			
			prepare();
		}
		
		return m_hasNext; 	
	}
	
	/**
	 * @return
	 */
	@Override
	public Set<T> next() {
		
		Set<T> result = null; 
		
		if (m_nextHS == null) {
			
			prepare();
		}
		
		result = m_nextHS; 
		
		try {
			
			addConstraint(m_model, m_nextHS, m_orderMap);
			
		} catch( CPException e ) {
			//TODO add logging
			throw new RuntimeException(e);
		}
		
		m_nextHS = null;
		m_hasNext = null;
		
		return result; 
	}

	private MIPSolverEx createMIPmodel(Set<Set<T>> sets, Map<T, Integer> orderMap) throws CPException {
		
		MIPSolverEx model = new GLPKMIPSolverImpl();
		int varNum = orderMap.size();
		MIPSolver.VAR_TYPE[] varTypes = new MIPSolver.VAR_TYPE[varNum];
		double[] obj = new double[varNum];
		double[] row = new double[varNum];
		//Initialize model
		Arrays.fill( obj, 1d );
		Arrays.fill(varTypes, MIPSolver.VAR_TYPE.BINARY);//All variables are binary
		model.initMIPInstance( varTypes );
		model.setObjective( obj );
		//Add constraints to express that all sets have to be hit by the solution
		for (Set<T> set : sets) {
			
			Arrays.fill( row, 0d );
			
			for (T element : set) row[orderMap.get( element )] = 1d;
			
			model.addRow( row, 1d, LPSolver.ROW_TYPE.GREATER_EQUAL, null );
		}
		//Total number of elements in the hitting sets need to be minimized
		model.setMaximize( false );
		
		return model;
	}

	/*
	 * Adds an extra constraint to the model to cut off the last solution (the hitting set last found)
	 */
	private void addConstraint(MIPSolverEx model, Set<T> hSet, Map<T, Integer> orderMap) throws CPException {
		
		double[] rowCoeffs = new double[model.getObjective().length];
		
		for (T element : hSet) rowCoeffs[orderMap.get( element )] = 1d;
		
		model.addRow( rowCoeffs, hSet.size() - 1, LPSolver.ROW_TYPE.LESS_EQUAL, null);
	}

	/*
	 * Extracts the resulting hitting set from a successfully solved MIP model
	 */
	private Set<T> extractSolution(MIPSolverEx model, Map<T, Integer> orderMap) {
		
		double[] solution = model.getAssignment();
		Set<T> hSet = new HashSet<T>();
		
		for (T element : orderMap.keySet()) {
			//Take elements which correspond to variables being equal to 1 in the optimal solution
			if (NumberUtils.equal(1d, solution[orderMap.get(element )])) {
				
				hSet.add(element);
			}
		}
		
		return hSet;
	}
}