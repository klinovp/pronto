/**
 * 
 */
package uk.ac.manchester.cs.pronto.zpartition;

import java.util.Iterator;
import java.util.Set;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface ZPartition {

	public void add(Set<ConditionalConstraint> ccSubset);
	public void add(Set<ConditionalConstraint> ccSubset, int specificity);
	public int numberOfPartitions();
	public Iterator<Set<ConditionalConstraint>> partitionIterator();
	public Iterator<Set<ConditionalConstraint>> partitionIterator(boolean reverse);
	public ZPartitionImpl clone();
}
