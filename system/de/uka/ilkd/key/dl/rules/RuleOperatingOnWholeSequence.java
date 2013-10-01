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
import java.util.List;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.IncompleteEvaluationException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.strategy.RealtimeStrategy;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.PosInTerm;
import de.uka.ilkd.key.logic.Semisequent;
import de.uka.ilkd.key.logic.SequentChangeInfo;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.BuiltInRuleApp;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.SequentWideBuiltInRule;

/**
 * This class is used as super class for rules operating on a whole sequence.
 * 
 * @author jdq
 * @since 08.03.2007
 * 
 */
public abstract class RuleOperatingOnWholeSequence extends Visitor implements
		SequentWideBuiltInRule, TestableBuiltInRule {

	private static class PairOfTermAndChangeset {
		public PairOfTermAndChangeset(Term resultTerm,
				List<PosInOccurrence> changes) {
			this.term = resultTerm;
			this.changes = changes;
		}

		Term term;
		List<PosInOccurrence> changes;
	}

	protected boolean addFormula;

	private boolean addFormulaDefault;

	private List<String> variables;

	private boolean testMode = false;

	private Term inputFormula;

	private Term resultFormula;

	private boolean unsolvable;

	private Services services;

	/**
	 * 
	 */
	public RuleOperatingOnWholeSequence(boolean addFormulaDef) {
		addFormulaDefault = addFormulaDef;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#apply(de.uka.ilkd.key.proof.Goal,
	 *      de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
	 */
	public synchronized ImmutableList<Goal> apply(Goal goal, Services services,
			RuleApp ruleApp) {
		if (goal.getGoalStrategy() instanceof RealtimeStrategy) {
			/*
			 * if (((RealtimeStrategy)goal.getGoalStrategy()).getTimeout(goal,
			 * ruleApp) <= 0) { System.out.println("\tRTC " +
			 * ruleApp.rule().name() + " " +
			 * ((RealtimeStrategy)goal.getGoalStrategy()).getTimeout(goal,
			 * ruleApp)); //XXX }
			 */
			return apply(goal, services, ruleApp, ((RealtimeStrategy) goal
					.getGoalStrategy()).getTimeout(goal, ruleApp));
		} else {
			return apply(goal, services, ruleApp, -1);
		}
	}

	public synchronized ImmutableList<Goal> apply(Goal goal, Services services,
			RuleApp ruleApp, long timeout) {
		// reset the testmode instantaniously to ensure that it cannot be
		// enabled accidently
        if(ruleApp instanceof BuiltInRuleApp) {
            ((BuiltInRuleApp) ruleApp).setQuantifierEliminator(DLOptionBean.INSTANCE.getQuantifierEliminator());
        }
		boolean testModeActive = testMode;
		unsolvable = false;
		testMode = false;

		this.services = services;

		Term resultTerm = TermBuilder.DF.tt();
		PairOfTermAndChangeset changes = iterate(goal, goal.sequent()
				.antecedent(), resultTerm, true, true);
		resultTerm = changes.term;
		PairOfTermAndChangeset changes2 = iterate(goal, goal.sequent()
				.succedent(), TermBuilder.DF.ff(), false, false);
		resultTerm = TermBuilder.DF.imp(resultTerm, changes2.term);

		if (ruleApp instanceof ReduceRuleApp) {
			variables = ((ReduceRuleApp) ruleApp).getVariables();
		} else {
			variables = new ArrayList<String>();
		}

		inputFormula = resultTerm;
		resultFormula = null;

		try {
			resultTerm = performQuery(resultTerm, timeout);
		} catch (RemoteException e) {
			throw new IllegalStateException(e.getCause().getMessage(), e
					.getCause());
		} catch (UnsolveableException e) {
			unsolvable = true;
			throw new IllegalStateException(e.getMessage(), e);
  		} catch (IncompleteEvaluationException e) {
  			throw new IllegalStateException("Rule application has been canceled. If you pressed abort, this behavior is expected. Otherwise it indicates a problem.\n" + e.getMessage(), e);
		} catch (SolverException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		resultFormula = resultTerm;
		if (resultFormula == null)
			throw new NullPointerException("Rule did not return a result, it was possibly aborted.");

		// if we are not in test mode, we want to apply the resulting changes
		if (!testModeActive) {
			// if the result is true, we can close the goal
			if (resultTerm.equals(TermBuilder.DF.tt())) {
				return goal.split(0);
			}
			// otherwise we got one child node
			ImmutableList<Goal> result = goal.split(1);
			// remove all formulas from the antecendent
			for (PosInOccurrence i : changes.changes) {
				result.head().setSequent(
						result.head().sequent().removeFormula(i));
			}

			// remove all formulas from the succedent as well
			for (PosInOccurrence i : changes2.changes) {
				result.head().setSequent(
						result.head().sequent().removeFormula(i));
			}
			// now if the result wasnt false add the resulting formula to the
			// sequent
			if (!resultTerm.equals(TermBuilder.DF.ff())) {
				SequentChangeInfo info = result.head().sequent().addFormula(
						new ConstrainedFormula(resultTerm), false, true);
				result.head().setSequent(info);
			}
			return result;
		}
		// if we are in testmode we always return an empty list
		return ImmutableSLList.nil();
	}

	/**
	 * This method is used to implement different rules operating on the whole
	 * sequent. This method gets the query term and the current timeout as input
	 * and should try to solve that term.
	 * 
	 * @param term
	 *            the term that should be solved
	 * @param timeout
	 *            the current timeout
	 * @return the resulting term from artihmetic transformations
	 * @throws RemoteException
	 *             if there is an error with RMI connections
	 * @throws SolverException
	 *             if there was an error from a background solver
	 */
	protected abstract Term performQuery(Term term, long timeout)
			throws RemoteException, SolverException;

	/**
	 * Iterates over the given formulas and constructs the conjunction or
	 * disjunction of all first order formulas in the sequence.
	 * 
	 * @param result
	 *            the current goal
	 * @param it
	 *            the iterator used to access the formulas
	 * @param resultTerm
	 *            the term built so far
	 * @param and
	 *            if true this function returns the conjunction, otherwise the
	 *            disjunction is returned
	 * @return the conjunction or disjunction of all first order formulas in the
	 *         sequence.
	 */
	private PairOfTermAndChangeset iterate(Goal result, Semisequent it,
			Term resultTerm, boolean and, boolean ante) {
		List<PosInOccurrence> changes = new ArrayList<PosInOccurrence>();
		for (ConstrainedFormula f : it) {
			addFormula = addFormulaDefault;
			f.formula().execPostOrder(this);
			if (addFormula) {
				changes.add(new PosInOccurrence(f, PosInTerm.TOP_LEVEL, ante));
				if (and) {
					resultTerm = TermBuilder.DF.and(resultTerm, f.formula());
				} else {
					resultTerm = TermBuilder.DF.or(resultTerm, f.formula());
				}
			}
		}
		return new PairOfTermAndChangeset(resultTerm, changes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
	 */
	/*@Override*/
	public void visit(Term visited) {
		performSearch(visited);
	}

	/**
	 * Filters the terms. The result is passed using the addFormula field.
	 * 
	 * @param visited
	 *            the term to test.
	 */
	protected abstract void performSearch(Term visited);

	/**
	 * @return the variables
	 */
	protected List<String> getVariables() {
		return variables;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.rules.TestableBuiltInRule#test(de.uka.ilkd.key.proof.Goal,
	 *      de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp, long)
	 */
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

	public Term getInputFormula() {
		return inputFormula;
	}

	public Term getResultFormula() {
		return resultFormula;
	}

	/**
	 * @return the unsolvable
	 */
	public boolean isUnsolvable() {
		return unsolvable;
	}

	protected Services getServices() {
		return services;
	}

}
