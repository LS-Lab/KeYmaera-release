/**
 * File created 20.02.2007
 */
package de.uka.ilkd.key.dl.strategy.features;

import java.util.WeakHashMap;

import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.Rule;
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
public class OnlyOncePerBranchFeature implements Feature {

	public static final OnlyOncePerBranchFeature INSTANCE = new OnlyOncePerBranchFeature();
	
	private WeakHashMap<RuleApp, Rule> applied = new WeakHashMap<RuleApp, Rule>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule.RuleApp,
	 *      de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.proof.Goal)
	 */
	public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
		if(applied.containsKey(app)) {
			return TopRuleAppCost.INSTANCE;
		}
		applied.put(app, app.rule());
//		IteratorOfRuleApp it = goal.appliedRuleApps().iterator();
//		while (it.hasNext()) {
//			RuleApp next = it.next();
//			if (next.rule() == app.rule()) {
//				return TopRuleAppCost.INSTANCE;
//			}
			
//		}
		return LongRuleAppCost.ZERO_COST;
	}
}
