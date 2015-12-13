/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import uk.ac.manchester.cs.graph.Graph;
import uk.ac.manchester.cs.graph.Hypergraph;
import uk.ac.manchester.cs.graph.Vertex;


/**
 * Computes pseudo-diameters of graphs and hypergraps and generates level
 * structures
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Feb 6, 2009
 */
public class LevelStructureAlgImpl {

	public static <V, E>  List<Set<Vertex<V>>> compute(Graph<V, E> graph) {
		
		return null;
	}
	
	public static <V, E>  List<Set<Vertex<V>>> compute(Hypergraph<V, E> graph) {
		
		SortedMap<Integer, List<Set<Vertex<V>>>> structures =
									new TreeMap<Integer, List<Set<Vertex<V>>>>();
		Vertex<V> v = HypergraphUtils.getMinDegreeVertex( graph );
		int vDepth = 0;
		int uDepth = 0;
		boolean found = false;
		
		List<Set<Vertex<V>>> vLevelStructure = computeLevelStructure(graph, v);
		
		do {
			
			vDepth = vLevelStructure.size();
			Collection<Vertex<V>> vertices = HypergraphUtils.orderByDegree(
								graph, vLevelStructure.get( vDepth - 1 ), true );
			
			structures.clear();
			found = true;
			//Now examine the most distant from v vertices (in the order
			//of increasing degree)
			for (Vertex<V> vertex : vertices) {
				
				List<Set<Vertex<V>>> structure = computeLevelStructure(graph, vertex);
				uDepth = structure.size();
				
				if (uDepth > vDepth) {
					//We found a "deeper" level structure. Make its root a 
					//starting vertex and repeat
					v = vertex;
					vLevelStructure.clear();
					vLevelStructure.addAll(structure);
					found = false;
					
					break;
					
				} else {
					
					structures.put(computeWidth(structure), structure );
				}
			}
		} while(!found);
		
		structures.put( computeWidth(vLevelStructure), vLevelStructure );
		//Return the structure of the minimal width
		return structures.values().iterator().next();
	}	
	

	private static <V, E> int computeWidth(List<Set<Vertex<V>>> ls ) {
		
		int width = 0;
		
		for (Set<Vertex<V>> level : ls) width = Math.max( width, level.size() );
		
		return width;
	}
	
	
	public static <V, E> List<Set<Vertex<V>>> computeLevelStructure(
									Hypergraph<V, E> graph, Vertex<V> vertex) {
	
		List<Set<Vertex<V>>> ls = new ArrayList<Set<Vertex<V>>>();
		Set<Vertex<V>> currentLevel = new HashSet<Vertex<V>>();
		Set<Vertex<V>> remainingVertices = new HashSet<Vertex<V>>(graph.getVertices());
				
		currentLevel.add( vertex );//starting level
		ls.add( currentLevel );
		remainingVertices.remove( vertex );
		//Now proceed in the width-first manner
		while (!remainingVertices.isEmpty() && !ls.get( ls.size() - 1 ).isEmpty()) {
			
			addLevelToStructure(graph, ls, remainingVertices);
		}		
		
		return ls;
	}
	
	private static <V, E> void addLevelToStructure(Hypergraph<V, E> graph
						, List<Set<Vertex<V>>> ls, Set<Vertex<V>> remaining) {
		
		Set<Vertex<V>> lastLevel = ls.get( ls.size() - 1 );
		Set<Vertex<V>> nextLevel = new HashSet<Vertex<V>>();
		//Get all adjacent vertices
		for( Vertex<V> vertex : lastLevel ) {
			
			Set<? extends Vertex<V>> neighbors = graph.getAdjacentVertices( vertex );
			
			neighbors.retainAll( remaining );
			nextLevel.addAll( neighbors );
			remaining.removeAll( neighbors );
			
			if (remaining.isEmpty()) break;
		}
		//Add the level
		ls.add( nextLevel );		
	}
}
