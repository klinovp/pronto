/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

import java.util.List;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;
import uk.ac.manchester.cs.pronto.exceptions.CPException;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Sep 21, 2008
 * 
 * Interface for classes responsible for producing new columns during the
 * optimization process
 */
public interface ColumnGenerator {

	public enum PROBLEM_TYPE {PSAT, TLOGENT};
	
	public void reset();
	public void setPTBox(PTBox ptbox);
	public PTBox getPTBox();
	public List<ConditionalConstraint> getConstraintList();
	public void setConstraintList(List<ConditionalConstraint> ccList);
	public void setEntailmentClass(ATermAppl clazz);
	/**
	 * 
	 * @param duals Vector of current dual values (coefficients of slack
	 * variables)
	 * @param ptbox 
	 * @return New prospective column or null if no such column exists
	 */
	public double[] generateColumn(double[] duals, boolean max) throws CPException;
}
