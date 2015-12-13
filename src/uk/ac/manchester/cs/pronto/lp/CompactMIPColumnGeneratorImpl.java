/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;
import aterm.ATermList;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.PTBoxImpl;
import uk.ac.manchester.cs.pronto.Pronto;
import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.explanation.PelletSATCheckerImpl;
import uk.ac.manchester.cs.pronto.explanation.SATChecker;
import uk.ac.manchester.cs.pronto.explanation.UnsatExplanationGenerator;
import uk.ac.manchester.cs.pronto.explanation.UnsatExplanationGeneratorImpl;
import uk.ac.manchester.cs.pronto.util.NumberUtils;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class CompactMIPColumnGeneratorImpl implements ColumnGeneratorEx {

	private final double LP_SOLVER_THRESHOLD = 1.0E-6;
	
	private Logger	s_logger = Logger.getLogger( CompactMIPColumnGeneratorImpl.class );
	protected MIPSolverEx m_solver = null;
	protected UnsatExplanationGenerator m_explGen = new UnsatExplanationGeneratorImpl();
	protected PTBox m_ptbox = null;
	protected SATChecker m_satChecker = null;
	protected List<ConditionalConstraint> m_ccList = null;
	protected ATermAppl m_entailment = ATermUtils.TOP;
	//Maps unique conjuncts onto variable indexes 
	protected Map<ATermAppl, Integer> m_conjunctVarMap = null;
	private int	m_columnNumber = 1;
	//Limit the number of rows that can be eagerly sucked into the column generation model
	//to protect against OutOfMemory exceptions.
	//TODO This parameter must be configurable depending on the amount of available memory
	private static final int MIP_ROW_LIMIT = 15000;
	
	public void setColumnsNumber(int number) {
		
		m_columnNumber  = number;
	}	
	
	public int getColumnsNumber() {
		
		return m_columnNumber;
	}	
	
	@Override
	public double[] generateColumn(double[] duals, boolean max) throws CPException {
		
		int tmp = m_columnNumber;
		
		m_columnNumber = 1;
		
		List<double[]> columns = generateColumns( duals, max );
		
		m_columnNumber = tmp;
		
		return (columns != null && !columns.isEmpty()) ? columns.get( 0 ) : null;
	}
	
	/*
	 * Precedes the generation process
	 */
	protected void preGenerate(double[] duals, boolean max, MIPSolverEx solver) {
		
		int numOfBoundingRows = duals.length - 2 * m_ccList.size();
		
		if (numOfBoundingRows != 2 && numOfBoundingRows != 1) {
			
			s_logger.fatal( "Wrong number of bounding constraints: " + numOfBoundingRows );
			
			throw new RuntimeException("Wrong number of bounding constraints");
		}
		//The MIP model is constructed only once per PTBox and cached for future use
		if (null == m_conjunctVarMap) {
			
			initConjunctVarMap( m_ptbox, m_entailment != null ? m_entailment : ATermUtils.TOP  );
		}

		if (null == m_solver) {
			
			try {
				
				m_solver = prepareSolver(max, numOfBoundingRows);
				
			} catch( CPException e ) {

				s_logger.fatal( "MIP construction failed", e );
				
				throw new RuntimeException(e);
			}
		}		
	}
	
	/*
	 * Follows the generation process
	 */
	protected void postGenerate(MIPSolverEx solver, List<double[]> columns, int numOfBoundingRows) {
		
		for (int i = 0; i < columns.size(); i++) 
			columns.set( i, restoreColumn(columns.get(i), numOfBoundingRows) );
	}
	
	/**
	 * 
	 * @param duals
	 * @param max
	 * @return
	 */
	@Override
	public List<double[]> generateColumns(double[] duals, boolean max) throws CPException {

		int numOfBoundingRows = duals.length - 2 * m_ccList.size();
		List<MIPSolution> solutions = null;
		
		preGenerate(duals, max, m_solver);
		
		double[] objective = prepareObjective(duals, numOfBoundingRows);
		
		m_solver.setObjective( objective );
		m_solver.setMaximize( max );
		m_solver.setSolutionPoolSize( m_columnNumber );
		m_solver.setMIPGap( 0.5 );
		m_explGen.setTimeLimit( 500 );
		//All work is done here
		solutions = generateValidColumns( m_solver, max, m_satChecker, numOfBoundingRows);
		
		List<double[]> columns = new ArrayList<double[]>(solutions.size());
		
		for (MIPSolution solution : solutions) {
			
			columns.add( restoreColumn( solution.m_solution, numOfBoundingRows ) );
		}

		return columns;		
	}

	public void setPTBox(PTBox ptbox) {
		
		m_ptbox = ptbox;
		m_ccList = null;
		m_satChecker = new PelletSATCheckerImpl(ptbox.getClassicalKnowledgeBase());
		m_solver = null;
		m_conjunctVarMap = null;
	}
	
	public void setConstraintList(List<ConditionalConstraint> ccList) {

		m_ccList = ccList;
		m_solver = null;
		m_conjunctVarMap = null;
	}	
	/*
	 * Associates conjuncts (i.e. DC, ~DC, ~C for each constraint) with indexes of variables 
	 * in the MIP instance. This allows for using a single variable for all duplicated conjuncts
	 */
	private void initConjunctVarMap(PTBox ptbox, ATermAppl entClass) {
		//Initialize the conjuncts->vars mapping
		Set<ConditionalConstraint> ccSet = ptbox.getDefaultConstraints();
		int varIndex = 0;
		
		m_conjunctVarMap = new HashMap<ATermAppl, Integer>();
		
		varIndex = initVarIndex(m_conjunctVarMap, ATermUtils.TOP, varIndex );
		varIndex = initVarIndex(m_conjunctVarMap, ATermUtils.BOTTOM, varIndex );
		varIndex = initVarIndex(m_conjunctVarMap, entClass, varIndex );
		
		for (ConditionalConstraint cc : ccSet) {

			varIndex = initVarIndex(m_conjunctVarMap, cc.getEvidence(), varIndex );
			varIndex = initVarIndex(m_conjunctVarMap, cc.getConclusion(), varIndex );
			varIndex = initVarIndex(m_conjunctVarMap, cc.getDCTerm(), varIndex );			
			varIndex = initVarIndex(m_conjunctVarMap, cc.getNotDCTerm(), varIndex );
		}
		
		Map<ATermAppl, ATermAppl> clauseMap = ((PTBoxImpl)ptbox).getATermNameExpressionMap();
		//Add some extra variables if some class names are synonyms for propositional clauses
		if (clauseMap != null) {
			
			for (Map.Entry<ATermAppl, ATermAppl> clauseEntry : clauseMap.entrySet()) {
				
				ATermAppl clause = clauseEntry.getValue();
				
				for (ATermAppl literal : getClauseLiterals( clause )) {

					ATermAppl term = ATermUtils.isNot( literal ) ? (ATermAppl)literal.getArgument( 0 ) : literal;				
					
					varIndex = initVarIndex(m_conjunctVarMap, term, varIndex);
				}				
			}
		}
	}
	
	private int initVarIndex(	Map<ATermAppl, Integer> conjunctVarMap,
								ATermAppl conjunct,
								int currVarIndex) {

		if (!conjunctVarMap.containsKey( conjunct )) {
			
			conjunctVarMap.put( conjunct, currVarIndex );
			currVarIndex += 1;
		}
		
		return currVarIndex;
	}
	
	public void setEntailmentClass(ATermAppl clazz) {
		
		m_entailment = clazz;
		m_conjunctVarMap = null;
	}	
	
	/*
	 * Tiny utility method that counts the number of variables (i.e. the width) of the MIP problem.
	 */
	protected int getMIPBaseWidth() {
		
		return m_conjunctVarMap.size();
	}
	
	/*
	 * Very important method, it prepares the MIP model.
	 * 
	 * Made protected for unit testing purposes, may be reverted to private later
	 */
	protected MIPSolverEx prepareSolver(boolean max, int numOfBoundingRows) throws CPException {
		
		MIPSolverEx solver = Pronto.createMIPSolver();
		int mipWidth = getMIPBaseWidth() + numOfBoundingRows;
		MIPSolver.VAR_TYPE[] varTypes = new MIPSolver.VAR_TYPE[mipWidth];
		
		Arrays.fill(varTypes, MIPSolver.VAR_TYPE.BINARY);//All variables are binary
		solver.initMIPInstance( varTypes );
		
		if (numOfBoundingRows == 1) {
			
			addRow( new int[]{mipWidth - 1}, new double[] {1d},	1d,	LPSolver.ROW_TYPE.EQUAL, mipWidth,	solver );
			
		} else {
			//Two last components must be equal to 1	
			addRow( new int[]{mipWidth - 2}, new double[] {1d},	1d,	LPSolver.ROW_TYPE.EQUAL, mipWidth,	solver );
			addRow( new int[]{mipWidth - 1}, new double[] {1d},	1d, LPSolver.ROW_TYPE.EQUAL, mipWidth, solver );
		}
		
		for (ConditionalConstraint cc : m_ccList) {
			
			if (cc.isConditional()) {
				//X_C + X_D - X_DC <= 1
				addRow( new int[]{	m_conjunctVarMap.get(cc.getEvidence()),
									m_conjunctVarMap.get(cc.getConclusion()),
									m_conjunctVarMap.get(cc.getDCTerm())},
						new double[]{	1, 1, -1},
						1d,
						LPSolver.ROW_TYPE.LESS_EQUAL,
						mipWidth, solver);
				//X_C + (1 - X_D) - X_~DC <= 1
				addRow( new int[]{	m_conjunctVarMap.get(cc.getEvidence()),
									m_conjunctVarMap.get(cc.getConclusion()),
									m_conjunctVarMap.get(cc.getNotDCTerm())},
						new double[]{	1, -1, -1},
						0d,
						LPSolver.ROW_TYPE.LESS_EQUAL,
						mipWidth, solver);
				//X_C = X_DC + X_~DC
				addRow( new int[]{	m_conjunctVarMap.get(cc.getEvidence()),
									m_conjunctVarMap.get(cc.getDCTerm()),
									m_conjunctVarMap.get(cc.getNotDCTerm())},
						new double[]{	1, -1, -1},
						0d,
						LPSolver.ROW_TYPE.EQUAL,
						mipWidth, solver);			
				//X_D >= X_DC
				addRow( new int[]{	m_conjunctVarMap.get(cc.getConclusion()),
									m_conjunctVarMap.get(cc.getDCTerm())},
						new double[]{	1, -1},
						0d,
						LPSolver.ROW_TYPE.GREATER_EQUAL,
						mipWidth, solver);			
				//X_D >= X_~DC
				addRow( new int[]{	m_conjunctVarMap.get(cc.getConclusion()),
									m_conjunctVarMap.get(cc.getNotDCTerm())},
						new double[]{ -1, -1},
						-1d,
						LPSolver.ROW_TYPE.GREATER_EQUAL,
						mipWidth, solver);
				
			} else if (m_conjunctVarMap.containsKey( cc.getConclusion() ) && m_conjunctVarMap.containsKey( cc.getNotDTerm() )) {
				//X_~D explicitly exists in the model, so need this constraint
				addRow( new int[]{	m_conjunctVarMap.get(cc.getConclusion()),
									m_conjunctVarMap.get(cc.getNotDCTerm())},
						new double[]{ 1, 1},
						1d,
						LPSolver.ROW_TYPE.EQUAL,
						mipWidth, solver);
			}
		}
		
		if (m_conjunctVarMap.containsKey(ATermUtils.TOP)) {
			//TOP is always equal to 1
			addRow( new int[]{m_conjunctVarMap.get(ATermUtils.TOP)},
					new double[] {1d},
					1d,
					LPSolver.ROW_TYPE.EQUAL,
					mipWidth,
					solver );			
		}
		
		if (m_conjunctVarMap.containsKey(ATermUtils.BOTTOM)) {
			//BOTTOM is always equal to 0
			addRow( new int[]{m_conjunctVarMap.get(ATermUtils.BOTTOM)},
					new double[] {1d},
					0d,
					LPSolver.ROW_TYPE.EQUAL,
					mipWidth,
					solver );			
		}
		//Add constraints that encode class name definitions
		addClauseConstraints(mipWidth, solver);		
		//Add constraints that represent subsumption and disjointness relationships in the TBox
		addSubsumptionAndDisjointnessConstraints(mipWidth, solver);
		
		s_logger.info( "Column generation MIP model: " + solver.getColumnNumber() + " x " + solver.getRowNumber() );
		
		return solver;
	}

	private void addClauseConstraints(int mipWidth, MIPSolverEx solver) throws CPException {
		
		for(Map.Entry<ATermAppl, ATermAppl> entry : ((PTBoxImpl)m_ptbox).getATermNameExpressionMap().entrySet()) {

			ATermAppl className = entry.getKey();
			ATermAppl clause = entry.getValue();
			List<ATermAppl> clauseLiterals = getClauseLiterals(clause);
			ATermAppl[] rowLiterals = new ATermAppl[1 + clauseLiterals.size()];
			double[] coeffs = new double[1 + clauseLiterals.size()];
			int i = 1;
			boolean isOr = ATermUtils.isOr( clause );
			boolean isAnd = ATermUtils.isAnd( clause );
			
			rowLiterals[0] = className;
			coeffs[0] = 1d;
			//Easy case, A = B => X_A - X_B = 0 (B is a literal, so can be negative)
			if (!isOr && !isAnd) {
				
				addRow( new ATermAppl[]{className, clause}, new double[] {1, -1}, 0d, LPSolver.ROW_TYPE.EQUAL, mipWidth, solver );
				continue;
			}
			//Conjunctive and disjunctive clauses are trickier
			for (ATermAppl literal : clauseLiterals) {

				rowLiterals[i] = literal;
				coeffs[i] = -1d;
				i++;
				
				//OR row: A = a or b => x_A >= x_a; x_A >= x_b
				//AND row: A = a and b => x_A <= x_a; x_A <= x_b
				LPSolver.ROW_TYPE type = isOr ? LPSolver.ROW_TYPE.GREATER_EQUAL : LPSolver.ROW_TYPE.LESS_EQUAL;
					
				addRow( new ATermAppl[]{className, literal}, new double[] {1, -1}, 0d, type, mipWidth, solver );
			}
			
			if (ATermUtils.isOr( clause )) {
				//OR row: A = a or b -> X_A <= X_a + X_b -> X_A - X_a - X_b <= 0
				addRow( rowLiterals, coeffs, 0, LPSolver.ROW_TYPE.LESS_EQUAL, mipWidth, solver );
				
			} else if (ATermUtils.isAnd( clause )) {
				//AND row: A = a and b -> X_A >= X_a + X_b - 1 -> X_A - X_a - X_b >= -1
				addRow( rowLiterals, coeffs, -(clauseLiterals.size()-1), LPSolver.ROW_TYPE.GREATER_EQUAL, mipWidth, solver );
			}
		}
	}

	/*
	 * 
	 */
	private void addSubsumptionAndDisjointnessConstraints(	int matrixWidth,
															MIPSolver solver
															) throws CPException {
		
		Map<ATermAppl, Set<ATermAppl>> subsMatrix = m_ptbox.getSupplementalData().getSubsumptionMatrix();
		Map<ATermAppl, Set<ATermAppl>> disjMatrix = m_ptbox.getSupplementalData().getDisjointnessMatrix();
		//Subsumptions
		for (Map.Entry<ATermAppl, Set<ATermAppl>> entry : subsMatrix.entrySet()) {
			
			ATermAppl subClass = entry.getKey();
			
			for (ATermAppl superClass : entry.getValue()) {
				
				if (solver.getRowNumber() >= MIP_ROW_LIMIT) break;
				//X_{superclass} >= X_{subclass}
				addRow( new ATermAppl[]{superClass, subClass},
						new double[]{ 1, -1},
						0d,
						LPSolver.ROW_TYPE.GREATER_EQUAL,
						matrixWidth,
						solver);	
			}
		}
		//Disjointnesses
		for (Map.Entry<ATermAppl, Set<ATermAppl>> entry : disjMatrix.entrySet()) {
			
			ATermAppl classA = entry.getKey();
			
			for (ATermAppl classB : entry.getValue()) {
				
				if (solver.getRowNumber() >= MIP_ROW_LIMIT) break;
				//X_{A} + X_{B} <= 1
				addRow( new ATermAppl[]{classA, classB},
						new double[]{1, 1},
						1d,
						LPSolver.ROW_TYPE.LESS_EQUAL,
						matrixWidth,
						solver);				
			}
		}
	}

	/*
	 * Corrects coefficients and the right handside to account for negative literals
	 */
	protected void addRow(		ATermAppl[] literals, 
								double[] varCoeffs,
								double rhs,
								LPSolver.ROW_TYPE type,
								int length,
								LPSolver solver) throws CPException {
		
		boolean allTermsInMap = true;
		int[] varIndexes = new int[literals.length];
		
		for (int i = 0; (i < literals.length) && allTermsInMap; i++) {

			ATermAppl term = literals[i];
			
			if (m_conjunctVarMap.containsKey( term )) {//No correction needed
				
				varIndexes[i] = m_conjunctVarMap.get( term );
				
			} else if (ATermUtils.isNot( term )) {
				
				term = (ATermAppl)term.getArgument( 0 );
				
				if (m_conjunctVarMap.containsKey( term )) {
				
					varIndexes[i] = m_conjunctVarMap.get( term );
					//Negative literal, correct the coefficients
					rhs += (varCoeffs[i] == 1d) ? -1d : 1d;
					varCoeffs[i] *= -1;
					
				} else {
					
					allTermsInMap = false;
				}
				
			} else {
				
				allTermsInMap = false;
			}
		}
		//If all terms are recognized then add the constraint
		if (allTermsInMap) addRow(varIndexes, varCoeffs, rhs, type, length, solver);
	}
	
	protected void addRow(	int[] varIndexes,
							double[] varCoeffs,
							double rh,
							LPSolver.ROW_TYPE type,
							int length,
							LPSolver solver) throws CPException {

		double[] row = new double[length];

		for( int i = 0; i < varIndexes.length; i++ ) {

			row[varIndexes[i]] = NumberUtils.round(row[varIndexes[i]] + varCoeffs[i] );
		}

		solver.addRow( row, rh, type, null );
	}	
	
	/**
	 * Adds extra rows to MIP to avoid regenerating columns that correspond
	 * to unsatisfiable classes
	 * 
	 */
	protected void addExtraConstraints(	MIPSolver solver,
										double[] solution,
										Set<Set<ATermAppl>> explSets,
										int mipWidth) {
		
		for (Set<ATermAppl> explanation : explSets) {
			
			ATermAppl[] terms = explanation.toArray(new ATermAppl[] {});
			double[] coeffs = new double[terms.length];
			
			Arrays.fill( coeffs, 1d );
			
			try {
				
				addRow( terms, coeffs, terms.length - 1d, LPSolver.ROW_TYPE.LESS_EQUAL, mipWidth, solver );
				
			} catch( CPException e ) {
				// Should never happen
				s_logger.fatal( e );
				
				throw new RuntimeException(e);
			}
		}
	}
	
	/*
	 * 
	 */
	protected double[] prepareObjective(double[] duals, int numOfBoundingRows) {
		
		double[] objective = new double[getMIPBaseWidth() + numOfBoundingRows];
		
		objective[m_conjunctVarMap.get( m_entailment )] = 1d; 

		if (numOfBoundingRows == 2) {
			//Sign of the 2nd last variable should be reverted			
			objective[objective.length - 2] = duals[duals.length - 2];
			objective[objective.length - 1] = -duals[duals.length - 1];
			
		} else objective[objective.length - 1] = duals[duals.length - 1];
		
		for (int ccIndex = 0; ccIndex < m_ccList.size(); ccIndex++) {
			
			ConditionalConstraint cc = m_ccList.get( ccIndex );
			
			//if (cc.isConditional()) {
				//(-l) and (u) domain values
				objective[m_conjunctVarMap.get( cc.getNotDCTerm() )] +=
														(-cc.getLowerBound()) * (-duals[2*ccIndex])
															+
														cc.getUpperBound() * (-duals[2*ccIndex + 1]);
				//(1 - l) and (u - 1) domain values
				objective[m_conjunctVarMap.get( cc.getDCTerm() )] +=
														(1 - cc.getLowerBound()) * (-duals[2*ccIndex])
															+
														(cc.getUpperBound() - 1) * (-duals[2*ccIndex + 1]);
			/*} else {
				
				objective[m_conjunctVarMap.get( cc.getNotDCTerm() )] = 0d;
				objective[m_conjunctVarMap.get( cc.getDCTerm() )] += -duals[2*ccIndex] -duals[2*ccIndex + 1];
			}*/
		}
		
		return objective;
	}
	
	/*
	 * 
	 */	
	protected double[] restoreColumn(double[] solution, int numOfBoundingRows) {
		
		double[] column = new double[1 + 2 * m_ccList.size() + numOfBoundingRows];
		//Objective coefficient
		column[0] = solution[m_conjunctVarMap.get( m_entailment )]; 
		
		if (numOfBoundingRows == 2) {
			
			column[column.length - 2] = -1d;//Last two components
			column[column.length - 1] = 1d;//The 2nd last variable sign is reverted to make the inequality
											//>= as others
		} else column[column.length - 1] = -1d;		
		
		for( int i = 0; i < m_ccList.size(); i++ ) {

			ConditionalConstraint cc = m_ccList.get( i );
			boolean cond = true;//Treat all constraints as conditionals for now
			
			if( NumberUtils.equal( solution[m_conjunctVarMap.get( cc.getNotDCTerm() )], 1d ) ) {

				column[1 + 2*i] = cond ? NumberUtils.round( -cc.getLowerBound() ) : 0d;
				column[2 + 2*i] = cond ? NumberUtils.round( cc.getUpperBound() ) : 0d;

			}
			else if ( NumberUtils.equal( solution[m_conjunctVarMap.get( cc.getDCTerm() )], 1d )) {

				column[1 + 2*i] = cond ? NumberUtils.round( 1 - cc.getLowerBound() ) : 1d;
				column[2 + 2*i] = cond ? NumberUtils.round( cc.getUpperBound() - 1 ) : 1d;
			} 
		}
		
		return column;
	}

	/*
	 */
	private List<MIPSolution> generateValidColumns(	MIPSolverEx solver,
													boolean max,
													SATChecker checker,
													int numOfBoundingRows) {
		
		List<MIPSolution> solutions = new ArrayList<MIPSolution>();
		Set<Set<ATermAppl>> explSets = null;
		long tsMIP = 0;
		long tsVal = 0;
		int valCnt = 0;
		double[][] sols = null;
		double[] objValues = null;
		//Enter the main generate-n-validate cycle
		do {
			
			boolean modelChanged = false;
			
			try {

				double ts = System.currentTimeMillis();
				
				LPUtils.solveMIPwithTimeOut( solver, 10000, 0d, true );
				
				sols = solver.getSolutions();
				objValues = solver.getObjValues();
				
				tsMIP += (System.currentTimeMillis() - ts);
				
			} catch( CPException e ) {
				//That means the problem is inconsistent
				s_logger.debug( "MIP is inconsistent or too hard, stop");
				s_logger.debug( e );
				
				return Collections.emptyList();
			}
	
			for (int i = 0; i < sols.length; i++) {
				
				double[] sol = sols[i];
				double objValue = objValues[i];
				
				if ((max && !NumberUtils.greater( objValue - LP_SOLVER_THRESHOLD, 0d))
						|| (!max && !NumberUtils.greater(0, objValue + LP_SOLVER_THRESHOLD)) ) {
					
					continue;
					
				} else {
					//Validation step
					long ts = System.currentTimeMillis();
					Set<ATermAppl> conjuncts = computeConjuncts(sol);
					//Potentially the most expensive operation
					explSets = m_explGen.computeExplanations( conjuncts, checker );
					
					tsVal += (System.currentTimeMillis() - ts);
					valCnt += 1;
					
					if (explSets != null && !explSets.isEmpty()) {
						
						addExtraConstraints(solver, sol, explSets, getMIPBaseWidth() + numOfBoundingRows);
						modelChanged = true;
						s_logger.debug( "Column is not valid, " + explSets.size() + " constraints added to MIP");
						
					} else {
						
						s_logger.debug( "Column is valid");
						//s_logger.debug( conjuncts );
						solutions.add( new MIPSolution(objValue, sol) );
					}
				}
			}
			
			if (!modelChanged && solutions.isEmpty()) {
				
				s_logger.debug( "No improving column can be generated, stop");
				
				return solutions;
			}			
			
		} while (solutions.isEmpty());
		
		s_logger.debug( "MIP: " + tsMIP + " ms, " + solutions.get( 0 ).m_value
						+ ", validation (" + valCnt + " iterations): " + tsVal + " ms" );
		
		return solutions;
	}
	
	/*
	 * Converts a generated column into a set of conjuncts
	 */
	private Set<ATermAppl> computeConjuncts(double[] solution) {
		
		Set<ATermAppl> conjuncts = new HashSet<ATermAppl>(solution.length / 3);

		for (Map.Entry<ATermAppl, Integer> conjunctVarMapEntry : m_conjunctVarMap.entrySet()) {
			
			if( NumberUtils.equal( solution[conjunctVarMapEntry.getValue()], 1d ) ) {

				conjuncts.add( conjunctVarMapEntry.getKey() );

			} else if (ATermUtils.isPrimitive( conjunctVarMapEntry.getKey() )){

				conjuncts.add( ATermUtils.negate( conjunctVarMapEntry.getKey() ) );
			}
		}
		
		return conjuncts;
	}
	
	
	/*
	 * Add extra constraints to maximize diversity (next column better be 
	 * as "far" from the previous ones as possible)
	 */
/*	private void updateMIP(	MIPSolver solver, List<double[]> solutions, Set<String> extraRowNames) throws CPException {

		double[] lastSolution = new double[solver.getColumnNumber()];
		//First update the objective function to maximize diversity
		double[] newObjective = new double[lastSolution.length];
		int lastSolutionNumOfOnes = 0;
		
		for( int j = 0; j < solutions.size(); j++) {
			
			double[] solution = solutions.get( j );
			
			for (ConditionalConstraint cc : m_ccList) {
				
				int dcIndex = m_conjunctVarMap.get( cc.getDCTerm() );
				int notdcIndex = m_conjunctVarMap.get( cc.getNotDCTerm() );
				
				if (NumberUtils.equal( 0, solution[dcIndex] )) {
					
					newObjective[dcIndex] += 1d;
					
				} else {
					
					newObjective[dcIndex] -= 1d;
					
					if (j == solutions.size() - 1) {
						
						lastSolution[dcIndex] = 1d;
						lastSolutionNumOfOnes++;
					}
				}
				
				if (NumberUtils.equal( 0, solution[notdcIndex] )) {
					
					newObjective[notdcIndex] += 1d;
					
				} else {
					
					newObjective[notdcIndex] -= 1d;
					
					if (j == solutions.size() - 1) {
						
						lastSolution[notdcIndex] = 1d;
						lastSolutionNumOfOnes++;
					}
				}				
			}
		}
		//Second add the diversity constraint to exclude the previous solution
		solver.setObjective( newObjective );
		solver.setMaximize( true );//Diversity is to be maximized
		solver.addRow(	lastSolution, lastSolutionNumOfOnes - 1, LIInstance.LESS_EQUAL, null );
	}	
	
	private void cleanupMIP(MIPSolver solver, Set<String> extraRowNames) throws CPException {
		
		solver.dispose();
	}*/
	
	public void reset() {
		
		m_solver.dispose();
		m_solver = null;
		m_conjunctVarMap = null;
	}
	
	@Override
	public List<ConditionalConstraint> getConstraintList() {
		
		return m_ccList;
	}

	@Override
	public PTBox getPTBox() {
		
		return m_ptbox;
	}	
	
	private List<ATermAppl> getClauseLiterals(ATermAppl clause) {
		
		if (ATermUtils.isOr( clause ) || ATermUtils.isAnd( clause )) {
			
			ATermList list = (ATermList)clause.getArgument( 0 );
			
			return Arrays.asList( ATermUtils.toArray( list ) );
			
		} else /*if (ATermUtils.isPrimitiveOrNegated( clause ))*/ {
			//The clause is simply a literal
			return Collections.singletonList( clause );
		}
		
		//throw new RuntimeException("Invalid clause: " + clause);
	}
	
	
	class MIPSolution {
		
		double[] m_solution = null;
		double m_value = 0;
		
		MIPSolution(double val, double[] sol) {
			
			m_value = val;
			m_solution = sol;
		}
	}	
}