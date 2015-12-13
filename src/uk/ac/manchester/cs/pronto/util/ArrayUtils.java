package uk.ac.manchester.cs.pronto.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Sep 19, 2008
 * 
 * Provides a few utility methods, e.g. for copying.
 * Warning: the class assumes that all bidimensional arrays are actually
 * matrices, so it may work incorrectly if rows have unequal lengths
 */
public class ArrayUtils {

	public static double[] resize(double[] oldArray, int newSize) {
		
		if( oldArray.length != newSize ) {

			double[] newArray = new double[newSize];

			System.arraycopy( oldArray, 0, newArray, 0, Math.min( newSize, oldArray.length ) );

			return newArray;
			
		} else {

			return oldArray;
		}
	}
	
	public static double[] remove(double[] array, int index) {

		if (index < array.length && array.length > 0) {
			
			double[] result = new double[array.length - 1];
			
			System.arraycopy( array, 0, result, 0, index );
			System.arraycopy( array, index + 1, result, index, array.length - 1 - index );
			
			return result;
			
		} else {
			
			throw new IllegalArgumentException("Wrong element index or the array is empty");
		}
	}
	
	public static double[] resize(double[] oldArray, int newSize, int startIndex) {
		
		if( oldArray.length != newSize ) {

			double[] newArray = new double[newSize];

			System.arraycopy( oldArray, 0, newArray, startIndex
							, Math.min( newSize - startIndex, oldArray.length ) );

			return newArray;
			
		} else {

			return oldArray;
		}
	}	
	
	public static double[][] resize(double[][] oldMatrix, int newRowCount, int newColumnCount) {

		double[][] newMatrix = new double[newRowCount][];

		for( int i = 0; i < oldMatrix.length; i++ ) {

			if( newColumnCount != oldMatrix[0].length ) {

				newMatrix[i] = resize( oldMatrix[i], newColumnCount );
				
			} else {
				
				newMatrix[i] = oldMatrix[i];				
			}
		}

		return newMatrix;
	}
	
	public static double[] add(double[] array, double element) {
		
		return insert(array, element, array.length);
	}
	
	public static int[] add(int[] array, int element) {
		
		int[] newArr = new int[array.length + 1];
		
		System.arraycopy( array, 0, newArr, 0, array.length );
		newArr[newArr.length - 1] = element;
		
		return newArr;
	}	
	
	public static double[] insert(double[] array, double element, int position) {
		
		if (position < 0) position = 0;
		
		if (position >= array.length) position = array.length;
		
		double[] newArray = new double[array.length + 1];

		System.arraycopy( array, 0, newArray, 0, position );
		newArray[position] = element;
		System.arraycopy( array, position, newArray, position + 1, array.length - position );
		
		return newArray;
	}	
	
	public static double[][] addRow(double[][] matrix, double[] row) {
		
		double[][] newMatrix = resize(matrix, matrix.length + 1, row.length);
		
		newMatrix[newMatrix.length - 1] = row;
		
		return newMatrix;
	}

	public static double[][] addColumn(double[][] matrix, double[] column) {
		
		if (column.length != matrix.length) {
			
			throw new IllegalArgumentException("Column length mismatch");
		}
		
		double[][] newMatrix = resize(matrix, column.length, matrix[0] == null ? 1 : matrix[0].length);
		
		for (int i = 0; i < newMatrix.length; i++) {
			
			newMatrix[i] = add(matrix[i], column[i]);
		}
		
		return newMatrix;
	}
	
	public static double[] clone(double[] vector) {
		
		double[] clon = new double[vector.length];
		
		System.arraycopy( vector, 0, clon, 0, vector.length );
		
		return clon;
	}
	
	public static int[] clone(int[] vector) {
		
		int[] clon = new int[vector.length];
		
		System.arraycopy( vector, 0, clon, 0, vector.length );
		
		return clon;
	}	
	
