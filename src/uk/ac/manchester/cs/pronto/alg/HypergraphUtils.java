/**
 * 
 */
package uk.ac.manchester.cs.pronto.alg;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import uk.ac.manchester.cs.graph.Hyperedge;
import uk.ac.manchester.cs.graph.Hypergraph;
import uk.ac.manchester.cs.graph.HypergraphImpl;
import uk.ac.manchester.cs.graph.Vertex;


/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Feb 6, 2009
 */
public class HypergraphUtils {

	public static <V, E> Vertex<V> getMinDegreeVertex(Hypergraph<V, E> graph) {
		
		Set<? extends Vertex<V>> vertices = graph.getVertices();
		
		if (!vertices.isEmpty()) {
			
			return orderByDegree( graph, vertices, true ).iterator().next();
			
		} else {
			
			return null; 
		}
	}	
	
	public static <V,E> Collection<Vertex<V>> orderByDegree(Hypergraph<V, E> graph
								, Collection<? extends Vertex<V>> vertices, boolean ascend) {
		
		Map<Integer, Vertex<V>> vertexMap = new TreeMap<Integer, Vertex<V>>();
		
		for (Vertex<V> vertex : graph.getVertices()) {
			
			int degree = graph.getVertexDegree( vertex );
			
			vertexMap.put( ascend ? degree : -degree, vertex );
		}
		
		return vertexMap.values();
	}
	
	public static <V, E> Hypergraph<V, E> clone(Hypergraph<V, E> graph) {
		
		Hypergraph<V, E> clone = new HypergraphImpl<V, E>();
		
		for (Hyperedge<V, E> hyperedge : graph.getHyperedges()) {
			
			clone.addHyperedge( hyperedge.getObject(), hyperedge.getVertices() );
		}
		//Take care of vertices that aren't contained in any of hyperedges
		for (Vertex<V> dangling : graph.getVertices()) {
			
			if (graph.getIncidentHyperedges( dangling ).isEmpty()) {
				
				clone.addVertex( dangling.getObject() );
			}
		}
		
		return clone;
	}
	
	public static <V, E> Hypergraph<V, E> subHypergraph(Hypergraph<V, E> graph
											, Collection<Vertex<V>> vertices) {
		
		Hypergraph<V, E> subgraph = new HypergraphImpl<V, E>();
		Set<V> addedObjects = new HashSet<V>();
		//Add hyperedges which scope is within the given collection of vertices
		for (Hyperedge<V, E> edge : graph.getHyperedges()) {
			
			if (vertices.containsAll( edge.getVertices() )) {
				
				Set<Vertex<V>> added = new HashSet<Vertex<V>>();
				
				for (Vertex<V> vertex : edge.getVertices()) {
					
					added.add( subgraph.addVertex( vertex.getObject() ) );
					addedObjects.add( vertex.getObject() );
				}
				
				subgraph.addHyperedge( edge.getObject(), added );
			}
		}
		
		//Take care of the remaining vertices
		for (Vertex<V> remaining : vertices) {
			
			if (!addedObjects.contains( remaining.getObject() )) {
				
				subgraph.addVertex( remaining.getObject() );
			}
		}
		
		return subgraph;
	}
	
	public static <V, E> Hypergraph<V, E> createHypergraphFromEdges( Collection<Hyperedge<V, E>> edges) {

		Hypergraph<V, E> subgraph = new HypergraphImpl<V, E>();

		for( Hyperedge<V, E> edge : edges ) {

			subgraph.addHyperedge( edge.getObject(), edge.getVertices() );
		}

		return subgraph;
	}	
	
	/*
	 * Decomposes the graph on a set of disjoint components
	 */
	public static <V, E> Collection<Hypergraph<V, E>> decompose(Hypergraph<V,E> graph) {
		
		//Start from some vertex and pull the set of connected vertices
		//Do that until the graph has no vertices
		Set<Hypergraph<V, E>> components = new HashSet<Hypergraph<V, E>>();
		Set<Vertex<V>> previous = new HashSet<Vertex<V>>();
		Set<Vertex<V>> frontier = new HashSet<Vertex<V>>();
		
		while (!graph.getVertices().isEmpty()) {
			
			Vertex<V> vertex = graph.getVertices().iterator().next();
			//Run BFS to pull the connected stuff
			frontier.add( vertex );
			bfs(graph, frontier, previous);
			//Extract the connected component
			components.add( subHypergraph(graph, previous) );
			//Remove it from the original graph
			for (Vertex<V> visited : previous) graph.removeVertex( visited );
			
			previous.clear();
			frontier.clear();
		}
		
		return components;
	}
	
	public static <V, E> Hypergraph<V,E> compose(Collection<Hypergraph<V, E>> components) {	
		
		Hypergraph<V, E> graph = new HypergraphImpl<V, E>();
		
		for (Hypergraph<V, E> component : components) {
			
			for (Hyperedge<V,E> edge : component.getHyperedges()) {
				
				graph.addHyperedge( edge.getObject(), edge.getVertices() );
			}
		}
		
		return graph;
	}
	
	protected static <V,E> void bfs(Hypergraph<V,E> graph
						, Set<Vertex<V>> frontier, Set<Vertex<V>> previous) {
		
		while (!frontier.isEmpty()) {
			
			Vertex<V> vertex = frontier.iterator().next();
			Set<? extends Vertex<V>> neighbors = graph.getAdjacentVertices(vertex);
			
			previous.add( vertex );
			frontier.remove( vertex );
			neighbors.removeAll( previous );
			frontier.addAll( neighbors );
		}
	}

}




