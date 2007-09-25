/**
 * File created 27.03.2007
 */
package de.uka.ilkd.key.dl.rules;

import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.rule.Rule;

/**
 * This rule eliminates the given skolem symbol using all formulas contained in
 * the sequent.
 * 
 * @author jdq
 * @since 27.03.2007
 * 
 */
public class EliminateQuantifierRuleWithContext extends EliminateQuantifierRule {

    public static final EliminateQuantifierRuleWithContext INSTANCE = new EliminateQuantifierRuleWithContext();

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#displayName()
     */
    @Override
    public String displayName() {
        return "Eliminate Quantifier With Context";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#name()
     */
    @Override
    public Name name() {
        return new Name("EliminateQuantifierWithContext");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.RuleOperatingOnWholeSequence#performSearch(de.uka.ilkd.key.logic.Term)
     */
    @Override
    protected void performSearch(Term visited) {
        // As want the whole context, always set addFormula to true
        addFormula = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.proof.RuleFilter#filter(de.uka.ilkd.key.rule.Rule)
     */
    @Override
    public boolean filter(Rule rule) {
        return rule instanceof EliminateQuantifierRuleWithContext;
    }

}
