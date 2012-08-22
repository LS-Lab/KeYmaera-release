/**
 * 
 */
package de.uka.ilkd.key.dl.rules;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import orbital.algorithm.Combinatorical;
import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.arithmetics.exceptions.IncompleteEvaluationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.formulatools.LexicographicalOrder;
import de.uka.ilkd.key.dl.formulatools.TermTools;
import de.uka.ilkd.key.dl.formulatools.TermTools.PairOfTermAndVariableList;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.RuleFilter;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * @author jdq
 * 
 */
public class IterativeReduceRule implements BuiltInRule, RuleFilter {

	private static class QueryTriple {

		private Term useForFindInstance;
		private Term useForReduce;
		private List<String> variables;

		private Term and;
		private Term or;
		private int count = -1;

		public QueryTriple(Term and, Term or) {
			this.and = and;
			this.or = or;
		}

		/**
		 * @param and2
		 * @param or2
		 * @param i
		 */
		public QueryTriple(Term and2, Term or2, int i) {
			this(and2, or2);
			count = i;
		}

		/**
		 * @return the useForFindInstance
		 */
		public Term getUseForFindInstance() {
			if (useForFindInstance == null) {
				useForFindInstance = TermBuilder.DF.imp(and, or);
			}
			return useForFindInstance;
		}

		/**
		 * @return the useForReduce
		 * @throws SolverException
		 * @throws RemoteException
		 */
		public Term getUseForReduce(Services services) throws RemoteException,
				SolverException {
			if (useForReduce == null) {
				Term result = getUseForFindInstance();
				PairOfTermAndVariableList pair = TermTools
						.quantifyAllSkolemSymbols(result);
				result = pair.getT();
				variables = pair.getVariables();
				if (DLOptionBean.INSTANCE.isSimplifyBeforeReduce()) {
					result = MathSolverManager.getCurrentSimplifier().simplify(
							result, services.getNamespaces());
				}
				useForReduce = result;
			}
			return useForReduce;
		}

		/**
		 * @return the reduceVariables
		 * @throws SolverException
		 * @throws RemoteException
		 */
		public List<String> getReduceVariables(Services services)
				throws RemoteException, SolverException {
			getUseForReduce(services); // update cache
			return variables;
		}
	}

