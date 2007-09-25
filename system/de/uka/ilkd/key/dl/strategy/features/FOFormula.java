/**
 * 
 */
package de.uka.ilkd.key.dl.strategy.features;

import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * TODO jdq documentation since Sep 6, 2007
 * 
 * @author jdq
 * @since Sep 6, 2007
 * 
 */
public class FOFormula implements Feature {

    public static final Feature INSTANCE = new FOFormula();
    
    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
     *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
     */
    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        final boolean[] result = { true };
        pos.constrainedFormula().formula().execPreOrder(new Visitor() {

            @Override
            public void visit(Term visited) {
                if (!FOSequence.isFOOperator(visited.op())) {
                    result[0] = false;
                }
            }

        });
        if (result[0]) {
            return LongRuleAppCost.ZERO_COST;
        } else {
            return TopRuleAppCost.INSTANCE;
        }
    }
}
