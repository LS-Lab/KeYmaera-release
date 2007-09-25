/**
 * File created 09.03.2007
 */
package de.uka.ilkd.key.dl.strategy.features;

import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * TODO jdq documentation
 * @author jdq
 * @since 09.03.2007
 *
 */
public class KeYBeyondFO implements Feature {

	public static final Feature INSTANCE = new KeYBeyondFO();

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp, de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
	 */
	public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
		if(DLOptionBean.INSTANCE.isSplitBeyondFO()) {
			return LongRuleAppCost.create(10000);
		}
		return LongRuleAppCost.create(-10000);
	}

}
