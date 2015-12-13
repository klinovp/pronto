/**
 * 
 */
package uk.ac.manchester.cs.pronto.explanation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;
import aterm.ATermList;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * This contraction strategy is based on Algorithm 2 presented in Baader and Suntisrivaraporn
 * in "Debugging Snomed CT Using Axiom Pinpointing in the Description Logic EL+". 
 */
public class DivideAndConquerStrategyImpl implements ContractionStrategy {

	private int m_cnt = 0; 
	/**
	 * @param conjuncts
	 * @return
	 */
	@Override
	public Set<ATermAppl> prune(Collection<ATermAppl> conjuncts, SATChecker checker) {
		
		m_cnt = 0;
		
		return !checker.isSatisfiable( conjuncts ) 
					? extract(	ATermUtils.EMPTY_LIST, new ArrayList<ATermAppl>(conjuncts), checker)
					: null;
	}

	protected Set<ATermAppl> extract(	ATermList supportList,
										List<ATermAppl> conjuncts,
										SATChecker checker) {
		
		if (conjuncts.size() == 1) return Collections.singleton( conjuncts.get( 0 ) );
		//Split the list of conjuncts in halves and check both
		ListHalves halves = new ListHalves(conjuncts);

		if (!isSatisfiable( supportList, halves.getS1(), checker )) {
			
			return extract( supportList, halves.getS1(), checker );
		}
		
		if (!isSatisfiable( supportList, halves.getS2(), checker )) {
			
			return extract( supportList, halves.getS2(), checker );
		}
		//No luck. Explanations are spread over both halves, so we recursively extract relevant
		//conjuncts from each using the other as support
		Set<ATermAppl> s1Prime = extract( 	appendConjuncts(supportList, halves.getS2()),
											halves.getS1(), checker );
		Set<ATermAppl> s2Prime = extract( 	appendConjuncts(supportList, s1Prime),
											halves.getS2(), checker );		
		//Merge relevant conjuncts found in both halves
		return union(s1Prime, s2Prime);
	}
	
	private boolean isSatisfiable(	ATermList supportList,
									List<ATermAppl> conjuncts,
									SATChecker checker) {
		
		m_cnt++;
		
		return checker.isSatisfiable( ATermUtils.makeAnd(appendConjuncts(supportList, conjuncts)) );
	}
	
	private ATermList appendConjuncts(ATermList conjunctList, Collection<ATermAppl> conjuncts) {
		
		for (ATermAppl conjunct : conjuncts) conjunctList = conjunctList.append( conjunct );
		
		return conjunctList;
	}
	
	private Set<ATermAppl> union(Set<ATermAppl> set1, Set<ATermAppl> set2) {
		
		HashSet<ATermAppl> union = new HashSet<ATermAppl>(set1.size() + set2.size());
		
		union.addAll( set1 );
		union.addAll( set2 );
		
		return union;
	}
	
    private class ListHalves {

        private List<ATermAppl> m_listS1;

        private List<ATermAppl> m_listS2;


        public ListHalves(List<ATermAppl> input) {
        	
            int listASize = input.size() / 2;
            
            m_listS1 = new ArrayList<ATermAppl>(input.subList(0, listASize));
            m_listS2 = new ArrayList<ATermAppl>(input.subList(listASize, input.size()));
        }

        public List<ATermAppl> getS1() {
        	
            return m_listS1;
        }

        public List<ATermAppl> getS2() {
        	
            return m_listS2;
        }
    }	
}
