/***************************************************************************
 *   Copyright (C) 2007 by Jan-David Quesel                                *
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
package de.uka.ilkd.key.dl.arithmetics.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import orbital.math.Arithmetic;
import orbital.math.Fraction;
import orbital.math.Integer;
import orbital.math.Polynomial;
import orbital.math.Real;
import orbital.math.Values;
import orbital.math.Vector;
import orbital.math.functional.Operations;
import de.uka.ilkd.key.dl.arithmetics.impl.csdp.CSDP;
import de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.GroebnerBasisChecker;
import de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.SparsePolynomial;
import de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.GroebnerBasisChecker.Square;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.OrbitalSimplifier;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool;
import de.uka.ilkd.key.dl.arithmetics.impl.sos.MaxPolynomPerDegreeOrder;
import de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder;
import de.uka.ilkd.key.dl.arithmetics.impl.sos.SimpleOrder;
import de.uka.ilkd.key.dl.formulatools.collector.AllCollector;
import de.uka.ilkd.key.dl.formulatools.collector.FilterVariableSet;
import de.uka.ilkd.key.dl.formulatools.collector.filter.FilterVariableCollector;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Greater;
import de.uka.ilkd.key.dl.model.GreaterEquals;
import de.uka.ilkd.key.dl.model.Less;
import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.dl.model.Minus;
import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Equality;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.TermSymbol;

/**
 * @author jdq
 * 
 */
public class SumOfSquaresChecker {

	private enum Result {
		SOLUTION_FOUND, NO_SOLUTION_AVAILABLE, UNKNOWN;
	}

	public enum FormulaStatus {
		VALID, INVALID, UNKNOWN;
	}

	public static class PolynomialClassification<T> {
		public Set<T> f;
		public Set<T> g;
		public Set<T> h;

		public PolynomialClassification(Set<T> f, Set<T> g, Set<T> h) {
			this.f = f;
			this.g = g;
			this.h = h;
		}
	}

	public static final SumOfSquaresChecker INSTANCE = new SumOfSquaresChecker();

