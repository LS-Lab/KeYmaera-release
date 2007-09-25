/**
 * 
 */
package de.uka.ilkd.key.dl.rules;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * @author jdq
 * 
 */
public interface TestableBuiltInRule {

    public boolean test(Goal goal, Services services, RuleApp app);

    public Term getInputFormula();

    public Term getResultFormula();
}
