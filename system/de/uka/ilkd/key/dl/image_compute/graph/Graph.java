/**
 * A directed graph object.
 *
 * Taken from 15-211 graph assignment.
 */

package de.uka.ilkd.key.dl.image_compute.graph;

import java.util.*;

public class Graph<V, E extends Edge<V>> implements GraphInterface<V, E>
{

	Map<V, List<E>> edgList;

	public Graph()
	{
		edgList = new HashMap<V, List<E>>();
	}

	public boolean addVertex(V vertex)
	{
		if (edgList.containsKey(vertex))
			return false;
		edgList.put(vertex, new ArrayList<E>());
		return true;
	}

	public boolean addVertices(Collection<? extends V> vertices)
	{
		boolean result = false;
		for (V v : vertices)
			result |= addVertex(v);
		return result;
	}

	public boolean addEdge(E e)
	{
		if (!edgList.containsKey(e.src()) || !edgList.containsKey(e.dest()))
			throw new IllegalArgumentException("bad edge");
		List<E> l = edgList.get(e.src());
		boolean result = true;
		for (E edge : l)
			if (edge.dest().equals(e.dest())) {
				result = false;
				break;
			}
		l.add(e);
		return result;
	}

	public boolean addEdges(Collection<? extends E> edges)
	{
		boolean result = false;
		for (E e : edges)
			result |= addEdge(e);
		return result;
	}

	public boolean removeEdge(V src, V dest)
	{
		if (!edgList.containsKey(src) || !edgList.containsKey(dest))
			throw new IllegalArgumentException("bad edge");
		List<E> l = edgList.get(src);
		boolean result = false;
		E remove = null;
		for (E edge : l)
			if (edge.dest().equals(dest)) {
				result = true;
				remove = edge;
				break;
			}
		// inefficient
		if (remove != null)
			l.remove(remove);
		return result;
	}

	public void clearEdges()
	{
		for (Map.Entry<V, List<E>> entry : edgList.entrySet())
			entry.getValue().clear();
	}

	public Set<V> vertices()
	{
		return edgList.keySet();
	}

	public E connected(V i, V j)
	{
		if (!edgList.containsKey(i) || !edgList.containsKey(j))
			throw new IllegalArgumentException("bad vertices");
		List<E> l = edgList.get(i);
		for (E edge : l)
			if (edge.dest().equals(j))
				return edge;
		return null;
	}

	public Set<V> neighbors(V vertex)
	{
		if (!edgList.containsKey(vertex))
			throw new IllegalArgumentException("bad vertex");
		Set<V> ret = new HashSet<V>();
		List<E> l = edgList.get(vertex);
		for (E edge : l)
			ret.add(edge.dest());
		return ret;
	}

	public Collection<E> outgoingEdges(V vertex)
	{
		if (!edgList.containsKey(vertex))
			throw new IllegalArgumentException("bad vertex");
		return edgList.get(vertex);
	}

}