	/**
	 * Given a sequent seperated into antecedent and succedent this methods
	 * generates a classification of all occuring inequalities, unequalities and
	 * equalities into sets f, g and h. Where f contains all inequalities, g the
	 * unequalities and h the equalities.
	 * 
	 * @param ante
	 *            the antecedent of the sequent
	 * @param succ
	 *            the succedent of the sequent
	 * @return a classification of the given terms
	 */
	public PolynomialClassification<Term> classify(Set<Term> ante,
			Set<Term> succ) {
		System.out.println("Computing f, g^2 and h");// XXX
		final Function lt = RealLDT.getFunctionFor(Less.class);
		final Function leq = RealLDT.getFunctionFor(LessEquals.class);
		final Function geq = RealLDT.getFunctionFor(GreaterEquals.class);
		final Function gt = RealLDT.getFunctionFor(Greater.class);
		final Function neq = RealLDT.getFunctionFor(Unequals.class);
		Term zero = TermBuilder.DF.func(NumberCache.getNumber(
				new BigDecimal(0), RealLDT.getRealSort()));
		// handle succedent
		Set<Term> conjunction = new HashSet<Term>();
		for (Term t : succ) {
			System.out.println("Checking " + t);
			Term sub, sub2;
			Operator op;
			if (t.op().equals(Op.NOT)) {
				if (t.sub(0).arity() != 2) {
					continue;
				}
				sub = t.sub(0).sub(0);
				sub2 = t.sub(0).sub(1);
				op = t.sub(0).op();
			} else {
				if (t.arity() != 2) {
					continue;
				}
				sub = t.sub(0);
				sub2 = t.sub(1);
				op = negationLookUp(t.op());
			}
			if (!(t.op() == gt || t.op() == geq || t.op() == Equality.EQUALS
					|| t.op() == leq || t.op() == lt || t.op() == neq)) {
				// we can only handle arithmetic predicates
				continue;
			}
			if (!(sub.equals(zero) || sub2.equals(zero))) {
				sub = TermBuilder.DF.func(RealLDT.getFunctionFor(Minus.class),
						t.sub(0), t.sub(1));
				sub2 = zero;
			}
			if (sub.equals(zero) && !sub2.equals(zero)) {
				Term hold = sub;
				sub = sub2;
				sub2 = hold;
			}
			if (op instanceof Function) {
				if (!op.equals(neq) && t.sub(0).equals(zero)
						&& !t.sub(1).equals(zero)) {
					op = negationLookUp(op);
				}
				conjunction
						.add(TermBuilder.DF.func((TermSymbol) op, sub, sub2));
			} else if (op instanceof Equality) {

				conjunction.add(TermBuilder.DF.equals(sub, sub2));
			}
		}
		for (Term t : ante) {
			Term sub, sub2;
			Operator op;
			if (t.op().equals(Op.NOT)) {
				sub = t.sub(0).sub(0);
				sub2 = t.sub(0).sub(1);
				op = negationLookUp(t.sub(0).op());
			} else {
				sub = t.sub(0);
				sub2 = t.sub(1);
				op = t.op();
			}
			if (!(t.op() == gt || t.op() == geq || t.op() == Equality.EQUALS
					|| t.op() == leq || t.op() == lt || t.op() == neq)) {
				// we can only handle arithmetic predicates
				continue;
			}
			if (!(sub.equals(zero) || sub2.equals(zero))) {
				sub = TermBuilder.DF.func(RealLDT.getFunctionFor(Minus.class),
						t.sub(0), t.sub(1));
				sub2 = zero;
			}
			if (sub.equals(zero) && !sub2.equals(zero)) {
				Term hold = sub;
				sub = sub2;
				sub2 = hold;
			}
			if (op instanceof Function) {
				if (!op.equals(neq) && t.sub(0).equals(zero)
						&& !t.sub(1).equals(zero)) {
					op = negationLookUp(op);
				}
				conjunction
						.add(TermBuilder.DF.func((TermSymbol) op, sub, sub2));
			} else if (op instanceof Equality) {
				conjunction.add(TermBuilder.DF.equals(sub, sub2));
			}
		}
		System.out.println("Finished computing conjunction");// XXX
		// split to f, g, h
		Set<Term> f = new HashSet<Term>();
		Set<Term> g = new HashSet<Term>();
		Set<Term> h = new HashSet<Term>();
		for (Term t : conjunction) {
			if (t.op() == Equality.EQUALS) {
				// H is the set of equalities thus we just need to add this term
				h.add(TermBuilder.DF
						.equals(TermBuilder.DF.func(RealLDT
								.getFunctionFor(Minus.class), t.sub(0), t
								.sub(1)), zero));
			} else if (t.op().equals(neq)) {
				// G is the set of unequalities thus we just need to add this
				// term
				g.add(TermBuilder.DF
						.func(neq, TermBuilder.DF.func(RealLDT
								.getFunctionFor(Minus.class), t.sub(0), t
								.sub(1)), zero));
			} else if (t.op().equals(geq)) {
				// F contains all inequalities of the form x >= y thus we just
				// need to add this term
				f.add(TermBuilder.DF
						.func(geq, TermBuilder.DF.func(RealLDT
								.getFunctionFor(Minus.class), t.sub(0), t
								.sub(1)), zero));
			} else if (t.op().equals(gt)) {
				// the term is x > y thus we add x - y >= 0 to F and x - y != 0
				// to G
				f.add(TermBuilder.DF
						.func(geq, TermBuilder.DF.func(RealLDT
								.getFunctionFor(Minus.class), t.sub(0), t
								.sub(1)), zero));
				g.add(TermBuilder.DF
						.func(neq, TermBuilder.DF.func(RealLDT
								.getFunctionFor(Minus.class), t.sub(0), t
								.sub(1)), zero));
			} else if (t.op().equals(leq)) {
				// switch arguments to turn x <= y into y - x >= 0
				f.add(TermBuilder.DF
						.func(geq, TermBuilder.DF.func(RealLDT
								.getFunctionFor(Minus.class), t.sub(1), t
								.sub(0)), zero));
			} else if (t.op().equals(lt)) {
				// the term is x < y thus we add y - x >= 0 to F and y - x != 0
				// to G
				f.add(TermBuilder.DF
						.func(geq, TermBuilder.DF.func(RealLDT
								.getFunctionFor(Minus.class), t.sub(1), t
								.sub(0)), zero));
				g.add(TermBuilder.DF
						.func(neq, TermBuilder.DF.func(RealLDT
								.getFunctionFor(Minus.class), t.sub(1), t
								.sub(0)), zero));
			} else if (t.op().equals(TermBuilder.DF.tt().op())
					|| t.op().equals(TermBuilder.DF.ff().op())) {
				// TODO jdq: we need to do something useful with this
				System.err.println("Found " + t.op() + " (" + t.op().getClass()
						+ ") " + " instead of an inequality");
			} else {
				throw new IllegalArgumentException(
						"Dont know how to handle the predicate " + t.op());
			}
		}
		return new PolynomialClassification<Term>(f, g, h);
	}

