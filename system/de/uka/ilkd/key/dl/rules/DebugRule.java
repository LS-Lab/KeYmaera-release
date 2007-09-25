/**
 * File created 01.02.2007
 */
package de.uka.ilkd.key.dl.rules;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.ListOfGoal;
import de.uka.ilkd.key.proof.RuleFilter;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * The debug rule is a rule used while developing the dL extension. It prints
 * out detailed informations about the current sequence.
 * 
 * @author jdq
 * @since 01.02.2007
 * 
 */
public class DebugRule extends Visitor implements BuiltInRule, RuleFilter {

    public static final DebugRule INSTANCE = new DebugRule();

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.BuiltInRule#isApplicable(de.uka.ilkd.key.proof.Goal,
     *      de.uka.ilkd.key.logic.PosInOccurrence,
     *      de.uka.ilkd.key.logic.Constraint)
     */
    public boolean isApplicable(Goal goal, PosInOccurrence pio,
            Constraint userConstraint) {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#apply(de.uka.ilkd.key.proof.Goal,
     *      de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
     */
    public synchronized ListOfGoal apply(Goal goal, Services services,
            RuleApp ruleApp) {
        System.out.println("Debug Rule");// XXX
        IteratorOfConstrainedFormula it = goal.sequent().iterator();
        while (it.hasNext()) {
            ConstrainedFormula f = it.next();
            f.formula().execPostOrder(this);
        }
        return goal.split(1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#displayName()
     */
    public String displayName() {
        return "Debug rule";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#name()
     */
    public Name name() {
        return new Name("DebugRule");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    @Override
    public void visit(Term visited) {
        System.out.println(visited + ": " + visited.getClass() + "@"
                + visited.hashCode());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.proof.RuleFilter#filter(de.uka.ilkd.key.rule.Rule)
     */
    public boolean filter(Rule rule) {
        return rule instanceof DebugRule;
    }

}
