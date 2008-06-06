/***************************************************************************
 *   Copyright (C) 2008 by Jan David Quesel                                *
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
package de.uka.ilkd.key.dl.arithmetics.impl.orbital;

import orbital.logic.functor.Function;
import orbital.math.AlgebraicAlgorithms;
import orbital.math.Polynomial;
import de.uka.ilkd.key.dl.arithmetics.IGroebnerBasisCalculator;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker.PolynomialClassification;
import de.uka.ilkd.key.logic.Term;

/**
 * @author jdq TODO Documentation since Jun 6, 2008
 */
public class GroebnerBasisChecker implements IGroebnerBasisCalculator {

	public static final GroebnerBasisChecker INSTANCE = new GroebnerBasisChecker();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IGroebnerBasisCalculator#checkForConstantGroebnerBasis()
	 */
	@Override
	public boolean checkForConstantGroebnerBasis(PolynomialClassification<Term> terms) {
		PolynomialClassification<Polynomial> classify2 = SumOfSquaresChecker.INSTANCE
				.classify(terms);
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
			return true;
		}
		if (!classify2.g.isEmpty()) {
			// we test if one of the inequalities g is unsatisfiable under the
			// variety \forall f \in h: f = 0. if it is, we get false on the
			// left side of the sequent and can close this goal
			for (Polynomial g : classify2.g) {
				Polynomial reduce = (Polynomial) groebnerBasis.apply(g);
				if (reduce.equals(reduce.zero())) {
					return true;
				}
			}
		}
		
		return false;
	}

}