	/**
	 * @param op
	 * @return
	 */
	private Operator negationLookUp(Operator op) {
		final Function lt = RealLDT.getFunctionFor(Less.class);
		final Function leq = RealLDT.getFunctionFor(LessEquals.class);
		final Function geq = RealLDT.getFunctionFor(GreaterEquals.class);
		final Function gt = RealLDT.getFunctionFor(Greater.class);
		final Function neq = RealLDT.getFunctionFor(Unequals.class);
		if (op.equals(neq)) {
			return Equality.EQUALS;
		} else if (op == Equality.EQUALS) {
			return neq;
		} else if (op.equals(geq)) {
			return lt;
		} else if (op.equals(gt)) {
			return leq;
		} else if (op.equals(lt)) {
			return geq;
		} else if (op.equals(leq)) {
			return gt;
		}
		throw new IllegalArgumentException("Unknown operator " + op);
	}

	/**
	 * Generate polynomials from the given term classification. The result
	 * contains only the leftside polynomial of the inequalities, where for f
	 * the omitted part is > 0, for g it is != 0 and for h it is = 0.
	 */
	public PolynomialClassification<Polynomial> classify(
			PolynomialClassification<Term> cla) {
		return classify(cla, false);
	}

	/**
	 * Generate polynomials from the given term classification. The result
	 * contains only the leftside polynomial of the inequalities, where for f
	 * the omitted part is > 0, for g it is != 0 and for h it is = 0.
	 */
	public PolynomialClassification<Polynomial> classify(
			PolynomialClassification<Term> cla, boolean addAddtionalVariable) {
		System.out.println("Try to find monominals");// XXX
		System.out.println("We check the following Terms:");// XXX
		Set<String> variables = new HashSet<String>();
		System.out.println("F contains: "); // XXX
		for (Term t : cla.f) {
			System.out.println(t);// XXX

			// added by Timo Michelsen
			// BEGIN
			FilterVariableSet set = AllCollector.getItemSet(t);
			FilterVariableSet set2 = set.filter(new FilterVariableCollector(
					null));
			variables.addAll(set2.getVariables());
			// END

			// replaced:
			// variables.addAll(VariableCollector.getVariables(t));
		}
		System.out.println("-- end F");
		System.out.println("G contains: "); // XXX
		for (Term t : cla.g) {
			System.out.println(t);// XXX
			// added by Timo Michelsen
			// BEGIN
			FilterVariableSet set = AllCollector.getItemSet(t);
			FilterVariableSet set2 = set.filter(new FilterVariableCollector(
					null));
			variables.addAll(set2.getVariables());
			// END

			// replaced:
			// variables.addAll(VariableCollector.getVariables(t));
		}
		System.out.println("-- end G");
		System.out.println("H contains: "); // XXX
		for (Term t : cla.h) {
			System.out.println(t);// XXX
			// added by Timo Michelsen
			// BEGIN
			FilterVariableSet set = AllCollector.getItemSet(t);
			FilterVariableSet set2 = set.filter(new FilterVariableCollector(
					null));
			variables.addAll(set2.getVariables());
			// END

			// replaced:
			// variables.addAll(VariableCollector.getVariables(t));
		}
		System.out.println("-- end H");
		List<String> vars = new ArrayList<String>();
		vars.addAll(variables);
		if (addAddtionalVariable) {
			vars.add("newVariable$var$blub");
		}
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("orbital.math.UnivariatePolynomial.sparse", "true");
		Values.getInstance(hashMap);
		Polynomial result = null;

		// now we need to construct the different polynomes
		HashSet<Polynomial> polyF = new HashSet<Polynomial>();
		HashSet<Polynomial> polyG = new HashSet<Polynomial>();
		HashSet<Polynomial> polyH = new HashSet<Polynomial>();

		for (Term t : cla.f) {
			Fraction p = PolynomTool.createFractionOfPolynomialsFromTerm(t
					.sub(0), vars);
			polyF.add((Polynomial) p.numerator());
			if (!p.denominator().isOne()) {
				polyG.add((Polynomial) p.denominator());
			}
		}
		for (Term t : cla.g) {
			Fraction p = PolynomTool.createFractionOfPolynomialsFromTerm(t
					.sub(0), vars);
			polyG.add((Polynomial) p.numerator());
			if (!p.denominator().isOne()) {
				polyG.add((Polynomial) p.denominator());
			}
		}
		for (Term t : cla.h) {
			Fraction p = PolynomTool.createFractionOfPolynomialsFromTerm(t
					.sub(0), vars);
			polyH.add((Polynomial) p.numerator());
			if (!p.denominator().isOne()) {
				polyG.add((Polynomial) p.denominator());
			}
		}
		return new PolynomialClassification<Polynomial>(polyF, polyG, polyH);
	}

