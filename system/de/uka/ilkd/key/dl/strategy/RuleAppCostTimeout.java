/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.strategy;

import de.uka.ilkd.key.dl.strategy.features.HypotheticalProvabilityFeature;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.util.Debug;

/**
 * Represents pseudo-costs of a timed out rule cost application computation.
 * 
 * @author ap
 * 
 */
public class RuleAppCostTimeout implements RuleAppCost {

	private final long timeout;

	public static RuleAppCost create(long timeout) {
		return new RuleAppCostTimeout(timeout);
	}

	private RuleAppCostTimeout(long timeout) {
		this.timeout = timeout;
	}

	public int compareTo(RuleAppCost o) {
		if (o instanceof TopRuleAppCost)
			return -1;
		if (o instanceof LongRuleAppCost)
			return 1;
		return compareTo((RuleAppCostTimeout) o);
	}

	/**
	 * checks whether an attempt of proving for time timeout is superior to the
	 * given (previous attempt) cost. Returns the superior cost, or null if
	 * still unknown.
	 */
	public static RuleAppCost superior(long timeout, RuleAppCost cost) {
		if (cost == null)
			return null;
		if (cost instanceof TopRuleAppCost)
			// we already know it won't work
			return TopRuleAppCost.INSTANCE;
		if (cost instanceof LongRuleAppCost)
			// we already know it works
			return cost;
		if (cost instanceof RuleAppCostTimeout) {
			if (timeout > ((RuleAppCostTimeout) cost).getValue()) {
				// larger timeout?, let's give it a try
				return null;
			} else {
				// we already know it'll probably timeout
				return HypotheticalProvabilityFeature.TIMEOUT_COST;
			}
		}
		throw new IllegalArgumentException("Don't know how to compare with "
				+ cost);
	}

	public int compareTo(RuleAppCostTimeout c) {
		return (timeout < c.timeout ? -1 : (timeout == c.timeout ? 0 : 1));
	}

	public boolean equals(Object o) {
		if (o instanceof RuleAppCost) {
			return compareTo((RuleAppCost) o) == 0;
		}
		return false;
	}

	public int hashCode() {
		return (int) timeout;
	}

	public RuleAppCost add(RuleAppCost cost2) {
		if (cost2 instanceof RuleAppCostTimeout) {
			return add((RuleAppCostTimeout) cost2);
		} else if (cost2 instanceof LongRuleAppCost) {
			// @todo or symbolically add cost plus timeout?
			return this;
		} else if (cost2 instanceof TopRuleAppCost) {
			return TopRuleAppCost.INSTANCE;
		} else {
			Debug.fail("Can't add costs of class " + cost2.getClass());
			// Should not be reached
			return null;
		}
	}

	public RuleAppCost add(RuleAppCostTimeout cost2) {
		return RuleAppCostTimeout.create(timeout + cost2.timeout);
	}

	public long getValue() {
		return timeout;
	}

	public String toString() {
		return "timeout " + timeout;
	}
}
