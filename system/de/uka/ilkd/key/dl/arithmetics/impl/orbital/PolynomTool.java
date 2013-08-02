/***************************************************************************
 *   Copyright (C) 2008 by Jan-David Quesel                                *
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import orbital.math.Arithmetic;
import orbital.math.Fraction;
import orbital.math.Integer;
import orbital.math.Polynomial;
import orbital.math.Values;
import orbital.math.Vector;
import orbital.math.functional.Operations;
import orbital.util.KeyValuePair;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import de.uka.ilkd.key.dl.formulatools.collector.AllCollector;
import de.uka.ilkd.key.dl.formulatools.collector.FilterVariableSet;
import de.uka.ilkd.key.dl.formulatools.collector.FoundItem;
import de.uka.ilkd.key.dl.formulatools.collector.filter.FilterVariableCollector;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Div;
import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.dl.model.Greater;
import de.uka.ilkd.key.dl.model.GreaterEquals;
import de.uka.ilkd.key.dl.model.Less;
import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.dl.model.Minus;
import de.uka.ilkd.key.dl.model.MinusSign;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.dl.model.Plus;
import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Equality;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.TermSymbol;
import de.uka.ilkd.key.logic.sort.Sort;

/**
 * This class provides static functions for creating polynomial objects provided
 * by the Orbital library.
 * 
 * @author jdq
 */
public abstract class PolynomTool {

	/**
	 * Given a fraction of polynomials this function creates a Fraction object
	 * provided by the Orbital library.
	 * 
	 * @param sub
	 * @param variables
	 * @return
	 */
	public static Fraction createFractionOfPolynomialsFromTerm(Term sub,
			List<String> variables) {
		if (sub.arity() == 0) {
			int[] size = new int[variables.size()];
			if (variables.contains(sub.op().name().toString())) {
				size[variables.indexOf(sub.op().name().toString())] = 1;
				return Values.getDefault().fraction(
						Values.getDefault().MONOMIAL(size),
						Values.getDefault().MONOMIAL(size).one());
			} else {
				try {
                    return Values.getDefault().fraction(
                    		Values.getDefault().MONOMIAL(
                    				OrbitalSimplifier.term2Rational(sub), size),
                    		Values.getDefault().MONOMIAL(size).one());
                } catch (UnableToConvertInputException e) {
                    throw new IllegalArgumentException(e);
                } catch (UnsupportedOperationException e) {
                    throw new IllegalArgumentException(e);
                }
			}
		} else {
			if (sub.arity() == 2) {
				Fraction p = createFractionOfPolynomialsFromTerm(sub.sub(0),
						variables);
				Fraction q = createFractionOfPolynomialsFromTerm(sub.sub(1),
						variables);
				if (sub.op().equals(RealLDT.getFunctionFor(Plus.class))) {
					return p.add(q);
				} else if (sub.op().equals(RealLDT.getFunctionFor(Minus.class))) {
					return p.subtract(q);
				} else if (sub.op().equals(RealLDT.getFunctionFor(Mult.class))) {
					return p.multiply(q);
				} else if (sub.op().equals(RealLDT.getFunctionFor(Div.class))) {
					try {
						if (p.denominator().isOne() && q.denominator().isOne()) {
							BigInteger pInt = new BigInteger(p.numerator()
									.toString());
							BigInteger qInt = new BigInteger(q.numerator()
									.toString());
							int[] size = new int[variables.size()];
							return Values.getDefault().fraction(
									Values.getDefault().MONOMIAL(
											Values.getDefault().rational(
													Values.getDefault()
															.valueOf(pInt),
													Values.getDefault()
															.valueOf(qInt)).representative(),
											size),
									Values.getDefault().MONOMIAL(size).one());
						}
					} catch (Exception e) {
					}
					return (Fraction) p.divide(q);
				} else if (sub.op().equals(RealLDT.getFunctionFor(Exp.class))) {
					try {
						Integer number = Values.getDefault().valueOf(
								new BigInteger(sub.sub(1).op().name()
										.toString()));
						if (Operations.less.apply(number, Values.getDefault()
								.ZERO()))
							return (Fraction) p.power(number.minus()).inverse();
						return (Fraction) p.power(number);
					} catch (NumberFormatException e) {
					    try {
					        return (Fraction) p.power(q);
					    } catch(ClassCastException ce) {
					        System.out.println("Cannot cast " + p.power(q));
					        throw ce;
					    }
					}
				}
			} else if (sub.arity() == 1) {
				if (sub.op().equals(RealLDT.getFunctionFor(MinusSign.class))) {
					return (Fraction) createFractionOfPolynomialsFromTerm(
							sub.sub(0), variables).minus();
				}
			}
		}
		throw new IllegalArgumentException("Dont know what to do with"
				+ sub.op());
	}

