/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.pronto.alg.HittingSetsAlgorithm;
import uk.ac.manchester.cs.pronto.alg.MaxConflictlessSubsetsAlgorithm;
import uk.ac.manchester.cs.pronto.alg.MaxConflictlessSubsetsAlgorithmImpl;
import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.constraints.CCSetAnalyzer;
import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.lp.glpk.GLPKLPSolverImpl;
import uk.ac.manchester.cs.pronto.util.NumberUtils;

/**
 * @author Pavel Klinov pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Mar 12, 2008
 */
public abstract class CCSetAnalyzerImpl implements CCSetAnalyzer {

	static Logger s_logger = Logger.getLogger(CCSetAnalyzerImpl.class);
	
	protected LPSolver				m_solver;
	protected LPGenerator			m_generator;
	/*
	 * Used to compute maximal satisfiable subsets of constraints
	 */
	protected MaxConflictlessSubsetsAlgorithm m_mcsAlg = new MaxConflictlessSubsetsAlgorithmImpl(); 

	public CCSetAnalyzerImpl() {

		m_solver = new GLPKLPSolverImpl();
		m_generator = new StabilizedCGLPGeneratorImpl();
	}
	
	public CCSetAnalyzerImpl(LPSolver solver, LPGenerator lpGenerator) {

		m_solver = solver;
		m_generator = lpGenerator;
	}

	public Set<Set<ConditionalConstraint>> getMaximalSatSubset(
			Set<ConditionalConstraint> toughConstraints, PTBox ptbox) {
		/*
		 * Not yet implemented. To be removed
		 */
		return null;
	}

	public Set<Set<ConditionalConstraint>> getMaximalSatSubsets(
										Set<ConditionalConstraint> toughConstraints, PTBox ptbox) {
		/*
		 * The strategy is to first find all minimal unsatisfiable subsets, then
		 * produce the minimal hitting sets and remove them from the PTBox
		 */
		Set<Set<ConditionalConstraint>> minUnsatSubsets = getMinimalUnsatSubsets( toughConstraints, ptbox );
		Set<Set<ConditionalConstraint>> maxSatSubsets = m_mcsAlg.compute( ptbox.getDefaultConstraints(), minUnsatSubsets );
		
		return maxSatSubsets;
	}

	/**
	 * Computes some minimal unsatisfiable subset
	 * 
	 * @return Minimal unsatisfiable subset of PTBox's constraint wrt toughConstraints or
	 * an empty set if PTBox is satisfiable wrt toughConstraints
	 * 
	 * FIXME !!!Broken!!!
	 */
	/*public Set<ConditionalConstraint> getMinimalUnsatSubset(
														Set<ConditionalConstraint> untouchCC,
														PTBox ptbox) {
		//We need to find *some* minimal unsatisfiable subset of PTBox given tough
		//(i.e. non-removable) constraints.
		Set<ConditionalConstraint> iiSet = null;
		Set<ConditionalConstraint> added = ptbox.addDefaultConstraints( untouchCC );
		LPInstance psatLP = null;//FIXME //m_generator.getLPforPSAT( ptbox );
		List<ConditionalConstraint> ccList = null;//((CCAwareLIInstanceImpl)psatLP.getLinearConstraints()).getConstraints();
		//First, get variable indexes for untouchable constraints
		Set<Integer> untouchVars = getVarIndexes(ccList, untouchCC);
		//Second, prepare the IIS LP model
		LPSolver iisSolver = createIISModel( psatLP );
		//Third, prepare the column generator
		ColumnGeneratorEx colGen = new CompactMIPColumnGeneratorImpl();
		
		colGen.setPTBox( ptbox );
		colGen.setConstraintList( ccList );
		colGen.setEntailmentClass( ATermUtils.TOP );
		
		Set<Integer> iisVars = findIIS( iisSolver, colGen );
		
		iisSolver.dispose();
		iisVars.removeAll( untouchVars );
		iiSet = getConstraints( ccList, iisVars );
		
		ptbox.removeDefaultConstraints( added );
		
		return iiSet;
	}*/

