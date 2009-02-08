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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import orbital.logic.functor.Function;
import orbital.math.AlgebraicAlgorithms;
import orbital.math.Arithmetic;
import orbital.math.Matrix;
import orbital.math.Polynomial;
import orbital.math.Values;
import orbital.math.Vector;

import org.w3c.dom.Node;

import de.uka.ilkd.key.dl.arithmetics.IGroebnerBasisCalculator;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker.PolynomialClassification;
import de.uka.ilkd.key.dl.arithmetics.impl.csdp.CSDP;
import de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.PSDDecomposition.NotPSDException;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Unequals;
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
	final Set<Polynomial> groebnerBasis =
	    orbital.math.AlgebraicAlgorithms.groebnerBasis(polys,
		     AlgebraicAlgorithms.DEGREE_REVERSE_LEXICOGRAPHIC);
	final Function groebnerReducer =
	    orbital.math.AlgebraicAlgorithms.reduce(
		     groebnerBasis, AlgebraicAlgorithms.DEGREE_REVERSE_LEXICOGRAPHIC);
	
	System.out.println("Groebner basis is: ");
	for (Polynomial p : groebnerBasis)
	    System.out.println(p);
	
	final Polynomial oneReduced =
	    (Polynomial) groebnerReducer.apply(polys.iterator().next().one());
	System.out.println(oneReduced);
	if (oneReduced.isZero()) {
	    System.out.println("Groebner basis is trivial and contains a unit");
	    return true;
	}
	
	// enumerate sums of squares s and check whether some monomial 1+s is
	// in the ideal
	final Iterator<Vector> monomials =
	    new SimpleMonomialIterator(indexNum(groebnerBasis), 2);
	return checkSOS(monomials, groebnerBasis, groebnerReducer);
    }


    private Set<Polynomial> extractPolynomials(PolynomialClassification<Term> terms) {
	final TermBuilder TB = TermBuilder.DF;
	
	final Term zero =
	    TB.func(NumberCache.getNumber(new BigDecimal(0), RealLDT.getRealSort()));
	final Term one =
	    TB.func(NumberCache.getNumber(new BigDecimal(1), RealLDT.getRealSort()));
        final TermSymbol minus = RealLDT.getFunctionFor(de.uka.ilkd.key.dl.model.Minus.class);
        final TermSymbol mul = RealLDT.getFunctionFor(de.uka.ilkd.key.dl.model.Mult.class);

        final Set<Term> equations = new HashSet<Term> ();
	equations.addAll(terms.h);
	
	// we assume that inequalities have already been transformed away
	// (represented as equations)
	assert (terms.f.isEmpty());
	
	// let's also get rid of the disequations: p != 0 <=> \exists x; p*x = 1
	int i = 0;
	for (Term g : terms.g) {
	    assert (g.op() == RealLDT.getFunctionFor(Unequals.class));
	    final Term diff = TB.func(minus, g.sub(0), g.sub(1));
	    final LogicVariable x =
		new LogicVariable(new Name("inv" + i), RealLDT.getRealSort());
	    final Term lhs = TB.func(minus, TB.func(mul, diff, TB.var(x)), one);
	    equations.add(TB.equals(lhs, zero));
	}
	
	final PolynomialClassification<Term> equationsOnly =
	    new PolynomialClassification<Term>(new HashSet<Term> (),
		                               new HashSet<Term> (),
		                               equations);
	final Set<Polynomial> polys =
	    SumOfSquaresChecker.INSTANCE.classify(equationsOnly).h;
	
	System.out.println("Polynomials are: ");
	for (Polynomial p : polys)
	    System.out.println(p);

	return polys;
    }

    private int indexNum(Set<Polynomial> polys) {
        int res = 0;
        for (Polynomial p : polys) {
            final Iterator<Vector> it = p.indices();
            while (it.hasNext()) {
                final Vector v = it.next();
                res = Math.max(res, v.dimension());
            }
        }
        return res;
    }
    
    private boolean checkSOS(Iterator<Vector> monomials,
                             Set<Polynomial> groebnerBasis,
                             Function groebnerReducer) {
        
        final Arithmetic two = Values.getDefault().rational(2);
        
        final List<Vector> consideredMonomials = new ArrayList<Vector> ();
        final SparsePolynomial reducedPoly = new SparsePolynomial ();
        int currentParameter = 0;
        
        while (monomials.hasNext()) {
            final Vector newMono = monomials.next();
            consideredMonomials.add(newMono);
            System.out.println("Adding a monomial: " + newMono + ", " +
                               Values.getDefault().MONOMIAL(newMono));
            
            // consider all products of the new monomial with the monomials
            // already considered
            for (int i = 0; i < consideredMonomials.size(); ++i) {
                final Vector oldMono = consideredMonomials.get(i);
                final Vector combinedMonoExp = oldMono.add(newMono);
                final Polynomial combinedMono;
                
                // all products but the product of <code>newMono</code> with
                // itself have to be taken times two (the matrix is symmetric,
                // and we only consider one half of it)
                if (i < consideredMonomials.size() - 1)
                    combinedMono = Values.getDefault().MONOMIAL(two, combinedMonoExp);
                else
                    combinedMono = Values.getDefault().MONOMIAL(combinedMonoExp);
                
                final Polynomial reducedMono =
                    (Polynomial) groebnerReducer.apply(combinedMono);
                System.out.println("Reduced " + combinedMono + " to " + reducedMono);
                reducedPoly.addTerms(reducedMono, currentParameter);
                
                currentParameter = currentParameter + 1;
            }
            
            System.out.println(reducedPoly);
            
            final int monoNum = consideredMonomials.size();
            final double[] homo = reducedPoly.coefficientComparison(monoNum);

            // the inhomogeneous part of the system of equations
            final double[] hetero = new double [reducedPoly.size()];
            Arrays.fill(hetero, 0.0);
            hetero[0] = -1.0; // we have to check that 1+s is in the ideal, hence a one
            
            final double[] approxSolution = new double [monoNum * monoNum];
            
            int res = CSDP.sdp(monoNum, reducedPoly.size(), hetero, homo, approxSolution);

            if (res == 0) {
                System.out.println("Found an approximate solution!");
                System.out.println(Arrays.toString(approxSolution));
                
                System.out.println("Trying to recover an exact solution ...");
                final Matrix exactHomo =
                    reducedPoly.exactCoefficientComparison(monoNum);
                
                // we add further constraints to ensure that the found solution
                // is a symmetric matrix
                exactHomo.insertRows(symmetryConstraints(monoNum));

                final Vector exactHetero =
                    Values.getDefault().newInstance(exactHomo.dimensions()[0]);
                for (int i = 0; i < exactHetero.dimension(); ++i)
                    exactHetero.set(i, Values.getDefault().valueOf(i == 0 ? -1 : 0));

                double eps = 0.1;
                
                while (eps > 0.000000001) {
                    System.out.println();
                    System.out.println("Trying eps: " + eps);

                    final Vector exactSolution = new FractionisingEquationSolver(
                            exactHomo, exactHetero, approxSolution, eps).exactSolution;
                    System.out.println(exactSolution);
                    
                    System.out.println("Difference to approx solution:");
                    System.out.println(exactSolution.subtract(Values.getDefault().valueOf(approxSolution)));
                    
                    // check that the solution is positive semi-definite
                    final Matrix solutionMatrix =
                        Values.getDefault().newInstance(monoNum, monoNum);
                    for (int i = 0; i < monoNum; ++i)
                        for (int j = 0; j < monoNum; ++j)
                            solutionMatrix.set(i, j,
                                               exactSolution.get(i * monoNum + j));

                    System.out.println(solutionMatrix);

                    try {
                        final PSDDecomposition dec =
                            new PSDDecomposition(solutionMatrix);
                        System.out.println("Solution is positive semi-definite");
                        System.out.println(dec.T);
                        System.out.println(dec.D);
                        return true;
                    } catch (NotPSDException e) {
                        System.out.println(e.getMessage());
                    }
                    
                    eps = eps / 10;
                }
            } else {
                System.out.println("No solution");
            }
            
        }
        
        return false;
    }
    
    
    private static Matrix symmetryConstraints(int matrixSize) {
        final Arithmetic one = Values.getDefault().ONE();
        final Arithmetic minus_one = one.minus();

        final int height = matrixSize * (matrixSize - 1) / 2;
        final int width = matrixSize * matrixSize;
        final Matrix res = Values.getDefault().newInstance(height, width);
        
        fill(res, Values.getDefault().ZERO());
        
        // generate the constraints
        int row = 0;
        for (int i = 1; i < matrixSize; ++i)
            for (int j = 0; j < i; ++j) {
                res.set(row, i * matrixSize + j, one);
                res.set(row, j * matrixSize + i, minus_one);
                row = row + 1;
            }
        
        assert (row == height);
        return res;
    }
    
    public static void fill(Matrix m, Arithmetic val) {
        final int height = m.dimensions()[0];
        final int width = m.dimensions()[1];
        for (int i = 0; i < height; ++i)
            for (int j = 0; j < width; ++j)
                m.set(i, j, val);
    }
    
    
    private static class SimpleMonomialIterator implements Iterator<Vector> {
        private final int indexNum;
        private final int maxTotalDegree;
        
        private final List<Vector> currentExps = new ArrayList<Vector> ();
        private int currentTotalDegree = 0;
        
        private final Vector zeroMonomial;
        private final List<Vector> linearMonomials = new ArrayList<Vector> ();
        
        public SimpleMonomialIterator(int indexNum, int maxTotalDegree) {
            this.indexNum = indexNum;
            this.maxTotalDegree = maxTotalDegree;
            
            final Arithmetic[] exps = new Arithmetic[indexNum];
            Arrays.fill(exps, Values.getDefault().ZERO());
            zeroMonomial = Values.getDefault().tensor(exps);
            
            for (int i = 0; i < indexNum; ++i) {
                final Arithmetic[] exps2 = new Arithmetic[indexNum];
                Arrays.fill(exps2, Values.getDefault().ZERO());
                exps2[i] = Values.getDefault().ONE();
                linearMonomials.add(Values.getDefault().tensor(exps2));
            }

            for (Vector v : linearMonomials) {
                System.out.println(v);
            }
            
            currentExps.add(zeroMonomial);
        }

        public boolean hasNext() {
            return !currentExps.isEmpty() || currentTotalDegree < maxTotalDegree;
        }

        public Vector next() {
            if (!currentExps.isEmpty())
                return currentExps.remove(currentExps.size() - 1);
            assert (currentTotalDegree < maxTotalDegree);
            
            currentTotalDegree = currentTotalDegree + 1;
            genExps(zeroMonomial, 0, currentTotalDegree);
            
            assert (!currentExps.isEmpty());
            return currentExps.remove(currentExps.size() - 1);
        }

        private void genExps(Vector currentExp, int startIndex, int degreesLeft) {
            if (degreesLeft == 0) {
                currentExps.add(currentExp);
                return;
            }
            
            assert (startIndex < indexNum);
            
            if (startIndex < indexNum - 1)
                genExps(currentExp, startIndex + 1, degreesLeft);
            
            genExps(currentExp.add(linearMonomials.get(startIndex)),
                    startIndex, degreesLeft - 1);
        }
        
        public void remove() {
            throw new UnsupportedOperationException();            
        }
    }
    
	
	public GroebnerBasisChecker(Node node) {}

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
