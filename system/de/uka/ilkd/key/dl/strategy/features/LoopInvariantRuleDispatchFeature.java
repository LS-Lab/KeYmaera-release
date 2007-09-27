/**
 * 
 */
package de.uka.ilkd.key.dl.strategy.features;

import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.options.DLOptionBean.InvariantRule;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * TODO jdq documentation since Sep 24, 2007
 * 
 * @author jdq
 * @since Sep 24, 2007
 * 
 */
public class LoopInvariantRuleDispatchFeature implements Feature {

    public static final Feature INSTANCE = new LoopInvariantRuleDispatchFeature();

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
     *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
     */
    @Override
    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {

        assert ruleKnown(app.rule().name());

        if (app.rule().name().toString().equals(
                DLOptionBean.INSTANCE.getInvariantRule().toString())) {
            return LongRuleAppCost.ZERO_COST;
        } else {
            return TopRuleAppCost.INSTANCE;
        }
    }

    /**
     * TODO jdq documentation since Sep 24, 2007
     * 
     * @param name
     * @return
     */
    private boolean ruleKnown(Name name) {
        for (InvariantRule inv : DLOptionBean.InvariantRule.values()) {
            if (name.toString().equals(inv.toString())) {
                return true;
            }
        }
        return false;
    }

}
