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

import java.util.HashSet;
import java.util.Set;

import orbital.logic.functor.Function;
import orbital.math.AlgebraicAlgorithms;
import orbital.math.Polynomial;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker.PolynomialClassification;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.IteratorOfConstrainedFormula;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.ListOfGoal;
import de.uka.ilkd.key.proof.RuleFilter;
import de.uka.ilkd.key.proof.SLListOfGoal;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * This class is used for the groebner basis backend
 * 
 * @author jdq
 * @since 17.01.2008
 * 
 */
public class GroebnerBasisRule implements BuiltInRule, RuleFilter {

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
		PolynomialClassification<Polynomial> classify2 = SumOfSquaresChecker.INSTANCE
				.classify(classify);
		boolean solutionFound = false;
		System.out.println("H is: ");
		for (Polynomial p : classify2.h) {
			System.out.println(p);
		}
		// we try to get a contradiction by computing the groebner basis of all
		// the equalities. if the common basis contains a constant part, the
		// equality system is unsatisfiable, thus we can close this goal
		Function groebnerBasis = orbital.math.AlgebraicAlgorithms.reduce(
				classify2.h, AlgebraicAlgorithms.DEGREE_REVERSE_LEXICOGRAPHIC);
		System.out.println(groebnerBasis);
		Polynomial apply = (Polynomial) groebnerBasis.apply(classify2.h
				.iterator().next().one());
		if(apply.equals(apply.zero())) {
			return SLListOfGoal.EMPTY_LIST;
		}
		if (!classify2.g.isEmpty()) {
			// we test if one of the inequalities g is unsatisfiable under the
			// variety \forall f \in h: f = 0. if it is, we get false on the
			// left side of the sequent and can close this goal
			for (Polynomial g : classify2.g) {
				Polynomial reduce = (Polynomial) groebnerBasis.apply(g);
				if (reduce.equals(reduce.zero())) {
					return SLListOfGoal.EMPTY_LIST;
				}
			}
		}

		return SLListOfGoal.EMPTY_LIST.append(goal);
		// return SLListOfGoal.EMPTY_LIST;
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
		// TODO jdq: insert application test
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#displayName()
	 */
	@Override
	public String displayName() {
		return "Groebner Basis";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.rule.Rule#name()
	 */
	@Override
	public Name name() {
		return new Name("Groebner Basis");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.proof.RuleFilter#filter(de.uka.ilkd.key.rule.Rule)
	 */
	@Override
	public boolean filter(Rule rule) {
		return rule instanceof GroebnerBasisRule;
	}

}
