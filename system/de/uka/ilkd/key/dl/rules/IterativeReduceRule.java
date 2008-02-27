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
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.formulatools.SkolemfunctionTracker;
import de.uka.ilkd.key.dl.formulatools.TermRewriter;
import de.uka.ilkd.key.dl.formulatools.TermTools;
import de.uka.ilkd.key.dl.formulatools.VariableOrderCreator;
import de.uka.ilkd.key.dl.formulatools.TermRewriter.Match;
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
		boolean automde = Main.getInstance().mediator().autoMode();
		VariableOrder order = VariableOrderCreator.getVariableOrder(goal
				.sequent().iterator());
		List<Term> initAnte = createOrderedList(order, goal.sequent()
				.antecedent().iterator());
		List<Term> initSucc = createOrderedList(order, goal.sequent()
				.succedent().iterator());
		Queue<Term> ante = new LinkedList<Term>();
		Queue<Term> succ = new LinkedList<Term>();
		List<Term> usedAnte = new ArrayList<Term>();
		List<Term> usedSucc = new ArrayList<Term>();
		List<Term> useForFindInstance = new LinkedList<Term>();
		List<Term> useForReduce = new LinkedList<Term>();
		List<List<String>> useReduceVariables = new LinkedList<List<String>>();
		int counter = 0;
		while (true) {
			if (automde && !Main.getInstance().mediator().autoMode()) {
				return null;
			}
			if (ante.isEmpty() && succ.isEmpty()) {
				timeout *= 2;
				ante.clear();
				ante.addAll(initAnte);
				succ.clear();
				succ.addAll(initSucc);
				usedAnte.clear();
				usedSucc.clear();
			}

			while (!ante.isEmpty() || !succ.isEmpty()
					|| !useForFindInstance.isEmpty()) {

				try {
					Term result = null;
					Term reduce = null;
					List<String> variables = null;
					if (!ante.isEmpty() || !succ.isEmpty()) {
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
						result = TermBuilder.DF.imp(and, or);

						useForFindInstance.add(result);
					} else {
						result = useForFindInstance.get(counter);
						reduce = useForReduce.get(counter);
						variables = useReduceVariables.get(counter);
						counter = ++counter % useForFindInstance.size();
					}
					System.out.println("Testing for CE for " + timeout);// XXX
					String findInstance = MathSolverManager
							.getCurrentCounterExampleGenerator().findInstance(
									TermBuilder.DF.not(result), timeout);
					if (findInstance.equals("") || findInstance.startsWith("$")) {
						System.out.println("Reducing for " + timeout);// XXX
						if (reduce == null) {
							List<Term> skolem = new LinkedList<Term>();
							final Set<Term> skolemSym = new HashSet<Term>();
							result.execPreOrder(new Visitor() {

								@Override
								public void visit(Term visited) {
									if (visited.op() instanceof RigidFunction
											&& ((RigidFunction) visited.op())
													.isSkolem()) {
										skolemSym.add(visited);
									}
								}

							});
							skolem.addAll(SkolemfunctionTracker.INSTANCE
									.getOrderedList(skolemSym));

							variables = new ArrayList<String>();
							Set<Match> matches = new HashSet<Match>();
							List<LogicVariable> vars = new ArrayList<LogicVariable>();
							for (Term sk : skolem) {
								LogicVariable logicVariable = new LogicVariable(
										new Name(sk.op().name() + "$sk"), sk
												.op().sort(new Term[0]));
								vars.add(logicVariable);
								matches.add(new Match((RigidFunction) sk.op(),
										TermBuilder.DF.var(logicVariable)));
								variables.add(logicVariable.name().toString());
							}
							result = TermRewriter.replace(result, matches);
							for (QuantifiableVariable v : vars) {
								result = TermBuilder.DF.all(v, result);
							}
							if (DLOptionBean.INSTANCE.isSimplifyBeforeReduce()) {
								result = MathSolverManager
										.getCurrentSimplifier().simplify(
												result,
												services.getNamespaces());
							}
							reduce = result;
							useForReduce.add(reduce);
							useReduceVariables.add(variables);
						}
						if (ante.isEmpty() && succ.isEmpty()
								&& useForReduce.size() == 1) {
							reduce = MathSolverManager
									.getCurrentQuantifierEliminator()
									.reduce(
											reduce,
											variables,
											new ArrayList<PairOfTermAndQuantifierType>(),
											services.getNamespaces());
						} else {
							reduce = MathSolverManager
									.getCurrentQuantifierEliminator()
									.reduce(
											reduce,
											variables,
											new ArrayList<PairOfTermAndQuantifierType>(),
											services.getNamespaces(), timeout);
						}
						if (DLOptionBean.INSTANCE.isSimplifyAfterReduce()) {
							result = MathSolverManager.getCurrentSimplifier()
									.simplify(result, services.getNamespaces());
						}
						if (reduce.equals(TermBuilder.DF.tt())) {
							return goal.split(0);
						} else if (ante.isEmpty() && succ.isEmpty()) {
							throw new IllegalStateException(
									"Dont know what to do, reduce returned: "
											+ reduce);
						} else {
							useForFindInstance.remove(counter);
							useForReduce.remove(counter);
							useReduceVariables.remove(counter);
						}
					} else if (ante.isEmpty() && succ.isEmpty()) {
						throw new IllegalStateException(
								"Dont know what to do, counterexample for: "
										+ result + " is " + findInstance);
					} else {
						useForFindInstance.remove(counter);
					}
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
