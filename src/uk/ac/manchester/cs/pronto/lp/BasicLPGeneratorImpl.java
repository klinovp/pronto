/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.exceptions.CPException;
import uk.ac.manchester.cs.pronto.index.ConceptTypeSetGeneratorImpl;
import uk.ac.manchester.cs.pronto.index.IndexSet;
import uk.ac.manchester.cs.pronto.index.IndexSetGenerator;
import uk.ac.manchester.cs.pronto.index.IndexTerm;

/**
 * <p>Title: LPGenerator</p>
 * 
 * <p>Description: 
 *  Generates LP instances for such problems as SAT or TLogEnt
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */ 

public class BasicLPGeneratorImpl implements LPGenerator {

	Logger	m_logger = Logger.getLogger( this.getClass() );

	private IndexSetGenerator m_gen;

	public BasicLPGeneratorImpl() {

		this( new ConceptTypeSetGeneratorImpl() );
	}

	public BasicLPGeneratorImpl(IndexSetGenerator gen) {

		m_gen = gen;
	}

	/**
	 * Generates the system of linear inequalities from PTbox = <TBox,F - set of
	 * conditional constraints>
	 * 
	 * @return
	 */
	public CCAwareLPSolver getLPforPSAT(PTBox ptbox) {
		//First, obtain the index set
		IndexSet indexSet = m_gen.generate(ptbox );
		CCAwareLPSolver lp = getBasicModel( ptbox, indexSet );
		double[] obj = new double[lp.getColumnNumber()];
		
		Arrays.fill(obj, 1d);
		
		try {
			
			lp.setObjective( obj );
			lp.setUpperBoundingRow();
			lp.setMaximize( true );
			
		} catch( CPException e ) {
			
			m_logger.fatal( "Error bulding the LP model for PSAT", e );
			
			throw new RuntimeException(e);
		}
			
		return lp;
	}

	protected CCAwareLPSolver getBasicModel(PTBox ptbox, IndexSet indexSet) {
		
		try {
			//Construct the solver and init the model
			CCAwareLPSolver lp = LPConfiguration.createCCAwareLPSolver();
			
			lp.initLPInstance( indexSet.getTerms().size() );
			//Now generate the linear inequalities
			generateLinearInequalities( lp, indexSet, ptbox );
			
			return lp;
			
		} catch( CPException e ) {
			
			m_logger.fatal( "Error bulding the basic LP model", e );
			
			throw new RuntimeException(e);
		}
	}
	
	
	/*
	 * Adds new rows to the LP model
	 */
	protected void addInequality(	ConditionalConstraint cc,
									IndexSet indexSet,
									CCAwareLPSolver model) throws CPException {

		double[] first = new double[indexSet.getTerms().size()];
		double[] second = new double[indexSet.getTerms().size()];
		
		//if (cc.isConditional()) {
			
			if( cc.getLowerBound() < 1 || cc.getUpperBound() < 1 ) {

				for( IndexTerm term : indexSet.getSubsumedTerms(cc.getDCTerm() ) ) {

					first[term.getIndex()] = (1 - cc.getLowerBound());
					second[term.getIndex()] = (cc.getUpperBound() - 1);
				}
			}

			if( cc.getLowerBound() > 0 || cc.getUpperBound() > 0 ) {

				for( IndexTerm term : indexSet.getSubsumedTerms( cc.getNotDCTerm() ) ) {

					first[term.getIndex()] = -cc.getLowerBound();
					second[term.getIndex()] = cc.getUpperBound();
				}
			}
			
			model.addRows( cc, first, second );
			
		/*} else {
			
			for( IndexTerm term : indexSet.getSubsumedTerms( cc.getConclusion() ) ) {

				first[term.getIndex()] = 1d;
			}
			
			model.addRows( cc, first, first );
		}*/
	}

	/**
	 * Generates the matrix of coefficients of linear inequalities
	 * 
	 * @param ccList
	 * @return
	 */
	protected void generateLinearInequalities(	CCAwareLPSolver model,
												IndexSet indexSet,
												PTBox ptbox) throws CPException {

		for (ConditionalConstraint cc : ptbox.getDefaultConstraints()) {
			
			addInequality(cc, indexSet, model);
		}
	}
	
	/**
	 * Finds those index items that entail concepts and assign 1 as coefficient
	 * to the corresponding variables. All others are set to zero
	 * 
	 * @param ptbox
	 * @param indexSet
	 * @param concept
	 * @return
	 */
	protected double[] generateObjectiveForConcept(IndexSet indexSet, ATermAppl concept) {

		double[] objective = new double[indexSet.getTerms().size()];

		for( IndexTerm term : indexSet.getSubsumedTerms( concept ) ) {
			
			objective[term.getIndex()] = 1.0;
		}

		return objective;
	}

	/**
	 * Generates a complete LP instance for computing tight logical entailment
	 * 
	 * @param ptbox
	 * @param concept
	 * @return
	 */
	protected CCAwareLPSolver getLPforTLogEnt(PTBox ptbox, ATermAppl concept, boolean max) {

		//First, obtain the index set
		IndexSet indexSet = m_gen.generate( ptbox );		
		CCAwareLPSolver lp = getBasicModel( ptbox, indexSet );
		double[] obj = generateObjectiveForConcept( indexSet, concept );
		
		try {
			
			lp.setObjective( obj );
			lp.setMaximize( max );
			lp.setBoundingRows();
			
		} catch( CPException e ) {
			
			m_logger.fatal( "Error bulding the LP model for TLogEnt", e );
			
			throw new RuntimeException(e);
		}
			
		return lp;		
	}

	public CCAwareLPSolver getLowerLPforTLogEnt(PTBox ptbox, ATermAppl concept) {
		
		return getLPforTLogEnt( ptbox, concept, false );
	}
	
	public CCAwareLPSolver getUpperLPforTLogEnt(PTBox ptbox, ATermAppl concept) {
		
		return getLPforTLogEnt( ptbox, concept, true );
	}	
	
	protected static void dumpIndexSet(IndexSet indexSet, Writer out) throws IOException {

		for( IndexTerm term : indexSet.getTerms() ) {
			
			out.write( term.getIndex() + ". " + term.toString()	+ System.getProperty( "line.separator" ) );
		}
	}

	@Override
	public void setInitialLP(CCAwareLPSolver model) {}

	
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
