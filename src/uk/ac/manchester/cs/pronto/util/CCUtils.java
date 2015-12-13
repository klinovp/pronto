package uk.ac.manchester.cs.pronto.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.utils.ATermUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;

import aterm.ATermAppl;

import com.clarkparsia.pellet.owlapiv3.PelletVisitor;
import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.PTBox;

/**
 * <p>Title: CCUtils</p>
 * 
 * <p>Description: 
 *  Provides various utility methods to load, process, transform and write XML
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class CCUtils {
  
	public static ATermAppl uriToATerm(String uri) {

		if( uri.equals( OWLManager.getOWLDataFactory().getOWLThing().getIRI().toString() ) ) {
			
			return ATermUtils.TOP;
		}
		else if( uri.equals( OWLManager.getOWLDataFactory().getOWLNothing().getIRI().toString() ) ) {
			
			return ATermUtils.BOTTOM;
		}
		else {
			return ATermUtils.makeTermAppl( uri );
		}
	}
	
	public static ATermAppl classExprToATerm(	OWLClassExpression classExpr,
												KnowledgeBase kb) {
		
		PelletVisitor visitor = new PelletVisitor(kb); 
		
		classExpr.accept( visitor );
		
		return visitor.result();
	}

	public static String aTermToString(ATermAppl term) {
		
		if (ATermUtils.TOP.equals( term )) {
			
			return OWLManager.getOWLDataFactory().getOWLThing().getIRI().toString();
			
		} else if (ATermUtils.BOTTOM.equals( term )) {
			
			return OWLManager.getOWLDataFactory().getOWLNothing().getIRI().toString();
			
		} else return term.toString();
	}
	
	public static ConditionalConstraint conceptVerificationConstraint(ATermAppl concept) {

		return new ConditionalConstraint( ATermUtils.TOP, concept, 1, 1 );
	}
	
	public static ConditionalConstraint unsatisfiableConstraint(ATermAppl evidence, ATermAppl conclusion) {
		
		return new ConditionalConstraint(evidence, conclusion, 1.0, 0.0);
	}
  
	public static String getLocalName(String qualifiedName) {

		int aIndex = qualifiedName.lastIndexOf( "#" );

		if( aIndex == -1 )
			
			aIndex = qualifiedName.lastIndexOf( "/" );

		return qualifiedName.substring( aIndex + 1 );
	}
	
	public static String prettyPrint(ConditionalConstraint cc) {
	
		String result = "(" + getLocalName( cc.getConclusion().toString() );
		
		result += !ATermUtils.TOP.equals( cc.getEvidence() )
						? " | " + getLocalName( cc.getEvidence().toString() )
						: "";
						
		result += ")[" + cc.getLowerBound() + "," + cc.getUpperBound() + "]";
		
		return result;
	}
	
	/*
	 * In CG constraints are stored together their verification constraints.
	 * For example, (D|C)[l,u] would be kept together with (C|T)[1,1] to
	 * store the information about tolerability.
	 * This method extracts constraints from such pairs
	 */
	public static ConditionalConstraint extractConstraintFromCGPair(Set<ConditionalConstraint> pair) {

		switch ( pair.size() ) {

		case 1:

			return pair.iterator().next();

		default:
			/*
			 * Size should be 2. If it's not, then the caller is unlucky
			 */
			Iterator<ConditionalConstraint> iter = pair.iterator();
			ConditionalConstraint first = iter.next();
			ConditionalConstraint second = iter.next();

			if( ATermUtils.TOP.equals( first.getEvidence() ) ) {

				return second;

			} else {

				return first;
			}
		}
	}
	
	public static Set<ConditionalConstraint> translateConstraints(Collection<ConditionalConstraint> ccSet, PTBox ptbox) {

		Set<ConditionalConstraint> translated = new HashSet<ConditionalConstraint>(ccSet.size());
		
		for (ConditionalConstraint cc : ccSet) {
			
			translated.add( ptbox.translateConstraint( cc ) );
		}
		
		return translated;
	}	
	
}
