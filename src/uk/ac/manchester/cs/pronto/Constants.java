/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.net.URI;

/**
 * <p>Title: Constants</p>
 * 
 * <p>Description: 
 *  Keeps important constants
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public interface Constants {
	
	public static enum PSAT_SOLVER_ENUM {BASIC, CG};
	public static enum MP_SOLVER {GLPK, CPLEX};
	
	//Probabilities below this are considered equal to zero
	public static double PROBABILITY_LOW_THRESHOLD = 1.0E-6;
	public static int PRECISION = (int)(-1 * Math.log10(PROBABILITY_LOW_THRESHOLD));
	
	public static URI CERTAINTY_ANNOTATION_URI = URI.create( "http://clarkparsia.com/pronto#certainty" );
	public static String PRONTO_DEFAULT_URI = "http://clarkparsia.com/pronto#";
	public static String PRONTO_CLASS_AUTO_IRI = "http://clarkparsia.com/pronto#clazz";
	
	public static final boolean GENERATE_NAMED_CLASSES_FOR_CONSTRAINTS = false;
	
	public static final String SERIALIZATION_FOLDER_PREFIX = "dump/";
	/**
	 * Various constants to turn on/off certain optimizations
	 */
	public static final boolean USE_PRONTO_CACHE = true;
	public static final int USE_SIMPLE_INDEX_SETS = 0;
	public static final int USE_HIERACHICAL_INDEX_SETS = 1;
	public static final int INDEX_SET_GENERATION = USE_SIMPLE_INDEX_SETS;
	public static final boolean USE_CG_ZPARTITIONER = false;
	public static final int USE_BASIC_LEX_REASONER = 0;
	public static final int USE_CG_LEX_REASONER = 1;
	public static final int USE_HS_LEX_REASONER = 2;	
	public static final int LEX_REASONER = USE_HS_LEX_REASONER;	
	public static PSAT_SOLVER_ENUM PSAT_SOLVER_CLASS = PSAT_SOLVER_ENUM.CG;
	public static MP_SOLVER LP_SOLVER = MP_SOLVER.GLPK;
	public static MP_SOLVER MIP_SOLVER = MP_SOLVER.GLPK;
	public static double COHERENCE_THRESHOLD = 0.01;
}
