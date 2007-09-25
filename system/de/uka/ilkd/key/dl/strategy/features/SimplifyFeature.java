/**
 * File created 20.02.2007
 */
package de.uka.ilkd.key.dl.strategy.features;

import java.math.BigDecimal;

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
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.TacletApp;
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

        if (!goal.appliedRuleApps().isEmpty()
                && goal.appliedRuleApps().head() instanceof TacletApp) {
            TacletApp tapp = (TacletApp) goal.appliedRuleApps().head();
            if (tapp.taclet().ruleSets().next() == goal.proof().getNamespaces()
                    .ruleSets().lookup(new Name("mathematica_reduce"))
                    || tapp.rule().name().equals(new Name("reduce_form"))
                    || tapp.rule().name().equals(new Name("reduce"))) {
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
    @Override
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
