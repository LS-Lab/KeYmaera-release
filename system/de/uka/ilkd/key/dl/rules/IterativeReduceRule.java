/**
 * 
 */
package de.uka.ilkd.key.dl.rules;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.IteratorOfTerm;
import de.uka.ilkd.key.logic.ListOfTerm;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.SLListOfTerm;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.pp.LogicPrinter;
import de.uka.ilkd.key.pp.NotationInfo;
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
		VariableOrder order = VariableOrderCreator.getVariableOrder(goal
				.sequent().iterator());
		ListOfTerm initAnte = createOrderedList(order, goal.sequent()
				.antecedent().iterator());
		ListOfTerm initSucc = createOrderedList(order, goal.sequent()
				.succedent().iterator());
		ListOfTerm ante = SLListOfTerm.EMPTY_LIST;
		ListOfTerm succ = SLListOfTerm.EMPTY_LIST;
		ListOfTerm usedAnte = SLListOfTerm.EMPTY_LIST;
		ListOfTerm usedSucc = SLListOfTerm.EMPTY_LIST;
		while (true) {
			if (ante.isEmpty() && succ.isEmpty()) {
				timeout *= 2;
				ante = initAnte;
				succ = initSucc;
				usedAnte = SLListOfTerm.EMPTY_LIST;
				usedSucc = SLListOfTerm.EMPTY_LIST;
			}

			while (!ante.isEmpty() || !succ.isEmpty()) {
				if (!ante.isEmpty() && !succ.isEmpty()) {
					if (order.compare(ante.head(), succ.head()) >= 0) {
						usedAnte = usedAnte.append(ante.head());
						ante = ante.removeFirst(ante.head());
					} else {
						usedSucc = usedSucc.append(succ.head());
						succ = succ.removeFirst(succ.head());
					}
				} else {
					if (!ante.isEmpty()) {
						usedAnte = usedAnte.append(ante.head());
						ante = ante.removeFirst(ante.head());
					} else if (!succ.isEmpty()) {
						usedSucc = usedSucc.append(succ.head());
						succ = succ.removeFirst(succ.head());
					}
				}
				Term and = TermTools.createJunctorTermNAry(TermBuilder.DF.tt(),
						Op.AND, usedAnte.iterator(), new HashSet<Term>());
				Term or = TermTools.createJunctorTermNAry(TermBuilder.DF.ff(),
						Op.OR, usedSucc.iterator(), new HashSet<Term>());
				Term result = TermBuilder.DF.imp(and, or);

				try {
					System.out.println("Testing for CE for " + timeout);// XXX
					String findInstance = MathSolverManager
							.getCurrentCounterExampleGenerator().findInstance(
									TermBuilder.DF.not(result), timeout);
					if (findInstance.equals("") || findInstance.startsWith("$")) {
						System.out.println("Reducing for " + timeout);// XXX
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

						List<String> variables = new ArrayList<String>();
						Set<Match> matches = new HashSet<Match>();
						List<LogicVariable> vars = new ArrayList<LogicVariable>();
						for (Term sk : skolem) {
							LogicVariable logicVariable = new LogicVariable(
									new Name(sk.op().name() + "$sk"), sk.op()
											.sort(new Term[0]));
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
							result = MathSolverManager.getCurrentSimplifier()
									.simplify(result, services.getNamespaces());
						}
						Term reduce = MathSolverManager
								.getCurrentQuantifierEliminator()
								.reduce(
										result,
										variables,
										new ArrayList<PairOfTermAndQuantifierType>(),
										services.getNamespaces(), timeout);
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
						}
					} else if (ante.isEmpty() && succ.isEmpty()) {
						throw new IllegalStateException(
								"Dont know what to do, counterexample for: "
										+ result + " is " + findInstance);
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
	private ListOfTerm createOrderedList(VariableOrder order,
			IteratorOfConstrainedFormula iterator) {
		ListOfTerm ante = SLListOfTerm.EMPTY_LIST;
		while (iterator.hasNext()) {
			IteratorOfTerm aIt = ante.iterator();
			Term next = iterator.next().formula();
			boolean inserted = false;
			while (aIt.hasNext()) {
				Term next2 = aIt.next();
				if (order.compare(next, next2) >= 0) {
					ante = ante.prepend(next);
					inserted = true;
					break;
				}
			}
			if (!inserted) {
				ante = ante.append(next);
			}
		}
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
