/**
 * An undirected edge in a graph, as specified by the nodes which lie at either end.
 *
 * Taken from graph assignment of 15-211.
 */

package de.uka.ilkd.key.dl.image_compute.graph;

public class Edge<N>
{

	private final N src;
	private final N dest;
	private final int weight;
	
	public N src()
	{
		return src;
	}
	
	public N dest()
	{
		return dest;
	}
	
	public int weight()
	{
		return weight;
	}
	
	public Edge(N src, N dest, int weight)
	{
		this.src = src;
		this.dest = dest;
		this.weight = weight;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Edge))
			return false;

		Edge e = (Edge) obj;
		return e.weight == weight
			&& (src == null && e.src == null || src != null && e.src.equals(src))
			&& (dest == null && e.dest == null || dest != null && e.dest.equals(dest));
	}

	@Override
	public int hashCode()
	{
		return src.hashCode() ^ (dest.hashCode()<<1) ^ weight;
	}
	
	@Override
	public String toString()
	{
		return "(" + src + "," + dest + ")";
	}

}
