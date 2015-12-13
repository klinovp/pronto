/**
 * 
 */
package uk.ac.manchester.cs.pronto.constraints;

import java.util.Set;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;

/**
 * Determines if one set of constraints if subsumed by another.
 * This isn't that trivial as it may seem
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface CCSetInclusionQualifier {

	public boolean includes(Set<ConditionalConstraint> ccSet1, Set<ConditionalConstraint> ccSet2);
}
