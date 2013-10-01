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
 * File created 01.02.2007
 */
package de.uka.ilkd.key.dl.rules;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableMapEntry;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.PairOfTermAndQuantifierType;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.formulatools.ContainsMetaVariableVisitor;
import de.uka.ilkd.key.dl.formulatools.ContainsMetaVariableVisitor.Result;
import de.uka.ilkd.key.dl.formulatools.SkolemfunctionTracker;
import de.uka.ilkd.key.dl.formulatools.TermRewriter;
import de.uka.ilkd.key.dl.formulatools.TermRewriter.Match;
import de.uka.ilkd.key.dl.formulatools.TermTools;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.PosInTerm;
import de.uka.ilkd.key.logic.SequentChangeInfo;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.RuleFilter;
import de.uka.ilkd.key.rule.*;
import de.uka.ilkd.key.rule.inst.InstantiationEntry;

/**
 * This class serves a rule to eliminate existential quantifiers
 * 
 * @author jdq
 * @since 27.08.2007
 * 
 */
public class EliminateExistentialQuantifierRule implements BuiltInRule,
		UnknownProgressRule, IrrevocableRule, RuleFilter, TestableBuiltInRule {

	/**
	 * A RuleApp that is used to close a branch
	 * 
	 * @author jdq
	 * @since Aug 27, 2007
	 * 
	 */
	private final class CloseRuleApp implements RuleApp {
		public boolean complete() {
			return true;
		}

		public Constraint constraint() {
			return Constraint.BOTTOM;
		}

		public ImmutableList<Goal> execute(Goal goal, Services services) {
			return goal.split(0);
		}

		public PosInOccurrence posInOccurrence() {
			// TODO Auto-generated method stub
			return null;
		}

        @Override
        public String getQuantifierEliminator() {
            return DLOptionBean.INSTANCE.getQuantifierEliminator();
        }

        public Rule rule() {
			return new Rule() {

				public ImmutableList<Goal> apply(Goal goal, Services services,
						RuleApp ruleApp) {
					// TODO Auto-generated method stub
					return null;
				}

				public String displayName() {
					return "Closed by"
							+ EliminateExistentialQuantifierRule.this
									.displayName();
				}

				public Name name() {
					return new Name("Closed by "
							+ EliminateExistentialQuantifierRule.this.name());
				}

			};
		}

		@Override
		public int getRuleAppNumber() {
			return Integer.MAX_VALUE;
		}
	}

	public static final EliminateExistentialQuantifierRule INSTANCE = new EliminateExistentialQuantifierRule();

	private boolean unsolvable;

	private boolean testMode;

	private Term resultTerm;

	/**
	 * 
	 */
	public EliminateExistentialQuantifierRule() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#apply(de.uka.ilkd.key.proof.Goal,
	 * de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
	 */
	public synchronized ImmutableList<Goal> apply(Goal goal, Services services,
			RuleApp ruleApp) {
        if(ruleApp instanceof BuiltInRuleApp) {
            ((BuiltInRuleApp) ruleApp).setQuantifierEliminator(DLOptionBean.INSTANCE.getQuantifierEliminator());
        }
		final boolean testModeActive = testMode;
		unsolvable = false;
		testMode = false;

		// Operator op = ruleApp.posInOccurrence().subTerm().op();
		List<Metavariable> variables = new ArrayList<Metavariable>();
		if (ruleApp instanceof ReduceRuleApp) {
			for (String varName : ((ReduceRuleApp) ruleApp).getVariables()) {
				Metavariable lookup = (Metavariable) services.getNamespaces()
						.variables().lookup(new Name(varName));
				variables.add(lookup);
			}
		}
		if (variables.isEmpty()) {
			final List<Metavariable> ops = new ArrayList<Metavariable>();
			Iterator<ConstrainedFormula> seqIt = goal.sequent().iterator();
			while (seqIt.hasNext()) {
				seqIt.next().formula().execPreOrder(new Visitor() {

					/* @Override */
					public void visit(Term visited) {
						if (visited.op() instanceof Metavariable) {
							if (!ops.contains(visited.op())) {
								ops.add((Metavariable) visited.op());
							}
						}
					}

				});
			}
			if (ops.isEmpty()) {
				throw new IllegalArgumentException(
						"This rule can only be applied to Metavariables. But there are none found.");
			}
			variables.addAll(ops);
		}

		// search the variable on all branches

		Set<Goal> goals = new LinkedHashSet<Goal>();
		ImmutableList<Goal> openGoals = goal.proof().openGoals();
		Iterator<Goal> goalIt = openGoals.iterator();
		while (goalIt.hasNext()) {
			Goal curGoal = goalIt.next();
			Iterator<ConstrainedFormula> it = curGoal.sequent().iterator();
			Result result = Result.DOES_NOT_CONTAIN_VAR;
			Set<NoPosTacletApp> toRemove = new java.util.HashSet<NoPosTacletApp>();
			for (NoPosTacletApp ta : curGoal.ruleAppIndex().tacletIndex()
					.allNoPosTacletApps()) {
				checkTacletApp(variables, toRemove, ta);
			}
			// remove marked hidden formulas
			for (NoPosTacletApp ta : toRemove) {
				curGoal.ruleAppIndex().removeNoPosTacletApp(ta);
			}
			while (it.hasNext()) {
				ConstrainedFormula next = it.next();
				Result res = ContainsMetaVariableVisitor
						.containsMetaVariableAndIsFO(variables, next.formula());
				if ((result == Result.CONTAINS_VAR)
						&& (res == Result.CONTAINS_VAR_BUT_CANNOT_APPLY || res == Result.DOES_NOT_CONTAIN_VAR_AND_IS_NOT_FO)) {
					result = Result.CONTAINS_VAR_BUT_CANNOT_APPLY;
					break;
				} else if (result == Result.DOES_NOT_CONTAIN_VAR_AND_IS_NOT_FO) {
					if (res == Result.CONTAINS_VAR) {
						result = Result.CONTAINS_VAR_BUT_CANNOT_APPLY;
					}
				} else if (res == Result.CONTAINS_VAR) {
					result = res;
				} else if (res == Result.CONTAINS_VAR_BUT_CANNOT_APPLY) {
					result = res;
					break;
				}
			}
			if (result == Result.CONTAINS_VAR_BUT_CANNOT_APPLY) {
				if(testModeActive) {
					// do not show a dialog here
					resultTerm = null;
					return null;
				}
				selectGoalAndShowDialog(curGoal);
				throw new IllegalStateException("All branches that contain "
						+ Arrays.toString(variables.toArray())
						+ " have to be first order");
			} else if (result == Result.CONTAINS_VAR) {
				goals.add(curGoal);
			}
		}

		// construct a conjunction of goal formulas containing implications for
		// sequents
		Term query = TermBuilder.DF.tt();
		Set<Term> skolemSymbols = new LinkedHashSet<Term>();
		Set<Term> skolemSymbolsWithDependcies = new LinkedHashSet<Term>();
		Set<Term> commonAnte = new LinkedHashSet<Term>();
		Set<Term> commonSucc = new LinkedHashSet<Term>();
		List<Goal> goalList = new ArrayList<Goal>(goals);
		Iterator<ConstrainedFormula> iterator = goalList.get(0).sequent()
				.antecedent().iterator();
		while (iterator.hasNext()) {
			commonAnte.add(iterator.next().formula());
		}
		iterator = goalList.get(0).sequent().succedent().iterator();
		while (iterator.hasNext()) {
			commonSucc.add(iterator.next().formula());
		}
		for (int i = 1; i < goals.size(); i++) {
			if (!commonAnte.isEmpty()) {
				Iterator<ConstrainedFormula> it = goalList.get(i).sequent()
						.antecedent().iterator();
				Set<Term> forms = new LinkedHashSet<Term>();
				while (it.hasNext()) {
					forms.add(it.next().formula());
				}
				Set<Term> remove = new LinkedHashSet<Term>();
				for (Term ante : commonAnte) {
					if (!forms.contains(ante)) {
						remove.add(ante);
					}
				}
				commonAnte.removeAll(remove);
			}
			if (!commonSucc.isEmpty()) {
				Iterator<ConstrainedFormula> it = goalList.get(i).sequent()
						.succedent().iterator();
				Set<Term> forms = new LinkedHashSet<Term>();
				while (it.hasNext()) {
					forms.add(it.next().formula());
				}
				Set<Term> remove = new LinkedHashSet<Term>();
				for (Term succ : commonSucc) {
					if (!forms.contains(succ)) {
						remove.add(succ);
					}
				}
				commonSucc.removeAll(remove);
			}
		}
		Term ante = TermBuilder.DF.tt();
		Set<Match> commonMatches = new LinkedHashSet<Match>();
		Map<Term, LogicVariable> commonVars = new LinkedHashMap<Term, LogicVariable>();
		Map<Term, LogicVariable> skolemSymbolWithDepsMap = new LinkedHashMap<Term, LogicVariable>();
		for (Term t : commonAnte) {
			ante = TermFactory.DEFAULT.createJunctorTermAndSimplify(Op.AND,
					ante, t);
			final Set<Term> sk = findSkolemSymbols(t);
			List<Term> orderedList = SkolemfunctionTracker.INSTANCE
					.getOrderedList(sk);
			for (Term s : orderedList) {
				if (s.arity() > 0) {
					LogicVariable logicVariable = new LogicVariable(new Name(s
							.op().name() + "$sk"), s.op().sort(new Term[0]));
					commonMatches.add(new Match((RigidFunction) s.op(),
							TermBuilder.DF.var(logicVariable)));
					skolemSymbolsWithDependcies.add(s);
					skolemSymbolWithDepsMap.put(s, logicVariable);
				} else {
					skolemSymbols.add(s);
				}
			}
		}
		Term succ = TermBuilder.DF.ff();
		for (Term t : commonSucc) {
			succ = TermFactory.DEFAULT.createJunctorTermAndSimplify(Op.OR,
					succ, t);
			final Set<Term> sk = findSkolemSymbols(t);
			List<Term> orderedList = SkolemfunctionTracker.INSTANCE
					.getOrderedList(sk);
			for (Term s : orderedList) {
				if (s.arity() > 0) {
					LogicVariable logicVariable = new LogicVariable(new Name(s
							.op().name() + "$sk"), s.op().sort(new Term[0]));
					commonMatches.add(new Match((RigidFunction) s.op(),
							TermBuilder.DF.var(logicVariable)));
					skolemSymbolsWithDependcies.add(s);
					skolemSymbolWithDepsMap.put(s, logicVariable);
					commonMatches.add(new Match((RigidFunction) s.op(),
							TermBuilder.DF.var(logicVariable)));
				} else {
					skolemSymbols.add(s);
				}
			}
		}
		for (Goal g : goals) {
			Set<Term> findSkolemSymbols = findSkolemSymbols(g.sequent()
					.iterator());

			Term antecendent = TermTools.createJunctorTermNAry(
					TermBuilder.DF.tt(), Op.AND, g.sequent().antecedent()
							.iterator(), commonAnte, true);
			Term succendent = TermTools.createJunctorTermNAry(
					TermBuilder.DF.ff(), Op.OR, g.sequent().succedent()
							.iterator(), commonSucc, true);
			Term imp = TermBuilder.DF.imp(antecendent, succendent);
			List<Term> orderedList = SkolemfunctionTracker.INSTANCE
					.getOrderedList(findSkolemSymbols);
			for (Term sk : orderedList) {
				if (!commonVars.containsKey(sk)) {
					if (sk.arity() > 0) {
						LogicVariable logicVariable = new LogicVariable(
								new Name(sk.op().name() + "$sk"), sk.op().sort(
										new Term[0]));
						skolemSymbolsWithDependcies.add(sk);
						skolemSymbolWithDepsMap.put(sk, logicVariable);
						commonMatches.add(new Match((RigidFunction) sk.op(),
								TermBuilder.DF.var(logicVariable)));
					} else {
						skolemSymbols.add(sk);
					}
				}
			}

			query = TermBuilder.DF.and(query, imp);
		}

		query = TermBuilder.DF.imp(ante, TermBuilder.DF.or(query, succ));

		// TODO: we have to add the quantifiers for the skolem symbols with
		// dependencies (as well as commonVars)in the outermost position that is
		// still sound (so respecting the dependencies)

		Map<Metavariable, LogicVariable> metavarReplacements = new LinkedHashMap<Metavariable, LogicVariable>();
		for (Metavariable var : variables) {
			LogicVariable logicVariable = new LogicVariable(new Name(var.name()
					+ "$mv"), var.sort(new Term[0]));
			metavarReplacements.put(var, logicVariable);
			commonMatches
					.add(new Match(var, TermBuilder.DF.var(logicVariable)));
		}

		List<Term> topLevelSkolems = SkolemfunctionTracker.INSTANCE
				.getOrderedList(skolemSymbols);
		Map<Term, LogicVariable> topLevelSkolemReplacements = new LinkedHashMap<Term, LogicVariable>();
		for (Term t : topLevelSkolems) {
			LogicVariable logicVariable = new LogicVariable(new Name(t.op()
					.name() + "$sk"), t.op().sort(new Term[0]));
			topLevelSkolemReplacements.put(t, logicVariable);
			commonMatches.add(new Match((RigidFunction) t.op(), TermBuilder.DF
					.var(logicVariable)));
		}

		// now we replace all those variables/skolem symbols be fresh variables
		if (!commonMatches.isEmpty()) {
			query = TermRewriter.replace(query, commonMatches);
		}

		// readd the quantifiers
		List<String> variableNames = new ArrayList<String>();
		commonVars.putAll(skolemSymbolWithDepsMap);

		List<Term> orderedList = SkolemfunctionTracker.INSTANCE
				.getOrderedList(commonVars.keySet());
		// now we built up a reference list for metavariables
		Map<Metavariable, Integer> metavariablesDeps = new LinkedHashMap<Metavariable, Integer>();
		for (Term sk : orderedList) {
			for (int i = 0; i < sk.arity(); i++) {
				assert (sk.sub(i).op() instanceof Metavariable) : "Skolem variables must only have metavariables as arguments!";
				Metavariable m = (Metavariable) sk.sub(i).op();
				Integer j = metavariablesDeps.get(m);
				if (j == null) {
					j = 0;
				}
				metavariablesDeps.put(m, ++j);
			}
		}
		for (Metavariable metaVar : variables) {
			if (!metavariablesDeps.containsKey(metaVar)) {
				LogicVariable nvar = metavarReplacements.get(metaVar);
				assert query.freeVars().contains(nvar);
				query = TermFactory.DEFAULT.createQuantifierTerm(Op.EX, nvar,
						query);
				variableNames.add(nvar.name().toString());
				// TODO: check if we can avoid adding these variables to the
				// namespace...
				services.getNamespaces().variables().add(nvar);
			}
		}

		for (Term sk : orderedList) {
			LogicVariable var = commonVars.get(sk);
			assert sk.arity() > 0 || query.freeVars().contains(var) : "Skolem functions should be replaced by free variables.";
			query = TermFactory.DEFAULT
					.createQuantifierTerm(Op.ALL, var, query);
			variableNames.add(var.name().toString());
			// TODO: check if we can avoid adding these variables to the
			// namespace...
			services.getNamespaces().variables().add(var);
			for (int i = 0; i < sk.arity(); i++) {
				assert (sk.sub(i).op() instanceof Metavariable) : "Skolem variables must only have metavariables as arguments!";
				Metavariable m = (Metavariable) sk.sub(i).op();
				Integer j = metavariablesDeps.get(m);
				if (j == 1) {
					metavariablesDeps.remove(m);
					LogicVariable nvar = metavarReplacements.get(m);
					assert query.freeVars().contains(nvar);
					query = TermFactory.DEFAULT.createQuantifierTerm(Op.EX,
							nvar, query);
					variableNames.add(nvar.name().toString());
					// TODO: check if we can avoid adding these variables to the
					// namespace...
					services.getNamespaces().variables().add(nvar);
				} else {
					metavariablesDeps.put(m, --j);
				}
			}
		}

		assert metavariablesDeps.isEmpty() : "All metavariables should be transformed into existential quantifiers before adding skolem symbols that dont have any dependcies!";

		// for (Metavariable metaVar : variables) {
		// LogicVariable var = metavarReplacements.get(metaVar);
		// assert query.freeVars().contains(var);
		// query = TermFactory.DEFAULT.createQuantifierTerm(Op.EX, var, query);
		// variableNames.add(var.name().toString());
		// // TODO: check if we can avoid adding these variables to the
		// // namespace...
		// services.getNamespaces().variables().add(var);
		// }

		for (Term sk : topLevelSkolems) {
			LogicVariable var = topLevelSkolemReplacements.get(sk);
			assert query.freeVars().contains(var);
			query = TermFactory.DEFAULT
					.createQuantifierTerm(Op.ALL, var, query);
			variableNames.add(var.name().toString());
			// TODO: check if we can avoid adding these variables to the
			// namespace...
			services.getNamespaces().variables().add(var);
		}

		try {
			if (DLOptionBean.INSTANCE.isSimplifyBeforeReduce()) {
				query = MathSolverManager.getCurrentSimplifier().simplify(
						query, services.getNamespaces());
			}
			resultTerm = MathSolverManager
					.getCurrentQuantifierEliminator().reduce(query,
							variableNames,
							new ArrayList<PairOfTermAndQuantifierType>(),
							services.getNamespaces());
			if (!testModeActive) {
				if (DLOptionBean.INSTANCE.isSimplifyAfterReduce()) {
					resultTerm = MathSolverManager.getCurrentSimplifier()
							.simplify(resultTerm, services.getNamespaces());
				}
				// if there is a result: close all goals beside this one... and
				// progress
				// here
				if (resultTerm != null) {
					for (Goal g : goals) {
						if (g != goal) {
							g.apply(new CloseRuleApp());
						}
					}
				} else {
					throw new UnsolveableException();
				}
				if (resultTerm.equals(TermBuilder.DF.tt())) {
					return goal.split(0);
				}
				ImmutableList<Goal> result = goal.split(1);
				removeAllFormulas(result.head(), goal.sequent().succedent()
						.iterator(), false);
				removeAllFormulas(result.head(), goal.sequent().antecedent()
						.iterator(), true);
				SequentChangeInfo info = result
						.head()
						.sequent()
						.addFormula(new ConstrainedFormula(resultTerm), false,
								true);
				result.head().setSequent(info);
				return result;
			} else {
				// if we are in testmode we always return an empty list
				return ImmutableSLList.nil();
			}
		} catch (RemoteException e) {
			throw new IllegalStateException("Cannot eliminate variable "
					+ Arrays.toString(variables.toArray()), e);
		} catch (UnsolveableException e) {
			unsolvable = true;
			throw new IllegalStateException("Cannot eliminate variable "
					+ Arrays.toString(variables.toArray()), e);
		} catch (SolverException e) {
			throw new IllegalStateException("Cannot eliminate variable "
					+ Arrays.toString(variables.toArray()), e);
		}

	}

	/**
	 * @param variables
	 * @param toRemove
	 * @param ta
	 */
	private void checkTacletApp(List<Metavariable> variables,
			Set<NoPosTacletApp> toRemove, NoPosTacletApp ta) {
		if (ta.taclet().displayName().startsWith("insert_hidden")) {
			Iterator<ImmutableMapEntry<SchemaVariable, InstantiationEntry>> pairIterator = ta
					.instantiations().pairIterator();
			while (pairIterator.hasNext()) {
				ImmutableMapEntry<SchemaVariable, InstantiationEntry> pair = pairIterator
						.next();
				if (pair.value().getInstantiation() instanceof Term) {
					Term t = (Term) pair.value().getInstantiation();
					Result res = ContainsMetaVariableVisitor
							.containsMetaVariableAndIsFO(variables, t);
					if (res == Result.CONTAINS_VAR
							|| res == Result.CONTAINS_VAR_BUT_CANNOT_APPLY) {
						toRemove.add(ta);
					}
				} else {
					// we are not sure, therefore better remove the taclet
					// TODO: find out if we really need to remove
					toRemove.add(ta);
					System.err
							.println("Removing hidden even though we weren't sure: "
									+ ta);
				}
			}
		}
	}

	/**
	 * Find skolem functions that occur in the given formulas
	 * 
	 * @param iterator
	 * @return
	 */
	private Set<Term> findSkolemSymbols(Iterator<ConstrainedFormula> iterator) {
		final Set<Term> result = new LinkedHashSet<Term>();
		while (iterator.hasNext()) {
			result.addAll(findSkolemSymbols(iterator.next().formula()));
		}
		return result;
	}

	/**
	 * Find skolem functions that occur in the given formulas
	 * 
	 * @param iterator
	 * @return
	 */
	private Set<Term> findSkolemSymbols(Term t) {
		final Set<Term> result = new LinkedHashSet<Term>();
		t.execPreOrder(new Visitor() {

			/* @Override */
			public void visit(Term visited) {
				if (visited.op() instanceof RigidFunction) {
					RigidFunction f = (RigidFunction) visited.op();
					if (f.isSkolem()) {
						result.add(visited);
					}
				}
			}

		});
		return result;
	}

	/**
	 * Select the given goal in the GUI and show a dialog that we cannot apply
	 * this rule
	 * 
	 * @param curGoal
	 */
	private void selectGoalAndShowDialog(Goal curGoal) {
		Main.getInstance().mediator().goalChosen(curGoal);
		JOptionPane
				.showMessageDialog(Main.getInstance(),
						"There was a goal that does not match the condition for applying this rule");
	}

	/**
	 * Remove all formulas in the iterator from the given goal
	 * 
	 * @param result
	 * @param iterator
	 * @param ante
	 */
	private void removeAllFormulas(Goal head,
			Iterator<ConstrainedFormula> iterator, boolean ante) {
		while (iterator.hasNext()) {
			ConstrainedFormula next = iterator.next();
			SequentChangeInfo removeFormula = head.sequent().removeFormula(
					new PosInOccurrence(next, PosInTerm.TOP_LEVEL, ante));
			head.setSequent(removeFormula);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.rule.BuiltInRule#isApplicable(de.uka.ilkd.key.proof.Goal,
	 * de.uka.ilkd.key.logic.PosInOccurrence, de.uka.ilkd.key.logic.Constraint)
	 */
	public boolean isApplicable(Goal goal, PosInOccurrence pio,
			Constraint userConstraint) {
		// if (MathSolverManager.isQuantifierEliminatorSet()
		// && ((DLOptionBean.INSTANCE.isSimplifyBeforeReduce() ||
		// DLOptionBean.INSTANCE
		// .isSimplifyAfterReduce()) ? MathSolverManager
		// .isSimplifierSet() : true)) {
		// return pio != null && pio.subTerm() != null
		// && pio.subTerm().op() instanceof Metavariable;
		// } else {
		// return false;
		// }
		if (MathSolverManager.isQuantifierEliminatorSet()
				&& pio != null
				&& pio.constrainedFormula() != null
				&& ((DLOptionBean.INSTANCE.isSimplifyBeforeReduce() || DLOptionBean.INSTANCE
						.isSimplifyAfterReduce()) ? MathSolverManager
						.isSimplifierSet() : true)) {
			final Operator[] ops = new Operator[1];
			final boolean[] fo = new boolean[1];
			fo[0] = true;
			pio.constrainedFormula().formula().execPreOrder(new Visitor() {

				/* @Override */
				public void visit(Term visited) {
					if (visited.op() instanceof Metavariable) {
						ops[0] = visited.op();
					} else if (!FOSequence.isFOOperator(visited.op())) {
						fo[0] = false;
					}
				}

			});
			Operator op = ops[0];
			if (!fo[0] || !(op instanceof Metavariable)) {
				return false;
			}

			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#displayName()
	 */
	public String displayName() {
		return "Eliminate Existential Quantifier";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#name()
	 */
	public Name name() {
		return new Name("Eliminate Existential Quantifier");
	}

	/* @Override */
	public String toString() {
		return displayName();
	}

	/**
	 * @return the unsolvable
	 */
	public boolean isUnsolvable() {
		return unsolvable;
	}

	/* @Override */
	public boolean irrevocable(Node parent) {
		// TODO find out if revocable because we didn't close any foreign goals
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.proof.RuleFilter#filter(de.uka.ilkd.key.rule.Rule)
	 */
	/* @Override */
	public boolean filter(Rule rule) {
		return rule instanceof EliminateExistentialQuantifierRule;
	}

	@Override
	public boolean test(Goal goal, Services services, RuleApp app, long timeout) {
		testMode = true;
		try {
			// try to apply the rule... if no exception occurs it was successful
		    //@todo shouldn't we pass timeout as a parameter?
			apply(goal, services, app);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Term getInputFormula() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Term getResultFormula() {
		return resultTerm;
	}

}