	/**
	 * Finds all minimal unsatisfiable subsets
	 */
	/*public Set<Set<ConditionalConstraint>> getMinimalUnsatSubsets(
														Set<ConditionalConstraint> untouchConstraints,
														PTBox ptbox) {

		Set<Set<Integer>> iisSets = new HashSet<Set<Integer>>();
		Set<Set<ConditionalConstraint>> unsatSubsets = new HashSet<Set<ConditionalConstraint>>();
		LPInstance psatLP = null;
		Set<ConditionalConstraint> added = ptbox.addDefaultConstraints( untouchConstraints );
		
		psatLP = null; //FIXME //m_generator.getLPforPSAT( ptbox );
		
		//It's quite possible that the system is solvable
		if( LPUtils.isPSATLPinstanceSolvable( null FIXME ) ) {

			s_logger.debug( "The system is solvable so there won't be any IISes" );

			unsatSubsets = Collections.emptySet();
		}
		else {
			
			List<ConditionalConstraint> ccList = null;//((CCAwareLIInstanceImpl)psatLP.getLinearConstraints()).getConstraints();
			//First, get variable indexes for untouchable constraints
			Set<Integer> untouchVars = getVarIndexes(ccList, untouchConstraints);
			//Second, prepare the IIS LP model
			LPSolver iisSolver = createIISModel( psatLP );
			//Third, prepare the column generator
			ColumnGeneratorEx colGen = new CompactMIPColumnGeneratorImpl();
			
			colGen.setPTBox( ptbox );
			colGen.setConstraintList( ccList );
			colGen.setEntailmentClass( ATermUtils.TOP );
			
			findAllIISes( untouchVars, iisSolver, colGen, new HittingSetsAlgorithmImpl<Integer>(), iisSets );
			// Final step: find constraints for variable indexes and remove duplicates
			for (Set<Integer> iis : iisSets) {
				
				Set<ConditionalConstraint> iiSet = getConstraints(ccList, iis);
				//Check minimality
				addToIISes(unsatSubsets, iiSet);
			}
			//Cleanup
			iisSolver.dispose();
		}
		
		ptbox.removeDefaultConstraints( added );

		return unsatSubsets;
	}*/
	
	
	
	private void addToIISes(Set<Set<ConditionalConstraint>> unsatSubsets,
							Set<ConditionalConstraint> iiSet) {
		
		boolean add = true;

		for( Set<ConditionalConstraint> unsatSet : unsatSubsets ) {

			if( iiSet.containsAll( unsatSet ) ) {
				// It's not minimal
				add = false;
				break;
			}

			if( unsatSet.containsAll( iiSet ) ) {
				// Some already added conflict is not minimal
				add = false;
				unsatSet.retainAll( iiSet );
				break;
			}
		}

		if( add ) unsatSubsets.add( iiSet );		
	}

	private Set<ConditionalConstraint> getConstraints(	List<ConditionalConstraint> ccList,
														Set<Integer> iis) {

		Set<ConditionalConstraint> iiSet = new HashSet<ConditionalConstraint>(iis.size());
		
		for (int varIndex : iis) {
			
			if (varIndex < 2 * ccList.size()) {
			
				iiSet.add( ccList.get( varIndex % 2 == 0 ? varIndex / 2 : (varIndex - 1) / 2 ) );
			}
		}
		
		return iiSet;
	}

	/*
	 * TODO Need a more efficient data structure than list (like sorted map)
	 */
	private Set<Integer> getVarIndexes(	List<ConditionalConstraint> ccList,
										Set<ConditionalConstraint> untouchConstraints) {
		
		Set<Integer> indexSet = new HashSet<Integer>(untouchConstraints.size() * 2);
		
		for (int i = 0; i < ccList.size(); i++) {
			
			if (untouchConstraints.contains( ccList.get( i ) ))  {
				
				indexSet.add( 2 * i );
				indexSet.add( 2 * i + 1 );
			}
		}
		
		return indexSet;
	}

