/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
/**
 * 
 */
package de.uka.ilkd.key.dl.strategy.features;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.rules.TestableBuiltInRule;
import de.uka.ilkd.key.dl.rules.UnknownProgressRule;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;

/**
 * Timeout Strategy
 * 
 * @author jdq
 * 
 */
public class TimeoutTestApplicationFeature implements Feature {

	private Map<Node, Long> branchingNodesAlreadyTested = new WeakHashMap<Node, Long>();

	private Map<Node, RuleAppCost> resultCache = new WeakHashMap<Node, RuleAppCost>();

	public static final TimeoutTestApplicationFeature INSTANCE = new TimeoutTestApplicationFeature();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.strategy.feature.Feature#compute(de.uka.ilkd.key.rule
	 * .RuleApp, de.uka.ilkd.key.logic.PosInOccurrence,
	 * de.uka.ilkd.key.proof.Goal)
	 */
	public RuleAppCost compute(RuleApp app, PosInOccurrence pos, Goal goal) {
		Node firstNodeAfterBranch = getFirstNodeAfterBranch(goal.node());
		if (branchingNodesAlreadyTested.containsKey(firstNodeAfterBranch)) {
			if (resultCache.containsKey(firstNodeAfterBranch)) {
				return resultCache.get(firstNodeAfterBranch);
			}
			return TopRuleAppCost.INSTANCE;
		} else {
			Long timeout = getLastTimeout(firstNodeAfterBranch);
			if (timeout != null && timeout < 0 ) {
			    throw new IllegalStateException("Negative Timeout " + timeout + " from last timeout " + " for " + app + " in " + goal);
			}
			if (timeout == null) {
				timeout = DLOptionBean.INSTANCE.getInitialTimeout();
				if (timeout != null && timeout < 0 ) {
				    throw new IllegalStateException("Negative Timeout " + timeout + " from initial timeout " + " for " + app + " in " + goal);
				}
			} else {
				final int a = DLOptionBean.INSTANCE
						.getQuadraticTimeoutIncreaseFactor();
				final int b = DLOptionBean.INSTANCE
						.getLinearTimeoutIncreaseFactor();
				final int c = DLOptionBean.INSTANCE
						.getConstantTimeoutIncreaseFactor();
				final Long lasttimeout = timeout;
				timeout = a * timeout * timeout + b * timeout + c;
				if (lasttimeout != null && lasttimeout > 0 && timeout < 0) {
				    // long arithmetic overflow?
				    // fix by setting to 0 which means indefinitely
				    timeout = 0L;
				}
				if (timeout != null && timeout < 0 ) {
				    throw new IllegalStateException("Negative Timeout " + timeout + " = " + " from lasttimeout " + lasttimeout + " = " + a +"*" + timeout + "^2" + "+ " + b + "*" + timeout + "+ " + c + " for " + app + " in " + goal);
				}
			}
			branchingNodesAlreadyTested.put(firstNodeAfterBranch, timeout);

			Rule rule = app.rule();
			if (rule instanceof TestableBuiltInRule) {
				TestableBuiltInRule tbr = (TestableBuiltInRule) rule;
				TestThread testThread = new TestThread(goal, tbr, app,
						timeout * 1000);
				testThread.start();
				try {
					testThread.join(2 * 1000 * timeout);
					if (testThread.getResult() == TestResult.SUCCESS) {

						// only accept formulas that are really better than the
						// input formula, i.e. contain less variables
						Term inputFormula = tbr.getInputFormula();
						Term resultFormula = tbr.getResultFormula();
						if (inputFormula != null && resultFormula != null) {
							int variableNumber = FOVariableNumberCollector
									.getVariableNumber(inputFormula);
							int variableNumber2 = FOVariableNumberCollector
									.getVariableNumber(resultFormula);
							int number = variableNumber - variableNumber2;
							if (number > 0
									|| resultFormula == TermBuilder.DF.tt()) {
								// dont put this in the result cache
								return LongRuleAppCost.ZERO_COST;
							}
						}
						resultCache.put(firstNodeAfterBranch,
								TopRuleAppCost.INSTANCE);
						return TopRuleAppCost.INSTANCE;
					} else if (testThread.getResult() == TestResult.UNSOLVABLE) {
						resultCache.put(firstNodeAfterBranch,
								TopRuleAppCost.INSTANCE);
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
				if (testThread.isAlive()) {
					try {
						MathSolverManager.getCurrentQuantifierEliminator()
								.abortCalculation();
					} catch (RemoteException f) {
						testThread.interrupt();

					}
				}
			}

			resultCache.put(firstNodeAfterBranch, LongRuleAppCost.create(1));
			return LongRuleAppCost.create(1);
		}
	}

	/**
	 * @param node
	 * @return
	 */
	private Long getLastTimeout(Node node) {
		Long result = null;
		if (node != null) {
			result = branchingNodesAlreadyTested.get(node);
			if (result == null) {
				result = getLastTimeout(node.parent());
			}
		}
		return result;
	}

	/**
	 * @return
	 */
	public static Node getFirstNodeAfterBranch(Node node) {
		if (node.root()
				|| node.parent().root()
				|| node.parent().childrenCount() > 1
				|| node.parent().getAppliedRuleApp().rule() instanceof UnknownProgressRule) {
			return node;
		}
		return getFirstNodeAfterBranch(node.parent());
	}

	static enum TestResult {
		UNKNOWN, SUCCESS, FAILURE, UNSOLVABLE;
	}

	static class TestThread extends Thread {

		private Goal goal;

		private TestableBuiltInRule rule;

		private RuleApp app;

		private TestResult result;

		private long timeout;

		public TestThread(Goal goal, TestableBuiltInRule rule, RuleApp app,
				long timeout) {
			this.goal = goal;
			this.rule = rule;
			this.app = app;
			this.result = TestResult.UNKNOWN;
			this.timeout = timeout;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		/*@Override*/
		public void run() {
			if (rule.test(goal, goal.proof().getServices(),
					app, timeout)) {
				result = TestResult.SUCCESS;
				if (!rule.getResultFormula().equals(TermBuilder.DF.tt())) {
					if (!(rule.getInputFormula().metaVars().size() == 0)) {
						result = TestResult.FAILURE;
					} else {
						final Set<Metavariable> vars = new HashSet<Metavariable>();
						Visitor visitor = new Visitor() {

							/*@Override*/
							public void visit(Term visited) {
								if (visited.op() instanceof Metavariable) {
									vars.add((Metavariable) visited.op());
								}
							}

						};
						rule.getInputFormula().execPostOrder(visitor);
						if (vars.size() > 0) {
							result = TestResult.FAILURE;
						}
					}
				}

			} else {
				if (rule.isUnsolvable()) {
					result = TestResult.UNSOLVABLE;
				} else {
					result = TestResult.FAILURE;
				}
			}
		}

		/**
		 * @return the result
		 */
		public TestResult getResult() {
			return result;
		}
	}

}
