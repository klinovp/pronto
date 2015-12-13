/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.Constants;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.PTBoxImpl;
import uk.ac.manchester.cs.pronto.events.EVENT_TYPES;
import uk.ac.manchester.cs.pronto.events.EventHandlerWithFeedback;
import uk.ac.manchester.cs.pronto.events.ReasoningEvent;
import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.exceptions.EventHandlingException;
import uk.ac.manchester.cs.pronto.util.ArrayUtils;
import uk.ac.manchester.cs.pronto.util.NumberUtils;

/**
 * This class implements a simple stabilization scheme based on perturbation
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class StabilizedCGLPGeneratorImpl extends CGLPGeneratorImpl {

	private static final String EARLY_CLASH_HIT = "EARLY_CLASH_HITS";
	private static final String EARLY_CLASH_MISS = "EARLY_CLASH_MISSES";
	private static final String S_COL_PREF = "Y";
	private double m_threshold = -2.0d;
	private EventHandlerWithFeedback<Set<ConditionalConstraint>> m_partialPSATchecker = null;
	private Map<String, Double> m_teleMap = new HashMap<String, Double>();
	
	public StabilizedCGLPGeneratorImpl() {
		
		resetMeasures();
		//setPartialPSATEventHandler( new CCSetAnalyzerImpl2() );
	}
	
	protected void setPartialPSATEventHandler(EventHandlerWithFeedback<Set<ConditionalConstraint>> handler) {
		
		m_partialPSATchecker = handler;
	}
	
	protected void setStabilizationThreshold(double threshold) {

		m_threshold = threshold;
	}
	
	@Override
	protected double generateColumns(ColumnGeneratorEx cGen, CCAwareLPSolver solver) throws CPException {
		
		List<double[]> columns = null;
		double lastObjValue = 0;
		double[] lastDuals = null;
		double epsilon = 1d;//Math.min( 1.0E-2, 1d / solver.getRowNumber());//Bounds for the extra variables
		boolean rmpOptimal = false;
		int cgIterCounter = 0;
		int totalIterCounter = 0;
		double[][] recentDuals = new double[5][];
		double[] recentObjValues = new double[recentDuals.length];
		LPSolver.STATUS lastStatus = LPSolver.STATUS.UNDEFINED;

		reset();
		m_teleMap.put( EARLY_CLASH_MISS, 0d );
		getColumnGenerator().setColumnsNumber( 5 );
		m_threshold = getProblemType().equals( PROBLEM_TYPE.PSAT ) ? 1d : -1d;
		
		s_logger.debug( "Initial epsilon: " + epsilon);
		
		long t = System.currentTimeMillis();
		
		//First we add surplus and slack variables		
		addSurplusAndSlackVars(solver, epsilon, solver.getRowNumber() - 2*solver.getConstraintList().size());	
		s_logger.debug( "Extra vars added in " + (System.currentTimeMillis() - t) + " ms"); 
		//Here we start the program generation main loop. We will add
		//columns to RMP until there's no improving column.
		while( !rmpOptimal ) {

			t = System.currentTimeMillis();
			
			s_logger.debug("Column generation step #" + cgIterCounter +
							", RMP: " + solver.getColumnNumber() + "x" + solver.getRowNumber() +
							", epsilon: " + epsilon);
			// Solve RMP to obtain duals
			lastStatus = solver.solveLP();
			lastObjValue = lastStatus == LPSolver.STATUS.OPTIMAL ? solver.getObjValue() : -1d;
			
			s_logger.debug( "RMP solved in: " + (System.currentTimeMillis() - t) + 	" ms, objective value: " + lastObjValue);
			
			lastDuals = solver.getDuals();
			totalIterCounter += 1;
			
			if (lastStatus == LPSolver.STATUS.OPTIMAL) {
				//Maintain the history of recent duals and objective values
				recentDuals[cgIterCounter % recentDuals.length] = lastDuals;
				recentObjValues[cgIterCounter % recentDuals.length] = lastObjValue;
				
				try {
					
					if ((cgIterCounter % recentDuals.length == recentDuals.length - 1) &&
							getProblemType().equals( PROBLEM_TYPE.PSAT ) && m_partialPSATchecker != null &&
							m_teleMap.get( EARLY_CLASH_MISS ) < 10) {
						//Check if it makes sense to try to isolate unsatisfiability
						if (handleStalling(m_partialPSATchecker, recentDuals, recentObjValues, cGen)) {
							//Yep, some subset of the PTBox is unsatisfiable, so we may stop
							rmpOptimal = true;
							break;
						}
					}
				} catch( EventHandlingException e ) {
					s_logger.error( "Error caught during an attempt to isolate unsatisfiability" );
					s_logger.error( e );
					m_partialPSATchecker = null;
				}
				//Keeping RMP small
				//TODO Increase to 100-200 if the new stabilization scheme is used
				if ((Double.valueOf(getMeasure( LPGenerator.TELEMETRY.COL_NUMBER.toString() )).intValue() + 1) % 50 == 0) { 				
					shrinkRMP(solver, solver.getRowNumber() / 2);
				}
			}
			
			t = System.currentTimeMillis();
			
			columns = cGen.generateColumns(lastDuals, solver.getMaximize() );
			
			t = System.currentTimeMillis() - t;
			s_logger.debug( columns.size() + " new column(s) generated in " + t + " ms");				
			
			if( !columns.isEmpty()) {
				//The column is added directly into the LP model
				for (double[] column : columns)	{
					//Check reduced cost
					//double rcost = 1d - ArrayUtils.scalarProduct( ArrayUtils.remove( column, 0 ), lastDuals); 
					solver.addColumn( column, null );
				}
				
				cgIterCounter += 1;	
				//Update telemetry measures
				updateColumnCounter(columns.size());
				updateTotalColGenTime(t);
				
			} else {
				double extraVarValue = extraVarSum( solver );
				epsilon = updateEpsilon(epsilon, rmpOptimal);
				
				if (fullStopOnThreshold(lastObjValue, solver.getMaximize(), m_threshold)) {
					//Full stop, RMP may not be optimal but it's sufficient
					s_logger.debug( "Full stop, RMP's value will never be good enough");
					s_logger.debug( getMeasure(LPGenerator.TELEMETRY.COL_NUMBER.toString()) + " columns generated" );
					s_logger.debug( "Average column generation/validation time: " + getMeasure(LPGenerator.TELEMETRY.TOTAL_COL_GEN_TIME.toString()) );
					rmpOptimal = true;
					
				} else	if (extraVarValue <= Constants.PROBABILITY_LOW_THRESHOLD/* || epsilon < LPSolver.PRECISION_THRESHOLD*/) {
					//Full stop, extra variables' values are too small
					s_logger.debug( "Full stop, RMP is optimal, extra vars sum to " + extraVarValue);
					s_logger.debug( getMeasure(LPGenerator.TELEMETRY.COL_NUMBER.toString()) + " columns generated" );
					s_logger.debug( "Average column generation/validation time: " + getMeasure(LPGenerator.TELEMETRY.TOTAL_COL_GEN_TIME.toString()) );
					rmpOptimal = true;
					
				} else {					
					//solver.saveBasis();
					updateSurplusAndSlackVars(solver, epsilon);
					//solver.restoreBasis();
					s_logger.debug( "\nHurrah, tightening bounds... \n");
					s_logger.debug( "Extra variables sum to " + extraVarValue + "\n");
					cgIterCounter = 0;						
				}
			}
		}
		//Cleanup
		solver.removeColumns( S_COL_PREF );
		
		//((GLPKLPSolverImpl)solver).writeLP( "test2.lp" );
		
		solver.solveLP();
		s_logger.debug( "Optimal value after removing extra vars: " + solver.getObjValue() );
		
		if (getNonZeroIndexes().isEmpty()) {
			
			setNonZeroVarIndexes(ArrayUtils.getSupport(lastDuals) );
		}

		return lastObjValue;
	}
	

	private boolean thresholdSet(double threshold) {
		
		return !(NumberUtils.greater( 0d, threshold ) || NumberUtils.greater( threshold, 1d ));
	}
	
	private boolean fullStopOnThreshold(double obj, boolean max, double threshold) {
	
		return thresholdSet( threshold ) &&	((max && NumberUtils.greater( threshold, obj ))
												|| (!max && NumberUtils.greater( obj, threshold )));
	}

	private double extraVarSum(LPSolver solver) {
		
		double totalValue = 0d;
		double[] solution = solver.getAssignment();
		int numOfCols = solver.getColumnNumber() + solver.getFirstColumnIndex();
		
		for (int i = solver.getFirstColumnIndex(); i < numOfCols; i++) {
			
			String colName = solver.getColumnName( i );
			
			if (colName != null && colName.startsWith( S_COL_PREF )) {
				
				totalValue += Math.abs(solution[i - solver.getFirstColumnIndex()]);
			}
		}		
		
		return totalValue;
	}

	/*
	 * Updates bounds for surplus and slack variables
	 */
	private void updateSurplusAndSlackVars(	LPSolver solver, double epsilon) throws CPException {

		int numOfCols = solver.getColumnNumber() + solver.getFirstColumnIndex();
		//double[] assignment = solver.getAssignment();
		
		for (int i = solver.getFirstColumnIndex(); i < numOfCols; i++) {
			
			String colName = solver.getColumnName( i );
			
			if (colName != null && colName.startsWith( S_COL_PREF )/* && (assignment[i] > LPSolver.PRECISION_THRESHOLD)*/) {
				//Update the upper bound
				solver.setVariableUpperBound(i, epsilon);
			}
		}
	}

	/*
	 * Updates epsilon. If rmpOptimal is true the value is reduced by half
	 */
	private double updateEpsilon(double epsilon, boolean rmpOptimal) {

		if (!rmpOptimal) {
			
			return epsilon * 0.5;
			
		} else return epsilon * 1.1;
	}
	
	/*
	 * Adds surplus and slack variables to the model
	 */
	private void addSurplusAndSlackVars(LPSolver solver, double epsilon, int boundRowNumber) throws CPException {
		
		double[] posColCoeffs = new double[1 + solver.getRowNumber()];
		double[] negColCoeffs = new double[1 + solver.getRowNumber()];
		int labelIndex = 0;
		boolean max = solver.getMaximize();

		Arrays.fill( posColCoeffs, 1d );
		Arrays.fill( negColCoeffs, -1d );
		//If the objective is to maximize then the auxiliary variables are negative
		//(if to minimize then they're positive)
		posColCoeffs[0] = max ? -1d : 1d;
		negColCoeffs[0] = max ? -1d : 1d;
		
		if (boundRowNumber == 2) {
			
			posColCoeffs[posColCoeffs.length - 2] = 0d;
			negColCoeffs[negColCoeffs.length - 2] = 0d;
			posColCoeffs[posColCoeffs.length - 1] = 0d;
			negColCoeffs[negColCoeffs.length - 1] = 0d;
			 			
/*			posColCoeffs[posColCoeffs.length - 2] = -1d;
			negColCoeffs[negColCoeffs.length - 2] = 1d;
			posColCoeffs[posColCoeffs.length - 1] = 1d;
			negColCoeffs[negColCoeffs.length - 1] = -1d;*/			
			
		} else {
			
			posColCoeffs[posColCoeffs.length - 1] = 0d;
			negColCoeffs[negColCoeffs.length - 1] = 0d;			
			 			
/*			posColCoeffs[posColCoeffs.length - 1] = -1d;
			negColCoeffs[negColCoeffs.length - 1] = 1d;*/			
		}
		//TODO Remove (/ k) if the sparse matrix is used
		for (int i = 0; i < (solver.getRowNumber() - boundRowNumber) / 1; i++) {
			
			//posColCoeffs[i + 1] = 1d;
			//negColCoeffs[i + 1] = -1d;			
			
			//Add the variables	
			int colIndex = solver.addColumn( posColCoeffs, S_COL_PREF + "_P_" + labelIndex );
			
			//solver.addColumn( negColCoeffs, S_COL_PREF + "_N_" + labelIndex );
			//Second set bounds
			solver.setVariableUpperBound(colIndex, epsilon );
			//solver.setVariableUpperBound(colIndex + 1, epsilon );
			
			labelIndex += 1;
			
			//posColCoeffs[i + 1] = 0d;
			//negColCoeffs[i + 1] = 0d;			
		}
	}
	
	private void shrinkRMP(LPSolver solver, int maxNumber) throws CPException {
		
		int colNum = solver.getColumnNumber();
		Set<Integer> colSet = new HashSet<Integer>();
		boolean max = solver.getMaximize();
		
		for (int i = solver.getFirstColumnIndex(); i < colNum + solver.getFirstColumnIndex(); i++) {
			
			String colName = solver.getColumnName( i );
			boolean isBasic = solver.isColumnBasic( i );
			double rCost = solver.getColumnReducedCost( i );
			
			if (!isBasic && colName == null &&
					((max && !NumberUtils.greater( rCost, 0d ))
							|| (!max && !NumberUtils.greater( -rCost, 0d )))) {
			
				colSet.add( i );
			}
		}
		
		int[] indexes = new int[colSet.size()];
		int i = 0;
		
		for (int index : colSet) indexes[i++] = index;
		
		solver.removeColumns( indexes );

		s_logger.debug( "\n RMP shrink: removing " + indexes.length + " non-basic columns\n" );
	}
	
	/*
	 * Check what proportion of non-zero dual values persist over a number of iterations 
	 */
	private boolean handleStalling(	EventHandlerWithFeedback<Set<ConditionalConstraint>> psatChecker,
									double[][] recentDuals,
									double[] recentObjValues,
									ColumnGenerator gen) throws EventHandlingException {
		
		Map<Integer, Integer> freqMap = new HashMap<Integer, Integer>();
		Set<Integer> freqVars = new HashSet<Integer>();
		double mean = 0d;
		Set<ConditionalConstraint> conflict = null;
		//First we fill the frequency map (how frequently each dual variable has had a non-zero value) 
		for (double[] duals : recentDuals) {
			
			for (int i = 0; i < duals.length; i++) {
				
				if (!NumberUtils.equal( duals[i], 0d )) {
		
					Integer freq = freqMap.get( i );
					
					freq = freq == null ? 1 : freq + 1;
					freqMap.put( i, freq );
				}
			}
		}
		//Order dual variables by frequency 
		//This could be made more efficient if we had a bi-directional frequency map
		SortedMap<Integer, Set<Integer>> ordered = new TreeMap<Integer, Set<Integer>>();
		
		for (Integer var : freqMap.keySet()) {
			
			 int freq = freqMap.get(var);
			 Set<Integer> vars = ordered.get( freq );
			 
			 vars = vars == null ? new HashSet<Integer>() : vars;
			 vars.add( var );
			 ordered.put( freq, vars );
		}
		//Taking the most frequent variables + getting some statistics
		for (Integer freq : ordered.keySet()) {
			
			 Set<Integer> vars = ordered.get( freq );
			
			 s_logger.debug( freq + " occurrences: " + vars.size() + " vars" );
			 
			 if (freq >= recentDuals.length) freqVars.addAll( vars );
			 
			 mean += freq * vars.size();
		}
		//Here we must decide if we should try to solve a partial PSAT
		Arrays.sort( recentObjValues );
		//Objective progress for the last iterations
		double progress = Math.abs(recentObjValues[0] - recentObjValues[recentObjValues.length-1]) / recentObjValues[0] ;
		
		s_logger.debug("Total number of vars: " + freqMap.size());
		s_logger.debug("Mean: " + mean / freqMap.size());
		s_logger.debug(freqVars.size() + " occurred " + recentDuals.length + " times");
		s_logger.debug(	"Progress during the last : " + recentObjValues.length + " iterations: " + progress * 100 + "%");
		
		//If the progress is slow (<10%) and more than 10% of the dual variables have been
		//non-zero in all recent iterations
		//TODO This parameters MUST be externally configurable!
		if (Math.abs( progress ) < 0.01 &&
				(0.1 < (double) freqVars.size() / freqMap.size()) ) {
			//Do it
			s_logger.info( "Trying PSAT for the following rows: " + freqVars + "..." );
			
			ReasoningEvent event = new ReasoningEvent.SimpleEventImpl(EVENT_TYPES.COL_GENERATION_STALLED);
			Set<ConditionalConstraint> ccSet = new HashSet<ConditionalConstraint>(freqVars.size());
			List<ConditionalConstraint> ccList = gen.getConstraintList();
			PTBox ptbox = gen.getPTBox();
			
			for (Integer varIndex : freqVars) {
				
				if ((varIndex / 2) < ccList.size()) ccSet.add( ccList.get( varIndex / 2 ) );
			}
			
			event.setParameters(new Object[] {new PTBoxImpl(ptbox.getClassicalKnowledgeBase(), ptbox.getClassicalOntology(), ccSet)} );
			conflict = psatChecker.handleEventWithFeedback( event );
			//The event handler returns a set of conflicting constraints
			if (!conflict.isEmpty()) {

				if (!ccSet.containsAll( conflict )) {
					
					throw new RuntimeException("Error");
				}
				
				s_logger.info( "Subset is infeasible, collect indexes of conflicting constraints" );
				
				Set<Integer> nonZeroVars = new HashSet<Integer>(conflict.size());
				//Save the list indexes for conflicting constraints
				for (int index = 0; index < ccList.size(); index++) {
					
					ConditionalConstraint cc = ccList.get( index );
					
					if (conflict.contains( cc )) {
						
						nonZeroVars.add( index * 2 );
						nonZeroVars.add( index * 2 + 1 );
					}
				}
				
				setEarlyConflictDetected( true );
				setNonZeroVarIndexes( nonZeroVars );
				m_teleMap.put( EARLY_CLASH_HIT, m_teleMap.get( EARLY_CLASH_HIT ) + 1 );
				
			} else {
				
				s_logger.info( "Subset is feasible, keep on going" );
				m_teleMap.put( EARLY_CLASH_MISS, m_teleMap.get( EARLY_CLASH_MISS ) + 1 );
			}			
		}
		
		return (conflict != null && !conflict.isEmpty());
	}
	
