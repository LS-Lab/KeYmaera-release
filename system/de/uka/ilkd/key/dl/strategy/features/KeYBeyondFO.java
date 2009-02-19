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
 * File created 09.03.2007
 */
package de.uka.ilkd.key.dl.strategy.features;

import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.options.DLOptionBean.FirstOrderStrategy;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * This feature returns 10000 if the option isSplitBeyondFO is set, -10000
 * otherwise. It is used as a penalty for quantifier elimination rules.
 * 
 * @author jdq
 * @since 09.03.2007
 * 
 */
public class KeYBeyondFO implements Feature {

	public static final Feature INSTANCE = new KeYBeyondFO();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule
	 * .RuleApp, de.uka.ilkd.key.logic.PosInOccurrence,
	 * de.uka.ilkd.key.proof.Goal)
	 */
	public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
		if (DLOptionBean.INSTANCE.getFoStrategy() == FirstOrderStrategy.LAZY
				|| DLOptionBean.INSTANCE.getFoStrategy() == FirstOrderStrategy.STOP) {
			return LongRuleAppCost.create(10000);
		}
		return LongRuleAppCost.create(-10000);
	}

}