	/*
	 * Basically, the method computes conflict graph
	 */
	public Map<Set<ConditionalConstraint>, Set<Set<ConditionalConstraint>>> getAllConflictSets(
			Set<Set<ConditionalConstraint>> strictConstraints, PTBox ptbox) {

		/*
		 * Naive implementation: it generates linear number of PSAT instances
		 */
		Map<Set<ConditionalConstraint>, Set<Set<ConditionalConstraint>>> resultMap = 
			new HashMap<Set<ConditionalConstraint>, Set<Set<ConditionalConstraint>>>();
		
		for (Set<ConditionalConstraint> ccSet : strictConstraints) {
			
			resultMap.put( ccSet, getMinimalUnsatSubsets( ccSet, ptbox ) );
		}

		return resultMap;
	}
	
	/**
	 * Finds all irreducible infeasible systems
	 */
	protected void findAllIISes(	Set<Integer> untouchVars,	
									LPSolver iisSolver,
									ColumnGeneratorEx cGen,
									HittingSetsAlgorithm<Integer> hsAlgo,
									Set<Set<Integer>> iisSets){

		Set<Integer> newIIS = null;		
		
		if (iisSets.isEmpty()) {
			//That's probably the first time the method is called, so need to find the first IIS
			newIIS = findIIS( iisSolver, cGen );
			newIIS.removeAll( untouchVars );
			iisSets.add(newIIS);
			
		} else {
		
			Set<Set<Integer>> hsSets = hsAlgo.compute( iisSets );
			
			for (Set<Integer> hSet : hsSets) {
				
				pullOutHittingSet(iisSolver, hSet);
				newIIS  = findIIS( iisSolver, cGen );
				newIIS.removeAll( untouchVars );//Remove untouchable var indexes from the IIS
				restoreHittingSet(iisSolver, hSet);
				
				if (!newIIS.isEmpty()) {
					
					iisSets.add( newIIS );
					
					break;
				}
			}
		}
		
		if (!newIIS.isEmpty()) {
			//New IIS has been found, so we need to repeat the whole thing
			findAllIISes( untouchVars, iisSolver, cGen, hsAlgo, iisSets );
		}		
	}
	
	/*
	 * Fixes the variables in the index set at zero
	 */
	private void pullOutHittingSet(LPSolver iisSolver, Set<Integer> set) {

		for (int varIndex : set) {
			
			iisSolver.setVariableUpperBound( varIndex + iisSolver.getFirstColumnIndex(), 0d );
		}

	}
	/*
	 * Sets the variables in the index set free
	 */
	private void restoreHittingSet(LPSolver iisSolver, Set<Integer> set) {

		for (int varIndex : set) {
			
			iisSolver.setVariableUpperBound( varIndex + iisSolver.getFirstColumnIndex(), Double.POSITIVE_INFINITY );
		}
		
	}

	/*
	 * Finds some IIS
	 */
	private Set<Integer> findIIS(LPSolver iisSolver, ColumnGeneratorEx colGen) {
		
		long ts = System.currentTimeMillis();
		double[] iisSol = null;
		Set<Integer> iis = new HashSet<Integer>();
		
		s_logger.debug( "Finding an IIS..." );
		/*
		 * If Ax <= b, x >= 0  is unsolvable then extreme points of the polytope
		 * P = {y | y A >= 0, y b = -1, y >= 0} will be in 1-1 correspondence with the IISs.
		 * So we first transform the LP into the "Ax <= b" form
		 * and then solve another LP (IIS LP) to find the extremes 
		 */
		//Now we generate rows for IIS LP (columns for the original LP) until all found IIS are real 
		iisSol = generateColumns(iisSolver, colGen);
		//See if any variables are greater than zero
		if (null != iisSol){
			
			for (int i = 0; i < iisSol.length - 2; i++) {
				
				if (!NumberUtils.equal( iisSol[i], 0d )) iis.add( i );
			}
			
			s_logger.debug( "IIS found in " + (System.currentTimeMillis() - ts) + " ms" );
			
		} else	s_logger.debug( "No IIS" );
			
		return iis;
	}

