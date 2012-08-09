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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
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
import orbital.util.KeyValuePair;

import org.w3c.dom.Node;

import de.uka.ilkd.key.dl.arithmetics.IGroebnerBasisCalculator;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker.PolynomialClassification;
import de.uka.ilkd.key.dl.arithmetics.impl.csdp.CSDP;
import de.uka.ilkd.key.dl.arithmetics.impl.csdp.CSDPInterface;
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
 * Nullstellensatz and, thus, a complete method for proving the unsatisfiability
 * of a conjunction of equations, inequalities and disequations. The application
 * of the Nullstellensatz requires to synthesise a sum of squares s such that
 * 1+s is in the ideal described by the Groebner basis, which is done through
 * positive semi-definite programming.
 */
public class GroebnerBasisChecker implements IGroebnerBasisCalculator {

	private ValueFactory vf;
	private static final Comparator monomialOrder = AlgebraicAlgorithms.DEGREE_REVERSE_LEXICOGRAPHIC;

	public boolean checkForConstantGroebnerBasis(
			PolynomialClassification<Term> terms, Services services) {
		// we need to get a new value factory here... the options might be
		// changed so that we cannot use the initial one.
		vf = Values.getDefault();
		final Set<Polynomial> rawPolys = extractPolynomials(terms);
		System.out.println("Polynomials are: ");
		printPolys(rawPolys);

		final Set<Polynomial> groebnerBasis = createOptimiseGroebnerBasis(
				rawPolys, false);
		if (groebnerBasis == null)
			// this means we have already found a contradiction
			return true;
		if (groebnerBasis.isEmpty())
			return false;

		final Function groebnerReducer = orbital.math.AlgebraicAlgorithms
				.reduce(groebnerBasis, monomialOrder);

		// enumerate sums of squares s and check whether some monomial 1+s is
		// in the ideal
		final Iterator<Vector> monomials = new SimpleMonomialIterator(
				indexNum(groebnerBasis), 6);
		// final Square[] cert = checkSOS2(groebnerBasis, groebnerReducer);
		final Square[] cert = checkSOS(monomials, groebnerBasis,
				groebnerReducer);

		if (cert != null) {
			// check that the certificate is correct

			System.out.println("Certificate:");
			System.out.println(" 1");

			Polynomial p = (Polynomial) groebnerBasis.iterator().next().one();
			for (int i = 0; i < cert.length; ++i) {
				assert (Operations.greaterEqual.apply(cert[i].coefficient, vf
						.ZERO()));
				p = (Polynomial) p.add(cert[i].body.multiply(cert[i].body)
						.scale(cert[i].coefficient));
				System.out.println(" + " + cert[i].coefficient + " * ( "
						+ cert[i].body + " ) ^2");
			}
			System.out.println(" =");
			System.out.println(" " + p);
			assert (((Polynomial) groebnerReducer.apply(p)).isZero());
			System.out.println("Certificate is correct");
			return true;
		}
		return false;
	}

	Set<Polynomial> createOptimiseGroebnerBasis(Set<Polynomial> polys,
			boolean isGroebnerBasis) {
		int varNum = indexNum(polys);
		System.out.println("Eliminating inverses...");
		final Set<Polynomial> polys2 = eliminateInverses(polys, varNum);
		System.out.println("Polynomials after eliminating inverses are: ");
		printPolys(polys2);
		System.out.println("Eliminating linear variables...");
		final Set<Polynomial> polys3 = eliminateLinearVariables(polys2);
		System.out
				.println("Polynomials after eliminating linear variables are: ");
		printPolys(polys3);

		System.out.println("Eliminating even degree variables...");
		final Set<Polynomial> polys3point1 = eliminateEvenDegreeVariables(polys3);
		System.out
				.println("Polynomials after eliminating even degree variables are: ");
		printPolys(polys3point1);

		System.out.println("Eliminating sums of squares...");
		final Set<Polynomial> polys3point2 = eliminateSumsOfSquares(
				polys3point1, varNum);
		System.out.println("Polynomials after eliminating sums of squares: ");
		printPolys(polys3point2);

		System.out.println("Eliminating unused variables...");
		final Set<Polynomial> polys4 = eliminateUnusedVariables(polys3point2);
		System.out
				.println("Polynomials after eliminating unused variables are: ");
		printPolys(polys4);

		System.out.println("Testing if there was progress...");
		if (isGroebnerBasis && polys.equals(polys4))
			// nothing has changed
			return polys;

		if (polys4.isEmpty())
			// well, then we probably won't get a contradiction
			return polys4;

		final Polynomial one = (Polynomial) polys4.iterator().next().one();

		// we try to get a contradiction by computing the groebner basis of all
		// the equalities. if the common basis contains a constant part, the
		// equality system is unsatisfiable, thus we can close this goal
		System.out.println("Computing groebner basis...");
		final Set<Polynomial> groebnerBasis = orbital.math.AlgebraicAlgorithms
				.groebnerBasis(polys4, monomialOrder);
		final Function groebnerReducer = orbital.math.AlgebraicAlgorithms
				.reduce(groebnerBasis, monomialOrder);

		System.out.println("Groebner basis is: ");
		printPolys(groebnerBasis);

		final Polynomial oneReduced = (Polynomial) groebnerReducer.apply(one);
		if (oneReduced.isZero()) {
			System.out.println("Groebner basis is trivial and contains a unit");
			return null;
		}

		System.out.println("Iterating optimisation ...");
		return createOptimiseGroebnerBasis(groebnerBasis, true);
	}

