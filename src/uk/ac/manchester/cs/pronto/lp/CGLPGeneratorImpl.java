package uk.ac.manchester.cs.pronto.lp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.events.EventHandlerWithFeedback;
import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.exceptions.ProbabilisticInconsistencyException;
import uk.ac.manchester.cs.pronto.index.IndexSetGenerator;
import uk.ac.manchester.cs.pronto.index.RandomConceptTypeSetGenImpl;
import uk.ac.manchester.cs.pronto.util.ArrayUtils;

/**
 * Generates LP instances incrementally using the column generation method. All produced systems
 * are guaranteed to be optimal. 
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class CGLPGeneratorImpl implements LPGenerator {

	protected enum PROBLEM_TYPE {PSAT, TLOGENT};
	
	protected static Logger	s_logger = Logger.getLogger( CGLPGeneratorImpl.class );
	
	protected PROBLEM_TYPE m_type = null;
	private ColumnGeneratorEx m_cGen = new CompactMIPColumnGeneratorImpl();
	//Initial LP to which new columns can be added
	private CCAwareLPSolver m_lp = null;
	protected Set<Integer> m_nonZeroVars = new HashSet<Integer>();
	protected boolean m_earlyConflictDetected = false;
	
	
	public CGLPGeneratorImpl() {		
		m_cGen.setColumnsNumber( 1 );
	}
	
	protected void reset() {		
		m_earlyConflictDetected = false;
		m_nonZeroVars.clear();
	}
	
	protected void setEarlyConflictDetected(boolean flag) {		
		m_earlyConflictDetected = flag;
	}
	
	protected boolean getEarlyConflictDetected() {		
		return m_earlyConflictDetected;
	}	
	
	protected void setPartialPSATEventHandler(EventHandlerWithFeedback<Set<ConditionalConstraint>> handler) {}
	
	protected PROBLEM_TYPE getProblemType() {		
		return m_type;
	}
	
	public void setInitialLP(CCAwareLPSolver lp) {	
		m_lp = lp;
	}
	
	@Override
	public CCAwareLPSolver getLPforPSAT(PTBox ptbox) {
		
		CCAwareLPSolver rmp = null;

		m_type = PROBLEM_TYPE.PSAT;
		
		if (m_lp == null) {
			
			LPGenerator lpGen = null;
			IndexSetGenerator gen = new RandomConceptTypeSetGenImpl();
			
			gen.setTermNumber( Math.min( ptbox.getDefaultConstraints().size(), 2) );
			lpGen = new BasicLPGeneratorImpl(gen);
			//Get the restricted initial version of the linear system (RMP)
			rmp = lpGen.getLPforPSAT( ptbox );
			
		} else	{
			
			rmp = m_lp;
		}

		m_cGen.setPTBox( ptbox );
		m_cGen.setEntailmentClass( ATermUtils.TOP );
		m_cGen.setConstraintList( rmp.getConstraintList() );
		rmp.unsetBoundingRows();
		//rmp.setBoundingRows();
		rmp.setUpperBoundingRow();
		
		completeLP(rmp, m_cGen, false);
		//Save it for the future
		m_lp = rmp;
		//Cleanup
		m_cGen.reset();
		
		return rmp;
	}

	private void completeLP(CCAwareLPSolver rmp, ColumnGeneratorEx cGen, boolean tlogent) {

		try {
			
			//if (!tlogent) rmp.addExtraVarsForFeasibility();
			
			generateColumns( cGen, rmp );
			
			//if (!tlogent) rmp.removeExtraVars();
			
		} catch( CPException e ) {

			s_logger.fatal( "Error during column generation for PSAT/TLogEnt", e );
			rmp.dispose();
			
			throw new RuntimeException(e);
		} 
	}
	
	
	public CCAwareLPSolver getLowerLPforTLogEnt(PTBox ptbox, ATermAppl concept) {
		
		return getLPforTLogEnt(ptbox, concept, false);
	}
	
	public CCAwareLPSolver getUpperLPforTLogEnt(PTBox ptbox, ATermAppl concept) {
		
		return getLPforTLogEnt(ptbox, concept, true);
	}

	
	public CCAwareLPSolver getLPforTLogEnt(PTBox ptbox, ATermAppl concept, boolean max) {
		
		CCAwareLPSolver rmp = null;
		ConditionalConstraint restriction = new ConditionalConstraint( ATermUtils.TOP, concept, 0, 1 );
		boolean added = ptbox.addDefaultConstraint( restriction );
	
		m_cGen.setPTBox( ptbox );
		m_type = PROBLEM_TYPE.PSAT;
		
		if (m_lp == null) {
			
			BasicLPGeneratorImpl lpGen = null;
			IndexSetGenerator gen = new RandomConceptTypeSetGenImpl();
			
			gen.setTermNumber( Math.min( ptbox.getDefaultConstraints().size(), 2) );
			lpGen = new BasicLPGeneratorImpl(gen);
			//Check PSAT first, get the basic LP model
			rmp = lpGen.getLPforPSAT( ptbox );
			
			m_cGen.setEntailmentClass( ATermUtils.TOP );
			m_cGen.setConstraintList( rmp.getConstraintList() );
			completeLP( rmp, m_cGen, false );
				
			if (!LPUtils.isPSATLPinstanceSolvable( rmp )) {
					
				throw new ProbabilisticInconsistencyException("Ontology is unsatisfiable");
			} 
			
			m_cGen.reset();
			//Save for future use
			m_lp = rmp;
			
		} else {
			
			rmp = m_lp;
		}
		
		List<ConditionalConstraint> ccList = rmp.getConstraintList();
		
		try {
			//This is a bit ugly but we need to get coefficients of the row which corresponds
			//to the original (concept|thing)[0;1] added to the RMP.
			//If we simply pass "restriction", it may not be found (has a different ID)
			double[] obj = rmp.getConstraintLowerRow( ccList.get( ccList.indexOf( restriction ) ) );
			
			rmp.setObjective( obj );
			rmp.setMaximize( max );
			rmp.unsetBoundingRows();
			rmp.setBoundingRows();
			
		} catch( CPException e ) {

			s_logger.fatal( "Can't create the LP model for TLogEnt", e );
			throw new RuntimeException(e);
		}
		
		m_cGen.setEntailmentClass( concept );
		m_cGen.setConstraintList( rmp.getConstraintList() );
		m_type = PROBLEM_TYPE.TLOGENT;
		completeLP( rmp, m_cGen, true );
		
		if (added)	ptbox.removeDefaultConstraint( restriction );

		return rmp;		
	}

	/**
	 * The key method. It enriches the current RMP by adding new columns until it's optimal
	 * 
	 * @param rmp
	 * @param cGen
	 * @param solver
	 */
	protected double generateColumns(ColumnGeneratorEx cGen, CCAwareLPSolver solver) throws CPException {
		
		List<double[]> columns = null;
		LPSolver.STATUS status = LPSolver.STATUS.UNDEFINED;
		double objValue = 0;
		double[] duals = null;
		int cgIterCounter = 1;
		//Here we start the program generation main loop. We will add
		//columns to RMP until there's no improving column.
		while( true ) {
			// Solve RMP to obtain duals
			double t = System.currentTimeMillis();
			
			s_logger.debug("Column generation step #" + cgIterCounter);	
			
			status = solver.solveLP();
			objValue = status == LPSolver.STATUS.OPTIMAL ? solver.getObjValue() : -1d;
			duals = solver.getDuals();
			
			s_logger.debug( "RMP solved in: " + (System.currentTimeMillis() - t) +
							" ms, objective value: " + objValue);
			
			columns = m_cGen.generateColumns( duals, solver.getMaximize() );

			s_logger.debug( columns.size() + " new columns generated in " +
														(System.currentTimeMillis() - t) + " ms");

			if( !columns.isEmpty() ) {
				//The column is added directly into the LP model, not via
				//any generic wrapper (like LPInstance)
				for (double[] column : columns)	solver.addColumn( column, null );
				
				cgIterCounter += 1;
			}
			else {
				// RMP is optimal, so we may stop
				break;
			}
		}
		
		setNonZeroVarIndexes(ArrayUtils.getSupport(duals) );
		
		return objValue;
	}

	protected ColumnGeneratorEx getColumnGenerator() {
		
		return m_cGen;
	}
	
	protected CCAwareLPSolver getLPModel() {
		
		return m_lp;
	}
	
	protected void setNonZeroVarIndexes(Collection<Integer> indexes) {
		
		m_nonZeroVars.clear();
		m_nonZeroVars.addAll( indexes );
	}
	
	protected Set<Integer> getNonZeroIndexes() {
		
		return m_nonZeroVars;
	}
	
	@Override
	public String getMeasure(String measure) {
		//Does not measure anything
		return null;
	}

	@Override
	public void resetMeasure(String measure) {}

	public void resetMeasures(){}

	@Override
	public Collection<String> getMeasureNames() {

		return Collections.emptySet();
	}

	@Override
	public void setMeasure(String name, String measure) {}

	@Override
	public boolean isMeasureSupported(String measure) {

		return false;
	}
}
