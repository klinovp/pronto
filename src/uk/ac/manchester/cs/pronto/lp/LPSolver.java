/**
 * 
 */
package uk.ac.manchester.cs.pronto.lp;

import uk.ac.manchester.cs.pronto.exceptions.CPException;

/**
 * <p>Title: LPSolver</p>
 * 
 * <p>Description: 
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public interface LPSolver {

	public static double PRECISION_THRESHOLD = 1.0E-9;	
	public enum STATUS {UNDEFINED, OPTIMAL, FEASIBLE, INFEASIBLE, OTHER};
	public enum ROW_TYPE {EQUAL, LESS_EQUAL, GREATER_EQUAL}
	
	public void initLPInstance(int colNum);
	public int addRow(double[] coeffs, double rhs, ROW_TYPE type, String name) throws CPException;
	public void setRowUpperBound(int index, double bound) throws CPException;
	public int addColumn(double[] column, String name) throws CPException;
	public void setVariableUpperBound(int varIndex, double bound);
	public void setVariableLowerBound(int varIndex, double bound);
	public void setRowBounds(int rowIndex, double lBound, double uBound);
	public String getRowName(int index);
	public String getColumnName(int index);
	public void setObjective(double[] objective) throws CPException;
	public double getObjectiveCoeff(int index);
	public void setObjectiveCoeff(int index, double coeff) throws CPException;
	public void setMaximize(boolean max) throws CPException;
	public boolean getMaximize();
	public STATUS solveLP();
	public double[] getAssignment();
	public double[] getDuals();
	public void removeRow(int index) throws CPException;
	public void removeRow(String name) throws CPException;
	public void removeRows(int[] indexes) throws CPException;
	public void removeColumn(int index) throws CPException;
	public void removeColumns(int[] indexes) throws CPException;
	public void removeColumn(String name) throws CPException;
	public double getColumnReducedCost(int index);
	public int getColumnNumber();
	public int getRowNumber();
	public int getFirstColumnIndex();
	public int getFirstRowIndex();
	//Needed to check if this instance is complete or needs more columns
	public boolean isComplete();
	public void setComplete(boolean complete);
	public boolean isColumnBasic(int colIndex);
	public void saveBasis();
	public void restoreBasis();
	public STATUS getStatus();
	public double getObjValue();
	
	public void dispose();
}

