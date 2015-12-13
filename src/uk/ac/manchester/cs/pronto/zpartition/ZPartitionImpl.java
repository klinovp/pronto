/**
 * 
 */
package uk.ac.manchester.cs.pronto.zpartition;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;

/**
 * <p>Title: ZPartition</p>
 * 
 * <p>Description: 
 *  Represents a z-partition of a probabilistic knowledge base
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class ZPartitionImpl implements ZPartition {
	
	private TreeSet<OrderWrapper> m_partition = new TreeSet<OrderWrapper>();

	/**
	 * Adds the new subset on top of the z-partition (assumes these are the most
	 * specific constraints)
	 * @param ccSubset
	 */
	public void add(Set<ConditionalConstraint> ccSubset) {
		
		int last = m_partition.size() > 0 ? m_partition.first().m_order : -1;
		
		m_partition.add( new OrderWrapper( new HashSet<ConditionalConstraint>( ccSubset ), last + 1));
	}
	
	
	public void add(Set<ConditionalConstraint> ccSubset, int specificity) {
		
		Set<ConditionalConstraint> subset = new HashSet<ConditionalConstraint>();
		
		subset.addAll( ccSubset );
		
		m_partition.add(new OrderWrapper(subset, specificity));
	}
	
	public int numberOfPartitions() {
		
		return m_partition.size();
	}
	
	/**
	 * Retrieves partitions in the order of decreasing specificity 
	 * 
	 * @return
	 */
	public Iterator<Set<ConditionalConstraint>> partitionIterator() {
		
		return partitionIterator(false);
	}

	public Iterator<Set<ConditionalConstraint>> partitionIterator(boolean reverse) {
		
		if (reverse) {
			//Sometimes we need to retrieve the most generic first 
			TreeSet<OrderWrapper> partition = new TreeSet<OrderWrapper>(Collections.reverseOrder());
			
			partition.addAll( m_partition );
			
			return new PartitionIterator(partition.iterator());
			
		} else {
			
			return new PartitionIterator(m_partition.iterator());	
		}
		
		
	}
	
	
	public ZPartitionImpl clone() {
		
		ZPartitionImpl zp = new ZPartitionImpl();
		
		zp.m_partition = new TreeSet<OrderWrapper>(m_partition);
		
		return zp;
	}
	
	public String toString() {
		
		StringBuffer buf = new StringBuffer("");
		
		for (Iterator<Set<ConditionalConstraint>> iter = partitionIterator(); iter.hasNext();) {
			
			Set<ConditionalConstraint> subset = iter.next();
			
			buf.append( subset.size() + ": " + subset );
			buf.append( System.getProperty( "line.separator" ) );
		}
		
		return buf.toString();
	}

	/**
	 * I need this silly wrapper around a subset of conditional constraints to keep
	 * them in the needed order (of decreasing specificity)
	 * 
	 * @author pavel
	 *
	 */
	class OrderWrapper implements Comparable<OrderWrapper> {
		
		protected int m_order;
		protected Set<ConditionalConstraint> m_subset;
		
		OrderWrapper(Set<ConditionalConstraint> ccSubset, int order) {
			m_subset = ccSubset;
			m_order = order;
		}
		
		protected Set<ConditionalConstraint> constraints() {
			
			return m_subset;
		}
		
		protected int specificity() {
			return m_order;
		}

		/*
		 * Higher order = more sprecific => should go ahead
		 */
		public int compareTo(OrderWrapper subset) {

			return - (m_order - subset.specificity());
		}
	}
	
	
	class PartitionIterator implements Iterator<Set<ConditionalConstraint>> {

		private Iterator<OrderWrapper> m_zpIter;
		
		PartitionIterator(Iterator<OrderWrapper> zpIter) {
			m_zpIter = zpIter; 
		}
		
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return m_zpIter.hasNext();
		}

		public Set<ConditionalConstraint> next() {
			// TODO Auto-generated method stub
			return m_zpIter.next().constraints();
		}

		public void remove() {}
	}
	
}
