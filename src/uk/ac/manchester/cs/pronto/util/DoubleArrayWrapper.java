/**
 * 
 */
package uk.ac.manchester.cs.pronto.util;

import java.util.Arrays;
import java.util.Comparator;

public class DoubleArrayWrapper implements Comparable<DoubleArrayWrapper> {
	
	private double[] m_array;
	private Comparable m_key;
	
	public DoubleArrayWrapper(double[] array) {
		
		m_array = array;
	}
	
	public DoubleArrayWrapper(double[] array, Comparable key) {
		
		this(array);
		m_key = key;
	}
	
	public Comparable getKey() {
		
		return m_key;
	}
	
	public void setKey(Comparable key) {
		
		m_key = key;
	}	

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof DoubleArrayWrapper)) {
			
			return false;
		}
		
		double[] array = ((DoubleArrayWrapper)obj).getArray();
		
		if (m_array.length == array.length) {
			
			for (int i = 0; i < m_array.length; i++) {
				
				if (!NumberUtils.equal( m_array[i], array[i] )) return false;
			}
		}
		
		return true;
		
		//return Arrays.equals( m_array, ((DoubleArrayWrapper)obj).getArray() );
	}

	@Override
	public int hashCode() {

		return Arrays.hashCode(m_array);
	}

	@Override
	public String toString() {

		return Arrays.toString( m_array);
	}
	
	public double[] getArray() {
		
		return m_array;
	}
	
	public void setArray(double[] array) {
		
		m_array = array;
	}

	public int compareTo(DoubleArrayWrapper o) {

		return m_key == null || o.getKey() == null ? 0 : m_key.compareTo( o.getKey() );
	}
	
	public static Comparator<DoubleArrayWrapper> reverseComparator() {
	
		return new Comparator<DoubleArrayWrapper>() {
			
			public int compare(DoubleArrayWrapper wrapper1, DoubleArrayWrapper wrapper2) {
				
				return wrapper2.compareTo(wrapper1);
			}
		};
	}
}