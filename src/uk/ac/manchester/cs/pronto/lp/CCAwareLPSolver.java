/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

import java.util.List;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.exceptions.CPException;

/**
 * LP solver that maintains a mapping between rows and conditional constraints
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 */
public interface CCAwareLPSolver extends LPSolver {

	public void addRows(ConditionalConstraint cc, double[] lCoeffs, double[] uCoeffs) throws CPException;
	public void relaxRows(ConditionalConstraint cc);
	public void enforceRows(ConditionalConstraint cc);
	public double[] getConstraintLowerRow(ConditionalConstraint cc);
	public double[] getConstraintUpperRow(ConditionalConstraint cc);
	public ConditionalConstraint getConstraint(int rowIndex);
	public List<ConditionalConstraint> getConstraintList();
	public void setLowerBoundingRow();
	public void setUpperBoundingRow();
	public void setBoundingRows();
	public void unsetBoundingRows();
	//This method adds auxiliary variables to ensure that the model is feasible
	//public void addExtraVarsForFeasibility();
	//public void removeExtraVars();
	public void removeColumns(String namePrefix);
}