	/**
	 * Compute a conjunction of inequaltities f of the form f >= 0, g != 0 or
	 * equalities h = 0. Afterwards check if f+g^2+h = 0 is satisfiable. If this
	 * holds the input is satisfiable too.
	 * 
	 * @return true if a combination of f, g and h is found such that f+g^2+h =
	 *         0.
	 */
	public FormulaStatus check(PolynomialClassification<Term> cla) {
		return check(cla.f, cla.g, cla.h);
	}

	/**
	 * Compute a conjunction of inequaltities f of the form f >= 0, g != 0 or
	 * equalities h = 0. Afterwards check if f+g^2+h = 0 is satisfiable. If this
	 * holds the input is satisfiable too.
	 * 
	 * @return true if a combination of f, g and h is found such that f+g^2+h =
	 *         0.
	 */
	public FormulaStatus check(Set<Term> f, Set<Term> g, Set<Term> h) {
		PolynomialClassification<Polynomial> classify = classify(new PolynomialClassification<Term>(
				f, g, h));
		Polynomial one = null;
		if (!classify.f.isEmpty()) {
			one = (Polynomial) classify.f.iterator().next().one();
		} else if (!classify.g.isEmpty()) {
			one = (Polynomial) classify.g.iterator().next().one();
		} else if (!classify.h.isEmpty()) {
			one = (Polynomial) classify.h.iterator().next().one();
		}
		if (one != null) {
			PolynomialOrder order = new MaxPolynomPerDegreeOrder(one);
			order.setF(classify.f);
			order.setG(classify.g);
			order.setH(classify.h);
			order.setMaxDegree(20);
			while (order.hasNext()) {
				System.out.println("searching");
				Result searchSolution = searchSolution(order.getNext());
				System.out.println("Result: " + searchSolution);
				if (searchSolution == Result.SOLUTION_FOUND) {
					return FormulaStatus.INVALID;
				}
			}
		}
		return FormulaStatus.UNKNOWN;
	}

