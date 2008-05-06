/**
 * 
 */
package de.uka.ilkd.key.dl.rules;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.arithmetics.exceptions.IncompleteEvaluationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.formulatools.SkolemfunctionTracker;
import de.uka.ilkd.key.dl.formulatools.TermRewriter;
import de.uka.ilkd.key.dl.formulatools.TermTools;
import de.uka.ilkd.key.dl.formulatools.VariableOrderCreator;
import de.uka.ilkd.key.dl.formulatools.TermRewriter.Match;
import de.uka.ilkd.key.dl.formulatools.TermTools.PairOfTermAndVariableList;
import de.uka.ilkd.key.dl.formulatools.VariableOrderCreator.VariableOrder;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.ListOfGoal;
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

		public QueryTriple(Term and, Term or) {
			this.and = and;
			this.or = or;
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
				PairOfTermAndVariableList pair = TermTools.quantifyAllSkolemSymbols(result);
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
	 * @see de.uka.ilkd.key.rule.BuiltInRule#isApplicable(de.uka.ilkd.key.proof.Goal,
	 *      de.uka.ilkd.key.logic.PosInOccurrence,
	 *      de.uka.ilkd.key.logic.Constraint)
	 */
	@Override
	public boolean isApplicable(Goal goal, PosInOccurrence pio,
			Constraint userConstraint) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#apply(de.uka.ilkd.key.proof.Goal,
	 *      de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
	 */
	@Override
	public ListOfGoal apply(Goal goal, Services services, RuleApp ruleApp) {
		long timeout = 2000;
		final boolean automode = Main.getInstance().mediator().autoMode();
		final VariableOrder order = VariableOrderCreator.getVariableOrder(goal
				.sequent().iterator());
		// IDEA: initial sequent is successively moved from ante/succ to usedAnte/usedSucc
		// parts of initAnte/initSucc that still make sense to be added
		Queue<Term> ante = new LinkedList<Term>();
		Queue<Term> succ = new LinkedList<Term>();
		// parts of ante/succ that are used in the current frontier
		List<Term> usedAnte = new ArrayList<Term>(createOrderedList(order, goal.sequent()
				.antecedent().iterator()));
		List<Term> usedSucc = new ArrayList<Term>(createOrderedList(order, goal.sequent()
				.succedent().iterator()));
		// iteratively built construction cache for the set of all queries
		List<QueryTriple> queryCache = new LinkedList<QueryTriple>();
		
		// current frontier of re-tested queries
		Queue<QueryTriple> currentQueryCache = new LinkedList<QueryTriple>();
		
		while (true) {
			if (automode && !Main.getInstance().mediator().autoMode()) {
				// automode stopped
				return null;
			}
			timeout *= 2;

			currentQueryCache.clear();
			currentQueryCache.addAll(queryCache);
			
			// loop until all added or all remaining cached items have been visited again
			while (!ante.isEmpty() || !succ.isEmpty() || !currentQueryCache.isEmpty()) {
				// during first sweep, only repeat with current timeout as long as there are further alternatives
				// further sweeps of the algorithm re-check the known alternatives with larger timeouts
				try {
					QueryTriple currentItem;
					
					if (!ante.isEmpty() || !succ.isEmpty()) {
						// first sweep of the algorithm keeps adding alternatives until all alternatives are in queryCache
						if (!ante.isEmpty() && !succ.isEmpty()) {
							if (order.compare(ante.peek(), succ.peek()) <= 0) {
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
					System.out.println("Testing for CE for " + timeout);// XXX
					String findInstance = "";
					try {
						findInstance = MathSolverManager
								.getCurrentCounterExampleGenerator()
								.findInstance(TermBuilder.DF.not(currentItem.getUseForFindInstance()),
										timeout);
					} catch (IncompleteEvaluationException e) {
						// timeout
					}
					if (findInstance.equals("") || findInstance.startsWith("$")) {
						// No CEX found
						System.out.println("Reducing for " + timeout);// XXX
					    Term reduce = currentItem.getUseForReduce(services);
						List<String> variables = currentItem
									.getReduceVariables(services);
						if (ante.isEmpty() && succ.isEmpty()
								&& queryCache.size() == 1) {
							// as a last resort if we run out of alternatives: this is the last item. do not timeout the reduce
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
						} else if (ante.isEmpty() && succ.isEmpty() && currentQueryCache.isEmpty()) {
							// maximum sequent is always last in the current query cache (likewise during first construction sweep)
							//TODO should return result
							throw new IllegalStateException(
									"Dont know what to do, reduce returned: "
											+ reduce);
						} else {
							// we did not find a useful result, but we still got
							// formulas we could add. therefore we remove the
							// current formula
							queryCache.remove(currentItem);
						}
					} else if (ante.isEmpty() && succ.isEmpty()  && currentQueryCache.isEmpty()) {
						// we have a counter example for maximum sequent
						throw new IllegalStateException(
								"Dont know what to do, counterexample for: "
										+ currentItem.getUseForFindInstance() + " is " + findInstance);
					} else {
						// we have a counter example
						queryCache.remove(currentItem);
					}
				} catch (IncompleteEvaluationException e) {
					// timeout while performing query
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
	private List<Term> createOrderedList(VariableOrder order,
			IteratorOfConstrainedFormula iterator) {
		List<Term> ante = new ArrayList<Term>();
		while (iterator.hasNext()) {
			Term next = iterator.next().formula();
			ante.add(next);
		}
		Collections.sort(ante, order);
		return ante;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#displayName()
	 */
	@Override
	public String displayName() {
		return "IterativeReduce";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#name()
	 */
	@Override
	public Name name() {
		return new Name("IterativeReduce");
	}

}