/* 
 * ============================================================================
 * Telemetry
 * ============================================================================
 */	
	/*
	 * Updates the number of generated columns
	 */
	private void updateColumnCounter(int colNum) {
		
		String measure = LPGenerator.TELEMETRY.COL_NUMBER.toString();
		
		m_teleMap.put( measure, colNum + m_teleMap.get( measure ) );
	}
	
	/*
	 * Updates the average time to generate a single *valid* column
	 */
	private void updateTotalColGenTime(long time) {
		
		String measure = LPGenerator.TELEMETRY.TOTAL_COL_GEN_TIME.toString();
		
		m_teleMap.put( measure, time + m_teleMap.get( measure ) );
	}
	
	public String getMeasure(String measure) {

		Double metric = m_teleMap.get( measure ); 
		
		return null == metric ? null : metric.toString();  
	}

	public void resetMeasure(String measure) {
		
		if (null != LPGenerator.TELEMETRY.valueOf( measure )) {
		
			m_teleMap.put( measure.toString(), 0d );
		}
	}

	public void resetMeasures() {

		m_teleMap.put( LPGenerator.TELEMETRY.COL_NUMBER.toString(), 0d );
		m_teleMap.put( LPGenerator.TELEMETRY.TOTAL_COL_GEN_TIME.toString(), 0d );
		m_teleMap.put( EARLY_CLASH_HIT, 0d );
		m_teleMap.put( EARLY_CLASH_MISS, 0d );
	}

	@Override
	public Collection<String> getMeasureNames() {
	
		return new HashSet<String>(m_teleMap.keySet());
	}

	@Override
	public boolean isMeasureSupported(String measure) {
		
		return m_teleMap.containsKey( measure );
	}

	@Override
	public void setMeasure(String name, String measure) {
		
		if (m_teleMap.containsKey( name )) {
			
			m_teleMap.put( name, Double.valueOf( measure ) );
		}
	}	
}