	/**
	 * Given a term that represents a polynomial inequality over reals this
	 * function calculates a representation without fractions.
	 * 
	 * @param t
	 *            the term to convert
	 * @return a set of terms containing the inequalities used to express the
	 *         equivalant proposition without fractions
	 */
	public static Term eliminateFractionsFromInequality(Term t, NamespaceSet nss) {
		final Function lt = RealLDT.getFunctionFor(Less.class);
		final Function leq = RealLDT.getFunctionFor(LessEquals.class);
		final Function geq = RealLDT.getFunctionFor(GreaterEquals.class);
		final Function gt = RealLDT.getFunctionFor(Greater.class);
		final Function neq = RealLDT.getFunctionFor(Unequals.class);

		// set sparse model for polynomials in orbital
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("orbital.math.UnivariatePolynomial.sparse", "true");
		hashMap.put("orbital.math.Fraction.normalize", "eager");

		Values.getInstance(hashMap);

		// assert that we got the right form of term
		assert t.arity() == 2 : "This function can only be applied to binary predicates.";
		if (!(t.op() instanceof Equality)) {
			assert t.op() == lt || t.op() == leq || t.op() == geq
					|| t.op() == gt || t.op() == neq;
		}

		// collect variables occurring in the term
		Map<String, Term> variables = new HashMap<String, Term>();
		List<String> varList = new ArrayList<String>();

		FilterVariableSet set = AllCollector.getItemSet(t);
		FilterVariableSet set2 = set.filter(new FilterVariableCollector(null));
		for (FoundItem i : set2) {
			variables.put(i.getName(), i.getTerm());
			varList.add(i.getName());
		}
		if (varList.isEmpty()) {
			// there are no variables, thus the fractions doesn't matter
			return t;
		}

		// convert the left side of the term to a fraction
		Fraction leftHandSide = createFractionOfPolynomialsFromTerm(t.sub(0),
				varList);
		// System.out.println(t.sub(0) + " becomes " + leftHandSide);

		// convert the right side of the term to a fraction
		Fraction rightHandSide = createFractionOfPolynomialsFromTerm(t.sub(1),
				varList);
		// System.out.println(t.sub(1) + " becomes " + rightHandSide);

		// reduce the fractions

		Term result = null;

		// add terms stating that the denominators are unequal to zero
		Sort r = RealLDT.getRealSort();
		Term zero = TermBuilder.DF.func(NumberCache.getNumber(
				new BigDecimal(0), r));

		Term leftDenominator = null;
		Polynomial leftPoly = (Polynomial) leftHandSide.numerator();
		if (!leftHandSide.denominator().isOne()) {
			leftDenominator = convertPolynomToTerm((Polynomial) leftHandSide
					.denominator(), varList, variables, nss);
			// cross-multiply with the denominators
			// System.out.println("Now calculating " + rightHandSide + " * "
			// + leftHandSide.denominator());// XXX
			// System.out.println("left is of type "
			// + leftHandSide.denominator().getClass());// XXX
			// System.out.println("right is of type " +
			// rightHandSide.getClass());// XXX
			rightHandSide = (Fraction) rightHandSide.multiply(Values
					.getDefault().fraction(leftHandSide.denominator(),
							leftHandSide.denominator().one()));
		}
		// System.out.println("leftDenominator: " + leftDenominator + " righthandside " + rightHandSide);

		// add terms stating that the denominators are unequal to zero
		Term rightDenominator = null;
		Polynomial rightPoly = (Polynomial) rightHandSide.numerator();
		if (!rightHandSide.denominator().isOne()) {
			rightDenominator = convertPolynomToTerm((Polynomial) rightHandSide
					.denominator(), varList, variables, nss);
			// cross-multiply with the denominators
			leftPoly = (Polynomial) leftPoly.multiply(rightHandSide
					.denominator());
		}

		// recalculate term structure
		Term left = convertPolynomToTerm(leftPoly, varList, variables, nss);
		Term right = convertPolynomToTerm(rightPoly, varList, variables, nss);

		if (t.op() instanceof Equality) {
			result = TermBuilder.DF.equals(left, right);
			if (rightDenominator != null) {
				result = and(result, func(neq, rightDenominator, zero));
			}
			if (leftDenominator != null) {
				result = and(result, func(neq, leftDenominator, zero));
			}
		} else if (t.op().name().toString().equals("neq")) {
			result = TermBuilder.DF.func((TermSymbol) t.op(), left, right);
			if (rightDenominator != null) {
				result = and(result, func(neq, rightDenominator, zero));
			}
			if (leftDenominator != null) {
				result = and(result, func(neq, leftDenominator, zero));
			}
		} else if (t.op().name().toString().equals("lt")) {
			result = generateResultingFormula(zero, leftDenominator,
					rightDenominator, left, right, lt, gt);
		} else if (t.op().name().toString().equals("leq")) {
			result = generateResultingFormula(zero, leftDenominator,
					rightDenominator, left, right, leq, geq);
		} else if (t.op().name().toString().equals("geq")) {
			result = generateResultingFormula(zero, leftDenominator,
					rightDenominator, left, right, geq, leq);
		} else if (t.op().name().toString().equals("gt")) {
			result = generateResultingFormula(zero, leftDenominator,
					rightDenominator, left, right, gt, lt);
		} else {
			throw new IllegalArgumentException(
					"Dont know what to do with the operator " + t.op());
		}

		// return the resulting terms
		// System.out.println("Converted " + t + " to " + result);// XXX

		boolean assertions = false;
		assert assertions = true;
		if (assertions) {

			Term prev = t;
			if (rightDenominator != null) {
				prev = and(prev, func(neq, rightDenominator, zero));
			}
			if (leftDenominator != null) {
				prev = and(prev, func(neq, leftDenominator, zero));
			}
			// if (MathSolverManager.getQuantifierElimantor("Mathematica") !=
			// null) {
			// try {
			// assert MathSolverManager.getQuantifierElimantor(
			// "Mathematica").reduce(
			// TermBuilder.DF.equiv(prev, result),
			// (nss == null) ? Main.getInstance().mediator()
			// .namespaces() : nss).equals(
			// TermBuilder.DF.tt());
			// } catch (RemoteException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (SolverException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }

		}

		return result;
	}

