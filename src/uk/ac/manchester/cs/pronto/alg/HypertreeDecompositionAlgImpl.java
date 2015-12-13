/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.graph.Hyperedge;
import uk.ac.manchester.cs.graph.Hypergraph;
import uk.ac.manchester.cs.graph.Node;
import uk.ac.manchester.cs.graph.Tree;
import uk.ac.manchester.cs.graph.Vertex;

import uk.ac.manchester.cs.pronto.util.SetUtils;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Feb 8, 2009
 */
public class HypertreeDecompositionAlgImpl implements HypertreeDecompositionAlg {

	protected Logger	s_logger	= Logger.getLogger( HypertreeDecompositionAlgImpl.class );
	/* 
	 * Computes decomposition in a recursive top-down manner
	 */
	public <V, E> Tree<Hypergraph<V, E>> compute(Hypergraph<V, E> graph) {
		
		Tree<Hypergraph<V, E>> tree = new Tree<Hypergraph<V, E>>(
												HypergraphUtils.clone( graph ));
		//Main recursive function
		tree = decompose(tree);
		
		return tree;
	}

	
	protected <V, E> Tree<Hypergraph<V, E>> decompose(Tree<Hypergraph<V, E>> tree) {
		//IMPORTANT! For the time being we do only one recursive step
		Hypergraph<V, E> graph = tree.getRoot().getObject();
		Set<Hyperedge<V, E>> separator = separator(graph);
		
		return (null == separator) ? tree : separate(graph, separator);
	}
	

	protected <V, E> Set<Hyperedge<V, E>> separator(Hypergraph<V, E> graph) {
		
		Set<Set<Hyperedge<V, E>>> candidates = new HashSet<Set<Hyperedge<V, E>>>(); 
		//Start with computing a level structure of minimal width
		List<Set<Vertex<V>>> levelStructure = LevelStructureAlgImpl.compute( graph );
		int median = levelStructure.size() / 2;
		
		if (levelStructure.size() % 2 == 0) {
			//Take edges that connect two center-most levels 
			candidates.add(levelConnectors(graph, levelStructure.get( median - 1 )
												, levelStructure.get( median )));
			
		} else if (median >= 1) {
			
			candidates.add(levelConnectors(graph, levelStructure.get( median - 1 )
					, levelStructure.get( median )));
			candidates.add(levelConnectors(graph, levelStructure.get( median )
					, levelStructure.get( median + 1 )));
			
		}
		
		return bestSeparator( graph, candidates );
	}	
	
	private <V, E> Set<Hyperedge<V, E>> bestSeparator(Hypergraph<V, E> graph
										, Set<Set<Hyperedge<V, E>>> candidates) {
		
		Set<Hyperedge<V, E>> best = null;
		int maxSize = graph.getVertices().size();
		//We evaluate separators basing on how equal are the separated parts
		for (Set<Hyperedge<V, E>> separator : candidates) {
			//Remove hyperedges with corresponding vertices, decompose and 
			//evaluate the result
			int currentMaxSize = evaluateSeparator(HypergraphUtils.clone( graph ), separator);
			
			if (currentMaxSize < maxSize) {
				
				best = separator;
				maxSize = currentMaxSize;
			}
		}
		
		return best;
	}
	
	/*
	 * Evaluates a separator by computing the maximum size of disjoint components 
	 */
	private <V, E> int evaluateSeparator(Hypergraph<V, E> graph, Set<Hyperedge<V, E>> separator) {
		
		int maxSize = 0;
		Set<Vertex<V>> vertices = new HashSet<Vertex<V>>();
		//First we remove the separator
		for (Hyperedge<V, E> edge : separator) {
			
			graph.removeHyperedge( edge.getVertices() );
			vertices.addAll( edge.getVertices() );
		}
		
		maxSize = vertices.size();
		//Now the graph must be disconnected. Decomposing is a heavy operation
		//can we optimize it?
		{
			Collection<Hypergraph<V, E>> components = HypergraphUtils.decompose( graph );
			
			for (Hypergraph<V, E> component : components) {
				
				maxSize = Math.max( maxSize, component.getVertices().size() );
			}
		}
		
		return maxSize;
	}
	
	/*
	 * Computes set of edges connecting to levels of vertices 
	 */
	private <V, E> Set<Hyperedge<V, E>> levelConnectors(Hypergraph<V, E> graph
								, Set<Vertex<V>> level1, Set<Vertex<V>> level2) {
		
		Set<Hyperedge<V, E>> connectors = new HashSet<Hyperedge<V, E>>();
		
		for (Vertex<V> vertexL1 : level1) {
			
			for (Hyperedge<V, E> edge : graph.getIncidentHyperedges( vertexL1 )) {
				
				Set<Vertex<V>> scope = new HashSet<Vertex<V>>(edge.getVertices());
				
				scope.retainAll( level2 );
				
				if (!scope.isEmpty()) {
					
					connectors.add( edge );
				}
			}
		}		
		
		return connectors;
	}
	
	protected <V, E> Tree<Hypergraph<V, E>> separate(Hypergraph<V, E> graph,
											Set<Hyperedge<V, E>> separator) {

		Collection<Hypergraph<V, E>> components = null;
		Hypergraph<V, E> root = HypergraphUtils.createHypergraphFromEdges( separator );
		Tree<Hypergraph<V, E>> subtree = new Tree<Hypergraph<V, E>>( root );

		for( Hyperedge<V, E> edge : separator )	graph.removeHyperedge( edge.getVertices() );
		//Now the graph must be disconnected
		//TODO Try to avoid this extra decomposition (we did it when evaluating the separators)
		components = HypergraphUtils.decompose( graph );
		// Handle the vertices that are shared between the root component and the rest
		for( Hypergraph<V, E> component : components ) {
			// Attach components to the root
			if (!root.getVertices().containsAll( component.getVertices() )) {
			
				subtree.getRoot().addChild( component );
			}
		}

		return subtree;
	}	
	/*
	 * Solely for debugging purposes
	 */
	protected <V, E> void evaluate(Tree<Hypergraph<V, E>> tree) {
		
		evaluate(tree.getRoot(), 0);
	}

	protected <V, E> void evaluate(Node<Hypergraph<V, E>> root, int level) {
		
		StringBuffer buf = new StringBuffer(level);
		Set<? extends Vertex<V>> separator = null;
		int sepSize = 0;
		
		if (root.getParent() != null) {
			
			separator = SetUtils.intersection(root.getObject().getVertices()
											, root.getParent().getObject().getVertices());
			sepSize = separator.size();
		}
		
		for (int i = 0; i < level; i++) buf.append( "\t" );
		
		s_logger.debug( buf.toString() + root.getObject().getVertices().size() + " nodes " );
		s_logger.debug( buf.toString() + sepSize + " nodes in the separator" );
	
		for (Node<Hypergraph<V, E>> child : root.getChildren()) evaluate( child, level + 1 );
	}
}
