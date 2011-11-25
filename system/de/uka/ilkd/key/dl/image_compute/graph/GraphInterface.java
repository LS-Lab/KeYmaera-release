/**
 * Interface for directed graph object.
 *
 * Usage notice: this is not a multi-graph. Self-referential vertices are forbidden.
 *
 * Taken from 15-211 graph assignment.
 */

package de.uka.ilkd.key.dl.image_compute.graph;

import java.util.*;

public interface GraphInterface<V, E extends Edge<V>>
{
	/**
	 * Add a vertex to the graph.
	 *
	 * @param vertex The vertex to add
	 * @return true if vertex was not present already.
	 */
	public boolean addVertex(V vertex);

	/**
	 * Adds multiple vertices to a graph.
	 *
	 * @return true if and only if the set of vertices was changed by the operation.
	 */
	public boolean addVertices(Collection<? extends V> vertices);

	/**
	 * Adds edge e to the graph.
	 *
	 * @param e The edge to add.
	 * @throws IllegalArgumentException
	 * 	If e represents a self-transition or is otherwise not a valid edge (eg. refers to vertices not in the graph).
	 * @return true
	 * 	If e was not already present; false if it was (in which case the graph is not updated).
	 */
	public boolean addEdge(E e);

	/**
	 * Adds multiple edges to a graph.
	 *
	 * @throws IllegalArgumentException
	 * 	If any edge in the collection is invalid.
	 * @return true
	 * 	If and only if the set of edges was changed by the operation.
	 */
	public boolean addEdges(Collection<? extends E> edges);

	/**
	 * Remove an edge from src to dest from the graph.
	 *
	 * @throws IllegalArgumentException
	 * 	If src or dest is not in the graph.
	 * @return true
	 * 	If an edge from src to dest edge was present.
	 */
	public boolean removeEdge(V src, V dest);

	/**
	 * Returns a view of the set of vertices in the graph.
	 *
	 * The resulting set is immutable, and is updated when the graph itself is.
	 * @return The set of all vertices in the graph.
	 */
	public Set<V> vertices();

	/** Removes all edges from the graph */
	public void clearEdges();

	/**
	 * Tests if vertices i and j are connected, returning the edge between them if so.
	 *
	 * @throws IllegalArgumentException
	 * 	If i or j are not vertices in the graph.
	 * @return The edge from i to j if it exists in the graph; null otherwise.
	 */
	public E connected(V i, V j);

	/**
	 * Return the vertices to which vertex has an outgoing edge.
	 *
	 * This set is immutable, and will change when the vertex gets new neighbors (it is is backed by the graph)
	 *
	 * @param vertex
	 * 	The vertex the neighbours of which to return.
	 * @throws IllegalArgumentException
	 * 	If vertex is not in the graph.
	 * @return The set of neighbours of vertex.
	 */
	public Set<V> neighbors(V vertex);

	/**
	 * Returns the edges leaving vertex.
	 *
	 * The collection should be immutable.
	 * When edges are added to this vertex, the returned collection will change
	 *
	 * @param vertex
	 * 	The vertex the outgoing edges of which to return.
	 * @throws IllegalArgumentException
	 * 	If vertex is not in the graph.
	 * @return The set of edges leaving vertex.
	 */
	public Collection<E> outgoingEdges(V vertex);
}
