/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import uk.ac.manchester.cs.graph.Graph;
import uk.ac.manchester.cs.graph.GraphImpl;
import uk.ac.manchester.cs.graph.Vertex;

import uk.ac.manchester.cs.pronto.util.SetUtils;

/**
 * FIXME BUGGY!
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Feb 5, 2009
 */
public class TreeDecompositionAlgImpl implements TreeDecompositionAlg {

	public <V,E> Graph<Set<V>,String> compute(Graph<V,E> graph) {
		//First compute the perfect elimination ordering (might add extra edges)
		SortedMap<Integer, Vertex<V>> ordering =
						new PerfectEliminationAlgImpl().compute( graph, true );
		Graph<Set<V>,String> tree = new GraphImpl<Set<V>,String>();
		
		compute(graph, ordering, tree);
		
		return tree;
	}
	
	private <V, E> void compute(Graph<V,E> graph
			, SortedMap<Integer, Vertex<V>> ordering, Graph<Set<V>,String> tree) {
		
		//Compute maximal cliques and order them
		List<Vertex<Set<V>>> nodes = createTreeNodes(graph, ordering, tree);
		
		for (int i = nodes.size() - 1; i >= 0; i--) {
			
			Vertex<Set<V>> node = nodes.get( i );
			Vertex<Set<V>> parent = null;
			Set<? extends V> shared = Collections.emptySet();
			
			for(int j = i - 1; j >= 0; j--) {
				
				Vertex<Set<V>> preceding = nodes.get( j );
				Set<? extends V> intersection = SetUtils.intersection( node.getObject(), preceding.getObject() );
				
				if (intersection.size() > shared.size()) {
					
					shared = intersection;
					parent = preceding;
				}
			}
			
			if (null != parent) {
				
				tree.addEdge( String.valueOf( shared.size() ), parent, node );
			}
		}
	}
	
	
	private <V, E> List<Vertex<Set<V>>> createTreeNodes(Graph<V,E> graph
				, SortedMap<Integer, Vertex<V>> ordering, Graph<Set<V>,String> tree) {
		
		List<Vertex<Set<V>>> nodes = new ArrayList<Vertex<Set<V>>>();
		Set<Set<V>> cliques = new HashSet<Set<V>>();
		
		for (int order : ordering.keySet()) {
			
			Vertex<V> vertex = ordering.get( order );
			//Get the clique which will become a new tree node
			Set<V> clique = getClique(graph, vertex, ordering.tailMap( order ).values());
			//Check whether the clique is maximal
			if (isMaximal(clique, cliques)) {
				
				nodes.add(tree.addVertex( clique ));
				cliques.add(clique);
			} 
		}		
		
		return nodes;
	}
	
	private <V,E> Set<V> getClique(Graph<V,E> graph, Vertex<V> vertex,
												Collection<Vertex<V>> parents) {
		
		Set<Vertex<V>> neighbors = graph.getAdjacentVertices( vertex );
		Set<V> clique = new HashSet<V>();
		
		neighbors.retainAll( parents );
		neighbors.add( vertex );
		
		for (Vertex<V> v : neighbors) clique.add( v.getObject() );
		
		return clique;
	}
	
	private <V,E> boolean isMaximal(Set<V> clique, Set<Set<V>> cliques) {
		
		boolean result = true;
		
		for (Set<V> cl : cliques) {
			
			if (cl.containsAll( clique )) {
				
				result = false;
				break;
			}
		}
		
		return result;
	}
}