	/**
	 * @param lt
	 * @param gt
	 * @param result
	 * @param zero
	 * @param leftDenominator
	 * @param rightDenominator
	 * @param left
	 * @param right
	 * @param operator
	 * @param dualOperator
	 * @return
	 */
	private static Term generateResultingFormula(Term zero,
			Term leftDenominator, Term rightDenominator, Term left, Term right,
			TermSymbol operator, TermSymbol dualOperator) {
		final Function lt = RealLDT.getFunctionFor(Less.class);
		final Function gt = RealLDT.getFunctionFor(Greater.class);

		Term nlt = func(operator, left, right);
		Term result = nlt;
		if (rightDenominator != null || leftDenominator != null) {
			Term ngt = func(dualOperator, left, right);

			if (rightDenominator == null) {
				Term leftGt = func(gt, leftDenominator, zero);
				Term leftLt = func(lt, leftDenominator, zero);
				result = or(and(nlt, leftGt), and(ngt, leftLt));
			} else if (leftDenominator == null) {
				Term rightGt = func(gt, rightDenominator, zero);
				Term rightLt = func(lt, rightDenominator, zero);
				result = or(and(nlt, rightGt), and(ngt, rightLt));
			} else {
				Term leftGt = func(gt, leftDenominator, zero);
				Term leftLt = func(lt, leftDenominator, zero);
				Term rightGt = func(gt, rightDenominator, zero);
				Term rightLt = func(lt, rightDenominator, zero);
				result = or(or(and(and(nlt, rightGt), leftGt), and(and(nlt,
						rightLt), leftLt)), or(and(and(ngt, rightLt), leftGt),
						and(and(ngt, rightGt), leftLt)));
			}
		}
		return result;
	}

	private static Term func(TermSymbol op, Term a, Term b) {
		return TermBuilder.DF.func(op, a, b);
	}

	private static Term or(Term a, Term b) {
		return TermBuilder.DF.or(a, b);
	}

	private static Term and(Term a, Term b) {
		return TermBuilder.DF.and(a, b);
	}

