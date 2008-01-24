/**
 * 
 */
package de.uka.ilkd.key.dl.strategy.features;

import java.util.ArrayList;
import java.util.List;

import de.uka.ilkd.key.dl.formulatools.ContainsMetaVariableVisitor;
import de.uka.ilkd.key.dl.formulatools.ContainsMetaVariableVisitor.Result;
import de.uka.ilkd.key.dl.rules.EliminateExistentialQuantifierRule;
import de.uka.ilkd.key.dl.rules.ReduceRuleApp;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.IteratorOfGoal;
import de.uka.ilkd.key.proof.ListOfGoal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * @author jdq
 * 
 */
public class EliminateExistentialApproveFeature implements Feature {

    public static final Feature INSTANCE = new EliminateExistentialApproveFeature();

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
     *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
     */
    @Override
    public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
        if (app instanceof EliminateExistentialQuantifierRule) {
            Operator op = app.posInOccurrence().subTerm().op();
            if (!(op instanceof Metavariable)) {
                return TopRuleAppCost.INSTANCE;
            }
            List<Metavariable> variables = new ArrayList<Metavariable>();
            if (app instanceof ReduceRuleApp) {
                for (String varName : ((ReduceRuleApp) app).getVariables()) {
                    variables
                            .add((Metavariable) Main.getInstance().mediator()
                                    .namespaces().variables().lookup(
                                            new Name(varName)));
                }
            }
            if (variables.isEmpty()) {
                variables.add((Metavariable) op);
            }
            ListOfGoal openGoals = goal.proof().openGoals();
            IteratorOfGoal goalIt = openGoals.iterator();
            while (goalIt.hasNext()) {
                Goal curGoal = goalIt.next();
                IteratorOfConstrainedFormula it = curGoal.sequent().iterator();
                Result result = Result.DOES_NOT_CONTAIN_VAR;
                while (it.hasNext()) {
                    ConstrainedFormula next = it.next();
                    Result res = ContainsMetaVariableVisitor
                            .containsMetaVariableAndIsFO(variables, next
                                    .formula());
                    if ((result == Result.CONTAINS_VAR)
                            && (res == Result.CONTAINS_VAR_BUT_CANNOT_APPLY || res == Result.DOES_NOT_CONTAIN_VAR_AND_IS_NOT_FO)) {
                        result = Result.CONTAINS_VAR_BUT_CANNOT_APPLY;
                        break;
                    } else if (result == Result.DOES_NOT_CONTAIN_VAR_AND_IS_NOT_FO) {
                        if (res == Result.CONTAINS_VAR) {
                            result = Result.CONTAINS_VAR_BUT_CANNOT_APPLY;
                        }
                    } else if (res == Result.CONTAINS_VAR) {
                        result = res;
                    } else if (res == Result.CONTAINS_VAR_BUT_CANNOT_APPLY) {
                        result = res;
                        break;
                    }
                }
                if (result == Result.CONTAINS_VAR_BUT_CANNOT_APPLY) {
                    return TopRuleAppCost.INSTANCE;
                }
            }
        }
        return LongRuleAppCost.ZERO_COST;
    }

}
