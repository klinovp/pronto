/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import uk.ac.manchester.cs.graph.Hypergraph;
import uk.ac.manchester.cs.graph.Tree;


/**
 * Decomposes a hypergraph on a set of hypertrees
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Feb 8, 2009
 */
public interface HypertreeDecompositionAlg {

	public <V, E> Tree<Hypergraph<V, E>> compute(Hypergraph<V, E> graph);
}
