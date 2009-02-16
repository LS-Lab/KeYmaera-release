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
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import orbital.logic.functor.Function;
import orbital.math.AlgebraicAlgorithms;
import orbital.math.Arithmetic;
import orbital.math.Integer;
import orbital.math.Matrix;
import orbital.math.Polynomial;
import orbital.math.Tensor;
import orbital.math.ValueFactory;
import orbital.math.Values;
import orbital.math.Vector;
import orbital.math.functional.Operations;

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
	final Set<Polynomial> rawPolys = extractPolynomials(terms);
        System.out.println("Polynomials are: ");
        printPolys(rawPolys);

        final Set<Polynomial> polys2 = eliminateLinearVariables(rawPolys);
        System.out.println("Polynomials after eliminating linear variables are: ");
        printPolys(polys2);
        
        final Set<Polynomial> polys = eliminateUnusedVariables(polys2);
        System.out.println("Polynomials after eliminating unused variables are: ");
        printPolys(polys);
        
        final Polynomial one = (Polynomial)polys.iterator().next().one();
	
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
	printPolys(groebnerBasis);
	
        final Polynomial oneReduced = (Polynomial) groebnerReducer.apply(one);
	if (oneReduced.isZero()) {
	    System.out.println("Groebner basis is trivial and contains a unit");
	    return true;
	}
	
	// enumerate sums of squares s and check whether some monomial 1+s is
	// in the ideal
	final Iterator<Vector> monomials =
	    new SimpleMonomialIterator(indexNum(groebnerBasis), 3);
        final Square[] cert = checkSOS(monomials, groebnerBasis, groebnerReducer);
        
        if (cert != null) {
            // check that the certificate is correct
            
            System.out.println("Certificate:");
            System.out.println(" 1");
            
            Polynomial p = one;
            for (int i = 0; i < cert.length; ++i) {
                assert (Operations.greaterEqual.apply(cert[i].coefficient,
                                                      Values.getDefault().ZERO()));
                p = (Polynomial) p.add(cert[i].body.multiply(cert[i].body)
                                                   .scale(cert[i].coefficient));
                System.out.println(" + " + cert[i].coefficient + " * ( " + cert[i].body + " ) ^2");
            }
            System.out.println(" =");
            System.out.println(" " + p);
            assert (((Polynomial) groebnerReducer.apply(p)).isZero());
            System.out.println("Certificate is correct");
            return true;
        }
        return false;
    }


    private void printPolys(Set<Polynomial> rawPolys) {
        for (Polynomial p : rawPolys)
            System.out.println(p);
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
	return SumOfSquaresChecker.INSTANCE.classify(equationsOnly).h;
    }

    private int indexNum(Set<Polynomial> polys) {
        for (Polynomial p : polys) {
            final Iterator it = p.indices();
            while (it.hasNext()) {
                final Object v = it.next();
                if (v instanceof orbital.math.Integer)
                    return 1;
                if (v instanceof Vector)
                    return ((Vector) v).dimension();
                throw new IllegalArgumentException("Don't know how to handle " + p);
            }
        }
        return 0;
    }

    /**
     * Turn a set of polynomials into an equivalent one that uses as few
     * variables as possible
     */
    private Set<Polynomial> eliminateUnusedVariables(Set<Polynomial> polys) {
        final int varNum = indexNum(polys);
        final BitSet occurring = new BitSet ();
        
        for (Polynomial p : polys) {
            final Iterator<Vector> indexIt = p.indices();
            final Iterator<Arithmetic> coeffIt = p.iterator();
            while (indexIt.hasNext()) {
                final Vector v = indexIt.next();
                final Arithmetic coeff = coeffIt.next();
                if (!coeff.isZero())
                    for (int i = 0; i < v.dimension(); ++i)
                        if (!v.get(i).isZero())
                            occurring.set(i);
            }
        }
        
        // ensure that there is at least one variable, otherwise Orbital
        // throws an exception later on
        if (occurring.isEmpty())
            occurring.set(0);

        final int newVarNum = occurring.cardinality();
        assert (newVarNum <= varNum);
        
        if (newVarNum == varNum)
            // nothing to be done
            return polys;

        final ValueFactory vf = Values.getDefault();
        final int[] newCoord = new int [newVarNum];
        final int[] oldCoord = new int [varNum];
        final int[] newDimensions = new int [newVarNum];
        
        final Set<Polynomial> res = new HashSet<Polynomial> ();
        for (Polynomial p : polys) {
            final Tensor pTensor = vf.asTensor(p);
            select(pTensor.dimensions(), newDimensions, occurring);
            
            final Tensor newTensor = vf.newInstance(newDimensions);
            Arrays.fill(newCoord, 0);
            
            // iterate over all components of the new tensor/polynomial and
            // fill it with the values from the old tensor
            while (true) {
                spread(newCoord, oldCoord, occurring);
                newTensor.set(newCoord, pTensor.get(oldCoord));
                
                int i = 0;
                for (; i < newVarNum; ++i) {
                    if (newCoord[i] < newDimensions[i] - 1) {
                        newCoord[i] = newCoord[i] + 1;
                        break;
                    } else {
                        assert (newCoord[i] == newDimensions[i] - 1);
                        newCoord[i] = 0;
                    }
                }
                if (i == newVarNum)
                    break;
            }
            
            res.add(vf.asPolynomial(newTensor));
        }
        
        return res;
    }

    private void spread(int[] input, int[] output, BitSet givenComponents) {
        assert (input.length <= output.length);
        
        int j = 0;
        for (int i = 0; i < output.length; ++i) {
            if (givenComponents.get(i)) {
                output[i] = input[j];
                j = j + 1;
            } else {
                output[i] = 0;
            }
        }
    }
    
    private void select(int[] input, int[] output, BitSet selectedComponents) {
        assert (output.length <= input.length);
        
        int j = 0;
        for (int i = 0; i < input.length; ++i) {
            if (selectedComponents.get(i)) {
                output[j] = input[i];
                j = j + 1;
            }
        }
    }
    
    /**
     * Given a set of polynomials, search for polynomials of the form
     * <code>a*x + t</code> (with <code>a</code> non-zero) and eliminate
     * the <code>x</code> from all polynomials. This is repeated until no
     * such linear variables are left.
     */
    private Set<Polynomial> eliminateLinearVariables(Set<Polynomial> polys) {
        final int varNum = indexNum(polys);
        Set<Polynomial> workPolys = new HashSet<Polynomial> (polys);
        
        while (true) {
            // search for a polynomial that contains a variable only in a linear
            // term
        
            int linVar = -1;
            Polynomial polyWithLinVar = null;
            for (Polynomial p : workPolys) {
                linVar = findLinearVariable(p, varNum);
                if (linVar >= 0) {
                    polyWithLinVar = p;
                    break;
                }
            }
     
            if (linVar < 0)
                // we did not find any linear variable, bail out
                return workPolys;
        
            System.out.println("eliminating " + linVar + " using " + polyWithLinVar);
            final Comparator order = lexVariableOrder(linVar, varNum);
            
            Set<Polynomial> reducePolys = new HashSet<Polynomial> ();
            reducePolys.add(polyWithLinVar);
        
//            reducePolys = AlgebraicAlgorithms.groebnerBasis(reducePolys, order);
            final Function reducer = AlgebraicAlgorithms.reduce(reducePolys, order);
        
            final Iterator<Polynomial> allPolysIt = workPolys.iterator();
            workPolys = new HashSet<Polynomial> ();
            while (allPolysIt.hasNext()) {
                final Polynomial reducedPoly =
                    (Polynomial)reducer.apply(allPolysIt.next());
                if (!reducedPoly.isZero())
                    workPolys.add(reducedPoly);
            }
        }
    }

    /**
     * Generate a lexicographic polynomial order in which the variable
     * <code>linVar</code> is the biggest variable
     */
    private Comparator lexVariableOrder(int linVar, final int varNum) {
        final int[] varOrderAr = new int[varNum];
        varOrderAr[0] = linVar;
        for (int i = 0; i < varNum; ++i) {
            if (i < linVar)
                varOrderAr[i+1] = i;
            if (i > linVar)
                varOrderAr[i] = i;
        }
        return AlgebraicAlgorithms.LEXICOGRAPHIC(varOrderAr);
    }


    private int findLinearVariable(Polynomial p, final int varNum) {
        final BitSet linearVars = new BitSet();
        final BitSet nonLinearVars = new BitSet();
        final Vector oneVec =
            Values.getDefault().CONST(varNum, Values.getDefault().ONE());
        
        final Iterator<Vector> indexIt = p.indices();
        final Iterator<Arithmetic> coeffIt = p.iterator();
        while (indexIt.hasNext()) {
            final Vector v = indexIt.next();
            final Arithmetic coeff = coeffIt.next();
            
            if (coeff.isZero())
                continue;
            
            final Arithmetic degree = v.multiply(oneVec);
            if (degree.isZero()) {
                // nothing
            } else if (degree.isOne()) {
                // linear term
                for (int i = 0; i < varNum; ++i)
                    if (!v.get(i).isZero())
                        linearVars.set(i);
            } else {
                // nonlinear term
                for (int i = 0; i < varNum; ++i)
                    if (!v.get(i).isZero())
                        nonLinearVars.set(i);
            }
        }
        
        linearVars.andNot(nonLinearVars);
        return linearVars.nextSetBit(0);
    }
    
    private Square[] checkSOS(Iterator<Vector> monomials,
                              Set<Polynomial> groebnerBasis,
                              Function groebnerReducer) {
        
        System.out.println("============ Searching for SOSs in the ideal");

        final Arithmetic two = Values.getDefault().rational(2);
        
        final List<Arithmetic> consideredMonomials = new ArrayList<Arithmetic> ();
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
                final Arithmetic oldMono = consideredMonomials.get(i);
                final Arithmetic combinedMonoExp = oldMono.add(newMono);
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
//            System.out.println(Arrays.toString(homo));
//            System.out.println(Arrays.toString(hetero));
            int sdpRes =
//                CSDP.robustSdp(monoNum, reducedPoly.size(), hetero, homo, approxSolution);
               CSDP.sdp(monoNum, reducedPoly.size(), hetero, homo, approxSolution);

            if (sdpRes == 0 || sdpRes == 3) {
                System.out.println("Found an approximate solution!");
                System.out.println(Arrays.toString(approxSolution));
                
                final Square[] squares =
                    approx2Exact(reducedPoly, consideredMonomials, approxSolution);
                if (squares != null)
                    return squares;
            } else {
                System.out.println("No solution");
            }
            
        }
        
        return null;
    }

    private static Square[] approx2Exact(SparsePolynomial reducedPoly,
                                         List<Arithmetic> consideredMonomials,
                                         double[] approxSolution) {
        final Vector exactHetero =
            Values.getDefault().newInstance(reducedPoly.size());
        for (int i = 0; i < exactHetero.dimension(); ++i)
            exactHetero.set(i, Values.getDefault().valueOf(i == 0 ? -1 : 0));
        return approx2Exact(reducedPoly, consideredMonomials, approxSolution, exactHetero);
    }

    public static Square[] approx2Exact(SparsePolynomial reducedPoly,
                                        List<Arithmetic> consideredMonomials,
                                        double[] approxSolution, Vector inExactHetero) {
        final int monoNum = consideredMonomials.size();

        System.out.println("Trying to recover an exact solution ...");
        final Matrix exactHomo =
            reducedPoly.exactCoefficientComparison(monoNum);
        
        // we add further constraints to ensure that the found solution
        // is a symmetric matrix
        exactHomo.insertRows(symmetryConstraints(monoNum));

        final Vector exactHetero =
            Values.getDefault().newInstance(exactHomo.dimensions()[0]);
        for (int i = 0; i < exactHetero.dimension(); ++i) {
            if(i < inExactHetero.dimension()) {
            	exactHetero.set(i, inExactHetero.get(i));
            } else {
            	exactHetero.set(i, Values.getDefault().valueOf(0));	
            }
        }
        
        double eps = 1;
        
        while (eps > 1e-6) {
            System.out.println();
            System.out.println("Trying eps: " + eps);

            final Vector exactSolution = new FractionisingEquationSolver(
                    exactHomo, exactHetero, approxSolution, eps).exactSolution;
            System.out.println(exactSolution);
            
            assert (exactHomo.multiply(exactSolution).equals(exactHetero));
            
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
                System.out.println("T-matrix:");
                System.out.println(dec.T);
                System.out.println("D-matrix:");
                System.out.println(dec.D);
                
                // generate the certificate (actual squares of polynomials)
                final Vector monomials =
                    Values.getDefault().newInstance(monoNum);
                for (int i = 0; i < monoNum; ++i) {
                    final Polynomial mono =
                        Values.getDefault().MONOMIAL(consideredMonomials.get(i));
                    monomials.set(i, mono);
                }

                final Polynomial zero =
                    Values.getDefault().MONOMIAL(Values.getDefault().ZERO(),
                                                 consideredMonomials.get(0).zero());
                
                final Square[] res = new Square [monoNum];
                for (int i = 0; i < monoNum; ++i) {
                    Polynomial p = zero;
                    for (int j = i; j < monoNum; ++j)
                        p = (Polynomial) p.add(monomials.get(j).scale(dec.T.get(i, j)));
                    res[i] = new Square (dec.D.get(i, i), p);
                }
                
                return res;
            } catch (NotPSDException e) {
                System.out.println(e.getMessage());
            }
            
            eps = eps / 10;
        }
        
        return null;
    }
    
    
    private static Matrix symmetryConstraints(int matrixSize) {
        final Arithmetic one = Values.getDefault().ONE();
        final Arithmetic minus_one = one.minus();

        final int height = matrixSize * (matrixSize - 1) / 2;
        final int width = matrixSize * matrixSize;
        final Matrix res = Values.getDefault().ZERO(height, width);
        
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
    
    
    public static class Square {
        public final Arithmetic coefficient;
        public final Polynomial body;
        public Square(Arithmetic coefficient, Polynomial body) {
            this.coefficient = coefficient;
            this.body = body;
        }
    }
    
    
    public static class SimpleMonomialIterator implements Iterator<Vector> {
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
