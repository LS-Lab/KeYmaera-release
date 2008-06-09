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
import java.util.HashSet;
import java.util.Set;

import orbital.logic.functor.Function;
import orbital.math.AlgebraicAlgorithms;
import orbital.math.Polynomial;
import de.uka.ilkd.key.dl.arithmetics.IGroebnerBasisCalculator;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker.PolynomialClassification;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Mathematica;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.GroebnerBasisChecker;
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

		IGroebnerBasisCalculator m = MathSolverManager
				.getCurrentGroebnerBasisCalculator();

		if (m != null) {
			try {
				if (m.checkForConstantGroebnerBasis(classify)) {
					return SLListOfGoal.EMPTY_LIST;
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// if(GroebnerBasisChecker.INSTANCE.checkForConstantGroebnerBasis(classify))
		// {
		// return SLListOfGoal.EMPTY_LIST;
		// }

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
