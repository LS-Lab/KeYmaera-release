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
import de.uka.ilkd.key.logic.op.QuanUpdateOperator;
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
	/*@Override*/
	public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
		Term term = pos.constrainedFormula().formula();
		while (term.op() instanceof QuanUpdateOperator) {
			term = ((QuanUpdateOperator) term.op()).target(term);
		}
		if ((pos.isInAntec() && term.op() == Op.BOX)
				|| (!pos.isInAntec() && term.op() == Op.DIA)) {
			DiffSystem one = (DiffSystem) ((StatementBlock) term.javaBlock()
					.program()).getChildAt(0);
			for (ProgramElement p : one.getDifferentialEquations(goal.proof().getNamespaces())) {
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
