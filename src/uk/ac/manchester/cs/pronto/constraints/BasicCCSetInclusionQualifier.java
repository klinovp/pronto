/**
 * 
 */
package uk.ac.manchester.cs.pronto.constraints;

import java.util.Set;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;

/**
 * Very straightforward, naive implementation
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public class BasicCCSetInclusionQualifier implements CCSetInclusionQualifier {

	/**
	 * @param ccSet1
	 * @param ccSet2
	 * @return
	 */
	public boolean includes(Set<ConditionalConstraint> ccSet1, Set<ConditionalConstraint> ccSet2) {
		
		return ccSet1.contains( ccSet2 );
	}

}
