package uk.ac.manchester.cs.pronto.util;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;


public class ArrayUtilsTest {

	@Test
	public void testLeftCyclicShift() {
	
		double[] array = new double[] {1, 2, 3, 4, 5};
		
		ArrayUtils.cyclicShift( array, 3 );
		
		assertTrue(Arrays.equals(array, new double[]{4, 5, 1, 2, 3}));
	}

	@Test
	public void testLeftCyclicShift2() {
	
		double[] array = new double[] {1, 2, 3, 4, 5};
		
		ArrayUtils.cyclicShift( array, -3 );
		
		assertTrue(Arrays.equals(array, new double[]{3, 4, 5, 1, 2}));
	}
	
	@Test
	public void testInsert() {
	
		double[] array = new double[] {1, 2, 3, 4, 5};
		double[] newArray = ArrayUtils.insert( array, 11, 2 );
		
		assertTrue(Arrays.equals(newArray, new double[]{1, 2, 11, 3, 4, 5}));
	}	
	
	@Test
	public void testAdd() {
	
		double[] array = new double[] {1, 2, 3, 4, 5};
		double[] newArray = ArrayUtils.add( array, 13 );
		
		assertTrue(Arrays.equals(newArray, new double[]{1, 2, 3, 4, 5, 13}));
	}	
			
}