	public static final IterativeReduceRule INSTANCE = new IterativeReduceRule();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.proof.RuleFilter#filter(de.uka.ilkd.key.rule.Rule)
	 */
	public boolean filter(Rule rule) {
		return rule instanceof IterativeReduceRule;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.rule.BuiltInRule#isApplicable(de.uka.ilkd.key.proof.Goal,
	 * de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.logic.Constraint)
	 */
	/*@Override*/
	public boolean isApplicable(Goal goal, PosInOccurrence pio,
			Constraint userConstraint) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#apply(de.uka.ilkd.key.proof.Goal,
	 * de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
	 */
	/*@Override*/
	public ImmutableList<Goal> apply(Goal goal, Services services, RuleApp ruleApp) {
		long timeout = 2000;
		final boolean automode = Main.getInstance().mediator().autoMode();

		Queue<Term> ante;
		Queue<Term> succ;
		if (DLOptionBean.INSTANCE.isUsePowersetIterativeReduce()) {
			ante = new LinkedList<Term>(createList(goal.sequent().antecedent()
					.iterator()));
			succ = new LinkedList<Term>(createList(goal.sequent().succedent()
					.iterator()));
		} else {
			// IDEA: initial sequent is successively moved from ante/succ to
			// usedAnte/usedSucc
			// parts of initAnte/initSucc that still make sense to be added
			ante = LexicographicalOrder.getOrder(createList(goal.sequent()
					.antecedent().iterator()), new HashSet<Term>());
			succ = LexicographicalOrder.getOrder(createList(goal.sequent()
					.succedent().iterator()), new HashSet<Term>());
		}

		System.out.println("Sorted seq: " + ante + " -> " + succ);// XXX
		// parts of ante/succ that are used in the current frontier
		List<Term> usedAnte = new ArrayList<Term>();
		List<Term> usedSucc = new ArrayList<Term>();
		// iteratively built construction cache for the set of all queries
		List<QueryTriple> queryCache = new LinkedList<QueryTriple>();

		// current frontier of re-tested queries
		Queue<QueryTriple> currentQueryCache = new LinkedList<QueryTriple>();
		Set<Term> currentVariables = new HashSet<Term>();

		if (DLOptionBean.INSTANCE.isUsePowersetIterativeReduce()) {
			Term[] anteArray = ante.toArray(new Term[ante.size()]);
			Term[] succArray = succ.toArray(new Term[succ.size()]);
			int availableFormulas = ante.size() + succ.size();
			double minimumFormulas = availableFormulas
					* (double) (((double) DLOptionBean.INSTANCE
							.getPercentOfPowersetForReduce()) / 100d);
			List<Combinatorical> combinations = new ArrayList<Combinatorical>();
			for (int i = availableFormulas; i > minimumFormulas; i--) {
				combinations.add(Combinatorical.getCombinations(i,
						availableFormulas, false));
			}
			for (Combinatorical com : combinations) {
				while (com.hasNext()) {
					Term and = TermBuilder.DF.tt();
					Term or = TermBuilder.DF.ff();
					int[] curComb = com.next();
					for (int pos : curComb) {
						if (pos < ante.size()) {
							and = TermBuilder.DF.and(and, anteArray[pos]);
						} else {
							or = TermBuilder.DF.or(or, succArray[pos
									- ante.size()]);
						}
					}
					queryCache.add(new QueryTriple(and, or));
				}
			}
			ante.clear();
			succ.clear();
			System.out.println("QueryCache size is " + queryCache.size());// XXX
		}

		while (true) {
			if (automode && !Main.getInstance().mediator().autoMode()) {
				// automode stopped
				return null;
			}
			timeout *= 2;

			currentQueryCache.clear();
			currentQueryCache.addAll(queryCache);
			if (ante.isEmpty() && succ.isEmpty() && currentQueryCache.isEmpty()) {
				System.out.println("There is nothing we can do anymore :/");// XXX
				return null;
			}
			// loop until all added or all remaining cached items have been
			// visited again

			while (!ante.isEmpty() || !succ.isEmpty()
					|| !currentQueryCache.isEmpty()) {
				// during first sweep, only repeat with current timeout as long
				// as there are further alternatives
				// further sweeps of the algorithm re-check the known
				// alternatives with larger timeouts
				try {
					if (automode && !Main.getInstance().mediator().autoMode()) {
						// automode stopped
						return null;
					}
					QueryTriple currentItem;

					if (!ante.isEmpty() || !succ.isEmpty()) {
						// first sweep of the algorithm keeps adding
						// alternatives until all alternatives are in queryCache
						if (!ante.isEmpty() && !succ.isEmpty()) {
							Set<Term> next = new HashSet<Term>();
							next.add(ante.peek());
							next.add(succ.peek());
							Queue<Term> order = LexicographicalOrder.getOrder(
									next, currentVariables);
							if (order.peek() == ante.peek()) {
								usedAnte.add(ante.poll());
							} else {
								usedSucc.add(succ.poll());
							}
						} else {
							if (!ante.isEmpty()) {
								usedAnte.add(ante.poll());
							} else if (!succ.isEmpty()) {
								usedSucc.add(succ.poll());
							}
						}

						Term and = TermTools.createJunctorTermNAry(
								TermBuilder.DF.tt(), Op.AND, usedAnte
										.iterator(), new HashSet<Term>());
						Term or = TermTools.createJunctorTermNAry(
								TermBuilder.DF.ff(), Op.OR,
								usedSucc.iterator(), new HashSet<Term>());

						currentItem = new QueryTriple(and, or);

						queryCache.add(currentItem);
					} else {
						currentItem = currentQueryCache.poll();
					}
					// System.out.println("Testing for CE for " + timeout /
					// 2);// XXX
					String findInstance = "";
					try {
						findInstance = MathSolverManager
								.getCurrentCounterExampleGenerator()
								.findInstance(
										TermBuilder.DF.not(currentItem
												.getUseForFindInstance()),
										timeout / 2, services);
					} catch (IncompleteEvaluationException e) {
						// timeout
					}
					if (findInstance.equals("") || findInstance.startsWith("$")) {
						// No CEX found
						// System.out.println("Reducing for " + timeout);// XXX
						Term reduce = currentItem.getUseForReduce(services);
						List<String> variables = currentItem
								.getReduceVariables(services);
						if (ante.isEmpty() && succ.isEmpty()
								&& queryCache.size() == 1) {
							// as a last resort if we run out of alternatives:
							// this is the last item. do not timeout the reduce
							reduce = MathSolverManager
									.getCurrentQuantifierEliminator()
									.reduce(
											reduce,
											variables,
											new ArrayList<PairOfTermAndQuantifierType>(),
											services.getNamespaces());
						} else {
							// reduce call with timeout
							reduce = MathSolverManager
									.getCurrentQuantifierEliminator()
									.reduce(
											reduce,
											variables,
											new ArrayList<PairOfTermAndQuantifierType>(),
											services.getNamespaces(), timeout);
						}
						if (DLOptionBean.INSTANCE.isSimplifyAfterReduce()) {
							reduce = MathSolverManager.getCurrentSimplifier()
									.simplify(reduce, services.getNamespaces());
						}
						if (reduce.equals(TermBuilder.DF.tt())) {
							return goal.split(0);
						} else if (ante.isEmpty() && succ.isEmpty()
								&& currentQueryCache.isEmpty()) {
							// maximum sequent is always last in the current
							// query cache (likewise during first construction
							// sweep)
							// TODO should return result
							throw new IllegalStateException(
									"Dont know what to do, reduce returned: "
											+ reduce);
						} else {
							// we did not find a useful result, but we still got
							// formulas we could add. therefore we remove the
							// current formula
							System.out.println("Counterexample found for "
									+ currentItem.getUseForFindInstance());// XXX
							System.out.println("It is: " + reduce);// XXX
							queryCache.remove(currentItem);
						}
					} else if (ante.isEmpty() && succ.isEmpty()
							&& currentQueryCache.isEmpty()) {
						// we have a counter example for maximum sequent
						System.out
								.println("Counterexample for the complete sequence found: "
										+ findInstance);// XXX
						throw new IllegalStateException(
								"Dont know what to do, counterexample for: "
										+ currentItem.getUseForFindInstance()
										+ " is " + findInstance);
					} else {
						// we have a counter example
						// System.out.println("Counterexample found for "
						// + currentItem.getUseForFindInstance());// XXX
						// System.out.println("Removing...");// XXX
						queryCache.remove(currentItem);
					}
				} catch (IncompleteEvaluationException e) {
					// timeout while performing query
					// System.out.println("Timeout while reducing");// XXX
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SolverException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param order
	 * @param ante
	 * @param iterator
	 * @return
	 */
	private Set<Term> createList(Iterator<ConstrainedFormula> iterator) {
		Set<Term> ante = new HashSet<Term>();
		while (iterator.hasNext()) {
			Term next = iterator.next().formula();
			if (!FOSequence.isFOOperator(next.op())
					|| FOSequence.isFunctionWithDifferentSort(next,
							RealLDT.getRealSort())) {
				ante.add(next);
			}
		}
		return ante;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#displayName()
	 */
	/*@Override*/
	public String displayName() {
		return "IterativeReduce";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#name()
	 */
	/*@Override*/
	public Name name() {
		return new Name("IterativeReduce");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/*@Override*/
	public String toString() {
		return displayName();
	}

}
