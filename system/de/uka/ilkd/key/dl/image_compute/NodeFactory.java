/**
 * Produces a Node object.
 *
 * @author jyn (jingyin@andrew.cmu.edu)
 */

package de.uka.ilkd.key.dl.image_compute;

import de.uka.ilkd.key.dl.image_compute.graph.Node;

class NodeFactory
{

	private static final NodeFactory nf = new NodeFactory();

	private int nTransitionNode;

	private NodeFactory()
	{
		nTransitionNode = -1;
	}

	static NodeFactory getInstance()
	{
		return nf;
	}

	synchronized Node createTransitionNode()
	{
		nTransitionNode++;
		return new TransitionNode("tn" + nTransitionNode);
	}

	private class TransitionNode extends Node
	{
		TransitionNode(String name)
		{
			super(name);
		}
	}

}
