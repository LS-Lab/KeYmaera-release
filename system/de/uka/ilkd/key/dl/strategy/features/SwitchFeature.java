/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
/**
 * 
 */
package de.uka.ilkd.key.dl.strategy.features;

import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * This feature is used to represent a switch statement in the strategy
 * language.
 * 
 * @author jdq
 * @since Sep 5, 2007
 * 
 */
public class SwitchFeature implements Feature {

	public static class Case {
		private Feature result;

		private Feature then;

		/**
		 * @param result
		 * @param then
		 */
		public Case(Feature result, Feature then) {
			super();
			this.result = result;
			this.then = then;
		}

	}

	private Feature condition;

	private Case[] events;

	public SwitchFeature(Feature condition, Case... condfeats) {
		this.condition = condition;
		this.events = condfeats;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
	 *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
	 */
	public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
		RuleAppCost cond = condition.compute(app, pos, goal);
		RuleAppCost result = LongRuleAppCost.ZERO_COST;
		for (Case feat : events) {
			if (cond.equals(feat.result.compute(app, pos, goal))) {
				result = result.add(feat.then.compute(app, pos, goal));
			}
		}
		return result;
	}

}
