/***************************************************************************
 *   Copyright (C) 2008 by Jan David Quesel                                *
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
package de.uka.ilkd.key.dl.strategy.features;

import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * @author jdq
 * TODO Documentation since 18.09.2008
 */
public class ContainsMetaVariableFeature implements Feature {

	public static final ContainsMetaVariableFeature INSTANCE = new ContainsMetaVariableFeature();
	
	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp, de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
	 */
	/*@Override*/
	public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
		final boolean[] containsMetaVariable = new boolean[1];
		for(ConstrainedFormula f: goal.sequent()) {
			if(f.formula().metaVars().size() > 0) {
				return LongRuleAppCost.ZERO_COST;
			}
			f.formula().execPostOrder(new Visitor() {

				/*@Override*/
				public void visit(Term visited) {
					if(visited.op() instanceof Metavariable) {
						containsMetaVariable[0] = true;
					}
				}
				
			});
			if(containsMetaVariable[0]) {
				return LongRuleAppCost.ZERO_COST;
			}
		}
		return TopRuleAppCost.INSTANCE;
	}

}
