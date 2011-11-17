/**
 * Edge object that embeds an Action object.
 *
 * @author jyn (jingyin@andrew.cmu.edu)
 */

package de.uka.ilkd.key.dl.image_compute;

import de.uka.ilkd.key.dl.image_compute.graph.Edge;

class ActionEdge<V> extends Edge<V>
{

	private final Action action;

	ActionEdge(V src, V dest, Action action)
	{
		super(src, dest, 1);
		this.action = action;
	}

	Action getAction()
	{
		return action;
	}
}
