/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.pronto.events.AfterComputeConflictGraphEventHandlerImpl;
import uk.ac.manchester.cs.pronto.events.AfterComputeZPartitionEventHandlerImpl;
import uk.ac.manchester.cs.pronto.events.BeforeComputeConflictGraphEventHandlerImpl;
import uk.ac.manchester.cs.pronto.events.BeforeComputeZPartitionEventHandlerImpl;
import uk.ac.manchester.cs.pronto.events.EVENT_TYPES;
import uk.ac.manchester.cs.pronto.events.EventConstants;
import uk.ac.manchester.cs.pronto.exceptions.OntologyLoadingException;
import uk.ac.manchester.cs.pronto.exceptions.QueryProcessingException;
import uk.ac.manchester.cs.pronto.io.KBEmbeddedLoader;
import uk.ac.manchester.cs.pronto.io.KBStandaloneLoader;
import uk.ac.manchester.cs.pronto.lp.LPSolver;
import uk.ac.manchester.cs.pronto.lp.MIPSolverEx;
import uk.ac.manchester.cs.pronto.lp.cplex.CPLEXLPSolverImpl;
import uk.ac.manchester.cs.pronto.lp.cplex.CPLEXMIPSolverImpl;
import uk.ac.manchester.cs.pronto.lp.glpk.GLPKLPSolverImpl;
import uk.ac.manchester.cs.pronto.lp.glpk.GLPKMIPSolverImpl;
import uk.ac.manchester.cs.pronto.query.EntailmentQueryImpl;
import uk.ac.manchester.cs.pronto.query.PQuery;
import uk.ac.manchester.cs.pronto.query.PQueryResult;
import uk.ac.manchester.cs.pronto.query.SimpleQueryImpl;

