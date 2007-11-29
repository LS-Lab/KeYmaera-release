/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel André Platzer                  *
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

import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * Detects if results from augmentation proof part of diffstrengthen
 * @author ap
 * 
 */
public class PostDiffStrengthFeature implements Feature {

    public static final Feature INSTANCE = new PostDiffStrengthFeature();

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
     *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
     */
    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {

        if (!goal.appliedRuleApps().isEmpty()
                && goal.appliedRuleApps().head() instanceof TacletApp) {
            TacletApp tapp = (TacletApp) goal.appliedRuleApps().head();
            if (tapp.taclet().ruleSets().next() == goal.proof().getNamespaces()
                    .ruleSets().lookup(new Name("diffstrengthen"))
                    || tapp.rule().name().equals(new Name("diffstrengthen"))) {
                if ("Invariant Valid".equals(goal.node().getNodeInfo().getBranchLabel())) {
                    return LongRuleAppCost.ZERO_COST;
                }
            }
        }
        return TopRuleAppCost.INSTANCE;
    }
}