	/**
	 * @param polys3
	 * @return TODO documentation since 24.04.2009
	 */
	private Set<Polynomial> eliminateSumsOfSquares(Set<Polynomial> polys,
			int varNum) {
		Set<Polynomial> workPolys = new HashSet<Polynomial>();
		final Integer two = vf.ONE().add(vf.ONE());
		final Vector oneVec = vf.CONST(varNum, vf.ONE());
		boolean changed = true;
		while (changed) {
			changed = false;
			polyloop: for (Polynomial p : polys) {
				if (p.isZero()) {
					// we do not need the zero polynomial in our generating set
					// for an ideal
					continue polyloop;
				}
				Iterator<KeyValuePair> monomialIt = p.monomials();
				int positiveCount = 0;
				int negativeCount = 0;
				while (monomialIt.hasNext()) {
					KeyValuePair nextMono = monomialIt.next();
					final Vector v = asVector((Arithmetic) nextMono.getKey());
					final Arithmetic coeff = (Arithmetic) nextMono.getValue();
					if (coeff.isZero())
						continue;
					if (coeff instanceof Comparable) {
						int compare = ((Comparable) coeff).compareTo(coeff
								.zero());
						if (compare < 0) {
							if (positiveCount > 0) {
								// we got a coefficient that is less than zero.
								// Therefore this optimization is not
								// applicable.
								workPolys.add(p);
								continue polyloop;
							} else {
								negativeCount++;
							}
						} else {
							positiveCount++;
						}
					} else {
						// we cannot do anything, if the coefficients are not
						// comparable
						return polys;
					}

					final Arithmetic degree = v.multiply(oneVec);
					if (degree.isZero()) {
						// nothing
					} else {
						for (int i = 0; i < varNum; ++i) {
							if (!v.get(i).isZero()) {
								if (!((Integer) v.get(i)).modulo(two).isZero()) {
									workPolys.add(p);
									continue polyloop;
								}
							}
						}
					}
				}
				if (negativeCount > 0 && positiveCount > 0) {
					workPolys.add(p);
					continue polyloop;
				}
				assert positiveCount > 0 || negativeCount > 0 : "Polynomial "
						+ p
						+ " does neither contain positive nor negative coefficients";
				// if -x^2 - y^2 is zero x^2 + y^2 is zero too
				if (positiveCount == 0) {
					p = (Polynomial) p.minus();
				}
				// at this point we know that we could reduce the degree of
				// every
				// monom by 2
				monomialIt = p.monomials();
				while (monomialIt.hasNext()) {
					final KeyValuePair nextMono = monomialIt.next();
					final Vector key = (Vector) asVector((Arithmetic) nextMono
							.getKey());
					int[] mono = new int[varNum];
					for (int i = 0; i < varNum; i++) {
						mono[i] = ((Integer) key.get(i)).intValue() / 2;
					}
					workPolys.add(vf.MONOMIAL((Arithmetic) nextMono.getValue(),
							mono));
				}
			}
			changed = !polys.equals(workPolys);
			if (changed) {
				System.out.println("Workpolys " + workPolys
						+ " is not equal to " + polys);
			}
			polys = workPolys;
			workPolys = new HashSet<Polynomial>();
		}
		return polys;
	}

	/**
	 * If polys contains a polynomial &alpha;<sub>0</sub>x<sup>2</sup> -
	 * &alpha;<sub>1</sub>m<sub>1</sub><sup>2</sup> - ... - -
	 * &alpha;<sub>n</sub>m<sub>n</sub><sup>2</sup> such that
	 * &alpha;<sub>i</sub> &gt; 0 for i &isin; {0,...,n}, where x only occurs
	 * with even degree in polys, then x can be eliminated by rewriting and the
	 * polynomial can be removed.
	 * 
	 * TODO: as we talk about an ideal a polynomial -
	 * &alpha;<sub>0</sub>x<sup>2</sup> +
	 * &alpha;<sub>1</sub>m<sub>1</sub><sup>2</sup> + ... - +
	 * &alpha;<sub>n</sub>m<sub>n</sub><sup>2</sup> could also be used for
	 * rewriting. This is not implemented yet.
	 * 
	 * @param polys
	 *            the set of polynomials to be reduce (will not be modified)
	 * @return the reduced set of polynomials
	 */
	private Set<Polynomial> eliminateEvenDegreeVariables(Set<Polynomial> polys) {
		return eliminateEvenDegreeVariables(polys, true);
	}

