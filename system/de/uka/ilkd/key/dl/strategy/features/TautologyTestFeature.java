package de.uka.ilkd.key.dl.strategy.features;

import java.rmi.RemoteException;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.rules.TestableBuiltInRule;
import de.uka.ilkd.key.dl.strategy.features.TimeoutTestApplicationFeature.TestResult;
import de.uka.ilkd.key.dl.strategy.features.TimeoutTestApplicationFeature.TestThread;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

public class TautologyTestFeature implements Feature {

	@Override
	public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
		Rule rule = app.rule();
		long timeout = 10;
		if (rule instanceof TestableBuiltInRule) {
			TestableBuiltInRule tbr = (TestableBuiltInRule) rule;
			TestThread testThread = new TestThread(goal, tbr, app,
					1000 * timeout);
			testThread.start();
			try {
				testThread.join(2 * 1000 * timeout);
				if (testThread.getResult() == TestResult.SUCCESS) {

					// only accept formulas that are really better than the
					// input formula, i.e. contain less variables
					Term resultFormula = tbr.getResultFormula();
					if(resultFormula == TermBuilder.DF.tt()) {
						return LongRuleAppCost.ZERO_COST;
					}
					return TopRuleAppCost.INSTANCE;
				} else if (testThread.getResult() == TestResult.UNSOLVABLE) {
					return TopRuleAppCost.INSTANCE;
				}
			} catch (InterruptedException e) {
				try {
					MathSolverManager.getCurrentQuantifierEliminator()
							.abortCalculation();
				} catch (RemoteException f) {
					testThread.interrupt();

				}
			}
		}
		return TopRuleAppCost.INSTANCE;
	}

}
