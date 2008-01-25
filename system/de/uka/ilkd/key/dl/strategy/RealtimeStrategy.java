/**
 * 
 */
package de.uka.ilkd.key.dl.strategy;

import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.Strategy;

/**
 * Tags strategies that have real-time constraints on intermediate proof rules.
 * @author ap
 */
public interface RealtimeStrategy extends Strategy {
    /**
     * Get the timeout to respect for the given rule application on the given goal.
     * @param app
     * @return
     */
    long getTimeout(Goal goal, RuleApp app); 
}