	private Set<Polynomial> eliminateEvenDegreeVariables(Set<Polynomial> polys,
			boolean reverse) {
		final int varNum = indexNum(polys);
		Set<Polynomial> workPolys = new HashSet<Polynomial>(polys);

		// while (true) {
		// search for a variable that only contains with even degree
		BitSet purlyEvenVariables = findPurlyEvenVariables(workPolys, varNum);

		if (purlyEvenVariables.isEmpty())
			// we did not find any variable that is purly even, bail out
			return workPolys;

		// find a polynomial where the variable occurs in the form a*x^2
		// -a_1*m_1^2 -...-a_n*m_n^2 with a,a_i>0
		final Vector oneVec = vf.CONST(varNum, vf.ONE());
		final Integer two = vf.ONE().add(vf.ONE());
		boolean changed = true;
		while (changed) {
			// repeat this loop as long as we were able to eliminate variables
			changed = false;
			outerloop: for (Polynomial p : workPolys) {
				Iterator<KeyValuePair> monomialIt = p.monomials();

				// the following two variables store a monomial a*x^2
				java.lang.Integer candidate = null;
				Arithmetic candidateCoeff = null;
				// the following loop has to collect the following informations:
				// - does the polynomial only contain square monomials
				// - does one of the square monomials only contain one variable
				// - are all coefficients of the other monomials negative
				while (monomialIt.hasNext()) {
					KeyValuePair nextMono = monomialIt.next();
					final Vector v = asVector((Arithmetic) nextMono.getKey());
					final Arithmetic coeff = (Arithmetic) nextMono.getValue();

					if (coeff.isZero())
						continue;

					final Arithmetic degree = v.multiply(oneVec);
					int varsInMonom = 0;
					int possibleCandidate = -1;
					boolean positive = false;
					if (coeff instanceof Comparable) {
					    int smallerZero = ((Comparable) coeff).compareTo(vf.ZERO());
					    if (smallerZero > 0) {
					        positive = true;
					    }
					} else {
					    // we cannot do anything, as we cannot
					    // compare the coefficients
					    return workPolys;
					}
					for (int i = 0; i < varNum; i++) {
					    if (!v.get(i).isZero()) {
					        varsInMonom++;
					        if (v.get(i).equals(two)) {
					            // if the exponent is two, we might have a
					            // candidate if this is the only variable
					            // with
					            // non-zero exponent in this monomial
					            possibleCandidate = i;
					        }
					        if (!((Integer) v.get(i)).modulo(two).isZero()) {
					            // assure that every exponent is an event
					            // number
					            continue outerloop;
					        }
					    }
					}
					if (varsInMonom != 1 && positive) {
					    continue outerloop;
					}
					if (varsInMonom == 1 && positive) {
					    if (possibleCandidate == -1) {
					        continue outerloop;
					    }
					    if (purlyEvenVariables.get(possibleCandidate)) {
					        if (candidate != null) {
					            continue outerloop;
					        } else {
					            candidate = possibleCandidate;
					            candidateCoeff = coeff;
					        }
					    }
					}
				}
				if (candidate != null && candidate != -1) {
					// we have found a candidate
					int[] tmp = new int[varNum];
					tmp[candidate] = 1;
					System.out.println("Eliminate variable " + vf.MONOMIAL(tmp)
							+ " of polynomial " + p);// XXX
					// from this candidate we get a resulting rewriting of the
					// form
					// a_1/a*m_1^2 + ... + a_n/a*m_n^2

					// Therefore, we need to assure that the variable only
					// occurs in
					// the monomial with positive coefficient. Otherwise, we
					// would
					// not be able to eliminate the variable.
					monomialIt = p.monomials();
					int occurencesOfCandidate = 0;
					Polynomial replacement = (Polynomial) p.zero();
					while (monomialIt.hasNext()) {
						KeyValuePair nextMono = monomialIt.next();
						final Vector v = asVector((Arithmetic) nextMono
								.getKey());
						final Arithmetic coeff = (Arithmetic) nextMono
								.getValue();

						if (coeff.isZero())
							continue;
						if (!v.get(candidate).isZero()) {
							occurencesOfCandidate++;
						} else {
							replacement = (Polynomial) replacement.add((vf
									.MONOMIAL(asVector((Arithmetic) nextMono
											.getKey()))).multiply(vf.MONOMIAL(
									coeff.divide(candidateCoeff),
									new int[varNum])));
						}
					}
					if (occurencesOfCandidate > 1) {
						// we cannot rewrite using this candidate, therefore
						// continue the search
						continue outerloop;
					}
					assert occurencesOfCandidate == 1 : "A variable in a polynomial cannot disappear.";
					// now replacement contains the polynomial we use for
					// rewriting
					workPolys = rewritePolynomials(varNum, Collections
							.singletonMap(candidate, p), workPolys);
					changed = true;
					break outerloop;
				}
			}
		}
		// try the same optimization for the additive inverses of the
		// polynomials
		if (reverse) {
			Set<Polynomial> newWorkPolys = new HashSet<Polynomial>();
			for (Polynomial p : workPolys) {
				newWorkPolys.add((Polynomial) p.minus());
			}
			newWorkPolys = eliminateEvenDegreeVariables(newWorkPolys, false);
			workPolys.clear();
			for (Polynomial p : newWorkPolys) {
				workPolys.add((Polynomial) p.minus());
			}
		}
		return workPolys;
	}

	/**
	 * @param varToEliminate
	 * @param rewrite
	 * @param p
	 */
	private Set<Polynomial> rewritePolynomials(final int varCount,
			final Map<java.lang.Integer, Polynomial> rewrite,
			Set<Polynomial> polys) {
		final HashSet<Polynomial> workPolys = new HashSet<Polynomial>();
		for (int variable : rewrite.keySet()) {
			Polynomial polynomial = rewrite.get(variable);
			System.out.println("eliminating " + variable + " using "
					+ polynomial);
			final Comparator order = lexVariableOrder(variable, varCount);

			final Function reducer = AlgebraicAlgorithms.reduce(Collections
					.singleton(polynomial), order);
			for (Polynomial p : polys) {
				p = (Polynomial) reducer.apply(p);
				if (!p.isZero()) {
					if (p.degree().isZero()) {
						// we have found a unit and can stop
						workPolys.clear();
						workPolys.add(p);
						return workPolys;
					}
					workPolys.add(p);
				}
			}
			polys = new HashSet<Polynomial>(workPolys);
		}
		return workPolys;
	}

