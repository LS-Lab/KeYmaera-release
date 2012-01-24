/**
 * Node class for use in a graph.
 *
 * @author jyn (jingyin@andrew.cmu.edu)
 */

package de.uka.ilkd.key.dl.image_compute.graph;

public abstract class Node
{
	protected final String name;

	protected Node(String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Node))
			return false;
		Node n = (Node) o;
		return n.name.equals(name);
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}

	@Override
	public String toString()
	{
		return name;
	}

}
