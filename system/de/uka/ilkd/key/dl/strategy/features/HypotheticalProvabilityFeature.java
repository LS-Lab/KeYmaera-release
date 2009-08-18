/***************************************************************************
 *   Copyright (C) 2007 by Andre Platzer                                   *
 *   @informatik.uni-oldenburg.de                                          *
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableMapEntry;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.IncompleteEvaluationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.rules.UnknownProgressRule;
import de.uka.ilkd.key.dl.strategy.DLStrategy;
import de.uka.ilkd.key.gui.ApplyStrategy;
import de.uka.ilkd.key.gui.KeYMediator;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Named;
import de.uka.ilkd.key.logic.Namespace;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.pp.LogicPrinter;
import de.uka.ilkd.key.pp.ProgramPrinter;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.IGoalChooser;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.Goal.GoalStatus;
import de.uka.ilkd.key.proof.proofevent.NodeChangeJournal;
import de.uka.ilkd.key.proof.proofevent.RuleAppInfo;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.rule.inst.InstantiationEntry;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.Strategy;
import de.uka.ilkd.key.strategy.TacletAppContainer;
import de.uka.ilkd.key.strategy.TopRuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;
import de.uka.ilkd.key.util.Debug;
import de.uka.ilkd.key.visualdebugger.ProofStarter;

/**
 * Or-branching Timeout Strategy for testing provability of subgoals within a
 * certain timeout. Feature gives 0 if all subgoals are indeed provable,
 * infinity if some subgoal is definitey not provable because it yields a
 * counterexample, 1 if a timeout occurs before a provable/nonprovable decision
 * has been made. Feature temporarily applies the given rule in a hypothetical
 * proof and computes a cost depending on whether the hypothetical proof works
 * out.
 * 
 * @author ap
 * @todo reuse hypothetical proof for doing rule applications in main proof
 * @todo iteratively increase timeout
 */
public class HypotheticalProvabilityFeature implements Feature {

	/**
	 * Possible results of a hypothetic proof attempt.
	 * 
	 * @author ap
	 * 
	 */
	public static enum HypotheticalProvability {
		UNKNOWN, PROVABLE, DISPROVABLE, TIMEOUT, ERROR;
	}

	public static final RuleAppCost TIMEOUT_COST = LongRuleAppCost.create(1);

	private static final TopRuleAppCost DISPROVABLE_COST = TopRuleAppCost.INSTANCE;

	private static final LongRuleAppCost PROVABLE_COST = LongRuleAppCost.ZERO_COST;

	/**
	 * maximum number of rule applications in hypothetical proofs.
	 */
	public static final int MAX_HYPOTHETICAL_RULE_APPLICATIONS = Main
			.getInstance().mediator().getMaxAutomaticSteps();

	/**
	 * Whether to stop on the first goal without progress.
	 */
	private static final boolean STOP_EARLY = true;

	private Map<Node, Long> branchingNodesAlreadyTested = new WeakHashMap<Node, Long>();

	private Map<Node, RuleAppCost> resultCache = new WeakHashMap<Node, RuleAppCost>();

	public static final HypotheticalProvabilityFeature INSTANCE = new HypotheticalProvabilityFeature();

	/**
	 * the default initial timeout, -1 means use
	 * DLOptionBean.INSTANCE.getInitialTimeout()
	 */
	private final long initialTimeout;

	/**
	 * @param timeout
	 *            the default overall (initial) timeout for the hypothetic proof
	 *            (in s!)
	 */
	public HypotheticalProvabilityFeature(long timeout) {
		this.initialTimeout = timeout;
	}

