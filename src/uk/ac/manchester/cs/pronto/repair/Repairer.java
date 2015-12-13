/**
 * 
 */
package uk.ac.manchester.cs.pronto.repair;

import java.util.Set;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;

/**
 * Should be implemented by classes which know how to repair unsatisfiable PTBoxes
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * 8 Nov 2010
 */
public interface Repairer {

	public PTBox repair(PTBox ptbox, Set<ConditionalConstraint> toughConstraints);
	public RepairReport getReport();
}
