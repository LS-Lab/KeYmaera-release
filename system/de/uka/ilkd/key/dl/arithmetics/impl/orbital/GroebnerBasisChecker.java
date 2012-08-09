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

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import orbital.logic.functor.Function;
import orbital.math.AlgebraicAlgorithms;
import orbital.math.Polynomial;
import orbital.math.Values;

import org.w3c.dom.Node;

import de.uka.ilkd.key.dl.arithmetics.IGroebnerBasisCalculator;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker.PolynomialClassification;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Term;

/**
 * @author jdq TODO Documentation since Jun 6, 2008
 */
public class GroebnerBasisChecker implements IGroebnerBasisCalculator {

	/**
	 * 
	 */
	public GroebnerBasisChecker(Node node) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uka.ilkd.key.dl.arithmetics.IGroebnerBasisCalculator#
	 * checkForConstantGroebnerBasis()
	 */
	/* @Override */
	public boolean checkForConstantGroebnerBasis(
			PolynomialClassification<Term> terms, Services services) {
		PolynomialClassification<Polynomial> classify2 = SumOfSquaresChecker
				.classify(terms, true);
		System.out.println("H is: ");
		for (Polynomial p : classify2.h) {
			System.out.println(p);
		}
		System.out.println("G is: ");
		for (Polynomial p : classify2.g) {
			System.out.println(p);
			if(p.isZero()) {
				// we found a contradiction 0 != 0
				return true;
			}
		}
		if(classify2.h.isEmpty()) {
			return false;
		}
		// we try to get a contradiction by computing the groebner basis of all
		// the equalities. if the common basis contains a constant part, the
		// equality system is unsatisfiable, thus we can close this goal
		Set gB = AlgebraicAlgorithms.groebnerBasis(classify2.h,
				AlgebraicAlgorithms.DEGREE_REVERSE_LEXICOGRAPHIC);
		Function groebnerBasis = orbital.math.AlgebraicAlgorithms.reduce(gB,
				AlgebraicAlgorithms.DEGREE_REVERSE_LEXICOGRAPHIC);
		System.out.println(groebnerBasis);
		Polynomial apply = (Polynomial) groebnerBasis.apply(classify2.h
				.iterator().next().one());
		System.out.println(apply);// XXX
		if (apply.equals(apply.zero())) {
			System.out.println(apply + " is equal to " + apply.zero());
			return true;
		}
		if (!classify2.g.isEmpty()) {
			// we test if one of the inequalities g is unsatisfiable under the
			// variety \forall f \in h: f = 0. if it is, we get false on the
			// left side of the sequent and can close this goal
			for (Polynomial g : classify2.g) {
				System.out.println("Testing " + g);
				Polynomial reduce = (Polynomial) groebnerBasis.apply(g);

				// TODO
				if (reduce.equals(reduce.zero())) {
					return true;
				}
			}
		}
		// now we try to not reduce one to zero in the groebner basis
		// {h1,...,hn,(g*t)-1}
		if (!classify2.g.isEmpty()) {
			int[] size = new int[apply.rank()];
			// the last variable is unused
			size[size.length - 1] = 1;
			Polynomial t = Values.getDefault().MONOMIAL(size);
			for (Polynomial g : classify2.g) {
				Set<Polynomial> curPolies = new HashSet<Polynomial>(classify2.h);
				Polynomial ng = (Polynomial) g.multiply(t).subtract(t.one());
				curPolies.add(ng);
				System.out.println("Creating groebner basis for " + curPolies);// XXX
				Set curgB = AlgebraicAlgorithms.groebnerBasis(curPolies,
						AlgebraicAlgorithms.DEGREE_REVERSE_LEXICOGRAPHIC);
				Function curGroebnerBasis = orbital.math.AlgebraicAlgorithms
						.reduce(
								curgB,
								AlgebraicAlgorithms.DEGREE_REVERSE_LEXICOGRAPHIC);

				Polynomial reduce = (Polynomial) curGroebnerBasis.apply(ng
						.one());

				if (reduce.equals(reduce.zero())) {
					System.out
							.println("We could reduce 1 to 0 in the prior GB + "
									+ ng);// XXX
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#abortCalculation()
	 */
	/* @Override */
	public void abortCalculation() throws RemoteException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getCachedAnwserCount()
	 */
	/* @Override */
	public long getCachedAnswerCount() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getName()
	 */
	/* @Override */
	public String getName() {
		return "Orbital";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getQueryCount()
	 */
	/* @Override */
	public long getQueryCount() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getTimeStatistics()
	 */
	/* @Override */
	public String getTimeStatistics() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getTotalCalculationTime()
	 */
	/* @Override */
	public long getTotalCalculationTime() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getTotalMemory()
	 */
	/* @Override */
	public long getTotalMemory() throws RemoteException,
			ServerStatusProblemException, ConnectionProblemException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#resetAbortState()
	 */
	/* @Override */
	public void resetAbortState() throws RemoteException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#isConfigured()
	 */
	/* @Override */
	public boolean isConfigured() {
		return true;
	}

	@Override
	public Term[] computeGroebnerBasis(Term[] polynomials, Services services)
			throws RemoteException, SolverException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Term polynomialReduce(Term poly, Term[] reductions, Services services)
			throws RemoteException, SolverException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
