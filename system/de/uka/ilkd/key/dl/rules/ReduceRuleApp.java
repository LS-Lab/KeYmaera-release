/**
 * File created 29.03.2007
 */
package de.uka.ilkd.key.dl.rules;

import java.util.List;

import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.BuiltInRuleApp;

/**
 * Special RuleApp for the DL builtin rules. This rule app can store a list of
 * variables that should be reduced.
 * 
 * @author jdq
 * @since 29.03.2007
 * 
 */
public class ReduceRuleApp extends BuiltInRuleApp {

    private List<String> variables;

    /**
     * @param builtInRule
     * @param pio
     * @param userConstraint
     */
    public ReduceRuleApp(BuiltInRule builtInRule, PosInOccurrence pio,
            Constraint userConstraint, List<String> variables) {
        super(builtInRule, pio, userConstraint);
        this.variables = variables;
    }

    /**
     * @return the variables
     */
    public List<String> getVariables() {
        return variables;
    }

    /**
     * @param variables
     *                the variables to set
     */
    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

}
