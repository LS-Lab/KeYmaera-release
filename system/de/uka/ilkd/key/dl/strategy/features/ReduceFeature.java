/**
 * File created 20.02.2007
 */
package de.uka.ilkd.key.dl.strategy.features;

import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.IUpdateOperator;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.Quantifier;
import de.uka.ilkd.key.logic.op.SubstOp;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * TODO jdq documentation
 * 
 * @author jdq
 * @since 20.02.2007
 * 
 */
public class ReduceFeature extends Visitor implements Feature {

    public static final Feature INSTANCE = new ReduceFeature();

    public boolean infinity = false;

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
     *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
     */
    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        if (pos.constrainedFormula().formula().op() instanceof Quantifier) {
            infinity = false;
            pos.constrainedFormula().formula().execPreOrder(this);
            if (!infinity) {
                return LongRuleAppCost.ZERO_COST;
            }
        }
        return TopRuleAppCost.INSTANCE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        if (visited.op() instanceof Modality || visited.op() instanceof SubstOp
                || visited.op() instanceof IUpdateOperator) {
            infinity = true;
        }
    }

}
