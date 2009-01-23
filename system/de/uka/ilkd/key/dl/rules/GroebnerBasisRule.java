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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import de.uka.ilkd.key.dl.arithmetics.IGroebnerBasisCalculator;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker.PolynomialClassification;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.dl.model.Minus;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.dl.strategy.features.FOSequence;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.ListOfGoal;
import de.uka.ilkd.key.proof.RuleFilter;
import de.uka.ilkd.key.proof.SLListOfGoal;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.SequentWideBuiltInRule;

/**
 * This class is used for the groebner basis backend
 * 
 * @author jdq
 * @since 17.01.2008
 * 
 */
public class GroebnerBasisRule implements SequentWideBuiltInRule, RuleFilter {

	public static final GroebnerBasisRule INSTANCE = new GroebnerBasisRule();

	/**
	 * 
	 */
	public GroebnerBasisRule() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#apply(de.uka.ilkd.key.proof.Goal,
	 *      de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
	 */
	public synchronized ListOfGoal apply(Goal goal, Services services,
			RuleApp ruleApp) {

		IteratorOfConstrainedFormula it = goal.sequent().antecedent()
				.iterator();
		Set<Term> ante = new HashSet<Term>();
		while (it.hasNext()) {
			ante.add(it.next().formula());
		}
		it = goal.sequent().succedent().iterator();
		Set<Term> succ = new HashSet<Term>();
		while (it.hasNext()) {
			succ.add(it.next().formula());
		}
		PolynomialClassification<Term> classify = SumOfSquaresChecker.INSTANCE
				.classify(ante, succ);

		if (MathSolverManager.isGroebnerBasisCalculatorSet()) {
			IGroebnerBasisCalculator m = MathSolverManager
					.getCurrentGroebnerBasisCalculator();

			if (m != null) {
				try {
					// we will rewrite the terms of the form f(x) >= 0 to f(x) = z^2 and add
					// them to h for this Groebner basis check

					// first get |f| new names and construct their squares
					int i = 0;
					String basename = "neu";
					Queue<Term> squares = new LinkedList<Term>();
					Sort r = RealLDT.getRealSort();
					Term zero = TermBuilder.DF.func(NumberCache.getNumber(new BigDecimal(0),
							r));
					Term two = TermBuilder.DF.func(NumberCache.getNumber(new BigDecimal(2),
							r));
					de.uka.ilkd.key.logic.op.Function exp = RealLDT
							.getFunctionFor(Exp.class);
					while (squares.size() < classify.f.size()) {
						Name n = new Name(basename + i++);
						while (services.getNamespaces().lookup(n) != null) {
							n = new Name(basename + i++);
						}
						squares.add(TermBuilder.DF.func(exp, TermBuilder.DF
								.var(new LogicVariable(n, r)), two));
					}
					de.uka.ilkd.key.logic.op.Function sub = RealLDT
							.getFunctionFor(Minus.class);
					
					// now we add the new equations
					for (Term t : classify.f) {
						classify.h.add(TermBuilder.DF.equals(TermBuilder.DF.func(sub, t.sub(0), squares.poll()), zero));
					}
					// and clear all inequalities, as we do not need them anymore
					classify.f.clear();
					if (m.checkForConstantGroebnerBasis(classify, services)) {
						return SLListOfGoal.EMPTY_LIST;
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return SLListOfGoal.EMPTY_LIST.append(goal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.BuiltInRule#isApplicable(de.uka.ilkd.key.proof.Goal,
	 *      de.uka.ilkd.key.logic.PosInOccurrence,
	 *      de.uka.ilkd.key.logic.Constraint)
	 */
	/*@Override*/
	public boolean isApplicable(Goal goal, Constraint userConstraint) {
		// TODO jdq: insert application test
		if(!MathSolverManager.isGroebnerBasisCalculatorSet()) {
			return false;
		}
		final boolean[] result = new boolean[1];
		result[0] = true;
		Visitor visitor = new Visitor() {

			/*@Override*/
			public void visit(Term visited) {
				if(visited.op() == Op.ALL || visited.op() == Op.EX || !FOSequence.isFOOperator(visited.op())) {
					result[0] = false;
				}
			}
			
		};
		for(ConstrainedFormula f: goal.sequent()) {
			visitor.visit(f.formula());
			if(!result[0]) {
				return false;
			}
		}
		return result[0];
	}

	public boolean isApplicable(Goal goal, PosInOccurrence pio,
	                           Constraint userConstraint) {
	    return isApplicable(goal, userConstraint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#displayName()
	 */
	/*@Override*/
	public String displayName() {
		return "Groebner Basis";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#name()
	 */
	/*@Override*/
	public Name name() {
		return new Name("Groebner Basis");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/*@Override*/
	public String toString() {
		return displayName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.proof.RuleFilter#filter(de.uka.ilkd.key.rule.Rule)
	 */
	/*@Override*/
	public boolean filter(Rule rule) {
		return rule instanceof GroebnerBasisRule;
	}

}
