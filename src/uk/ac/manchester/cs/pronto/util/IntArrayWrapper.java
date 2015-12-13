/**
 * 
 */
package uk.ac.manchester.cs.pronto.util;

import java.util.Arrays;
import java.util.Comparator;

public class IntArrayWrapper implements Comparable<IntArrayWrapper> {
	
	private int[] m_array = null;
	private Comparable m_key = null;
	
	public IntArrayWrapper(int[] array) {
		
		m_array = array;
	}
	
	public IntArrayWrapper(int[] array, Comparable key) {
		
		m_array = array;
		m_key = key;
	}	
	
	public Comparable getKey() {
		
		return m_key;
	}

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof IntArrayWrapper)) {
			
			return false;
		}
		
		return Arrays.equals( m_array, ((IntArrayWrapper)obj).getArray() );
	}

	@Override
	public int hashCode() {

		return Arrays.hashCode(m_array);
	}

	@Override
	public String toString() {

		return Arrays.toString( m_array);
	}
	
	public int[] getArray() {
		
		return m_array;
	}

	public int compareTo(IntArrayWrapper o) {

		return m_key == null || o.getKey() == null ? 0 : m_key.compareTo( o.getKey() );
	}
	
	public static Comparator<IntArrayWrapper> reverseComparator() {
		
		return new Comparator<IntArrayWrapper>() {
			
			public int compare(IntArrayWrapper wrapper1, IntArrayWrapper wrapper2) {
				
				return wrapper2.compareTo(wrapper1);
			}
		};
	}	
}