	public static void sort(double[] vector, boolean ascending) {
		
		Arrays.sort( vector );
		
		if (!ascending) {
			
			for (int i = 0; i <= (vector.length - 1) / 2; i++) {
				
				double tmp = vector[i];
				
				vector[i] = vector[vector.length - i - 1];
				vector[vector.length - i - 1] = tmp;
			}
		}
	}
	
	public static void swap(double[] array, int i, int j) {
		
		double tmp = array[i];
		
		array[i] = array[j];
		array[j] = tmp;
	}
	
	public static List<Integer> toIntegerList(int[] array) {
		
		List<Integer> list = new ArrayList<Integer>(array.length);
		
		for (int el : array) list.add( el );
		
		return list;
	}
	
	public static int[] fromIntegerCollection(Collection<Integer> collection) {
		
		int[] array = new int[collection.size()];
		int i = 0;
		
		for (int item : collection) array[i++] = item;
		
		return array;
	}
	
	public static List<Double> toDoubleList(double[] array) {
		
		List<Double> list = new ArrayList<Double>(array.length);
		
		for (double el : array) list.add( el );
		
		return list;
	}
	
	public static double[] fromDoubleCollection(Collection<Double> collection) {
		
		double[] array = new double[collection.size()];
		int i = 0;
		
		for (double item : collection) array[i++] = item;
		
		return array;
	}	
	/*
	 * FIXME Create something like ListUtils for the following two methods
	 */
	public static <T> void leftCyclicShift(List<T> list, int steps) {
		
		for (int i = 0; i < steps; i++) {
			
			list.add( list.remove( 0 ) );
		}
	}
	/**
	 * 
	 * @param array Array to be cyclically shifted
	 * @param steps If positive, the shift will be left, otherwise - right
	 */
	public static void cyclicShift(double[] array, int steps) {
		
		int count = 0;
		int index = 0;
		double next = array[index];
		
		while (count < array.length) {
			
			double current = next;			
			//Find new place for array[index]
			index -= steps;
			
			if (index < 0 ) {
				
				index = array.length + (index % array.length);
				
			} else if (index >= array.length) {
				
				index = index % array.length; 
			}
			
			next = array[index];
			array[index] = current;
			count++;
		}
	}
	
	public static int linearSearch(double[] array, double element) {

		int index = -1;
		
		for (int i = 0; i < array.length; i++) {
			
			if (NumberUtils.equal( element, array[i] )) return i;
		}

		return index;
	}
	
	public static double max(double[] array) {
		
		double result = -Double.MAX_VALUE;
		
		for (double element : array) result = Math.max( result, element );
		
		return result;
	}
	
	public static double min(double[] array) {
		
		double result = Double.MAX_VALUE;
		
		for (double element : array) result = Math.min( result, element );
		
		return result;
	}	
	
	public static <T> void rightCyclicShift(List<T> list, int steps) {
		
		for (int i = 0; i < steps; i++) {
			
			list.add(0,  list.remove( list.size() - 1 ) );
		}
	}	
	
	public static Collection<List<Integer>> cartesian(Collection<Collection<Integer>> sets) {
		
		List<List<Integer>> product = new ArrayList<List<Integer>>();
		
		product.add(new ArrayList<Integer>());
		
		for (Collection<Integer> set : sets) product = cartesian( product, set );
		
		return product;
	}
	
	private static List<List<Integer>> cartesian(List<List<Integer>> product, Collection<Integer> next) {
		
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		
		for (Collection<Integer> tuple : product) {
			
			for (Integer item : next) {
				
				List<Integer> nextTuple = new ArrayList<Integer>(tuple);
				
				nextTuple.add(item);
				result.add( nextTuple );
			}
		}
		
		return result;
	}
	
	public static double[] parseDoubleArray(String string) {
		
		double[] array = null;
		String[] strArray = null;
		
		string = string.replace( '[', ' ' );
		string = string.replace( ']', ' ' );
		
		strArray = string.split( ", " );
		
		array = new double[strArray.length];
		
		for (int i = 0; i < array.length; i++) {
			
			array[i] = Double.parseDouble( strArray[i] );
		}
		
		return array;
	}
	