	/*
	 * Currently the system is in the form A x >= b. We first transform it to -A x <= b and then
	 * create the y A >= 0, y b = -1 system.
	 */
/*	private LPSolver createIISModel(LPInstance lp) {
		
		LPSolver iisModel = new GLPKLPSolverImpl();
		LIInstance li = lp.getLinearConstraints();
		double[][] lpMatrix = li.getMatrix();
		//iisMatrix = lpMatrix transposed + 1 extra row (cone bounding constraint)
		iisModel.initLPInstance( lpMatrix.length );
		//Transpose A and revert signs of the coefficients
		double[] iisRow = new double[lpMatrix.length]; 
		
		try {
			
			for (int i = 0; i < lp.getObjective().length; i++) {
				
				for (int j = 0; j < lpMatrix.length; j++) {
					
					iisRow[j] = -lpMatrix[j][i];
				}
				
				iisModel.addRow( iisRow, 0, LIInstance.GREATER_EQUAL, null );
			}		
			//Cone bounding constraint (b'y = -1)
			for (int i = 0; i < lpMatrix.length; i++) {
				
				iisRow[i] = -li.getRightHandsides()[i];
			}
			
			iisModel.addRow( iisRow, -1d, LIInstance.EQUAL, null );
			//Need to add 2 columns that correspond to the normalization rows in the original LP
			double[] iisNormalizCol = new double[iisModel.getRowNumber() + 1];
			
			Arrays.fill( iisNormalizCol, 1d );
			iisModel.addColumn( iisNormalizCol, null );
			Arrays.fill( iisNormalizCol, -1d );
			iisModel.addColumn( iisNormalizCol, null );		
			//We can use basically any objective function for the IIS problem
			//because the cone bounding constraint is explicitly included in the system
			double[] objective = new double[lpMatrix.length + 2];
			
			//objective[objective.length - 2] = 1d;
			//objective[objective.length - 1] = -1d;
			
			Arrays.fill( objective, 1d );
			iisModel.setObjective( objective );
			iisModel.setMaximize( false );
			
		} catch( CPException e ) {
			// Should not happen
			s_logger.fatal( "Unexpected error during construction of IIS LP", e );
			
			throw new RuntimeException(e);
		}
		
		return iisModel;
	}*/
	

	private double[] generateColumns(	LPSolver solver,
										ColumnGeneratorEx colGen) {
		double[] column = null;
		double[] iisSol = null;
		double iisValue = 0d;
		
		try {
			
			((GLPKLPSolverImpl)solver).writeLP( "C:///kl//tmp//test.lp" );
			
			//Here we start the program generation main loop. We will add
			//rows to IIS LP until we can't show that the solution is NOT an IIS for the full system
			while( true ) {
				//Re-optimize after adding new row (few lines below). 
				//Note that some LP solvers (e.g. GLPK) require pre-solver to be on
				//otherwise they may not report potential unsolvability
				solver.solveLP();
				
				iisValue = solver.getObjValue();
				iisSol = solver.getAssignment();//This is current y* which holds indices of the IS
				
				s_logger.debug( "IIS value: " + iisValue );
				s_logger.debug( "IIS candidate: " + Arrays.toString(iisSol) );

				//Compute column "a" s.t. -y*' a < 0 (or -a' y* < 0)
				//If successful, it will invalidate current system -A' y >= 0
				column = colGen.generateColumn( iisSol, false );
				
				s_logger.debug( "Row: " + Arrays.toString(column) );				

				if( null != column ) {
					//The column is added directly into the LP model, not via
					//any generic wrapper (like LPInstance)
					//Unfortunately we'll have to strip the first component (i.e. objective
					//coefficient). It's not needed here.
					//We also must revert the sign again because it's a column for Ax >= 0
					//while we need -Ax >= 0
					double[] row = new double[column.length - 1];
					
					for (int i = 0; i < row.length - 1; i++) row[i] = -column[i + 1];
					
					solver.addRow( row, 0, LPSolver.ROW_TYPE.GREATER_EQUAL, null );
				}
				else {
					// IIS LP is optimal, so we may stop
					break;
				}
			}

		} catch( CPException e ) {
			// This should indicate that the original LP is solvable
			s_logger.debug( "Apparently the original LP is solvable since IIS LP is infeasible" );
			
			return null;
		}
		
		return iisSol;
	}	
	
	/*private void dumpLPtoSTDO(LPInstance lp) {
		
		StringWriter writer = new StringWriter();

		try {

			((LPInstanceImpl) lp).serializeLP( writer );
			writer.flush();

			System.out.println( writer.toString() );

		} catch( IOException e ) {

			e.printStackTrace();
		}
	}*/
}