	/**
	 * 
	 * TODO documentation since Jan 15, 2009
	 */
	private static Term convertPolynomToTerm(Polynomial p,
			List<String> variables, Map<String, Term> varMap, NamespaceSet nss) {
		final Sort r = RealLDT.getRealSort();
		final Term zero = TermBuilder.DF.func(NumberCache.getNumber(
				new BigDecimal(0), r));
		final Term one = TermBuilder.DF.func(NumberCache.getNumber(
				new BigDecimal(1), r));
		final Function mult = RealLDT.getFunctionFor(Mult.class);
		final Function exp = RealLDT.getFunctionFor(Exp.class);
		final Function plus = RealLDT.getFunctionFor(Plus.class);
		System.out.println("Converting " + p);
		final Iterator<KeyValuePair> monomials = p.monomials();
		Term result = null;
		while (monomials.hasNext()) {
			KeyValuePair next = monomials.next();
			Object coefficient = next.getValue();
			Object nextVector = next.getKey();

			Vector monomialDegrees = null;
			if (nextVector instanceof Vector) {
				monomialDegrees = (Vector) nextVector;
			} else {
				monomialDegrees = Values.getDefault().valueOf(
						new Integer[] { (Integer) nextVector });
			}
			if (!((Arithmetic) coefficient).isZero()) {
				Term summand = null;
				if (!((Arithmetic) coefficient).isOne()) {
					summand = Orbital.convertOrbitalToTerm(r, zero, nss,
							coefficient);
				}
				// String blub = "";
				for (int i = 0; i < monomialDegrees.dimension(); i++) {
					if (!monomialDegrees.get(i).isZero()) {
						Term s2 = varMap.get(variables.get(i));
						if (!monomialDegrees.get(i).isOne()) {
							s2 = TermBuilder.DF.func(exp, s2, Orbital
									.convertOrbitalToTerm(r, zero, nss,
											monomialDegrees.get(i)));
						}
						if (summand == null) {
							summand = s2;
						} else {
							summand = TermBuilder.DF.func(mult, summand, s2);
						}
						// blub += variables.get(i) + "^" +
						// monomialDegrees.get(i);
					}
				}
				// if (!blub.equals("")) {
				// System.out.println(coefficient + " * " + blub);// XXX
				// }
				if (result == null) {
					if (summand == null && ((Arithmetic) coefficient).isOne()) {
						result = one;
					} else {
						result = summand;
					}
				} else {
					if (summand == null && ((Arithmetic) coefficient).isOne()) {
						summand = one;
					}
					System.out.println("adding " + result + " + " + summand);
					result = TermBuilder.DF.func(plus, result, summand);
				}
			}
		}
		if (result == null) {
			return zero;
		}
		return result;
	}

	public static class BigFraction {
		private BigInteger numerator = null;
		private BigInteger denominator = BigInteger.ONE;

		/**
		 * @return the numerator
		 */
		public BigInteger getNumerator() {
			return numerator;
		}

		/**
		 * @return the denominator
		 */
		public BigInteger getDenominator() {
			return denominator;
		}

	}

	public static BigFraction convertStringToFraction(final String numberAsString)
			throws NumberFormatException, ArithmeticException {
		BigFraction result = new BigFraction();
		BigDecimal d = new BigDecimal(numberAsString);
		try {
			result.numerator = d.toBigIntegerExact();
		} catch (ArithmeticException e) {
		    try {
			assert numberAsString.indexOf('.') >= 0 && numberAsString.indexOf('.') <= numberAsString.length() - 2 : "non-integral decimal numbers have a . but don't end with ." + numberAsString;
			int digitsAfterComma = numberAsString.length()
					- numberAsString.indexOf('.') - 1;
			assert digitsAfterComma > 0 : "non-integral decimal numbers have a positive number of digits after . " + digitsAfterComma + " in " + numberAsString;
			BigInteger denominator = BigInteger.TEN.pow(digitsAfterComma);
			assert new BigDecimal(denominator).equals(BigDecimal.TEN.pow(digitsAfterComma)) : "10^n = 10.0^n";
			assert d.multiply(new BigDecimal(denominator)).equals(d.multiply(BigDecimal.TEN.pow(digitsAfterComma))) : "d*10^n = d*10.0^n";
			d = d.multiply(new BigDecimal(denominator));

			// calculate the greatest common divisor of the
			// fraction
			BigInteger numerator = d.toBigIntegerExact();
			//BigInteger gcd = numerator.gcd(denominator);
			BigInteger tmp = numerator.abs();
			BigInteger gcd = denominator;
			BigInteger t;
			while (tmp.compareTo(BigInteger.valueOf(0)) > 0) {
				t = tmp;
				tmp = gcd.mod(tmp);
				gcd = t;
			}
			numerator = numerator.divide(gcd);
			denominator = denominator.divide(gcd);
			result.numerator = numerator;
			result.denominator = denominator;
			assert new BigDecimal(result.getNumerator()).divide(
					new BigDecimal(result.getDenominator())).stripTrailingZeros().equals(
					new BigDecimal(numberAsString).stripTrailingZeros()) : "Numbers should be the same: " + new BigDecimal(result.getNumerator()).divide(
							new BigDecimal(result.getDenominator())).stripTrailingZeros() + " != " + new BigDecimal(numberAsString).stripTrailingZeros();
		    } catch (ArithmeticException e2) {
			throw (InternalError) new InternalError("Do not know how to convert string to fraction: " + numberAsString + "\nrewritten as " + d + "\nbecause of " + e2).initCause(e2);
		    }
		}
		return result;
	}

}