	public HypotheticalProvabilityFeature() {
		this(-1);
	}

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
		// if (branchingNodesAlreadyTested.containsKey(firstNodeAfterBranch)) {
		// if (resultCache.containsKey(firstNodeAfterBranch)) {
		// return resultCache.get(firstNodeAfterBranch);
		// }
		// return TopRuleAppCost.INSTANCE;
		// } else {
		final Services services = goal.proof().getServices();
		Long timeout = getLastTimeout(firstNodeAfterBranch);
		if (timeout == null) {
			timeout = initialTimeout >= 0 ? initialTimeout
					: DLOptionBean.INSTANCE.getInitialTimeout();
		} else {
			final int a = DLOptionBean.INSTANCE
					.getQuadraticTimeoutIncreaseFactor();
			final int b = DLOptionBean.INSTANCE
					.getLinearTimeoutIncreaseFactor();
			final int c = DLOptionBean.INSTANCE
					.getConstantTimeoutIncreaseFactor();
			timeout = a * timeout * timeout + b * timeout + c;
		}
		// branchingNodesAlreadyTested.put(firstNodeAfterBranch, timeout);

		System.out.println("HYPO: " + app.rule().name()
				+ prettyPrint(app, services));
		HypotheticalProvability result = provable(app, pos, goal,
				MAX_HYPOTHETICAL_RULE_APPLICATIONS, timeout * 1000);
		System.out
				.println("HYPO: "
						+ app.rule().name()
						+ " "
						+ result
						+ "\t"
						+ (result == HypotheticalProvability.PROVABLE ? SimpleDateFormat
								.getTimeInstance().format(
										System.currentTimeMillis())
								: "") + prettyPrint(app, services));
		switch (result) {
		case PROVABLE:
			return PROVABLE_COST;
		case ERROR:
		case DISPROVABLE:
			// resultCache.put(firstNodeAfterBranch, TopRuleAppCost.INSTANCE);
			return DISPROVABLE_COST;
		case UNKNOWN:
		case TIMEOUT:
			// resultCache.put(firstNodeAfterBranch, LongRuleAppCost.create(1));
			return TIMEOUT_COST;
		default:
			throw new AssertionError("enum known");
		}
	}

	private String prettyPrint(RuleApp app, Services services) {
		if (app instanceof TacletApp) {
			TacletApp tapp = (TacletApp) app;
			String r = "";
			for (Iterator<ImmutableMapEntry<SchemaVariable, InstantiationEntry>> i = tapp
					.instantiations().interesting().entryIterator(); i
					.hasNext();) {
				ImmutableMapEntry<SchemaVariable, InstantiationEntry> e = i.next();
				r += "\n\t";
				try {
					final LogicPrinter lp = new LogicPrinter(
							new ProgramPrinter(null), Main.getInstance()
									.mediator().getNotationInfo(), services);
					lp.printTerm((Term) e.value().getInstantiation());
					r += e.key().name() + "<-  " + lp.toString();
				} catch (Exception ignore) {
					r += e.key().name() + "<-  "
							+ e.value().getInstantiation().toString();
				}
			}
			return r;
		} else {
			return "";
		}
	}

	// caching

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

	// hypothetical rule application engines

	/**
	 * Make a new hypothetical proof with the given goal as its only goal
	 * 
	 * @param seq
	 *            the problem to proof
	 * @param context
	 *            in which context (which will be copied) to try to prove seq.
	 * @param taboo
	 *            which rules are not to be used (tabu) during the hypothetical
	 *            proof.
	 * @return
	 */
	static Proof newHypotheticalProofFor(Proof context, Sequent seq,
			long timeout, java.util.Set<Name> taboo) {
		// new proof with settings like goal.proof() but goal as its
		// only goal
		Proof hypothetic = new Proof(new Name("hypothetic"), context, seq);
		Goal hgoal = hypothetic.getGoal(hypothetic.root());
		assert hgoal != null && hgoal.sequent().equals(seq);
		Strategy stopEarly;
		if (taboo == null) {
			stopEarly = DLStrategy.Factory.INSTANCE.create(hypothetic, null,
					true, timeout);
		} else {
			stopEarly = DLStrategy.Factory.INSTANCE.create(hypothetic, null,
					true, timeout, taboo);
		}
		hgoal.setGoalStrategy(stopEarly);
		hypothetic.setActiveStrategy(stopEarly);
		return hypothetic;
	}

	static Proof newHypotheticalProofFor(Proof context, Sequent seq) {
		return newHypotheticalProofFor(context, seq, -1, null);
	}

	static Proof newHypotheticalProofFor(Goal goal) {
		return newHypotheticalProofFor(goal.proof(), goal.sequent());
	}

	static Proof newHypotheticalProofFor(Goal goal, long timeout) {
		return newHypotheticalProofFor(goal.proof(), goal.sequent(), timeout,
				null);
	}

	/**
	 * Determines whether the given goal can finally be proven.
	 * 
	 * @param goal
	 *            the goal to close hypothetically.
	 * @param timeout
	 *            the maximum time how long the proof is attempted to close.
	 * @return Result of proving goal.
	 */
	public static HypotheticalProvability provable(Proof context,
			Sequent problem, int maxsteps, long timeout, Set<Name> taboo) {
		NamespaceSet copy = null;
		assert (copy = context.getServices().getNamespaces().copy()) != null;
		// continue hypothetic proof to see if it closes/has
		// counterexamples
		try {
			return provable(newHypotheticalProofFor(context, problem, timeout,
					taboo), maxsteps, timeout);
		} finally {
			assert context.getServices().getNamespaces().equalContent(copy) : "no change in original namespaces\n"
					+ printDelta(copy, context.getServices().getNamespaces());
		}
	}

	/**
	 * Determines whether the given goal can finally be proven.
	 * 
	 * @param timeout
	 *            the maximum time (in ms) how long the proof is attempted to
	 *            close.
	 * @return Result of proving goal.
	 */
	public static HypotheticalProvability provable(Proof context,
			Sequent problem, int maxsteps, long timeout) {
		NamespaceSet copy = null;
		assert (copy = context.getServices().getNamespaces().copy()) != null;
		// continue hypothetic proof to see if it closes/has
		// counterexamples
		try {
			return provable(newHypotheticalProofFor(context, problem, timeout,
					null), maxsteps, timeout);
		} finally {
			assert context.getServices().getNamespaces().equalContent(copy) : "no change in original namespaces\n"
					+ printDelta(copy, context.getServices().getNamespaces());
		}
	}

	/**
	 * Determines whether the given goal can finally be proven.
	 * 
	 * @param goal
	 *            the goal to close hypothetically.
	 * @param timeout
	 *            the maximum time (in ms) how long the proof is attempted to
	 *            close.
	 * @return Result of proving goal.
	 */
	public static HypotheticalProvability provable(Goal goal, int maxsteps,
			long timeout) {
		NamespaceSet copy = null;
		assert (copy = goal.proof().getServices().getNamespaces().copy()) != null;
		Proof hypothetic = newHypotheticalProofFor(goal, timeout);
		// continue hypothetic proof to see if it closes/has
		// counterexamples
		try {
			return provable(hypothetic, maxsteps, timeout);
		} finally {
			assert goal.proof().getServices().getNamespaces()
					.equalContent(copy) : "no change in original namespaces\n"
					+ printDelta(copy, goal.proof().getServices()
							.getNamespaces());
		}
	}

	/**
	 * Determines whether the given goal can finally be proven by applying the
	 * given RuleApp.
	 * 
	 * @param app
	 *            the first rule to apply.
	 * @param goal
	 *            the goal to close hypothetically.
	 * @param timeout
	 *            the maximum time how long the proof is attempted to close.
	 * @return Result of proving goal.
	 */
	/*
	 * public static HypotheticalProvability provable(RuleApp app, Goal goal,
	 * int maxsteps, long timeout) { NamespaceSet copy = null; assert (copy =
	 * goal.proof().getServices().getNamespaces().copy()) != null; Proof
	 * hypothetic = newHypotheticalProofFor(goal, timeout); Goal hgoal =
	 * hypothetic.getGoal(hypothetic.root()); // apply app on hypothetic proof
	 * apply(hgoal, app); Debug.out("HYPO: after first application"); //
	 * continue hypothetic proof to see if it closes/has // counterexamples try
	 * { return provable(hypothetic, maxsteps, timeout); } finally { assert
	 * goal.proof().getServices().getNamespaces().equalContent(copy) :
	 * "no change in original namespaces\n" + printDelta(copy,
	 * goal.proof().getServices().getNamespaces()); } }
	 */
	/**
	 * Determines whether the given goal can finally be proven by applying the
	 * given RuleApp, by possibly completing the RuleApp at the specified pos.
	 * 
	 * @param app
	 *            the first rule to apply.
	 * @param goal
	 *            the goal to close hypothetically.
	 * @param timeout
	 *            the maximum time how long the proof is attempted to close.
	 * @return Result of proving goal.
	 */
	public static HypotheticalProvability provable(RuleApp app,
			PosInOccurrence pos, Goal goal, int maxsteps, long timeout) {
		// TODO could use
		// goal.proof().getServices().getNamespaces().startProtocol(); and
		// introduce remove later on
		// and use appropriate Services.setBackCounters.
		NamespaceSet copy = goal.proof().getServices().getNamespaces().copy();
		assert copy != null;
		Proof hypothetic = newHypotheticalProofFor(goal, timeout);
		Goal hgoal = hypothetic.getGoal(hypothetic.root());
		if (!app.complete()) {
			// completing incomplete rule application
			app = completeRuleApp(hgoal, (TacletApp) app, pos, hgoal.proof()
					.getActiveStrategy(), true);
			if (app == null || !app.complete()) {
				throw new IllegalArgumentException(
						"incompletable rule application\n" + app);
			}
		}
		// apply app on hypothetic proof
		apply(hgoal, app);
		Debug.out("HYPO: after first application");
		goal.proof().getServices().setNamespaces(copy.copy());
		System.out
				.println("We might have changed the original namespace... resetting"); // FIXME
		// use
		// asserting
		// below
		// instead
		// assert goal.proof().getServices().getNamespaces().equalContent(copy)
		// : "no change in original namespaces\n"
		// + printDelta(copy, goal.proof().getServices().getNamespaces());
		// continue hypothetic proof to see if it closes/has
		// counterexamples
		try {
			return provable(hypothetic, maxsteps, timeout);
		} finally {
			boolean assertions = false;
			assert assertions = true;
			if (assertions
					&& !goal.proof().getServices().getNamespaces()
							.equalContent(copy)) {
				System.out.println("WARNING:  change in original namespaces"); // +
				// printDelta
				// (
				// copy
				// ,
				// goal
				// .
				// proof
				// (
				// )
				// .
				// getServices
				// (
				// )
				// .
				// getNamespaces
				// (
				// )
				// )
				// ;
				// TODO undo this HACK
				// goal.proof().getServices().setNamespaces(copy);
			}
			// assert
			// goal.proof().getServices().getNamespaces().equalContent(copy) :
			// "no change in original namespaces\n" + printDelta(copy,
			// goal.proof().getServices().getNamespaces());
		}
	}

	// background proving engine

	private static Collection<HypothesizeThread> running = new LinkedHashSet<HypothesizeThread>(
			10);

	private static Map<List<Sequent>, HypotheticalProvability> provableRuleCache = new WeakHashMap<List<Sequent>, HypotheticalProvability>();

	/**
	 * Determines whether the given proof can finally be closed/disproven/times
	 * out.
	 * 
	 * @param hypothesis
	 *            the beginning of the hypothetical proof to continue.
	 * @param timeout
	 *            the maximum time how long the proof is attempted to close.
	 * @return Result of proving hypothesis.
	 */
	private static HypotheticalProvability provable(Proof hypothesis,
			int maxsteps, long timeout) {
		final List<Sequent> initialIndex = cacheIndex(hypothesis);
		final HypotheticalProvability cached = provableRuleCache
				.get(initialIndex);
		if (cached != null) {
			return cached;
		}
		HypothesizeThread hypothesizer = new HypothesizeThread(hypothesis,
				maxsteps, timeout);
		synchronized (running) {
			running.add(hypothesizer);
		}
		try {
			HypotheticalProvability result;
			hypothesizer.start();
			try {
				hypothesizer.join(2 * timeout);
				result = hypothesizer.getResult();
				hypothesizer.giveUp = true;
				switch (result) {
				case TIMEOUT:
				case UNKNOWN:
					break;
				case PROVABLE:
				case DISPROVABLE:
					provableRuleCache.put(initialIndex, result);
					break;
				case ERROR:
					break;
				default:
					throw new AssertionError("all cases known " + result);
				}
				return result;
			} catch (InterruptedException e) {
				result = HypotheticalProvability.TIMEOUT;
				try {
					hypothesizer.giveUp = true;
					MathSolverManager.getCurrentQuantifierEliminator()
							.abortCalculation();
				} catch (RemoteException f) {
					hypothesizer.interrupt();
				}
			}
			while (hypothesizer.isAlive()) {
				try {
					hypothesizer.giveUp = true;
					hypothesizer.interrupt();
					MathSolverManager.getCurrentQuantifierEliminator()
							.abortCalculation();
					MathSolverManager.getCurrentODESolver().abortCalculation();
				} catch (RemoteException f) {
					hypothesizer.interrupt();
				}
			}
			return result;
		} finally {
			if (hypothesizer != null && hypothesizer.isAlive()) {
				while (hypothesizer.isAlive()) {
					hypothesizer.giveUp = true;
					hypothesizer.interrupt();
					try {
						MathSolverManager.getCurrentQuantifierEliminator()
								.abortCalculation();
						MathSolverManager.getCurrentODESolver()
								.abortCalculation();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				synchronized (running) {
					// @internal this synch is a bit pessimistic
					running.remove(hypothesizer);
				}
				hypothesizer = null;
			}
		}
	}

	private static List<Sequent> cacheIndex(Proof hypothesis) {
		// @todo could switch to Set representations of sequents and sequent
		// sets to make independent of order
		List<Sequent> open = new ArrayList<Sequent>(hypothesis.openGoals()
				.size() + 1);
		for (Iterator<Goal> i = hypothesis.openGoals().iterator(); i.hasNext();) {
			open.add(i.next().sequent());
		}
		return open;
	}

	/**
	 * Attempts to stop all running HypotheticalProvabilityFeature threads.
	 */
	public static void stop() {
		Collection<HypothesizeThread> copy;
		synchronized (running) {
			copy = new LinkedHashSet<HypothesizeThread>(running);
		}
		for (HypothesizeThread hypothesizer : copy) {
			if (hypothesizer != null && hypothesizer.isAlive()) {
				hypothesizer.giveUp = true;
				hypothesizer.interrupt();
				running.remove(hypothesizer);
			}
		}
		try {
			if (MathSolverManager.isQuantifierEliminatorSet()) {
				MathSolverManager.getCurrentQuantifierEliminator()
						.abortCalculation();
			}
		} catch (RemoteException ignore) {
		}
	}

	// rule application engines

	/**
	 * @see Goal#apply without events, which would cause synchronization
	 *      blocking
	 */
	private static ImmutableList<Goal> apply(Goal goal, RuleApp p_ruleApp) {
		// System.err.println(Thread.currentThread());
		assert !goal.node().isClosed() : "cannot apply rule " + p_ruleApp
				+ " to closed goal " + goal + " which has been closed by "
				+ goal.appliedRuleApps().head().rule().name();

		final Proof proof = goal.proof();

		// TODO: this is maybe not the right place for this check
		// FIXME: proof.mgt().ruleApplicable(p_ruleApp, goal) is not available
		// anymore
		// assert proof.mgt().ruleApplicable(p_ruleApp, goal) :
		// "Someone tried to apply the rule "
		// + p_ruleApp + " that is not justified";

		final NodeChangeJournal journal = new NodeChangeJournal(proof, goal);
		// addGoalListener(journal);

		final RuleApp ruleApp = completeRuleApp(goal, p_ruleApp);

		final ImmutableList<Goal> goalList = ruleApp.execute(goal, proof.getServices());

		if (goalList == null) {
			System.err.println("WARNING: resulting goals after applying "
					+ p_ruleApp.rule().name() + " is null");
			// this happens for the simplify decision procedure
			// we do nothing in this case
		} else if (goalList.isEmpty()) {
			proof.closeGoal(goal, ruleApp.constraint());
			assert !proof.openGoals().contains(goal) : "closing a goals makes it not-open "
					+ goal;
		} else {
			proof.replace(goal, goalList);
			if (ruleApp instanceof TacletApp
					&& ((TacletApp) ruleApp).taclet().closeGoal()) {
				// the first new goal is the one to be closed
				proof.closeGoal(goalList.head(), ruleApp.constraint());
				assert !proof.openGoals().contains(goal) : "closing a goals makes it not-open "
						+ goal;
			}
		}

		final RuleAppInfo ruleAppInfo = journal.getRuleAppInfo(p_ruleApp);

		/*
		 * disable events if ( goalList != null ) fireRuleApplied( new
		 * ProofEvent ( proof, ruleAppInfo ) );
		 */
		return goalList;
	}

	/**
	 * Create a <code>RuleApp</code> that is suitable to be applied or
	 * <code>null</code>.
	 * 
	 * @param forced
	 *            whether to force the rule application for strategic tests
	 *            (i.e., strategic approval failures should not have any
	 *            effect).
	 * @see TacletAppContainer#completeRuleApp
	 */
	static RuleApp completeRuleApp(Goal p_goal, TacletApp app,
			PosInOccurrence pio, Strategy strategy, boolean forced) {
		// if ( !isStillApplicable ( p_goal ) )
		// return null;
		//    
		// if ( !ifFormulasStillValid ( p_goal ) )
		// return null;

		if (!forced && !strategy.isApprovedApp(app, pio, p_goal))
			return null;

		if (pio != null) {
			app = app.setPosInOccurrence(pio);
			if (app == null)
				return null;
		}

		if (!app.complete())
			app = app.tryToInstantiate(p_goal, p_goal.proof().getServices());

		return app;
	}

	/**
	 * make Taclet instantions complete with regard to metavariables and skolem
	 * functions
	 * 
	 * @see Goal#completeRuleApp
	 */
	private static RuleApp completeRuleApp(Goal goal, RuleApp ruleApp) {
		final Proof proof = goal.proof();
		if (ruleApp instanceof TacletApp) {
			TacletApp tacletApp = (TacletApp) ruleApp;

			tacletApp = tacletApp.instantiateWithMV(goal);

			ruleApp = tacletApp.createSkolemFunctions(proof.getNamespaces()
					.functions(), proof.getServices());
		}
		return ruleApp;
	}

	/**
	 * Thread performing a hypothetical proof.
	 * 
	 * @author ap
	 * 
	 */
	private static class HypothesizeThread extends Thread {

		private final long timeout;

		private KeYMediator mediator;

		private Proof hypothesis;

		private IGoalChooser goalChooser;

		private HypotheticalProvability result;

		private int blockedGoals = 0;
		private int initialBlockedGoalSize = -1;

		/**
		 * giveUp being set to true notifies that this thread should stop
		 */
		volatile boolean giveUp = false;

		public HypothesizeThread(Proof hypothesis, int maxsteps, long timeout) {
			super("hypothetical prover");
			setDaemon(true);
			this.result = HypotheticalProvability.UNKNOWN;
			this.timeout = timeout;
			this.hypothesis = hypothesis;
			this.mediator = Main.getInstance().mediator();
			this.goalChooser = mediator.getProfile()
					.getSelectedGoalChooserBuilder().create();
			this.goalChooser.init(hypothesis, hypothesis.openGoals());
			this.maxApplications = maxsteps;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		/* @Override */
		public void run() {
			try {
				this.result = proofEngine();
				Debug.out("HYPO: regular end " + getResult());
			} catch (NullPointerException e) {
				result = HypotheticalProvability.ERROR;
				System.out.println("Exception during hypothetic proof " + e);
				e.printStackTrace();
				throw e;
			} catch (RuntimeException e) {
				result = HypotheticalProvability.ERROR;
				System.out.println("Exception during hypothetic proof " + e);
				e.printStackTrace();
				throw e;
			} catch (AssertionError e) {
				result = HypotheticalProvability.ERROR;
				System.out.println("Error during hypothetic proof " + e);
				e.printStackTrace();
				throw e;
			} finally {
				Debug.out("HYPO: finished " + getResult());
			}
		}

		/**
		 * @return the result
		 */
		public HypotheticalProvability getResult() {
			return result;
		}

		/**
		 * applies rules that are chosen by the active strategy
		 * 
		 * @return true iff a rule has been applied, false otherwise
		 * @see ProofStarter#applyAutomaticRule()
		 * @see ApplyStrategy#applyAutomaticRule()
		 */
		private boolean applyAutomaticRule() throws InterruptedException {
			// Look for the strategy ...
			RuleApp app = null;
			Goal g = null;
			// ReuseListener rl = mediator.getReuseListener();
			// @todo could also use reuses as in ApplyStrategy rather than just
			// producing them
			while (!maxRuleApplicationOrTimeoutExceeded()
					&& ((g = goalChooser.getNextGoal()) != null || blockedGoals > 0)) {
				boolean first = true;
				if (g == null) {
					first = false;
					if (blockedGoals == initialBlockedGoalSize) {
						// stop as we have checked all blocked goals again
						// and
						// we could close none of them
						return false;
					}
					// new goalchooser because we want to reevaluate the
					// open
					// goals
					for (Goal h : hypothesis.openGoals()) {
						h.clearAndDetachRuleAppIndex();
						h.setGoalStrategy(Main.getInstance().mediator()
								.getProfile().getDefaultStrategyFactory()
								.create(hypothesis, null));
					}
					this.goalChooser = mediator.getProfile()
							.getSelectedGoalChooserBuilder().create();
					this.goalChooser.init(hypothesis, hypothesis.openGoals());
					initialBlockedGoalSize = blockedGoals;
					blockedGoals = 0;
					g = goalChooser.getNextGoal();
					if (g == null) {
						return false;
					}
				}
				app = g.getRuleAppManager().next();

				if (app == null) {
					// cannot find applicable and affordable rules
					if (g.getStatus() == GoalStatus.BLOCKING) {
						goalChooser.removeGoal(g);
						blockedGoals++;
					} else {
						if (STOP_EARLY) {
							// ignore goal
							goalChooser.removeGoal(g);
						} else {
							// give up the proof and stop early presuming it
							// can't get better by working on other goals
							// TODO except for
							// EliminateExistentialQuantifierRule
							return false;
						}
					}
				} else {
					break;
				}
				if (Thread.interrupted())
					throw new InterruptedException();
			}
			if (maxRuleApplicationOrTimeoutExceeded()) {
				return false;
			}
			assert g != null || app == null : "no chosen goal implies no rule app";
			if (app == null) {
				return false;
			}
			ImmutableList<Goal> subgoals;
			try {
				subgoals = apply(g, app);
			} catch (IllegalStateException ex) {
				if (ex.getCause() instanceof IncompleteEvaluationException) {
					// application of rule aborted
					// let's just hence pretend that there was a ruleapp and do
					// nothing
					// @todo is this okay?
					return true;
				} else if (ex.getCause() instanceof UnsolveableException) {
					// @todo tell strategy never to try this bad choice again
					throw ex;
				} else {
					throw ex;
				}
			}
			// keep track of and promote reuses
			// deactivated as not yet usable by the main prover strategy
			/*
			 * rl.removeRPConsumedGoal(g); rl.addRPOldMarkersNewGoals(subgoals);
			 */

			if (g != null) {
				goalChooser.updateGoalList(g.node(), subgoals);
			}
			return true;
		}

		private long time;

		private int countApplied;

		private int maxApplications;

		/**
		 * returns if the maximum number of rule applications or the timeout has
		 * been reached
		 * 
		 * @return true if automatic rule application shall be stopped because
		 *         the maximal number of rules have been applied or the time out
		 *         has been reached
		 */
		private boolean maxRuleApplicationOrTimeoutExceeded() {
			return giveUp || Thread.interrupted()
					|| countApplied >= maxApplications || timeout >= 0 ? System
					.currentTimeMillis()
					- time >= timeout : false;
		}

		/**
		 * applies rules until this is no longer possible or the thread is
		 * interrupted.
		 */
		HypotheticalProvability proofEngine() {
			countApplied = 0;
			time = System.currentTimeMillis();
			try {
				Debug.out("Strategy started.");
				while (!maxRuleApplicationOrTimeoutExceeded()) {
					Debug.out("HYPO: goals " + hypothesis.openGoals().size());
					if (!applyAutomaticRule()) {
						// no more rules applicable
						if (hypothesis.openGoals().isEmpty()) {
							return HypotheticalProvability.PROVABLE;
						} else {
							// if counterexample
							// TODO make ALL counterexamples known here,
							// including FindTransitionTest counterexamples,
							// otherwise, they just count as non-cached
							// unknowns.
							if (hypothesis.getActiveStrategy() instanceof DLStrategy
									&& ((DLStrategy) hypothesis
											.getActiveStrategy())
											.foundCounterexample()) {
								return HypotheticalProvability.DISPROVABLE;
							} else {
								// no more rules applicable on
								// hypothesis.openGoals()
								return HypotheticalProvability.UNKNOWN;
							}
						}
					}
					countApplied++;
					if (Thread.interrupted())
						throw new InterruptedException();
				}
				return HypotheticalProvability.TIMEOUT;
			} catch (InterruptedException e) {
				return HypotheticalProvability.TIMEOUT;
			} finally {
				time = System.currentTimeMillis() - time;
				Debug.out("Strategy stopped.");
				Debug.out("Applied ", countApplied);
				Debug.out("Time elapsed: ", time);
			}
		}
	}

	// debug helper
	/**
	 * prints the difference of two namespacesets
	 */
	private static boolean printDelta(NamespaceSet a, NamespaceSet b) {
		NamespaceSet c = new NamespaceSet();
		System.out.println("Sort delta");
		printDelta(a.sorts(), b.sorts());
		System.out.println("RuleSet delta");
		printDelta(a.ruleSets(), b.ruleSets());
		System.out.println("Function delta");
		printDelta(a.functions(), b.functions());
		System.out.println("Variables delta");
		printDelta(a.variables(), b.variables());
		System.out.println("ProgramVariables delta");
		printDelta(a.programVariables(), b.programVariables());
		System.out.println("Choices delta");
		printDelta(a.choices(), b.choices());
		return true;
	}

	private static void printDelta(Namespace a, Namespace b) {
		for (Iterator<Named> it = a.elements().iterator(); it.hasNext();) {
			Named n = it.next();
			if (b.lookup(n.name()) == null) {
				System.out.println("  A\\B: " + n);
			}
		}
		for (Iterator<Named> it = b.elements().iterator(); it.hasNext();) {
			Named n = it.next();
			if (a.lookup(n.name()) == null) {
				System.out.println("  B\\A: " + n);
			}
		}
	}

}
