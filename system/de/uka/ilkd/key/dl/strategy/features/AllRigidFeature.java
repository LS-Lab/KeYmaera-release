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
package de.uka.ilkd.key.dl.strategy.features;

import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.NonRigidFunctionLocation;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * @author jdq
 *
 */
public class AllRigidFeature implements Feature {

    public static final AllRigidFeature INSTANCE = new AllRigidFeature();
    
    /* (non-Javadoc)
     * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp, de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
     */
    @Override
    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        for(ConstrainedFormula f: goal.sequent()) {
            final boolean[] result = new boolean[1];
            f.formula().execPreOrder(new Visitor() {
                
                @Override
                public void visit(Term visited) {
                    if(visited.op() instanceof NonRigidFunctionLocation) {
                        result[0] = true;
                    }
                }
            });
            if(result[0]) {
                return TopRuleAppCost.INSTANCE;
            }
        }
        return LongRuleAppCost.ZERO_COST;
    }

}
