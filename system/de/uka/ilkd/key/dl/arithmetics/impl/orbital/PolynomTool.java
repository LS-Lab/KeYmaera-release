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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import orbital.math.Arithmetic;
import orbital.math.Fraction;
import orbital.math.Integer;
import orbital.math.Polynomial;
import orbital.math.Values;
import orbital.math.Vector;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
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
import de.uka.ilkd.key.gui.Main;
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
		System.out.println(sub);// XXX
		try {
			if (sub.arity() == 0) {
				int[] size = new int[variables.size()];
				if (variables.contains(sub.op().name().toString())) {
					size[variables.indexOf(sub.op().name().toString())] = 1;
					return Values.getDefault().fraction(
							Values.getDefault().MONOMIAL(size),
							Values.getDefault().MONOMIAL(size).one());
				} else {
					return Values.getDefault().fraction(
							Values.getDefault()
									.MONOMIAL(
											Values.getDefault().valueOf(
													new BigDecimal(sub.op()
															.name().toString())
															.doubleValue()),
											size),
							Values.getDefault().MONOMIAL(size).one());
				}
			} else {
				if (sub.arity() == 2) {
					Fraction p = createFractionOfPolynomialsFromTerm(
							sub.sub(0), variables);
					Fraction q = createFractionOfPolynomialsFromTerm(
							sub.sub(1), variables);
					System.out.println("p = " + p + " of type " + p.getClass());// XXX
					System.out.println("with q = " + q + " of type "
							+ q.getClass());// XXX
					if (sub.op().equals(RealLDT.getFunctionFor(Plus.class))) {
						return p.add(q);
					} else if (sub.op().equals(
							RealLDT.getFunctionFor(Minus.class))) {
						return p.subtract(q);
					} else if (sub.op().equals(
							RealLDT.getFunctionFor(Mult.class))) {
						return p.multiply(q);
					} else if (sub.op().equals(
							RealLDT.getFunctionFor(Div.class))) {
						return (Fraction) p.divide(q);
					} else if (sub.op().equals(
							RealLDT.getFunctionFor(Exp.class))) {
						try {
							Integer number = Values.getDefault().valueOf(
									new BigInteger(sub.sub(1).op().name()
											.toString()));
							return (Fraction) p.power(number);
						} catch (NumberFormatException e) {
							return (Fraction) p.power(q);
						}
					}
				} else if (sub.arity() == 1) {
					if (sub.op()
							.equals(RealLDT.getFunctionFor(MinusSign.class))) {
						return (Fraction) createFractionOfPolynomialsFromTerm(
								sub.sub(0), variables).minus();
					}
				}
			}
		} finally {
			System.out.println("Finished: " + sub);// XXX
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
		if(varList.isEmpty()) {
			// there are no variables, thus the fractions doesn't matter
			return t;
		}

		// convert the left side of the term to a fraction
		Fraction leftHandSide = createFractionOfPolynomialsFromTerm(t.sub(0),
				varList);

		// convert the right side of the term to a fraction
		Fraction rightHandSide = createFractionOfPolynomialsFromTerm(t.sub(1),
				varList);

		// reduce the fractions

		Term result = null;

		// add terms stating that the denominators are unequal to zero
		Sort r = RealLDT.getRealSort();
		Term zero = TermBuilder.DF.func(NumberCache.getNumber(
				new BigDecimal(0), r));
		System.out.println("Left is " + leftHandSide);// XXX
		System.out.println("Right is " + rightHandSide);// XXX

		Term leftDenominator = null;
		if (!leftHandSide.denominator().isOne()) {
			leftDenominator = convertPolynomToTerm((Polynomial) leftHandSide
					.denominator(), varList, variables, nss);

			// cross-multiply with the denominators
			System.out.println("Now calculating " + rightHandSide + " * "
					+ leftHandSide.denominator());// XXX
			System.out.println("left is of type "
					+ leftHandSide.denominator().getClass());// XXX
			System.out.println("right is of type " + rightHandSide.getClass());// XXX
			if (!leftHandSide.denominator().isOne()) {
				rightHandSide = (Fraction) rightHandSide.multiply(Values
						.getDefault().fraction(leftHandSide.denominator(),
								leftHandSide.denominator().one()));
			}
		}

		Polynomial leftPoly = (Polynomial) leftHandSide.numerator();

		// add terms stating that the denominators are unequal to zero
		Term rightDenominator = null;
		if (!rightHandSide.denominator().isOne()) {
			rightDenominator = convertPolynomToTerm((Polynomial) rightHandSide
					.denominator(), varList, variables, nss);
			// cross-multiply with the denominators
			leftPoly = (Polynomial) leftPoly.multiply(rightHandSide
					.denominator());
		}

		Polynomial rightPoly = (Polynomial) rightHandSide.numerator();

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
		System.out.println("Converted " + t + " to " + result);// XXX

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
			if (MathSolverManager.getQuantifierElimantor("Mathematica") != null) {
				try {
					assert MathSolverManager.getQuantifierElimantor(
							"Mathematica").reduce(
							TermBuilder.DF.equiv(prev, result),
							(nss == null) ? Main.getInstance().mediator()
									.namespaces() : nss).equals(
							TermBuilder.DF.tt());
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SolverException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

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

		ListIterator mono = p.iterator();
		Iterator indices = p.indices();
		Term result = null;
		while (mono.hasNext()) {
			Object next = mono.next();
			Vector v = (Vector) indices.next();
			if (!((Arithmetic) next).isZero()) {
				Term summand = null;
				if (!((Arithmetic) next).isOne()) {
					summand = Orbital.convertOrbitalToTerm(r, zero, nss, next);
				}
				String blub = "";
				for (int i = 0; i < v.dimension(); i++) {
					if (!v.get(i).isZero()) {
						Term s2 = varMap.get(variables.get(i));
						if (!v.get(i).isOne()) {
							s2 = TermBuilder.DF.func(exp, s2, Orbital
									.convertOrbitalToTerm(r, zero, nss, v
											.get(i)));
						}
						if (summand == null) {
							summand = s2;
						} else {
							summand = TermBuilder.DF.func(mult, summand, s2);
						}
						blub += variables.get(i) + "^" + v.get(i);
					}
				}
				if (!blub.equals("")) {
					System.out.println(next + " * " + blub);// XXX
				}
				if (result == null) {
					if(summand == null && ((Arithmetic) next).isOne()) {
						result = one;
					} else {
						result = summand;
					}
				} else {
					result = TermBuilder.DF.func(plus, result, summand);
				}
			}
		}
		if (result == null) {
			return zero;
		}
		return result;
	}

}
