/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.Set;

import uk.ac.manchester.cs.graph.Graph;


/**
 * Computes tree decomposition of a given graph
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Feb 5, 2009
 */
public interface TreeDecompositionAlg {

	public <V,E> Graph<Set<V>,String> compute(Graph<V,E> graph);
}
