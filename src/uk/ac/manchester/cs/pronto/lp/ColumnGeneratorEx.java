/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

import java.util.List;

import uk.ac.manchester.cs.pronto.exceptions.CPException;

/**
 * An extended interface for generators that are able to generate multiple columns
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * 29 Jan 2010
 */
public interface ColumnGeneratorEx extends ColumnGenerator {

	public List<double[]> generateColumns(double[] duals, boolean max) throws CPException;
	public void setColumnsNumber(int number);
}
