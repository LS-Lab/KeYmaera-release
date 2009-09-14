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
 * File created 20.02.2007
 */
package de.uka.ilkd.key.dl.strategy.features;

import java.math.BigDecimal;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.Quantifier;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.PosTacletApp;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.rule.UpdateSimplificationRule;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * This feature checks if we should apply the Simplify rule to the current
 * formula. It returns -10000 if the last rule applied was a local reduce.
 * Otherwise it checks if the formula only contains numbers, if that is the case
 * it computes a low cost, otherwise it returns TopRuleCost.
 * 
 * @author jdq
 * @since 20.02.2007
 * 
 */
public class SimplifyFeature extends Visitor implements Feature {

	public static final Feature INSTANCE = new SimplifyFeature();

	private int cost;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
	 *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
	 */
	public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {

		if (!goal.appliedRuleApps().isEmpty()) {
			boolean wasReduce = false;
			if (goal.appliedRuleApps().head() instanceof PosTacletApp) {
				TacletApp tapp = (TacletApp) goal.appliedRuleApps().head();
				wasReduce = tapp.taclet().ruleSets().next() == goal.proof()
						.getNamespaces().ruleSets().lookup(
								new Name("mathematica_reduce"))
						|| tapp.rule().name().equals(new Name("reduce_form"))
						|| tapp.rule().name().equals(new Name("reduce"));
			}
			RuleApp cur = goal.appliedRuleApps().head();
			int i = 1;
			while (cur.rule() instanceof UpdateSimplificationRule) {
				ImmutableList<RuleApp> take = goal.appliedRuleApps().take(i++);
				if(take.isEmpty()) {
					return TopRuleAppCost.INSTANCE;
				}
				cur = take.head();
			}
			boolean wasODESolve = false;
			if (cur instanceof PosTacletApp) {
				TacletApp tapp = (TacletApp) cur;
				wasODESolve = tapp.taclet().ruleSets().next() == goal.proof()
						.getNamespaces().ruleSets().lookup(
								new Name("diff_solve"))
						|| tapp.rule().name().equals(new Name("ODESolve_left"))
						|| tapp.rule().name()
								.equals(new Name("ODESolve_right"));
				wasODESolve &= (cur.posInOccurrence().isInAntec() == pos.isInAntec());
			}
			if (wasReduce || wasODESolve) {
				return LongRuleAppCost.create(-10000);
			}
		}

		cost = 0;
		pos.constrainedFormula().formula().execPreOrder(this);
		if (cost == Integer.MAX_VALUE) {
			return TopRuleAppCost.INSTANCE;
		}
		return LongRuleAppCost.create(cost);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
	 */
	/*@Override*/
	public void visit(Term visited) {
		if (cost != Integer.MAX_VALUE) {
			if (visited.op() instanceof Modality) {
				cost = Integer.MAX_VALUE;
			} else if (visited.op() instanceof LogicVariable) {
				cost = Integer.MAX_VALUE;
			} else if (visited.op() instanceof Function) {
				if (visited.op().arity() == 0) {
					cost -= 100;
					try {
						new BigDecimal(visited.op().name().toString());
					} catch (Exception e) {
						cost = Integer.MAX_VALUE;
					}
				}
			} else if (visited.op() instanceof ProgramVariable) {
				cost = Integer.MAX_VALUE;
			} else if (visited.op() instanceof Quantifier) {
				cost = Integer.MAX_VALUE;
			} else if (visited.op() instanceof Metavariable) {
				cost = Integer.MAX_VALUE;
			}
		}
	}
}
