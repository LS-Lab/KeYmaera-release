/***************************************************************************
 *   Copyright (C) 2009 by Philipp Ruemmer                                 *
 *   philipp@chalmers.se                                                   *
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
package de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import orbital.logic.functor.Function;
import orbital.math.AlgebraicAlgorithms;
import orbital.math.Polynomial;
import de.uka.ilkd.key.dl.arithmetics.IGroebnerBasisCalculator;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker.PolynomialClassification;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.TermSymbol;

/**
 * Augmented version of the Groebner basis rule that is based on the real
 * Nullstellensatz and, thus, a complete method for proving the
 * unsatisfiability of a conjunction of equations, inequalities and
 * disequations. The application of the Nullstellensatz requires to
 * synthesise a sum of squares s such that 1+s is in the ideal described by
 * the Groebner basis, which is done through positive semi-definite programming.
 */
public class GroebnerBasisChecker implements IGroebnerBasisCalculator {

    public boolean checkForConstantGroebnerBasis(PolynomialClassification<Term> terms,
	                                         Services services) {
	final Set<Polynomial> polys = extractPolynomials(terms);
	
	// we try to get a contradiction by computing the groebner basis of all
	// the equalities. if the common basis contains a constant part, the
	// equality system is unsatisfiable, thus we can close this goal
	final Function groebnerReducer =
	    orbital.math.AlgebraicAlgorithms.reduce(
		     polys, AlgebraicAlgorithms.DEGREE_REVERSE_LEXICOGRAPHIC);
	
	System.out.println("Groebner basis:");
	System.out.println(groebnerReducer);
	
	final Polynomial oneReduced =
	    (Polynomial) groebnerReducer.apply(polys.iterator().next().one());
	if (oneReduced.isZero()) {
	    System.out.println("Groebner basis is trivial and contains 1");
	    return true;
	}
	
	return false;
    }


    private Set<Polynomial> extractPolynomials(PolynomialClassification<Term> terms) {
	final TermBuilder TB = TermBuilder.DF;
	
	final Term minus_one =
	    TB.func(NumberCache.getNumber(new BigDecimal(-1), RealLDT.getRealSort()));
        final TermSymbol plus = RealLDT.getFunctionFor(de.uka.ilkd.key.dl.model.Plus.class);
        final TermSymbol mul = RealLDT.getFunctionFor(de.uka.ilkd.key.dl.model.Mult.class);

        final Set<Term> equations = new HashSet<Term> ();
	equations.addAll(terms.h);
	
	// we assume that inequalities have already been transformed away
	// (represented as equations)
	assert (terms.f.isEmpty());
	
	// let's also get rid of the disequations: p != 0 <=> \exists x; p*x = 1
	int i = 0;
	for (Term g : terms.g) {
	    final LogicVariable x =
		new LogicVariable(new Name("inv" + i), RealLDT.getRealSort());
	    equations.add(TB.func(plus, TB.func(mul, g, TB.var(x)), minus_one));
	}
	
	final PolynomialClassification<Term> equationsOnly =
	    new PolynomialClassification<Term>(new HashSet<Term> (),
		                               new HashSet<Term> (),
		                               equations);
	final Set<Polynomial> polys =
	    SumOfSquaresChecker.INSTANCE.classify(equationsOnly).h;
	
	System.out.println("Polynomials are: ");
	for (Polynomial p : polys) {
	    System.out.println(p);
	}
	return polys;
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
		return "Groebnerian SOS (via Orbital)";
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

    
}