	public static int[] parseIntArray(String string) {
		
		int[] array = null;
		String[] strArray = null;
		
		string = string.replace( '[', ' ' );
		string = string.replace( ']', ' ' );
		
		strArray = string.split( "," );
		
		array = new int[strArray.length];
		
		for (int i = 0; i < array.length; i++) {
			
			array[i] = Integer.parseInt( strArray[i].trim() );
		}
		
		return array;
	}	
	
	public static int[] intArray(int start, int end) {
		
		int[] result = new int[end - start + 1];
		
		for (int i = start; i <= end; i++) result[i - start] = i;
		
		return result;
	}	
	
	public static int minElement(int[] array) {
		
		int result = Integer.MAX_VALUE;
		
		for (int element : array) result = Math.min( result, element );
		
		return result;
	}
	
	public static Set<Integer> intersection(int[] array1, int[] array2) {
		
		Set<Integer> result = new HashSet<Integer>();
		int[] copy = clone( array1 );
		
		for (int element : array2) {
			
			if (Arrays.binarySearch( copy, element ) > -1) result.add( element );
		}
		
		return result;
	}
	
	public static int nThLargest(int[] array, int n) {
		
		if (array.length < n + 1) throw new IllegalArgumentException("Array is too small");
		
		int[] clon = clone(array);
		
		Arrays.sort( clon );
		
		return clon[clon.length - n - 1];
	}
	
	public static int nThSmallest(int[] array, int n) {
		
		if (array.length < n + 1) throw new IllegalArgumentException("Array is too small");
		
		int[] clon = clone(array);
		
		Arrays.sort( clon );
		
		return clon[n];
	}	

	public static int maxElement(int[] array) {
		
		int result = Integer.MIN_VALUE;
		
		for (int element : array) result = Math.max( result, element );
		
		return result;
	}
	
	public static boolean contains(int[] array1, int[] array2) {
		
		Arrays.sort( array1 );
		//Arrays.sort( array2 );
		
		for (int el : array2) {
			
			if (Arrays.binarySearch( array1, el ) < 0) return false;
		}
		
		return true;
	}
	/*
	 * Reorders elements as specified in rMap
	 */
	public static double[] reorder(double[] array, Map<Integer, Integer> rMap, boolean forth) {
		
		double[] newArray = new double[array.length];
		
		for (Map.Entry<Integer, Integer> entry : rMap.entrySet()) {
			
			if (forth) {
				
				newArray[entry.getValue()] = array[entry.getKey()];
				
			} else {
				
				newArray[entry.getKey()] = array[entry.getValue()];
			}
		}		

		return newArray;
	}
	/*
	 * Stupid copy-paste from reorder(double[]) but arrays is a pain in Java
	 */
	public static int[] reorder(int[] array, Map<Integer, Integer> rMap) {
		
		int[] newArray = new int[array.length];
		
		for (int i = 0; i < array.length; i++) newArray[i] = rMap.get( array[i] );

		return newArray;
	}	
	
	public static double scalarProduct(double[] arr1, double[] arr2) {
		
		double result = 0;
		
		for (int i = 0; i < arr1.length; i++) {
			
			result += arr1[i] * arr2[i];
		}
		
		return NumberUtils.round( result );
	}
	
	public static double[] removeDuplicates(double[] array) {
		
		double[] result = null;
		Map<Double, Object> values = new HashMap<Double, Object>(array.length);
		
		for (double value : array) values.put( value, new Object() );
		{
			int i = 0;
			
			result = new double[values.size()];
			
			for (double value : values.keySet()) result[i++] = value;
		}
		
		return result;
	}
	
	public static double[] subArray(double[] array, int startIndex, int length) {
		
		double[] result = new double[length];
		
		System.arraycopy( array, startIndex, result, 0, length );
		
		return result;
	}
	
	public static Set<Integer> getSupport(double[] array) {
		
		Set<Integer> support = new HashSet<Integer>();
		
		for (int i = 0; i < array.length - 1; i++) {
			
			if (!NumberUtils.equal( array[i], 0d )) support.add( i );
		}
		
		return support;
	}
}
