/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.benchmark.TelemetryEx;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Interface for classes that generate LP instances for PSAT and TLogEnt problems
 */
public interface LPGenerator extends TelemetryEx {

	public enum TELEMETRY {COL_NUMBER, TOTAL_COL_GEN_TIME};
	
	/*
	 * If this method is called prior to getLP*() then the latter may try
	 * to _enrich_ the LP system (i.e. generate and add columns) rather than
	 * start from scratch.
	 * Note that it's the caller's responsibility to ensure that this LP is
	 * a valid one. Otherwise the results are undefined
	 */
	public void setInitialLP(CCAwareLPSolver model);
	
	/*
	 * If the optimization of this instance gives 1 then the PTBox is satisfiable, if anything
	 * less than 1 - unsatisfiable
	 */
	public CCAwareLPSolver getLPforPSAT(PTBox ptbox);
	public CCAwareLPSolver getLowerLPforTLogEnt(PTBox ptbox, ATermAppl concept);
	public CCAwareLPSolver getUpperLPforTLogEnt(PTBox ptbox, ATermAppl concept);
	
}
