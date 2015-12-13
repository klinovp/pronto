/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.List;
import java.util.Map;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.benchmark.Telemetry;
import uk.ac.manchester.cs.pronto.events.EVENT_TYPES;
import uk.ac.manchester.cs.pronto.events.ReasoningEventHandler;
import uk.ac.manchester.cs.pronto.zpartition.ZPartition;

/**
 * <p>Title: ConsistencyChecker</p>
 * 
 * <p>Description: 
 *  Classes implementing this interface are responsible for checking
 *  the consistency of probabilistic knowledge bases. Each class may have its own
 *  notion of "consistency"
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public interface ConsistencyChecker extends Telemetry {

	/*
	 * PTBox is g-consistent and probabilistic assertional knowledge is
	 * consistent with TBox for every individual
	 */
	public static int PKB_CONSISTENT = 0;
	/*
	 * PTBox is not g_consistent
	 */
	public static int PKB_G_INCONSISTENT = -1;
	/*
	 * PTBox is g_consistent but there individuals for which probabilistic
	 * assertional knowledge contradicts with TBox
	 */
	public static int PKB_A_INCONSISTENT = -2;	
	
	public static int PTBOX_UNSATISFIABLE = -3;	
	
	public int isConsistent(ProbKnowledgeBase pkb);
	
	public boolean isAConsistent(ProbKnowledgeBase pkb, ATermAppl individual);
	
	public ZPartition decideGenericConsistency(PTBox pkb);
	
	public void setEventHandlers(Map<EVENT_TYPES, List<ReasoningEventHandler>> handlersMap);
}
