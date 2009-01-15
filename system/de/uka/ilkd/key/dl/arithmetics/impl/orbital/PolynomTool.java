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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import orbital.math.Arithmetic;
import orbital.math.Fraction;
import orbital.math.Integer;
import orbital.math.Polynomial;
import orbital.math.Real;
import orbital.math.Values;
import orbital.math.Vector;
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
		System.out.println(sub);// XXX
		try {
			int[] size = new int[variables.size()];
			if (sub.arity() == 0) {
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
	public static Set<Term> eliminateFractionsFromInequality(Term t,
			NamespaceSet nss) {
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

		// convert the left side of the term to a fraction
		Fraction leftHandSide = createFractionOfPolynomialsFromTerm(t.sub(0),
				varList);

		// convert the right side of the term to a fraction
		Fraction rightHandSide = createFractionOfPolynomialsFromTerm(t.sub(1),
				varList);

		// reduce the fractions

		Set<Term> result = new HashSet<Term>();

		// add terms stating that the denominators are unequal to zero
		Sort r = RealLDT.getRealSort();
		Term zero = TermBuilder.DF.func(NumberCache.getNumber(
				new BigDecimal(0), r));
		System.out.println("Left is " + leftHandSide);// XXX
		System.out.println("Right is " + rightHandSide);// XXX

		if (!leftHandSide.denominator().isOne()) {
			Term leftDenominator = convertPolynomToTerm(
					(Polynomial) leftHandSide.denominator(), varList,
					variables, nss);

			result.add(TermBuilder.DF.func(neq, leftDenominator, zero));

			// cross-multiply with the denominators
			System.out.println("Now calculating " + rightHandSide + " * "
					+ leftHandSide.denominator());// XXX
			if (!leftHandSide.denominator().isOne()) {
				rightHandSide = (Fraction) rightHandSide.multiply(leftHandSide
						.denominator());
			}
		}

		Polynomial leftPoly = (Polynomial) leftHandSide.numerator();

		// add terms stating that the denominators are unequal to zero
		if (!rightHandSide.denominator().isOne()) {
			Term rightDenominator = convertPolynomToTerm(
					(Polynomial) rightHandSide.denominator(), varList,
					variables, nss);
			result.add(TermBuilder.DF.func(neq, rightDenominator, zero));
			// cross-multiply with the denominators
			leftPoly = (Polynomial) leftPoly.multiply(rightHandSide
					.denominator());
		}

		Polynomial rightPoly = (Polynomial) rightHandSide.numerator();

		// recalculate term structure
		Term left = convertPolynomToTerm(leftPoly, varList, variables, nss);
		Term right = convertPolynomToTerm(rightPoly, varList, variables, nss);

		if (t.op() instanceof Equality) {
			System.out.println(left);// XXX
			System.out.println(right);// XXX
			result.add(TermBuilder.DF.equals(left, right));
		} else {
			System.out.println("Creating " + left + " " + t.op() + " " + right);// XXX
			result.add(TermBuilder.DF.func((TermSymbol) t.op(), left, right));
		}

		// return the resulting terms
		System.out.println("Converted " + t + " to " + result);// XXX
		return result;
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
					summand = Orbital.convertOrbitalToTerm(r, zero, nss,
							next);
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
					result = summand;
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
