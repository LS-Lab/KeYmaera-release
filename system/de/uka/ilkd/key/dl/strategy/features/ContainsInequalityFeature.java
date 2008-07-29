/**
 * 
 */
package de.uka.ilkd.key.dl.strategy.features;

import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * @author jdq
 * 
 */
public class ContainsInequalityFeature implements Feature {

	public static final Feature INSTANCE = new ContainsInequalityFeature();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule
	 * .RuleApp, de.uka.ilkd.key.logic.PosInOccurrence,
	 * de.uka.ilkd.key.proof.Goal)
	 */
	@Override
	public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
		System.out.println(app.rule());//XXX
		if ((pos.isInAntec() && pos.constrainedFormula().formula().op() == Op.BOX)
				|| (!pos.isInAntec() && pos.constrainedFormula().formula().op() == Op.DIA)) {
			Term formula = pos.constrainedFormula().formula();
			DiffSystem one = (DiffSystem) ((StatementBlock) formula.javaBlock()
					.program()).getChildAt(0);
			for (ProgramElement p : one.getDifferentialEquations()) {
				if (p instanceof PredicateTerm) {
					if (!(((PredicateTerm) p).getChildAt(0) instanceof Equals)) {
						return LongRuleAppCost.ZERO_COST;
					}
				}
			}
		}
		return TopRuleAppCost.INSTANCE;
	}

}