	/**
	 * @param inputPolynomial
	 * @return
	 */
	private Result searchSolution(Polynomial inputPolynomial) {
		// now we need to translate the polynominal into a matrix representation
		// monominals are iterated x^0y^0, x^0y^1, x^0y^2, ..., x^1y^0, x^1y^1,
		// x^1y^2,..., x^2y^0, x^2y^1,...
		ListIterator coefficients = inputPolynomial.iterator();
		System.out.println("Degree: " + inputPolynomial.degree());
		System.out.println("Degree-Value: " + inputPolynomial.degreeValue());// XXX
		Iterator indices = inputPolynomial.indices();
		System.out.println("Rank: " + inputPolynomial.rank());// XXX
		List<Vector> monominals = new ArrayList<Vector>();
		while (coefficients.hasNext()){
			Object next = coefficients.next();
			String blub = "";
			Vector v = (Vector) indices.next();
			for (int i = 0; i < v.dimension(); i++) {
				blub += ((char) ('a' + i)) + "^" + v.get(i);
			}
			System.out.println(next + "*" + blub);// XXX
			if (!next.equals(Values.getDefault().ZERO())) {
				boolean ok = true;
				Vector div = Values.getDefault()
						.valueOf(new int[v.dimension()]);
				for (int i = 0; i < v.dimension(); i++) {
					if (v.get(i) instanceof Real) {
						Real in = (Real) v.get(i);
						Real sqrt = in.divide(Values.getDefault().valueOf(2));
						try {
							new BigDecimal(sqrt.doubleValue()).intValueExact();
							System.out.println("Found nice half: " + sqrt);// XXX
							double[] d = new double[v.dimension()];
							d[i] = in.divide(Values.getDefault().valueOf(2))
									.doubleValue();
							div = div.add(Values.getDefault().valueOf(d));
						} catch (Exception e) {
							ok = false;
						}
					}
				}
				if (ok) {
					System.out.println("Adding monominal: " + div);// XXX
					monominals.add(div);
				}
			}
		}
		// now we know the monominals and need to construct the constraints for
		// the matrix
		// This matrix encodes the indices of q_i_j (the positions of the
		// parameters in the matrix).
		Vector[][] matrix = new Vector[monominals.size()][monominals.size()];
		for (int i = 0; i < monominals.size(); i++) {
			for (int j = 0; j < monominals.size(); j++) {
				matrix[i][j] = Values.getDefault().valueOf(
						new Integer[] { Values.getDefault().valueOf(i + 1),
								Values.getDefault().valueOf(j + 1) });
			}
		}
		// The result of this multiplication is a polynomial
		Poly multiplyVec = multiplyVec(multiplyMatrix(monominals, matrix),
				monominals);
		System.out.println("Polynom: " + multiplyVec);// XXX
		coefficients = inputPolynomial.iterator();
		indices = inputPolynomial.indices();

		List<Constraint> constraints = new ArrayList<Constraint>();
		while (coefficients.hasNext()) {
			Object next = coefficients.next();
			Vector v = (Vector) indices.next();
			if (!Values.getDefault().ZERO().equals(next)) {
				System.out.println("Checking: " + next + " and vector " + v);// XXX
				List<Vector> list = multiplyVec.vec.get(v);
				if (list != null) {
					constraints.add(new Constraint(v, list, (Arithmetic) next));
				} else {
					System.out.println("Cannot express: " + v);// XXX
					return Result.UNKNOWN;
				}
			}
		}
		System.out.println(constraints);// XXX
		// outputMatlab(monominals, constraints);

		double[] solution = new double[monominals.size() * monominals.size()];
		if (CSDP
				.sdp(monominals.size(), constraints.size(),
						convertConstraintsToResultVector(constraints,
								monominals.size()), convertConstraintsToCSDP(
								constraints, monominals.size()), solution) == 0) {
//			Square[] cert = GroebnerBasisChecker.approx2Exact(
//					multiplyVec.toSparsePolynomial(), monominals, solution);
//	        if (cert != null) {
//	            // check that the certificate is correct
//	            
//	            System.out.println("Certificate:");
//	            System.out.println(" 1");
//	            
//	            Polynomial p = (Polynomial) inputPolynomial.one();
//	            for (int i = 0; i < cert.length; ++i) {
//	                assert (Operations.greaterEqual.apply(cert[i].coefficient,
//	                                                      Values.getDefault().ZERO()));
//	                p = (Polynomial) p.add(cert[i].body.multiply(cert[i].body)
//	                                                   .scale(cert[i].coefficient));
//	                System.out.println(" + " + cert[i].coefficient + " * ( " + cert[i].body + " ) ^2");
//	            }
//	            System.out.println(" =");
//	            System.out.println(" " + p);
////	            assert (((Polynomial) groebnerReducer.apply(p)).isZero());
//	            System.out.println("Certificate is correct");
	            return Result.SOLUTION_FOUND;
//	        }
//	        return Result.UNKNOWN;
		} else {
			return Result.NO_SOLUTION_AVAILABLE;
		}
	}

	/**
	 * @param monominals
	 * @param constraints
	 */
	private void outputMatlab(List<Vector> monominals,
			List<Constraint> constraints) {
		System.out.println("Matlab Input: ");// XXX
		System.out.println("n = " + monominals.size() + ";");// XXX
		System.out.println("cvx_begin");// XXX
		System.out.println("variable X(n,n) symmetric;");// XXX
		System.out.println("minimize(0);");// XXX
		System.out.println("subject to");// XXX
		convertConstraints(constraints, monominals.size());
		System.out.println("X == semidefinite(n)");// XXX
		System.out.println("cvx_end");// XXX
		// print out X
		System.out.println("X");// XXX
	}

