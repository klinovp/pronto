/**
 * 
 */
package uk.ac.manchester.cs.pronto;

import java.util.Map;
import java.util.Set;

import aterm.ATermAppl;

/**
 * <p>Title: PABox</p>
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
public interface PABox {

	public Map<ATermAppl, Set<ConditionalConstraint>> getConstraintsMap();

	public Set<ATermAppl> getProbabilisticIndividuals();

	/*
	 * Must return null if no such individual exists and empty set of it exists
	 * but doesn't have any constraints
	 */
	public Set<ConditionalConstraint> getConstraintsForIndividual(ATermAppl individual);

	public void setConstraintsForIndividual(ATermAppl individual, Set<ConditionalConstraint> ccSet);

	public void addConstraintForIndividual(ATermAppl individual, ConditionalConstraint cc);

	public void addConstraintsForIndividual(ATermAppl individual, Set<ConditionalConstraint> ccSet);
	
	public boolean probabilisticIndividualExists(ATermAppl individual);
	
	public void removeProbabilisticIndividual(ATermAppl individual);
}