	private void printPolys(Set<Polynomial> rawPolys) {
		for (Polynomial p : rawPolys)
			System.out.println(p);
	}

	private Set<Polynomial> extractPolynomials(
			PolynomialClassification<Term> terms) {
		final TermBuilder TB = TermBuilder.DF;

		final Term zero = TB.func(NumberCache.getNumber(new BigDecimal(0),
				RealLDT.getRealSort()));
		final Term one = TB.func(NumberCache.getNumber(new BigDecimal(1),
				RealLDT.getRealSort()));
		final TermSymbol minus = RealLDT
				.getFunctionFor(de.uka.ilkd.key.dl.model.Minus.class);
		final TermSymbol mul = RealLDT
				.getFunctionFor(de.uka.ilkd.key.dl.model.Mult.class);

		final Set<Term> equations = new HashSet<Term>();
		equations.addAll(terms.h);

		// we assume that inequalities have already been transformed away
		// (represented as equations)
		assert (terms.f.isEmpty());

		// let's also get rid of the disequations: p != 0 <=> \exists x; p*x = 1
		System.out.println("terms.g: " + terms.g);
		System.out.println("equations: " + equations);
		int i = 0;
		for (Term g : terms.g) {
			assert (g.op() == RealLDT.getFunctionFor(Unequals.class));
			final Term diff = TB.func(minus, g.sub(0), g.sub(1));
			final LogicVariable x = new LogicVariable(new Name(
					"invNobodyElseKnowsThisName" + i), RealLDT.getRealSort());
			final Term lhs = TB.func(minus, TB.func(mul, diff, TB.var(x)), one);
			equations.add(TB.equals(lhs, zero));
			i = i + 1;
		}

		System.out.println("equations: " + equations);
		final PolynomialClassification<Term> equationsOnly = new PolynomialClassification<Term>(
				new HashSet<Term>(), new HashSet<Term>(), equations);
		//TODO: the SumOfSquaresChecker might use another ValueFactory... this might cause trouble
		return SumOfSquaresChecker.classify(equationsOnly).h;
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
				throw new IllegalArgumentException("Don't know how to handle "
						+ p);
			}
		}
		return 0;
	}

	private BitSet occurringVars(Polynomial p) {
		final BitSet res = new BitSet();
		final Iterator<KeyValuePair> monomialIt = p.monomials();
		while (monomialIt.hasNext()) {
			KeyValuePair nextMono = monomialIt.next();
			final Vector v = asVector((Arithmetic) nextMono.getKey());
			final Arithmetic coeff = (Arithmetic) nextMono.getValue();
			if (!coeff.isZero())
				for (int i = 0; i < v.dimension(); ++i)
					if (!v.get(i).isZero())
						res.set(i);
		}
		return res;
	}

	/**
	 * Turn a set of polynomials into an equivalent one that uses as few
	 * variables as possible
	 */
	private Set<Polynomial> eliminateUnusedVariables(Set<Polynomial> polys) {
		final int varNum = indexNum(polys);
		if (varNum == 0)
			// nothing to do
			return polys;

		final BitSet occurring = new BitSet();
		for (Polynomial p : polys)
			occurring.or(occurringVars(p));

		// ensure that there is at least one variable, otherwise Orbital
		// throws an exception later on
		if (occurring.isEmpty())
			occurring.set(0);

		final int newVarNum = occurring.cardinality();
		assert (newVarNum <= varNum);

		if (newVarNum == varNum)
			// nothing to be done
			return polys;

		final int[] newCoord = new int[newVarNum];
		final int[] oldCoord = new int[varNum];
		final int[] newDimensions = new int[newVarNum];

		final Set<Polynomial> res = new HashSet<Polynomial>();
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
	 * <code>a*x + t</code> (with <code>a</code> non-zero) and eliminate the
	 * <code>x</code> from all polynomials. This is repeated until no such
	 * linear variables are left.
	 */
	private Set<Polynomial> eliminateLinearVariables(Set<Polynomial> polys) {
		final int varNum = indexNum(polys);
		Set<Polynomial> workPolys = new HashSet<Polynomial>(polys);

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

			workPolys = rewritePolynomials(varNum, Collections.singletonMap(
					linVar, polyWithLinVar), workPolys);
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
				varOrderAr[i + 1] = i;
			if (i > linVar)
				varOrderAr[i] = i;
		}
		return AlgebraicAlgorithms.LEXICOGRAPHIC(varOrderAr);
	}

	private int findLinearVariable(Polynomial p, final int varNum) {
		final BitSet linearVars = new BitSet();
		final BitSet nonLinearVars = new BitSet();
		final Vector oneVec = vf.CONST(varNum, vf.ONE());

		final Iterator<KeyValuePair> monomialIt = p.monomials();
		Arithmetic compare = null;
		while (monomialIt.hasNext()) {
			KeyValuePair nextMono = monomialIt.next();
			if (compare == null) {
				compare = (Arithmetic) vf.MONOMIAL((Arithmetic) nextMono
						.getValue(), (Arithmetic) nextMono.getKey());
			} else {
				compare = compare.add((Arithmetic) vf.MONOMIAL(
						(Arithmetic) nextMono.getValue(), (Arithmetic) nextMono
								.getKey()));
			}
			final Vector v = asVector((Arithmetic) nextMono.getKey());
			final Arithmetic coeff = (Arithmetic) nextMono.getValue();

			if (coeff.isZero())
				continue;

			final Arithmetic degree = v.multiply(oneVec);
			// System.out.println("Degree of " + v + " is " + degree);
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
		assert compare.equals(p) : "Polynomial " + compare
				+ " is not equals to its creating polynomial " + p;
		linearVars.andNot(nonLinearVars);
		return linearVars.nextSetBit(0);
	}

	private BitSet findPurlyEvenVariables(Set<Polynomial> polys,
			final int varNum) {
		final Integer two = vf.ONE().add(vf.ONE());
		final BitSet purlyEvenVars = new BitSet(varNum);
		final BitSet nonPurlyEvenVars = new BitSet(varNum);
		final Vector oneVec = vf.CONST(varNum, vf.ONE());
		for (Polynomial p : polys) {
			final Iterator<KeyValuePair> monomialIt = p.monomials();
			while (monomialIt.hasNext()) {
				KeyValuePair nextMono = monomialIt.next();
				final Vector v = asVector((Arithmetic) nextMono.getKey());
				final Arithmetic coeff = (Arithmetic) nextMono.getValue();

				if (coeff.isZero())
					continue;

				final Arithmetic degree = v.multiply(oneVec);
				if (degree.isZero()) {
					// nothing
				} else {
					for (int i = 0; i < varNum; ++i) {
						if (!v.get(i).isZero()) {
							if (((Integer) v.get(i)).modulo(two).isZero()) {
								purlyEvenVars.set(i);
							} else {
								nonPurlyEvenVars.set(i);
							}
						}
					}
				}
			}
		}

		purlyEvenVars.andNot(nonPurlyEvenVars);
		return purlyEvenVars;
	}

	/**
	 * Eliminate polynomials of the form <code>x*y - a</code> (where
	 * <code>a</code> is a unit), provided that some other polynomial
	 * <code>t + x^n</code> exists.
	 */
	private Set<Polynomial> eliminateInverses(Set<Polynomial> polys, int varNum) {

		Set<Polynomial> workPolys = new HashSet<Polynomial>(polys);

		while (true) {
			final BitSet pureVars = findPureTerms(workPolys, varNum);
			if (pureVars.isEmpty())
				return workPolys;

			// check whether we have some polynomial x*y - a, where x or y
			// is pure
			Polynomial inversDef = null;
			BitSet productVars = null;
			int eliminableVar = -1;
			for (Polynomial p : workPolys) {
				productVars = isInversDefinition(p, varNum);
				if (productVars == null)
					continue;

				final BitSet pureProductVar = (BitSet) productVars.clone();
				pureProductVar.and(pureVars);
				if (pureProductVar.isEmpty())
					continue;

				// we have found everything we need
				eliminableVar = pureProductVar.nextSetBit(0);
				inversDef = p;
				break;
			}

			if (productVars == null)
				return workPolys;

			// inversDef is now some polynomial x*y - a, and we know that
			// eliminableVar occurs in some other polynomial t + eliminableVar^n

			productVars.clear(eliminableVar);
			final int inversVar = productVars.nextSetBit(0);
			final Vector inversVarExp = vf.ZERO(varNum);
			inversVarExp.set(inversVar, vf.ONE());
			final Polynomial inversVarPoly = vf.MONOMIAL(inversVarExp);

			final List<Polynomial> reducingPolys = new ArrayList<Polynomial>();
			reducingPolys.add(inversDef);
			final Function reducer = AlgebraicAlgorithms.reduce(reducingPolys,
					monomialOrder);

			final Iterator<Polynomial> allPolysIt = workPolys.iterator();
			workPolys = new HashSet<Polynomial>();
			while (allPolysIt.hasNext()) {
				final Polynomial p = allPolysIt.next();

				if (p == inversDef)
					// we can simple drop this polynomial
					continue;

				// eliminate the eliminable variable
				Polynomial newP = p;
				while (occurringVars(newP).get(eliminableVar)) {
					newP = newP.multiply(inversVarPoly);
					newP = (Polynomial) reducer.apply(newP);
				}

				workPolys.add(newP);
			}
		}

	}

	/**
	 * Check whether
	 * <code>p<code> is a polynomial of the form <code>x*y-a</code> with
	 * <code>a</code> a unit. In this case, return the two variables contained
	 * in the polynomial; otherwise, return <code>null</code>
	 */
	private BitSet isInversDefinition(Polynomial p, int varNum) {
		final Vector oneVec = vf.CONST(varNum, vf.ONE());

		Arithmetic constantTerm = null;
		int nonConstantNum = 0;
		final BitSet productVars = new BitSet();

		final Iterator<KeyValuePair> monomialIt = p.monomials();
		while (monomialIt.hasNext()) {
			KeyValuePair nextMono = monomialIt.next();
			final Vector v = asVector((Arithmetic) nextMono.getKey());
			final Arithmetic coeff = (Arithmetic) nextMono.getValue();

			if (coeff.isZero())
				continue;

			final Arithmetic degree = v.multiply(oneVec);
			if (degree.isZero()) {
				assert constantTerm == null;
				constantTerm = coeff;
			} else {
				nonConstantNum = nonConstantNum + 1;
			}

			if (degree.equals(vf.valueOf(2))) {
				for (int i = 0; i < v.dimension(); ++i) {
					if (v.get(i).isOne())
						productVars.set(i);
					else if (!v.get(i).isZero())
						break;
				}
			}
		}

		if (constantTerm != null && !constantTerm.isZero()
				&& nonConstantNum == 1 && !productVars.isEmpty()) {
			assert productVars.cardinality() == 2;
			return productVars;
		}

		return null;
	}

	/**
	 * Find variables <code>x</code> that occur as monomials <code>x^n</code> in
	 * some polynomial <code>t + x^n</code>, such that <code>x^n</code> does not
	 * divide any monomial in <code>t</code>
	 */
	private BitSet findPureTerms(Set<Polynomial> polys, int varNum) {
		final BitSet res = new BitSet();

		for (Polynomial p : polys) {
			final int[] pureMaxPowers = new int[varNum];
			final int[] imPureMaxPowers = new int[varNum];

			final Iterator<KeyValuePair> monomialIt = p.monomials();
			while (monomialIt.hasNext()) {
				KeyValuePair nextMono = monomialIt.next();
				final Vector v = asVector((Arithmetic) nextMono.getKey());
				final Arithmetic coeff = (Arithmetic) nextMono.getValue();

				if (coeff.isZero())
					continue;

				// check how many different variables occur in this monomial
				int num = 0;
				for (int i = 0; i < v.dimension(); ++i)
					if (!v.get(i).isZero())
						num = num + 1;

				if (num < 1)
					continue;

				final int[] relevantPowers = num == 1 ? pureMaxPowers
						: imPureMaxPowers;

				for (int i = 0; i < varNum; ++i) {
					final int pow = ((Integer) v.get(i)).intValue();
					relevantPowers[i] = Math.max(relevantPowers[i], pow);
				}
			}

			for (int i = 0; i < varNum; ++i)
				if (pureMaxPowers[i] > imPureMaxPowers[i])
					res.set(i);
		}

		return res;
	}

	private static Vector asVector(Arithmetic exp) {
		if (exp instanceof Vector)
			return (Vector) exp;
		else if (exp instanceof Integer)
			return Values.getDefault().valueOf(new Arithmetic[] { exp });
		else
			assert false;
		return null;
	}

	private Square[] checkSOS(Iterator<Vector> monomials,
			Set<Polynomial> groebnerBasis, Function groebnerReducer) {

		System.out.println("============ Searching for SOSs in the ideal");

		final SOSChecker checker = new SOSChecker(groebnerReducer,
				new AddedMonomialListener() {
					public void addedMonomial(Arithmetic v) {
					}
				});

		while (monomials.hasNext()) {
			checker.addMonomial(monomials.next());
			final Square[] squares = checker.check();
			if (squares != null) {
				System.out.println("Considered " + checker.monomialsNum()
						+ " monomials");
				return squares;
			}
		}

		return null;
	}

	private Square[] checkSOS2(Set<Polynomial> groebnerBasis,
			Function groebnerReducer) {

		System.out.println("============ Searching for SOSs in the ideal");

		final MonomialFactorQueue queue = new MonomialFactorQueue();
		for (Polynomial p : groebnerBasis) {
			final Arithmetic leadingMono = (Arithmetic) Collections.max(
					AlgebraicAlgorithms.occurringMonomials(p), monomialOrder);
			queue.addFactors(leadingMono, 0);
		}

		final int[] i = new int[1];

		final SOSChecker checker = new SOSChecker(groebnerReducer,
				new AddedMonomialListener() {
					public void addedMonomial(Arithmetic v) {
						queue.addFactors(v, i[0]);
					}
				});

		while (!queue.isEmpty()) {
			final Vector mono = queue.poll();
			checker.addMonomial(mono);
			final Square[] squares = checker.check();
			if (squares != null) {
				System.out.println("Considered " + checker.monomialsNum()
						+ " monomials");
				return squares;
			}
			i[0] = i[0] + 1;
		}

		return null;
	}

	private static class MonomialFactorQueue {
		final PriorityQueue<MonomialToDo> queue = new PriorityQueue<MonomialToDo>();

		// set of all monomials that we have already put in the queue
		// this set is closed under factors, i.e., if m is in the set then also
		// all factors of m
		final Set<Vector> putIntoQueue = new HashSet<Vector>();

		public void addFactors(Arithmetic x, int time) {
			final Vector mono = asVector(x);

			if (putIntoQueue.contains(mono))
				return;

			final Iterator<Vector> it = new MonomialFactorIterator(mono);
			while (it.hasNext()) {
				final Vector v = it.next();
				if (!putIntoQueue.contains(v)) {
					putIntoQueue.add(v);
					queue.add(new MonomialToDo(v, time));
				}
			}
		}

		public boolean isEmpty() {
			return queue.isEmpty();
		}

		public Vector poll() {
			return queue.poll().monomial;
		}
	}

	private static class MonomialToDo implements Comparable<MonomialToDo> {
		public final Vector monomial;
		public final int cost;

		public MonomialToDo(Vector monomial, int time) {
			this.monomial = monomial;
			Arithmetic degree = Values.getDefault().ZERO();
			for (int i = 0; i < monomial.dimension(); ++i)
				degree = degree.add(monomial.get(i));
			this.cost = time + 10 * ((Integer) degree).intValue();
		}

		public int compareTo(MonomialToDo o) {
			return this.cost - o.cost;
		}
	}

	// //////////////////////////////////////////////////////////////////////////

	private interface AddedMonomialListener {
		void addedMonomial(Arithmetic v);
	}

	private class SOSChecker {

		private final Function groebnerReducer;

		private final Arithmetic two = vf.rational(2);

		private final AddedMonomialListener monoListener;

		private final List<Arithmetic> consideredMonomials = new ArrayList<Arithmetic>();
		private final SparsePolynomial reducedPoly = new SparsePolynomial();
		private int currentParameter = 0;

		public SOSChecker(Function groebnerReducer,
				AddedMonomialListener monoListener) {
			this.groebnerReducer = groebnerReducer;
			this.monoListener = monoListener;
		}

		public int monomialsNum() {
			return consideredMonomials.size();
		}

		public void addMonomial(Vector newMono) {
			consideredMonomials.add(newMono);
			System.out.println("Adding a monomial: " + newMono + ", "
					+ vf.MONOMIAL(newMono));

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
					combinedMono = vf.MONOMIAL(two, combinedMonoExp);
				else
					combinedMono = vf.MONOMIAL(combinedMonoExp);

				final Polynomial reducedMono = (Polynomial) groebnerReducer
						.apply(combinedMono);
				System.out.println("Reduced " + combinedMono + " to "
						+ reducedMono);
				for (Object v : AlgebraicAlgorithms
						.occurringMonomials(reducedMono))
					monoListener.addedMonomial((Arithmetic) v);

				reducedPoly.addTerms(reducedMono, currentParameter);

				currentParameter = currentParameter + 1;
			}

			// System.out.println(reducedPoly);
		}

		public Square[] check() {
			final int monoNum = consideredMonomials.size();
			final double[] homo = reducedPoly.coefficientComparison(monoNum);

			// the inhomogeneous part of the system of equations
			final double[] hetero = new double[reducedPoly.size()];
			Arrays.fill(hetero, 0.0);
			hetero[0] = -1.0; // we have to check that 1+s is in the ideal,
			// hence a one

			final double[] approxSolution = new double[monoNum * monoNum];
			// System.out.println(Arrays.toString(homo));
			// System.out.println(Arrays.toString(hetero));

			/*
			 * int sdpRes = CSDP.sdp(monoNum, homo, hetero, approxSolution);
			 * 
			 * if (sdpRes == 0 || sdpRes == 3) {
			 * System.out.println("Found an approximate solution!");
			 * System.out.println(Arrays.toString(approxSolution));
			 * 
			 * final Square[] squares = approx2Exact(reducedPoly,
			 * consideredMonomials, approxSolution); if (squares != null) return
			 * squares; } else { System.out.println("No solution"); }
			 */
			final BitSet removedMonomials = new BitSet();
			int sdpRes = CSDPInterface.solveAndMinimiseSdp(monoNum, homo,
					hetero, approxSolution, removedMonomials);
			final double[] smallApproxSolution = new double[(monoNum - removedMonomials
					.cardinality())
					* (monoNum - removedMonomials.cardinality())];
			System.arraycopy(approxSolution, 0, smallApproxSolution, 0,
					smallApproxSolution.length);

			// int sdpRes = CSDP.minimalSdp(monoNum, homo, hetero,
			// approxSolution);
			// int sdpRes = CSDP.sdp(monoNum, homo, hetero, approxSolution);

			if (sdpRes == 0 // || sdpRes == 3
			) {
				System.out.println("Found an approximate solution!");
				System.out.println(Arrays.toString(smallApproxSolution));

				return approx2Exact(reducedPoly, consideredMonomials,
						removedMonomials, smallApproxSolution);
			} else {
				System.out.println("No solution");
				return null;
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////

	private static Square[] approx2Exact(SparsePolynomial reducedPoly,
			List<Arithmetic> consideredMonomials, BitSet removedMonomials,
			double[] approxSolution) {
		final ValueFactory vf = Values.getDefault();
		final Vector exactHetero = vf.newInstance(reducedPoly.size());
		for (int i = 0; i < exactHetero.dimension(); ++i)
			exactHetero.set(i, vf.valueOf(i == 0 ? -1 : 0));
		return approx2Exact(reducedPoly, consideredMonomials, removedMonomials,
				approxSolution, exactHetero);
	}

	public static Square[] approx2Exact(SparsePolynomial reducedPoly,
			List<Arithmetic> consideredMonomials, BitSet removedMonomials,
			double[] approxSolution, Vector sideConstraintRhss) {

		System.out.println("Trying to recover an exact solution ...");
		final Matrix exactHomo = reducedPoly.exactCoefficientComparison(
				consideredMonomials.size(), removedMonomials);

		return approx2Exact(consideredMonomials, removedMonomials,
				approxSolution, exactHomo, sideConstraintRhss);
	}

	public static Square[] approx2Exact(List<Arithmetic> consideredMonomials,
			double[] approxSolution, Matrix exactHomo, Vector sideConditionRhs) {
		return approx2Exact(consideredMonomials, new BitSet(), approxSolution,
				exactHomo, sideConditionRhs);
	}

	private static Square[] approx2Exact(List<Arithmetic> consideredMonomials,
			BitSet removedMonomials, double[] approxSolution, Matrix exactHomo,
			Vector sideConditionRhs) {
		final ValueFactory vf = Values.getDefault();
		final int monoNum = consideredMonomials.size()
				- removedMonomials.cardinality();
		assert (exactHomo.dimensions()[1] == monoNum * monoNum);

		// we add further constraints to ensure that the found solution
		// is a symmetric matrix
		exactHomo.insertRows(symmetryConstraints(monoNum));

		System.out.println("sideConstraintRhss: " + sideConditionRhs);// XXX
		final Vector exactHetero = vf.newInstance(exactHomo.dimensions()[0]);
		for (int i = 0; i < exactHetero.dimension(); ++i) {
			if (i < sideConditionRhs.dimension()) {
				exactHetero.set(i, sideConditionRhs.get(i));
			} else {
				exactHetero.set(i, vf.valueOf(0));
			}
		}
		System.out.println("hetero exact: " + exactHetero);// XXX
		double eps = 1;

		while (eps > 1e-6) {
			System.out.println();
			System.out.println("Trying eps: " + eps);

			final Vector exactSolution = new FractionisingEquationSolver(
					exactHomo, exactHetero, approxSolution, eps).exactSolution;
			System.out.println(exactSolution);

			assert (exactHomo.multiply(exactSolution).equals(exactHetero));

			System.out.println("Difference to approx solution:");
			System.out.println(exactSolution.subtract(vf
					.valueOf(approxSolution)));

			// check that the solution is positive semi-definite
			final Matrix solutionMatrix = vf.newInstance(monoNum, monoNum);
			for (int i = 0; i < monoNum; ++i)
				for (int j = 0; j < monoNum; ++j)
					solutionMatrix
							.set(i, j, exactSolution.get(i * monoNum + j));

			System.out.println(solutionMatrix);

			try {
				final PSDDecomposition dec = new PSDDecomposition(
						solutionMatrix);
				System.out.println("Solution is positive semi-definite");
				System.out.println("T-matrix:");
				System.out.println(dec.T);
				System.out.println("D-matrix:");
				System.out.println(dec.D);

				// generate the certificate (actual squares of polynomials)
				final Vector monomials = vf.newInstance(monoNum);
				for (int i = 0, j = 0; i < consideredMonomials.size(); ++i) {
					if (removedMonomials.get(i))
						continue;
					monomials.set(j, vf.MONOMIAL(consideredMonomials.get(i)));
					j = j + 1;
				}

				final Polynomial zero = vf.MONOMIAL(vf.ZERO(),
						consideredMonomials.get(0).zero());

				final Square[] res = new Square[monoNum];
				for (int i = 0; i < monoNum; ++i) {
					Polynomial p = zero;
					for (int j = i; j < monoNum; ++j)
						p = (Polynomial) p.add(monomials.get(j).scale(
								dec.T.get(i, j)));
					res[i] = new Square(dec.D.get(i, i), p);
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
		final ValueFactory vf = Values.getDefault();

		final Arithmetic one = vf.ONE();
		final Arithmetic minus_one = one.minus();

		final int height = matrixSize * (matrixSize - 1) / 2;
		final int width = matrixSize * matrixSize;

		if (height == 0)
			// just return a zero-matrix so that we don't have to come back
			// empty-handed
			return vf.ZERO(1, width);

		final Matrix res = vf.ZERO(height, width);

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

	// //////////////////////////////////////////////////////////////////////////

	public static class MonomialFactorIterator implements Iterator<Vector> {
		private final ValueFactory vf = Values.getDefault();

		private final Vector degreeBounds;
		private final Vector currentFactor;
		private boolean finished = false;

		public MonomialFactorIterator(Vector degreeBounds) {
			this.degreeBounds = degreeBounds;
			this.currentFactor = (Vector) degreeBounds.zero();
		}

		public boolean hasNext() {
			return !finished;
		}

		public Vector next() {
			final Vector res = (Vector) currentFactor.clone();
			for (int i = 0; i < degreeBounds.dimension(); ++i) {
				if (Operations.less.apply(currentFactor.get(i), degreeBounds
						.get(i))) {
					currentFactor.set(i, currentFactor.get(i).add(vf.ONE()));
					return res;
				}
				assert currentFactor.get(i).equals(degreeBounds.get(i));
				currentFactor.set(i, vf.ZERO());
			}
			finished = true;
			return res;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	// //////////////////////////////////////////////////////////////////////////

	public static class SimpleMonomialIterator implements Iterator<Vector> {
		private final ValueFactory vf = Values.getDefault();

		private final int indexNum;
		private final int maxTotalDegree;

		private final List<Vector> currentExps = new ArrayList<Vector>();
		private int currentTotalDegree = 0;

		private final Vector zeroMonomial;
		private final List<Vector> linearMonomials = new ArrayList<Vector>();

		public SimpleMonomialIterator(int indexNum, int maxTotalDegree) {
			this.indexNum = indexNum;
			this.maxTotalDegree = indexNum > 0 ? maxTotalDegree : 0;

			final Arithmetic[] exps = new Arithmetic[indexNum];
			Arrays.fill(exps, vf.ZERO());
			zeroMonomial = vf.tensor(exps);

			for (int i = 0; i < indexNum; ++i) {
				final Arithmetic[] exps2 = new Arithmetic[indexNum];
				Arrays.fill(exps2, vf.ZERO());
				exps2[i] = vf.ONE();
				linearMonomials.add(vf.tensor(exps2));
			}

			for (Vector v : linearMonomials) {
				System.out.println(v);
			}

			currentExps.add(zeroMonomial);
		}

		public boolean hasNext() {
			return !currentExps.isEmpty()
					|| currentTotalDegree < maxTotalDegree;
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

	// //////////////////////////////////////////////////////////////////////////

	public GroebnerBasisChecker(Node node) {
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
		return "Groebnerian SOS";
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
		return null;
	}

	@Override
	public Term polynomialReduce(Term poly, Term[] reductions, Services services)
			throws RemoteException, SolverException {
		// TODO Auto-generated method stub
		return null;
	}

}