	/**
	 * <p>
	 * Convert the given constraints into a CSDP input matrix. The indices in
	 * the matrix are used to determine which positions in the matrix have to be
	 * marked with 1.
	 * </p>
	 * <p>
	 * &forall; c &isin; constraints: &forall; (i,j) &isin; c.indices:
	 * result[position(c) &sdot; size<sup>2</sup> + i &sdot; size + j] = 1
	 * </p>
	 * <p>
	 * The result is one double array of size (constraints.size() &times; size
	 * &times; size).
	 * </p>
	 * 
	 * @param constraints
	 * @param size
	 */
	private double[] convertConstraintsToCSDP(List<Constraint> constraints,
			int size) {
		double[] result = new double[constraints.size() * size * size];
		int cnum = 0;
		for (Constraint c : constraints) {
			int[][] selectionMatrix = new int[size][size];
			for (Vector v : c.indizes) {
				selectionMatrix[((Integer) v.get(0)).intValue() - 1][((Integer) v
						.get(1)).intValue() - 1] = 1;
			}
			// we generate one big matrix in c representation containing all
			// selection matrices
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					result[cnum * size * size + i * size + j] = selectionMatrix[i][j];
				}
			}
			cnum++;
		}
		return result;
	}

	/**
	 * @param constraints
	 * @param i
	 */
	private double[] convertConstraintsToResultVector(
			List<Constraint> constraints, int size) {
		double[] result = new double[constraints.size()];
		int cnum = 0;
		for (Constraint c : constraints) {
			result[cnum++] = OrbitalSimplifier.toDouble(c.pre);
		}
		return result;
	}

	/**
	 * @param constraints
	 * @param i
	 */
	private void convertConstraints(List<Constraint> constraints, int size) {
		for (Constraint c : constraints) {
			int[][] selectionMatrix = new int[size][size];
			for (Vector v : c.indizes) {
				selectionMatrix[((Integer) v.get(0)).intValue() - 1][((Integer) v
						.get(1)).intValue() - 1] = 1;
			}
			StringBuilder matrix = new StringBuilder();
			matrix.append("[ ");
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					matrix.append(selectionMatrix[i][j] + " ");
				}
				if (i + 1 < size) {
					matrix.append("; ");
				}
			}
			matrix.append(" ]");
			System.out.println("trace( " + matrix + " * X) == " + c.pre + ";");// XXX
		}
	}

	/**
	 * A constraint is a tupel containing 3 things. A vector v representing the
	 * current monomial. A list of vectors indizes representing the sum of
	 * q<sub>i</sub><sub>j</sub> coefficients, as well as an Arithmetic object
	 * pre which represents the righthand side of the equation:
	 * 
	 * (X<sub>1</sub><sup>i<sub>1</sub></sup> &sdot;&sdot;&sdot;
	 * X<sub>n</sub><sup>i<sub>n</sub></sup> &sdot; (&sum; <sub>(i,j) &isin;
	 * indizes</sub> (q<sub>i</sub><sub>j</sub>))) = pre
	 * 
	 * @author jdq
	 */
	private class Constraint {

		Constraint(Vector v, List<Vector> indizes, Arithmetic pre) {
			this.v = v;
			this.indizes = indizes;
			this.pre = pre;
		}

		Vector v;

		List<Vector> indizes;

		Arithmetic pre;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		/* @Override */
		public String toString() {
			StringBuilder b = new StringBuilder();
			String plus = "";
			b.append(plus);
			for (Vector w : indizes) {
				b.append(plus + "q");
				plus = "+";
				for (int i = 0; i < w.dimension(); i++) {
					b.append("_");
					b.append(w.get(i));
				}
			}
			plus = "+";
			b.append("* (");
			for (int i = 0; i < v.dimension(); i++) {
				b.append(((char) ('a' + i)) + "^" + v.get(i));
			}
			b.append(" ) = " + pre);
			return b.toString();
		}
	}

	/**
	 * This class represents a polynomial. The {@link HashMap} vec contains a
	 * key which encodes the monomial and the corresponding value in vec is the
	 * coefficient. The coefficient is represented by a list of vectors encoding
	 * the indices of the parameters. Thus the polynomial has the form:<br>
	 * 
	 * &sum; <sub>(i<sub>1</sub>,...,i<sub>n</sub>) &isin; vec</sub>
	 * (X<sub>1</sub><sup>i<sub>1</sub></sup> &sdot;&sdot;&sdot;
	 * X<sub>n</sub><sup>i<sub>n</sub></sup> &sdot; (&sum; <sub>(i,j) &isin;
	 * vec.get(v)</sub> (q<sub>i</sub><sub>j</sub>)))
	 * 
	 * @author jdq
	 */
	private class Poly {
		HashMap<Vector, List<Vector>> vec = new HashMap<Vector, List<Vector>>();

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		/* @Override */
		public String toString() {
			StringBuilder b = new StringBuilder();
			String plus = "";
			for (Vector v : vec.keySet()) {
				b.append(plus + "(");
				plus = "";
				for (Vector w : vec.get(v)) {
					b.append(plus + " q");
					plus = "+";
					for (int i = 0; i < w.dimension(); i++) {
						b.append("_");
						b.append(w.get(i));
					}
				}
				plus = "+";
				b.append(" ) * ");
				for (int i = 0; i < v.dimension(); i++) {
					b.append(((char) ('a' + i)) + "^" + v.get(i));
				}

			}
			return b.toString();
		}

		public SparsePolynomial toSparsePolynomial() {
			System.out.println("Converting " + this);//XXX
			int maxX = 0;
			int maxY = 0;
			for (Vector mono : vec.keySet()) {
				for (Vector coefficient : vec.get(mono)) {
					assert coefficient.dimension() == 2;
					int x = ((Integer) coefficient.get(0)).intValue();
					if (x > maxX) {
						maxX = x;
					}
					int y = ((Integer) coefficient.get(1)).intValue();
					if (y > maxY) {
						maxY = y;
					}
				}
			}
			assert maxX == maxY;
			SparsePolynomial sparsePolynomial = new SparsePolynomial();
			for(Vector mono: vec.keySet()) {
				for (Vector coefficient : vec.get(mono)) {
					int x = ((Integer) coefficient.get(0)).intValue();
					int y = ((Integer) coefficient.get(1)).intValue();
					if(x <= y) {
						// we only need diagonal constraints here
						int monoInts[] = new int[mono.rank()];
						for(int i = 0; i < mono.rank(); i++) {
							Real r = (Real) mono.get(i);
							monoInts[i] = (int) r.doubleValue();
						}
						sparsePolynomial.addTerms(Values.getDefault().MONOMIAL(monoInts), x + y*maxX);
					}
				}
			}
			return sparsePolynomial;
		}

	}

	/**
	 * This function multiplies an array of polynomials with a list of
	 * monomials. The result is a polynomial.
	 * 
	 * @param multiplyMatrix
	 * @param monominals
	 */
	private Poly multiplyVec(Vec multiplyMatrix, List<Vector> monominals) {
		Poly p = new Poly();
		for (int i = 0; i < monominals.size(); i++) {
			for (Vector qij : multiplyMatrix.vec[i].vec.keySet()) {
				// add qij * monomial to the resulting polynomial
				Vector monomial = qij.add(monominals.get(i));
				List<Vector> result = p.vec.get(monomial);
				if (result == null) {
					result = new ArrayList<Vector>();
					p.vec.put(monomial, result);
				}
				result.addAll(multiplyMatrix.vec[i].vec.get(qij));
			}
		}
		return p;
	}

	private class Vec {
		Poly[] vec;
	}

	/**
	 * This function multiplies the vector of vectors (called monomials) with
	 * the matrix of vectors (called matrix). The result is an array of
	 * polynomials.
	 * 
	 * @param monominals
	 * @param matrix
	 */
	private Vec multiplyMatrix(List<Vector> monominals, Vector[][] matrix) {
		Vec p = new Vec();
		p.vec = new Poly[monominals.size()];
		for (int i = 0; i < monominals.size(); i++) {
			p.vec[i] = new Poly();
			for (int j = 0; j < monominals.size(); j++) {
				List<Vector> list = p.vec[i].vec.get(monominals.get(j));
				System.out.println("Multiplying: " + monominals.get(j)
						+ " with " + matrix[i][j]);// XXX
				if (list == null) {
					list = new ArrayList<Vector>();
					p.vec[i].vec.put(monominals.get(j), list);
				}
				list.add(matrix[i][j]);
			}
		}
		return p;
	}
}
