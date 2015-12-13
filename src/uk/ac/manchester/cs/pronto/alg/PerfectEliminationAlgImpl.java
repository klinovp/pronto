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

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.graph.Graph;
import uk.ac.manchester.cs.graph.Vertex;


/**
 * Attempts to compute a perfect elimination of a graph (succeeds iff the graph 
 * is chordal)
 * 
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Feb 4, 2009
 */
public class PerfectEliminationAlgImpl {
	
	protected Logger	s_logger	= Logger.getLogger( PerfectEliminationAlgImpl.class );

	public <V,E> SortedMap<Integer, Vertex<V>> compute(Graph<V, E> graph, boolean enforce) {
		
		SortedMap<Integer, Vertex<V>> peOrdering = new TreeMap<Integer, Vertex<V>>();
		//Start with a single partition
		List<Set<Vertex<V>>> partition = new ArrayList<Set<Vertex<V>>>();
		Set<Vertex<V>> visited = new HashSet<Vertex<V>>();
		
		partition.add( new HashSet<Vertex<V>>(graph.getVertices()) );
		
		{
			int setIndex = 0;
			
			while (setIndex > -1) {
				
				setIndex = split(graph, partition, visited, peOrdering, setIndex);
			}
		}
		
		s_logger.info( peOrdering.values() );
		
		boolean isPE = verify(peOrdering, graph, enforce);
		
		return  (enforce || isPE)? peOrdering : null;
	}
	
	private <V,E> int split(Graph<V, E> graph, List<Set<Vertex<V>>> partition
					, Set<Vertex<V>> visited, SortedMap<Integer, Vertex<V>> ordering, int start) {
		
		int nextStart = -1;
		//Start at setIndex
		Set<Vertex<V>> vertexSet = partition.get( start );
		//Pick some vertex
		Vertex<V> vertex = vertexSet.iterator().next();
		
		visited.add( vertex );
		vertexSet.remove( vertex );
		
		//Place it in the ordering
		if (ordering.isEmpty()) {
			
			ordering.put( vertexSet.size(), vertex );
			
		} else {
			
			ordering.put( ordering.firstKey() - 1, vertex );
		}
		//Now split each set preceding setIndex
		int i = 0;
		
		while (i <= start) {
			
			Set<Vertex<V>> nextSet = partition.get( i );
			
			if( nextSet.size() > 1 ) {

				// Find members of nextSet which are neighbors of vertex
				Set<Vertex<V>> neighbors = getNeighbors( graph, vertex, nextSet );

				if( !neighbors.isEmpty() && (neighbors.size() < nextSet.size()) ) {

					nextSet.removeAll( neighbors );
					partition.add( i + 1, neighbors );

					start++;
					i += 2;
					
				} else {
					
					i++;
				}
				
			} else {
				
				i++;
			}

			if (!nextSet.isEmpty()) {
				
				nextStart = i - 1;
			}
		}
		
		return nextStart;
	}
	
	private <V,E> Set<Vertex<V>> getNeighbors(Graph<V, E> graph, Vertex<V> vertex
														, Set<Vertex<V>> set) {
		
		Set<Vertex<V>> neighbors = graph.getAdjacentVertices( vertex );
		
		neighbors.retainAll( set );
		
		return neighbors;
	}
	
	/*
	 * Check whether the ordering is a perfect elimination
	 */
	private <V,E> boolean verify(SortedMap<Integer, Vertex<V>> ordering, Graph<V, E> graph, boolean enforce) {

		boolean result = true;
		//We don't worry much about speed at this point
		
		for (int order : ordering.keySet()) {
			//Get higher ordered neighbors
			Vertex<V> vertex = ordering.get( order );
			Collection<Vertex<V>> tailSet = ordering.tailMap( order ).values();
			List<Vertex<V>> higherNeighbors = new ArrayList<Vertex<V>>(graph.getAdjacentVertices( vertex ));
			
			higherNeighbors.add( vertex );
			higherNeighbors.retainAll( tailSet );
			//Check that they form a clique (there exists an edge between 
			//every two vertices)
			if (!higherNeighbors.isEmpty() && higherNeighbors.size() > 1) {
				
				for (int i = 0; i < higherNeighbors.size() && result; i++) {
					
					for (int j = i + 1; j < higherNeighbors.size() && result; j++) {
						
						Vertex<V> iVertex = higherNeighbors.get( i );
						Vertex<V> jVertex = higherNeighbors.get( j );
						
						result &= graph.findEdge( iVertex, jVertex ) != null;
						
						if (!result) {
							
							s_logger.info( vertex + ":" );
							s_logger.info( "No edge between " + iVertex + " and " + jVertex );
							
							if (enforce) {
								
								graph.addEdge( null, iVertex, jVertex );
							}
						}
					}
				}
			}
			
			if (!result) {
				
				break;
			}
		}
		
		return result;
	}
}
 