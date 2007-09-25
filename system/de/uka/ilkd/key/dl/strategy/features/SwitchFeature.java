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
 * TODO jdq documentation since Sep 5, 2007
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