/**
 * <p>Title: Pronto</p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class Pronto {
	
	static Logger s_logger = Logger.getLogger(Pronto.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) throws OntologyLoadingException, QueryProcessingException {

		ProbKnowledgeBase pkb = null;
		
		if (args.length < 3 || "--help".equals(args[0])) {
			help();
			return;
		}
		
		try {
			
			pkb = loadProbabilisticOntology( args[0], args[1] );
			
			if (null != pkb) {
				
				run(pkb, args[2]); 
			}
			
		} catch (OntologyLoadingException ole) {
			
			System.out.println("Ontology loading failed, see below");
			
			if (null != ole.getMessage()) {
				
				System.out.println(ole.getMessage());
			}

			if (null != ole.getCause()) {
				
				ole.getCause().printStackTrace();
			}

			
		} catch (QueryProcessingException qpe) {
			
			System.out.println("Query processing failed, see below");
			
			if (null != qpe.getMessage()) {
				
				System.out.println(qpe.getMessage());
			}
			
			if (null != qpe.getCause()) {
				
				qpe.getCause().printStackTrace();
			}
		}
		
	}

	private static ProbKnowledgeBase loadProbabilisticOntology(String ontoURI, String mode) throws OntologyLoadingException {

		if ("s".equals(mode)) {
			
			return (new KBStandaloneLoader()).load(ontoURI );
			
		} else if ("e".equals(mode)) {
			
			return (new KBEmbeddedLoader()).load(ontoURI );
			
		} else {
			
			help();
			
			return null;
		}
	}

	
	private static void run(ProbKnowledgeBase pkb, String queryURI) throws QueryProcessingException {
		
		PQuery query = extract(queryURI);
		ProntoQueryProcessor qp = new ProntoQueryProcessor( createReasoner() );
		PQueryResult qResult = null;
		
		qp.setProbKnowledgeBase(pkb);
		qp.setExplanationsRequired( true );
		
		long t = System.currentTimeMillis();
		qResult = qp.process(query);
		
		System.out.println("--------------------");		
		System.out.println("Query : " + query);
		System.out.println("Result: " + qResult.toString() + "\n");
		System.out.println("Result computed in " + (System.currentTimeMillis() - t) + "ms");		
		
		s_logger.info("--------------------");		
		s_logger.info("Query : " + query);
		s_logger.info("Result: " + qResult.toString() + "\n");
		s_logger.info("Result computed in " + (System.currentTimeMillis() - t) + "ms");		
	}
	
	
	public static PQuery extract(String queryURI) throws QueryProcessingException {
		
		BufferedReader reader = null;
		
		try {
			
			reader = new BufferedReader(new InputStreamReader(URI.create(queryURI).toURL().openStream()));
			
			return createQuery(reader.readLine());
			
		} catch (IOException ioe) {
			
			throw new QueryProcessingException(ioe, "Query file couldn't be loaded");
		}
	}
	
	
	public static PQuery createQuery(String qString) throws QueryProcessingException {
		
		PQuery query = null;
		String type = null;
		//Get query type first
		type = (0 > qString.indexOf( " " ))
			? qString
			: qString.substring( 0, qString.indexOf( " " ) );

		if( "entail".equals( type ) ) {
			
			query = new EntailmentQueryImpl();
			
		} else if( "psat".equals( type ) ) {
			
			query = new SimpleQueryImpl(PQuery.SATISFIABILITY_QUERY );
			
		} else if( "consistency".equals( type ) ) {
			
			query = new SimpleQueryImpl( PQuery.CONSISTENCY_QUERY );
			
		} else if( "unsat_subsets".equals( type ) ) {
			
			query = new SimpleQueryImpl(PQuery.UNSAT_SUBSETS_QUERY );
		}

		if( null == query ) {
			
			throw new QueryProcessingException( null, "Query type not found" );
		}

		if( !type.equals( qString ) ) {
			
			query.deserialize( qString.substring( qString.indexOf( " " ) ), " " );
		}
		
		return query;
	}
	
	public static ProntoReasoner createReasoner() {
		
		ProntoReasoner reasoner = null;
		
		switch ( Constants.LEX_REASONER ) {

		case Constants.USE_HS_LEX_REASONER:
			
			reasoner = new HSOptimizedLexReasoner( createPSATSolver() );
			break;
			
		default:
			
			reasoner = new BasicLexicographicReasoner( createPSATSolver() );
		}
		
		setDefaultEventHandlers(reasoner);
		
		return reasoner;
	}
	
	public static PSATSolver createPSATSolver() {
		
		return new PSATSolverImpl();
	}
	
	public static LPSolver createLPSolver() {
		
		switch (Constants.LP_SOLVER) {
		
		case GLPK: return new GLPKLPSolverImpl();
		case CPLEX: return new CPLEXLPSolverImpl();
		default: return new GLPKLPSolverImpl();
		
		}
	}
	
	public static MIPSolverEx createMIPSolver() {
		
		switch (Constants.MIP_SOLVER) {
		
		case GLPK: return new GLPKMIPSolverImpl();
		case CPLEX: return new CPLEXMIPSolverImpl();
		default: return new GLPKMIPSolverImpl();
		
		}
	}
	
	/**
	 * Installs handlers of basic events 
	 */
	public static void setDefaultEventHandlers(ProntoReasoner reasoner) {

		if (Boolean.valueOf( true ).equals(
				EventConstants.S_EVENT_CFG.get(EVENT_TYPES.BEFORE_CONFLICT_GRAPH_COMPUTED))) {
			
			reasoner.setEventHandler(EVENT_TYPES.BEFORE_CONFLICT_GRAPH_COMPUTED,
			                         new BeforeComputeConflictGraphEventHandlerImpl() );
		}
		
		if (Boolean.valueOf( true ).equals(
				EventConstants.S_EVENT_CFG.get(EVENT_TYPES.AFTER_CONFLICT_GRAPH_COMPUTED))) {
			
			reasoner.setEventHandler( 	EVENT_TYPES.AFTER_CONFLICT_GRAPH_COMPUTED,
										new AfterComputeConflictGraphEventHandlerImpl() );
		}
		
		if (EventConstants.S_EVENT_CFG.get(EVENT_TYPES.BEFORE_ZPARTITION_COMPUTED)) {
			
			reasoner.setEventHandler( 	EVENT_TYPES.BEFORE_ZPARTITION_COMPUTED, new BeforeComputeZPartitionEventHandlerImpl() );			
		}
		
		if (EventConstants.S_EVENT_CFG.get(EVENT_TYPES.AFTER_ZPARTITION_COMPUTED)) {
			
			reasoner.setEventHandler( 	EVENT_TYPES.AFTER_ZPARTITION_COMPUTED, new AfterComputeZPartitionEventHandlerImpl() );
		}		
	}
	
	private static void help() {
		
		System.out.println("Pronto - a probabilistic extension to Pellet (by Pavel Klinov)");
		System.out.println("Usage:");
		System.out.println("pronto.bat(sh) <ontology uri> <mode> <query file uri>");
		System.out.println("mode can be 's' (probabilistic part in a separate file) or 'e' (embedded into classical part)");
		System.out.println("query file format: <query type> <query parameter>*");
		System.out.println("supported queries:");
		System.out.println(" - entail <evidence class uri> <conclusion class uri>");
		System.out.println(" - entail <individual uri> <class uri>");		
		System.out.println(" - psat - decides probabilistic satisfiability");
		System.out.println(" - consistency - decides probabilistic consistency");
		System.out.println(" - unsat_subsets - computes minimal unsatisfiable subsets");
		System.out.println("pavel.klinov@gmail.com if you have questions. enjoy.");		
	}
}
