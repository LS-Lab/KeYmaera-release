/**
 * Action used in a GSP to find counterexamples.
 *
 * @author jyn (jingyin@andrew.cmu.edu)
 */

package de.uka.ilkd.key.dl.image_compute;

import java.util.Iterator;

abstract class Action
{
	abstract void apply(NumericalState state);
}
