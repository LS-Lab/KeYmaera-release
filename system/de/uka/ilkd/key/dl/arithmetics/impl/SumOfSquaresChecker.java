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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import orbital.math.Arithmetic;
import orbital.math.Fraction;
import orbital.math.Integer;
import orbital.math.Matrix;
import orbital.math.Polynomial;
import orbital.math.Real;
import orbital.math.ValueFactory;
import orbital.math.Values;
import orbital.math.Vector;
import orbital.math.functional.Operations;
import orbital.util.KeyValuePair;

import org.w3c.dom.Node;

import de.uka.ilkd.key.dl.arithmetics.ISOSChecker;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.impl.csdp.CSDP;
import de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.FractionisingEquationSolver;
import de.uka.ilkd.key.dl.arithmetics.impl.csdp.CSDPInterface;
import de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.GroebnerBasisChecker;
import de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.PSDDecomposition;
import de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.SparsePolynomial;
import de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.GroebnerBasisChecker.SimpleMonomialIterator;
import de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.GroebnerBasisChecker.Square;
import de.uka.ilkd.key.dl.arithmetics.impl.groebnerianSOS.PSDDecomposition.NotPSDException;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.OrbitalSimplifier;
import de.uka.ilkd.key.dl.arithmetics.impl.orbital.PolynomTool;
import de.uka.ilkd.key.dl.arithmetics.impl.sos.MaxPolynomPerDegreeOrder;
import de.uka.ilkd.key.dl.arithmetics.impl.sos.PolynomialOrder;
import de.uka.ilkd.key.dl.arithmetics.impl.sos.MaxPolynomPerDegreeOrder.Monoid;
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
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Equality;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.TermSymbol;
import de.uka.ilkd.key.pp.LogicPrinter;

/**
 * @author jdq
 * 
 */
public class SumOfSquaresChecker implements ISOSChecker {

	private enum Result {
		SOLUTION_FOUND, NO_SOLUTION_AVAILABLE, UNKNOWN;
	}

	public enum FormulaStatus {
		VALID, INVALID, UNKNOWN;
	}

	public static class PolynomialClassification<T> {
		/**
		 * the set of polynomials with condition f >= 0
		 */
		public Set<T> f;
		/**
		 * the set of polynomials with condition g != 0	
		 */
		public Set<T> g;
		/**
		 * the set of polynomials with condition h = 0	
		 */
		public Set<T> h;

		public PolynomialClassification(Set<T> f, Set<T> g, Set<T> h) {
			this.f = f;
			this.g = g;
			this.h = h;
		}
		
		public String toString() {
			String s = "PolynomialClassification[";
			Services services = Main.getInstance().mediator().getServices();
			for (T t : f) {
				if (t instanceof Term) s += "'>' " + LogicPrinter.quickPrintTerm((Term)t, services);
			}
			for (T t : g) {
				if (t instanceof Term) s += "'!=' " + LogicPrinter.quickPrintTerm((Term)t, services);
			}
			for (T t : h) {
				if (t instanceof Term) s += "'='  " + LogicPrinter.quickPrintTerm((Term)t, services);
			}
			s += "]";
			return s;
		}
	}

	public SumOfSquaresChecker(Node n) {
	}

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
	 * @param services TODO
	 * @return a classification of the given terms
	 */
	public static PolynomialClassification<Term> classify(Set<Term> ante,
			Set<Term> succ, Services services) {
		System.out.println("Computing f, g^2 and h");// XXX
		final Function lt = RealLDT.getFunctionFor(Less.class);
		final Function leq = RealLDT.getFunctionFor(LessEquals.class);
		final Function geq = RealLDT.getFunctionFor(GreaterEquals.class);
		final Function gt = RealLDT.getFunctionFor(Greater.class);
		final Function neq = RealLDT.getFunctionFor(Unequals.class);
        final Function minus = RealLDT.getFunctionFor(Minus.class);
		final Term zero = TermBuilder.DF.func(NumberCache.getNumber(
				new BigDecimal(0), RealLDT.getRealSort()));
		// handle succedent
		Set<Term> conjunction = new HashSet<Term>();
        for (Term t : succ) {
			System.out.println("Checking succedent part " + LogicPrinter.quickPrintTerm(t, services));
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
			if (op instanceof Function) {
		/*		if (!op.equals(neq) && t.sub(0).equals(zero)
						&& !t.sub(1).equals(zero)) {
					op = negationLookUp(op);
				} */
				conjunction.add(TermBuilder.DF.func((TermSymbol) op, sub, sub2));
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
			if (op instanceof Function) {
			/*	if (!op.equals(neq) && t.sub(0).equals(zero)
						&& !t.sub(1).equals(zero)) {
					op = negationLookUp(op);
				} */
				conjunction
						.add(TermBuilder.DF.func((TermSymbol) op, sub, sub2));
			} else if (op instanceof Equality) {
				conjunction.add(TermBuilder.DF.equals(sub, sub2));
			}
		}
		// now all formulas in the set conjunction can be considered as formulas in the antecedent
		// during the previous loops the predicates where negated where necessary
		System.out.println("Finished computing conjunction");// XXX
		// split to f, g, h
		Set<Term> f = new HashSet<Term>();
		Set<Term> g = new HashSet<Term>();
		Set<Term> h = new HashSet<Term>();
		for (Term t : conjunction) {
			if (t.op() == Equality.EQUALS) {
				// H is the set of equalities thus we just need to add this term
			    // This is, for "a = b" we add a - b = 0 to H
				h.add(TermBuilder.DF
						.equals(TermBuilder.DF.func(minus, t.sub(0), t
								.sub(1)), zero));
			} else if (t.op().equals(neq)) {
				// G is the set of unequalities thus we just need to add this
				// term
			    // This is, for "a != b" we add a - b = 0 to G
				g.add(TermBuilder.DF
						.func(neq, TermBuilder.DF.func(minus, t.sub(0), t
								.sub(1)), zero));
			} else if (t.op().equals(geq)) {
				// F contains all inequalities of the form x >= y thus we just
				// need to add this term
				f.add(TermBuilder.DF
						.func(geq, TermBuilder.DF.func(minus, t.sub(0), t
								.sub(1)), zero));
			} else if (t.op().equals(gt)) {
				// the term is x > y thus we add x - y >= 0 to F and x - y != 0
				// to G
				f.add(TermBuilder.DF
						.func(geq, TermBuilder.DF.func(minus, t.sub(0), t
								.sub(1)), zero));
				g.add(TermBuilder.DF
						.func(neq, TermBuilder.DF.func(minus, t.sub(0), t
								.sub(1)), zero));
			} else if (t.op().equals(leq)) {
				// switch arguments to turn x <= y into y - x >= 0
				f.add(TermBuilder.DF
						.func(geq, TermBuilder.DF.func(minus, t.sub(1), t
								.sub(0)), zero));
			} else if (t.op().equals(lt)) {
				// the term is x < y thus we add y - x >= 0 to F and y - x != 0
				// to G
				f.add(TermBuilder.DF
						.func(geq, TermBuilder.DF.func(minus, t.sub(1), t
								.sub(0)), zero));
				g.add(TermBuilder.DF
						.func(neq, TermBuilder.DF.func(minus, t.sub(1), t
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
		PolynomialClassification<Term> result = new PolynomialClassification<Term>(f, g, h);
		System.out.println(result);
		return result;
	}

	/**
	 * @param op
	 * @return
	 */
	private static Operator negationLookUp(Operator op) {
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
	 * the omitted part is >= 0, for g it is != 0 and for h it is = 0.
	 */
	public static PolynomialClassification<Polynomial> classify(
			PolynomialClassification<Term> cla) {
		return classify(cla, false);
	}

	/**
	 * Generate polynomials from the given term classification. The result
	 * contains only the leftside polynomial of the inequalities, where for f
	 * the omitted part is >= 0, for g it is != 0 and for h it is = 0.
	 */
	public static PolynomialClassification<Polynomial> classify(
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
	public static FormulaStatus check(PolynomialClassification<Term> cla) {
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
	public static FormulaStatus check(Set<Term> f, Set<Term> g, Set<Term> h) {
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
				Result searchSolution = testIfPolynomialIsSumOfSquares(order
						.getNext());
				System.out.println("Result: " + searchSolution);
				if (searchSolution == Result.SOLUTION_FOUND) {
					return FormulaStatus.INVALID;
				}
			}
		}
		return FormulaStatus.UNKNOWN;
	}

	
	private static class DegreePair {
	    final public int d;
	    final public int d2;
	    public DegreePair(int d, int d2) {
                this.d = d;
                this.d2 = d2;
            }
	    public boolean equals(Object obj) {
	        if (!(obj instanceof DegreePair))
	            return false;
	        DegreePair that = (DegreePair)obj;
                return this.d == that.d && this.d2 == that.d2;
            }
	    public int hashCode() {
                return d * 101 + d2;
            }
	}
	
	
	private static List<DegreePair> enumerateDegrees(int degreeBound) {
	    final HashSet<DegreePair> degreesSeen = new HashSet<DegreePair> ();
	    final List<DegreePair> res = new ArrayList<DegreePair> ();
	    
	    for (int rad = 0; rad <= degreeBound; ++rad) {
	        for (int d = 0; d <= rad; ++d) {
	            for (int d2 = 0; d*d + d2*d2 <= rad*rad; ++d2) {
	                final DegreePair newPair = new DegreePair (d, d2);
	                if (!degreesSeen.contains(newPair)) {
	                    degreesSeen.add(newPair);
	                    res.add(newPair);
	                }
	            }
	        }
	    }
	    
	    return res;
	}
	
	
	public static boolean checkCombinedSetForEmptyness(Set<Term> f, Set<Term> g,
	                                                   Set<Term> h, int degreeBound) {
		// degreeBound = 4;
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
		if (one == null)
		    return false;
		    
		if (classify.g.contains(one.zero()))
		    // special case: the succedent contains an equation t=t
		    return true;
		
		final Polynomial[] gPolys = classify.g.toArray(new Polynomial [0]);
		
                // therefore we construct all f combinations and add parametric
                // polynomials as coefficients
                final Set<Polynomial> prodsOfFs = generateFProducts(classify, one);
                System.out.println("prodsOfFs: " + prodsOfFs);// XXX

                final List<DegreePair> degrees = enumerateDegrees(degreeBound);
                
                for (DegreePair pair : degrees) {
                    final int d = pair.d;
                    final int d2 = pair.d2;
                
		    System.out.println("====================================================");
                    System.out.println("Now testing polynomials g of degree d=" + d);
                    System.out.println("Now testing parametric pi-polynomials of degree d2=" + d2);
                    
		    final SimpleMonomialIterator gExponentIt =
		        new SimpleMonomialIterator(classify.g.size(), d/2);
		    
		    while (gExponentIt.hasNext()) {
		        final Vector gExponents = gExponentIt.next(); 
                        Polynomial nextG = one;
                        for (int i = 0; i < gExponents.dimension(); ++i) {
                            int exp = ((Integer)gExponents.get(i)).intValue();
                            while (exp > 0) {
                                nextG = nextG.multiply(gPolys[i]);
                                exp = exp - 1;
                            }
                        }
                        
                        // g has to be a square
                        nextG = nextG.multiply(nextG);
                        if (nextG.degree().intValue() != d)
                            // wrong degree -> try the next g-product
                            continue;
		    
                        System.out.println("nextG is: " + nextG);
                        System.out.println("(degree: " + nextG.degreeValue() + ")");
                        
                        List<Vector> qMonomials = new ArrayList<Vector>();
                        {
                            SimpleMonomialIterator monomialIterator =
                                new SimpleMonomialIterator(one.rank(), d2 / 2);
                            while (monomialIterator.hasNext())
                                qMonomials.add(monomialIterator.next());
                        }

                        final int qDegree = Math.max(d, d2);
//                        for (int qDegree = 0; qDegree <= Math.max(d, d2); ++qDegree) 
                        {
                            System.out.println("Now testing parametric qi-polynomials of degree qDegree=" + qDegree);
                                //////////////////////////////////////////////////////////////////////////
				// the next step is to construct all those parametric
				// polynomials p_i (one per f in sumOfFs)

				// in this step we construct the p_i as sparse polynomial and in
				// the same iteration construct the resulting f polynomial by
				// multiplication of the corresponding sumOfFs polynomial
				final List<SparsePolynomial> pis = new ArrayList<SparsePolynomial>();
				final ValueFactory vf = Values.getDefault();
                                final Arithmetic two = vf.rational(2);
                                SparsePolynomial nextF = new SparsePolynomial();
                                
                                System.out.println("prodsOfFs size: " + prodsOfFs.size());
                                System.out.println("qMonomials size: " + qMonomials.size());
                                System.out.println("qMonomials are: " + qMonomials);

                                int currentParameter = 0;
                                int totalMonomialNum = 0;
                                
                                for (Polynomial nF : prodsOfFs) {
                                    SparsePolynomial s = new SparsePolynomial();

                                    List<Vector> consideredMonomials = new ArrayList<Vector>();
                                    for (Vector p : qMonomials) {
                                        consideredMonomials.add(p);
                                        for (int i = 0; i < consideredMonomials.size(); ++i) {
                                            final Arithmetic oldMono = consideredMonomials.get(i);
                                            final Arithmetic combinedMonoExp = oldMono.add(p);
                                            final Polynomial combinedMono;

                                            // all products but the product of
                                            // <code>newMono</code> with
                                            // itself have to be taken times two (the matrix is
                                            // symmetric,
                                            // and we only consider one half of it)
                                            if (i < consideredMonomials.size() - 1)
                                                combinedMono = vf.MONOMIAL(two, combinedMonoExp);
                                            else
                                                combinedMono = vf.MONOMIAL(combinedMonoExp);
                                            
                                            s.addTerms(combinedMono, currentParameter);
                                            currentParameter = currentParameter + 1;
                                        }                                            
                                        totalMonomialNum = totalMonomialNum + 1;
                                    }

                                    pis.add(s);
                                    nextF = nextF.add(s.multiply(nF));
                                }
                                
				final int piBlockSize = qMonomials.size();

				System.out.println("nextF is " + nextF);// XXX

				//////////////////////////////////////////////////////////////////////////
				// The next step is to construct all those parametric
				// polynomials q_i (one per h in classify.h). We
				// put all the parameters/coefficients in a further
				// block of the constructed block matrix

				SparsePolynomial nextH = new SparsePolynomial();
				
				int blockRow = 0;
				int blockColumn = 0;

                                for (Polynomial hPoly : classify.h) {
                                    System.out.println("h-polynomial: " + hPoly);
                                    
                                    final SparsePolynomial s = new SparsePolynomial();
                                    final SimpleMonomialIterator monomialIt =
                                        new SimpleMonomialIterator(one.rank(), qDegree);

                                    while (monomialIt.hasNext()) {
                                        final Vector monoExp = monomialIt.next();
                                        final boolean diagonal = (blockRow == blockColumn);
                                        
                                        final Polynomial mono;
                                        if (diagonal)
                                            mono = vf.MONOMIAL(monoExp);
                                        else
                                            mono = vf.MONOMIAL(two, monoExp);
                                        
                                        s.addTerms(mono, currentParameter);
                                        
                                        currentParameter = currentParameter + 1;
                                        if (diagonal) {
                                            blockColumn = blockColumn + 1;
                                            blockRow = 0;
                                        } else {
                                            blockRow = blockRow + 1;
                                        }
                                    }

                                    nextH = nextH.add(s.multiply(hPoly));
                                }

                                // we might already have filled a further column
                                // halfway
                                final int qiBlockSize;
                                if (blockRow == 0)
                                    qiBlockSize = blockColumn;
                                else
                                    qiBlockSize = blockColumn + 1;
                                
                                System.out.println("nextH is " + nextH);
				
				// now we can add nextF and nextH, we cannot represent nextG as
				// SparsePolynomial, as it is not parametric
				final SparsePolynomial fh = nextF.add(nextH);
				final List<Arithmetic> monomialsInFH = extractMonomialsInFH(fh, nextG);

				if (monomialsInFH == null)
				    continue;
				
				System.out.println("f+h = " + fh);// XXX

				////////////////////////////////////////////////////////////////////////////
				// Extract the side constraints and pre-process them
				// (we need to eliminate all parameters that were introduced
				// as part of the qi's, because they are not supposed to
				// satisfy semi-definiteness)
				
                                // the blocks of the resulting block matrix (one
                                // for each f polynomial coefficient)
                                final int[] blockSizes = new int [prodsOfFs.size() + 1];
                                Arrays.fill(blockSizes, piBlockSize);
                                blockSizes[prodsOfFs.size()] = qiBlockSize;
                                System.out.println("blockSizes: " + Arrays.toString(blockSizes));

                                final Matrix rawEquations =
				    fh.exactCoefficientComparisonBlock(blockSizes,
				                                       monomialsInFH);
				
//				System.out.println("rawEquations: " + rawEquations);

                                final Vector rawExactHetero =
                                    genExactHetero(fh, nextG, monomialsInFH);
//                                System.out.println("Result vector is " + rawExactHetero);// XXX

                                rawEquations.insertColumns(asMatrix(rawExactHetero));

                                final Matrix rawSmallEquations;

                                if (qiBlockSize == 0) {
                                    rawSmallEquations = rawEquations;
                                } else {
                                    // determine the columns that we have to eliminate
                                    final BitSet colsToEliminate = new BitSet ();
                                    final int lastBlockLength = qiBlockSize * qiBlockSize;
                                    final int colNum = rawEquations.dimensions()[1] - 1;
                                    colsToEliminate.set(colNum - lastBlockLength, colNum);
                                    
                                    rawSmallEquations =
                                        project(rawEquations, colsToEliminate);
                                }
                                
                                assert rawSmallEquations.dimensions()[1] ==
                                    prodsOfFs.size()*piBlockSize*piBlockSize + 1;
                                
                                final Vector exactHetero =
                                    rawSmallEquations.getColumn(rawSmallEquations.dimensions()[1]-1);
                                final Matrix exactHomo =
                                    vf.newInstance(rawSmallEquations.dimensions()[0],
                                                   rawSmallEquations.dimensions()[1]-1);
                                copy(rawSmallEquations, exactHomo,
                                     0, 0, 0, 0,
                                     rawSmallEquations.dimensions()[0],
                                     rawSmallEquations.dimensions()[1]-1);

//                                System.out.println("exactHomo is " + exactHomo);
//                                System.out.println("exactHetero is " + exactHetero);
                                
                                // assert symmetry
                                {
                                    int offset = 0;
                                    while (offset < exactHomo.dimensions()[1]) {
                                        for (int i = 0; i < piBlockSize; ++i)
                                            for (int j = 0; j < i; ++j)
                                                assert exactHomo.getColumn(offset + i*piBlockSize + j).equals(
                                                       exactHomo.getColumn(offset + j*piBlockSize + i));
                                        offset = offset + piBlockSize * piBlockSize;
                                    }
                                }

                                ////////////////////////////////////////////////////////////////////////////
				// Let's have CSDP do the hard work for us
				
                                final int[] piBlockSizes = Arrays.copyOf(blockSizes, prodsOfFs.size());
//				System.out.println("matrix block sizes: " + Arrays.toString(piBlockSizes));

				final double[] homo = SparsePolynomial.toDoubleArray(exactHomo);
				final double[] hetero = SparsePolynomial.toDoubleArray(asMatrix(exactHetero));
				
				final double[] approxSolution =
				    new double[prodsOfFs.size()*piBlockSize*piBlockSize];

				final int sdpRes =
				    CSDPInterface.sdp(piBlockSizes, homo, hetero, approxSolution);

				if (sdpRes == 0 || sdpRes == 3) {
					System.out.println("Found an approximate solution!");
					System.out.println(Arrays.toString(approxSolution));
					
					final Vector exactSolution =
					    approx2Exact(piBlockSizes, approxSolution, exactHomo, exactHetero);
					
					if (exactSolution != null) {
					    System.out.println("Found an exact solution");
					    return true;
					}
					
/*
					final List<Arithmetic> consideredMonomials =
					    convertToMonomList(qMonomials, prodsOfFs.size());
                                            final Square[] cert =
                                                GroebnerBasisChecker.approx2Exact(consideredMonomials,
                                                                                  approxSolution,
                                                                                  exactHomo, exactHetero);
					
					if (cert != null) {
						// check that the certificate is correct

						System.out.println("Certificate:");
						System.out.println(" 1");

						Polynomial p = one;
						for (int i = 0; i < cert.length; ++i) {
							assert (Operations.greaterEqual.apply(
									cert[i].coefficient, Values.getDefault()
											.ZERO()));
							p = (Polynomial) p.add(cert[i].body.multiply(
									cert[i].body).scale(cert[i].coefficient));
							System.out.println(" + " + cert[i].coefficient
									+ " * ( " + cert[i].body + " ) ^2");
						}
						System.out.println(" =");
						System.out.println(" " + p);
						System.out.println("Certificate is correct");
						return true;
					} */
				} else {
					System.out.println("No solution");
				}
			}}
		}
		
		return false;
	}

    private static Vector approx2Exact(int[] blockSizes,
                                       double[] approxSolution,
                                       Matrix exactHomo,
                                       Vector sideConditionRhs) {
        final ValueFactory vf = Values.getDefault();

        // we add further constraints to ensure that the found solution
        // is a symmetric matrix
        exactHomo.insertRows(symmetryConstraints(blockSizes));

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

            final Vector exactSolution =
                new FractionisingEquationSolver(exactHomo, exactHetero, approxSolution,
                                                eps).exactSolution;
            System.out.println("Exact solution candidate: " + exactSolution);

            assert (exactHomo.multiply(exactSolution).equals(exactHetero));

            System.out.println("Difference to approx solution:");
            System.out.println(exactSolution.subtract(vf.valueOf(approxSolution)));

            // check that the solution is positive semi-definite
            // we check each of the matrix blocks independently
            
            PSDcheck: {
                
            int offset = 0;
            for (int blockSize : blockSizes) {
                final Matrix solutionMatrix = vf.newInstance(blockSize, blockSize);
                for (int i = 0; i < blockSize; ++i)
                    for (int j = 0; j < blockSize; ++j)
                        solutionMatrix.set(i, j, exactSolution.get(offset + i * blockSize + j));
                
                System.out.println("checking block: " + solutionMatrix);
                
                try {
                    final PSDDecomposition dec = new PSDDecomposition(solutionMatrix);
                    System.out.println("Block is positive semi-definite");
                    System.out.println("T-matrix:");
                    System.out.println(dec.T);
                    System.out.println("D-matrix:");
                    System.out.println(dec.D);
                    // TODO: generate a real certificate and check it
                } catch (NotPSDException e) {
                    System.out.println(e.getMessage());
                    break PSDcheck;
                }
                
                offset = offset + blockSize * blockSize;
            }
            
            // all blocks are positive semidefinite
            return exactSolution;
            }

            eps = eps / 10;
        }

        return null;
    }

    private static Matrix symmetryConstraints(int[] blockSizes) {
        final ValueFactory vf = Values.getDefault();

        final Arithmetic one = vf.ONE();
        final Arithmetic minus_one = one.minus();

        int height = 0;
        int width = 0;
        for (int blockSize : blockSizes) {
            height = height + blockSize * (blockSize - 1) / 2;
            width = width + blockSize * blockSize;
        }
        
        if (height == 0)
            // just return a zero-matrix so that we don't have to come back
            // empty-handed
            return vf.ZERO(1, width);

        final Matrix res = vf.ZERO(height, width);

        // generate the constraints
        int row = 0;
        int offset = 0;
        for (int blockSize : blockSizes) {
            for (int i = 1; i < blockSize; ++i)
                for (int j = 0; j < i; ++j) {
                    res.set(row, offset + i * blockSize + j, one);
                    res.set(row, offset + j * blockSize + i, minus_one);
                    row = row + 1;
                }
            offset = offset + blockSize * blockSize;
        }

        assert (row == height);
        return res;
    }

    private static Vector genExactHetero(SparsePolynomial fh, Polynomial nextG,
                                  List<Arithmetic> monomialsInFH) {
        final Vector exactHetero = Values.getDefault().ZERO(fh.size());
        Iterator<KeyValuePair> monoms = nextG.monomials();
        while (monoms.hasNext()) {
            KeyValuePair nextMonom = monoms.next();
			int indexOf = monomialsInFH.indexOf(nextMonom.getKey());
            Arithmetic next = (Arithmetic) nextMonom.getValue();
            if (!next.isZero())
                exactHetero.set(indexOf, next.minus());
        }
        return exactHetero;
    }

    private static Set<Polynomial>
                   generateFProducts(PolynomialClassification<Polynomial> classify,
                                     Polynomial one) {
        Set<Polynomial> prodsOfFs = new LinkedHashSet<Polynomial>();
        prodsOfFs.add(one);
        List<Polynomial> curF = new ArrayList<Polynomial>(classify.f);
        System.out.println("curF " + curF);// XXX
        int maxFDegree = 0;
        
        final boolean[] combination = new boolean[curF.size()];
        while (true) {
            Polynomial currentF = one;
            for (int i = 0; i < combination.length; ++i)
                if (combination[i])
                    currentF = currentF.multiply(curF.get(i));
            if (currentF.degreeValue() > maxFDegree) {
                maxFDegree = currentF.degreeValue();
            }
            prodsOfFs.add(currentF);
                            
            int i = 0;
            for (; i < combination.length; ++i) {
                if (combination[i]) {
                    combination[i] = false;
                } else {
                    combination[i] = true;
                    break;
                }
            }
            if (i == combination.length)
                break;
        }
        return prodsOfFs;
    }

    private static List<Arithmetic> extractMonomialsInFH(SparsePolynomial fh,
                                                         Polynomial nextG) {
        final List<Arithmetic> monomialsInFH =
            new ArrayList<Arithmetic>(fh.getMonomials());
        // Vector zeroMonomial = Values.getDefault().valueOf(
        // new int[one.rank()]);
        // assert zeroMonomial.equals(zeroMonomial.zero()) : "Not 0 "
        // + zeroMonomial;

        Iterator<KeyValuePair> monomials = nextG.monomials();

        System.out.println(monomialsInFH);// XXX
        while (monomials.hasNext()) {
        	KeyValuePair next = monomials.next();
        	Arithmetic monomial = (Arithmetic) next.getKey();
        	Arithmetic coefficient = (Arithmetic) next.getValue();
        	if (!coefficient.isZero() && !monomialsInFH.contains(monomial)) {
        	    // then we immediately now that there is no solution
        	    return null;
        	}
        }
        return monomialsInFH;
    }

	private static void copy(Matrix src, Matrix target,
	                         int srcRow, int srcColumn,
	                         int targetRow, int targetColumn,
	                         int height, int width) {
            for (int j = 0; j < height; ++j)
                for (int i = 0; i < width; ++i)
                    target.set(j + targetRow, i + targetColumn,
                               src.get(j + srcRow, i + srcColumn));
	}
	
	private static Matrix asMatrix(Vector v) {
	    final Matrix res = Values.getDefault().newInstance(v.dimension(), 1);
	    res.setColumn(0, v);
	    return res;
	}

	
	/**
	 * Existentially quantify certain variables in a system of linear
	 * equations and eliminate them.
	 */
	private static Matrix project(Matrix m, BitSet elimColumns) {
	    // first solve the equations
	    
	    final Matrix echelon = (Matrix)m.clone();
	    echelon(echelon);
	    jordan(echelon);
	    
	    final Vector particular = particularSolution(echelon);
	    if (particular == null) {
	        // the system is unsolvable. we can just remove the columns
	        // for the eliminated variables
	        final Matrix res = (Matrix)m.clone();
	        for (int i = res.dimensions()[1] - 2; i >= 0; --i)
	            if (elimColumns.get(i))
	                res.removeColumn(i);
	        return res;
	    }
	    
	    final Matrix space = homoSolutionSpace(echelon);
	    
	    // then remove all variables that we want to eliminate
	    for (int i = particular.dimension() - 1; i >= 0; --i)
	        if (elimColumns.get(i)) {
	            particular.remove(i);
	            space.removeRow(i);
	        }
	    
	    // and then convert back to equations
	    final Matrix resEquations = space.transpose();
	    final Matrix zeroRhs = Values.getDefault().ZERO(resEquations.dimensions()[0], 1);
            resEquations.insertColumns(zeroRhs);
	    
            echelon(resEquations);
            jordan(resEquations);

            final Matrix res = homoSolutionSpace(resEquations).transpose();
	    res.insertColumns(asMatrix(res.multiply(particular)));
            return res;
	}
	
	    /**
	     * Read of an arbitrary particular solution from a matrix in echelon form
	     * (it is assumed that the last column of the matrix is the RHS of a
	     * system of equations). If the system of equations is unsolvable,
	     * return <code>null</code>
	     */
	    private static Vector particularSolution(Matrix m) {
	        final int height = m.dimensions()[0];
	        final int width = m.dimensions()[1];

	        final Vector res = Values.getDefault().ZERO(width - 1);
	        
	        int row = height - 1;
	        int col = width - 2;

	        while (row >= 0 && col >= 0) {
	            // find the left-most column with a non-zero entry in the current row
	            int i = 0;
	            while (i < width && m.get(row, i).isZero())
	                i = i + 1;

	            if (i == width) {
	                // trivial row, go to the next one
	                row = row - 1;
	                continue;
	            } else if (i == width - 1) {
	                // this system of equations is unsolvable
	                return null;
	            }
	            
	            assert (i <= col);
	            while (i < col) {
	                // these solution components can be chosen arbitrarily, we just
	                // take zero
	                res.set(col, Values.getDefault().ZERO());
	                col = col - 1;
	            }
	            
	            assert (m.get(row, col).isOne());

	            Arithmetic val = m.get(row, width - 1);
	            for (int j = col + 1; j < width - 1; ++j)
	                val = val.subtract(res.get(j).multiply(m.get(row, j)));
	            
	            res.set(col, val);
	            
	            col = col - 1;
	            row = row - 1;
	        }
	        
	        return res;        
	    }
	    
	    
	    /**
	     * Read of the space of homogeneous solutions of a system of linear
	     * equations that is described by a matrix in echelon form (it is assumed
	     * that the last column of the matrix is the RHS of the equations). The
	     * columns of the returned matrix are the vectors generating the solution
	     * space.
	     */
	    private static Matrix homoSolutionSpace(Matrix m) {
	        final Integer minus_one = Values.getDefault().valueOf(-1);

	        final List<java.lang.Integer> zeroRows =
	            new ArrayList<java.lang.Integer> ();
	        
	        final int height = m.dimensions()[0];
	        final int width = m.dimensions()[1];

	        final Matrix solutions = Values.getDefault().ZERO(width - 1, width - 1);
	        
	        int row = 0;
	        int col = 0;
	        int nextSolutionColumn = 0;
	        
	        while (row <= height && col < width - 1) {
	            while (col < width - 1 && (row == height || m.get(row, col).isZero())) {
	                // add a further vector to the solution matrix
	                int vecPos = 0;
	                int mPos = 0;
	                int zeroRowsPos = 0;
	                while (mPos < row) {
	                    if (zeroRowsPos < zeroRows.size() &&
	                        zeroRows.get(zeroRowsPos).equals(mPos)) {
	                        zeroRowsPos = zeroRowsPos + 1;
	                    } else {
	                        solutions.set(vecPos, nextSolutionColumn, m.get(mPos, col));
	                        mPos = mPos + 1;
	                    }
	                    vecPos = vecPos + 1;
	                }
	                
	                vecPos = vecPos + zeroRows.size() - zeroRowsPos;
	                
	                solutions.set(vecPos, nextSolutionColumn, minus_one);
	                nextSolutionColumn = nextSolutionColumn + 1;
	                
	                zeroRows.add(row);
	                col = col + 1;
	            }
	            
	            assert (col == width - 1 || m.get(row, col).isOne());
	            
	            row = row + 1;
	            col = col + 1;
	        }
	        
	        // because Orbital does not like empty matrices, we return a matrix with
	        // the zero-vector in case the solution space only contains a single
	        // element
                final int resWidth = Math.max(nextSolutionColumn, 1);
	        final Matrix res = Values.getDefault().newInstance(width - 1, resWidth);
	        copy(solutions, res, 0, 0, 0, 0, width - 1, resWidth);
	        
	        return res;
	    }
	    
	    /**
	     * Turn a matrix into row echelon form
	     */
	    private static void echelon(Matrix m) {
	        final int height = m.dimensions()[0];
	        final int width = m.dimensions()[1];
	        
	        int row = 0;
	        int col = 0;

	        while (row < height && col < width) {
	            // search for the first non-zero element in the current column
	            int i = row;
	            while (i < height && m.get(i, col).isZero())
	                i = i + 1;

	            if (i == height) {
	                // there is no pivot element in the current column, we try the
	                // next one
	                col = col + 1;
	                continue;
	            } else if (i != row) {
	                // swap the current row with the row with the pivot element
	                for (int j = col; j < width; ++j) {
	                    final Arithmetic t = m.get(row, j);
	                    m.set(row, j, m.get(i, j));
	                    m.set(i, j, t);
	                }
	            }

	            // turn the pivot element of this row into a one
	            final Arithmetic pivot = m.get(row, col);
	            if (!pivot.isOne()) {
	                for (int j = col; j < width; ++j)
	                    m.set(row, j, m.get(row, j).divide(pivot));
	            }

	            // simplify the other rows using the row with the pivot element
	            i = i + 1;
	            while (i < height) {
	                if (!m.get(i, col).isZero()) {
	                    final Arithmetic factor = m.get(i, col);

	                    for (int j = col; j < width; ++j) {
	                        final Arithmetic newEl =
	                            m.get(i, j).subtract(m.get(row, j).multiply(factor));
	                        m.set(i, j, newEl);
	                    }
	                }
	                
	                i = i + 1;
	            }
	            
	            row = row + 1;
	            col = col + 1;
	        }
	    }

	    /**
	     * Clean up a matrix in row echelon form
	     * 
	     * TODO: better name for the method
	     */
	    private static void jordan(Matrix m) {
	        final int height = m.dimensions()[0];
	        final int width = m.dimensions()[1];

	        int row = height - 1;

	        while (row >= 0) {
	            // find the left-most column with a non-zero entry in the current row
	            int col = 0;
	            while (col < width && m.get(row, col).isZero())
	                col = col + 1;

	            if (col == width) {
	                // trivial row, go to the next one
	                row = row - 1;
	                continue;
	            }
	            
	            assert (m.get(row, col).isOne());
	            
	            // simplify this column by subtracting multiplies of this row from
	            // other rows
	            int i = row - 1;
	            while (i >= 0) {
	                if (!m.get(i, col).isZero()) {
	                    final Arithmetic factor = m.get(i, col);

	                    for (int j = col; j < width; ++j) {
	                        final Arithmetic newEl =
	                            m.get(i, j).subtract(m.get(row, j).multiply(factor));
	                        m.set(i, j, newEl);
	                    }
	                }
	                i = i - 1;
	            }
	            
	            row = row - 1;
	        }
	    }

	
	/**
	 * @param monomials
	 * @return TODO documentation since Feb 23, 2009
	 */
	private static List<Arithmetic> convertToMonomList(List<Vector> monomials,
			int count) {
		List<Arithmetic> result = new ArrayList<Arithmetic>();
		for (int i = 0; i < count; i++) {
			result.addAll(monomials);
		}
		return result;
	}

	/**
	 * This method test whether a given polynomial is a sum of squares.
	 * 
	 * @param inputPolynomial
	 * @return
	 */
	private static Result testIfPolynomialIsSumOfSquares(Polynomial inputPolynomial) {
		// now we need to translate the polynominal into a matrix representation
		// monominals are iterated x^0y^0, x^0y^1, x^0y^2, ..., x^1y^0, x^1y^1,
		// x^1y^2,..., x^2y^0, x^2y^1,...
		Iterator<KeyValuePair> monomials = inputPolynomial.monomials();
		List<Vector> monominals = new ArrayList<Vector>();
		
		while (monomials.hasNext()) {
			KeyValuePair nextMono = monomials.next();
			Object nextVector = nextMono.getKey();
			Object next = nextMono.getValue();
			String blub = "";

			Vector monomialDegrees = null;
			if (nextVector instanceof Vector) {
				monomialDegrees = (Vector) nextVector;
			} else {
				monomialDegrees = Values.getDefault().valueOf(
						new Integer[] { (Integer) nextVector });
			}
			for (int i = 0; i < monomialDegrees.dimension(); i++) {
				blub += ((char) ('a' + i)) + "^" + monomialDegrees.get(i);
			}
			System.out.println(next + "*" + blub);// XXX
			if (!next.equals(Values.getDefault().ZERO())) {
				boolean ok = true;
				Vector div = Values.getDefault().valueOf(
						new int[monomialDegrees.dimension()]);
				for (int i = 0; i < monomialDegrees.dimension(); i++) {
					if (monomialDegrees.get(i) instanceof Real) {
						Real in = (Real) monomialDegrees.get(i);
						Real sqrt = in.divide(Values.getDefault().valueOf(2));
						try {
							new BigDecimal(sqrt.doubleValue()).intValueExact();
							double[] d = new double[monomialDegrees.dimension()];
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
		Poly quadraticForm = multiplyVec(multiplyMatrix(monominals, matrix),
				monominals);
		System.out.println("Polynom: " + quadraticForm);// XXX
		monomials = inputPolynomial.monomials();

		List<Constraint> constraints = new ArrayList<Constraint>();
		while (monomials.hasNext()) {
			KeyValuePair nextMono = monomials.next();
			Object next = nextMono.getValue();
			Object nextVector = nextMono.getKey();

			Vector monomialDegrees = null;
			if (nextVector instanceof Vector) {
				monomialDegrees = (Vector) nextVector;
			} else {
				monomialDegrees = Values.getDefault().valueOf(
						new Integer[] { (Integer) nextVector });
			}
			if (!Values.getDefault().ZERO().equals(next)) {
				System.out.println("Checking: " + next + " and vector "
						+ monomialDegrees);// XXX
				List<Vector> list = quadraticForm.vec.get(monomialDegrees);
				if (list != null) {
					Constraint constraint = new Constraint(monomialDegrees,
							list, (Arithmetic) next);
					System.out.println("Added constraint " + constraint);// XXX
					constraints.add(constraint);
				} else {
					System.out.println("Cannot express: " + monomialDegrees);// XXX
					return Result.UNKNOWN;
				}
			}
		}
		System.out.println(constraints);// XXX
		// outputMatlab(monominals, constraints);

		double[] solution = new double[monominals.size() * monominals.size()];
		if (CSDPInterface.sdp(monominals.size(), convertConstraintsToCSDP(constraints,
				monominals.size()),
				convertConstraintsToResultVector(constraints), solution) == 0) {
			// System.out.println(quadraticForm.toSparsePolynomial());//XXX
			// Square[] cert = GroebnerBasisChecker.approx2Exact(
			// quadraticForm.toSparsePolynomial(), monominals, solution);
			// if (cert != null) {
			// // check that the certificate is correct
			//	            
			// System.out.println("Certificate:");
			// System.out.println(" 1");
			//	            
			// Polynomial p = (Polynomial) inputPolynomial.one();
			// for (int i = 0; i < cert.length; ++i) {
			// assert (Operations.greaterEqual.apply(cert[i].coefficient,
			// Values.getDefault().ZERO()));
			// p = (Polynomial) p.add(cert[i].body.multiply(cert[i].body)
			// .scale(cert[i].coefficient));
			// System.out.println(" + " + cert[i].coefficient + " * ( " +
			// cert[i].body + " ) ^2");
			// }
			// System.out.println(" =");
			// System.out.println(" " + p);
			// // assert (((Polynomial) groebnerReducer.apply(p)).isZero());
			// System.out.println("Certificate is correct");
			return Result.SOLUTION_FOUND;
			// }
			// return Result.UNKNOWN;
		} else {
			return Result.NO_SOLUTION_AVAILABLE;
		}
	}

	/**
	 * @param monominals
	 * @param constraints
	 */
	private static void outputMatlab(List<Vector> monominals,
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
	private static double[] convertConstraintsToCSDP(List<Constraint> constraints,
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
	 * <p>
	 * Extract the result vector part from the given list of constraints.
	 * </p>
	 * 
	 * @param constraints
	 */
	private static double[] convertConstraintsToResultVector(
			List<Constraint> constraints) {
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
	private static void convertConstraints(List<Constraint> constraints, int size) {
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
	private static class Constraint {

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
	private static class Poly {
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
			System.out.println("Converting " + this);// XXX
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
			for (Vector mono : vec.keySet()) {
				for (Vector coefficient : vec.get(mono)) {
					int x = ((Integer) coefficient.get(0)).intValue() - 1;
					int y = ((Integer) coefficient.get(1)).intValue() - 1;
					// if(x <= y) {
					// we only need diagonal constraints here
					int monoInts[] = new int[mono.dimension()];
					for (int i = 0; i < mono.dimension(); i++) {
						Real r = (Real) mono.get(i);
						monoInts[i] = (int) r.doubleValue();
					}
					sparsePolynomial.addTerms(Values.getDefault().MONOMIAL(
							monoInts), x + y * maxX);
					// }
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
	private static Poly multiplyVec(Vec multiplyMatrix, List<Vector> monominals) {
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

	private static class Vec {
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
	private static Vec multiplyMatrix(List<Vector> monominals, Vector[][] matrix) {
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

	/**
	 * @param classify
	 * @param i
	 * @return
	 */
	public static boolean checkCombinedSetForEmptyness(
			PolynomialClassification<Term> classify, int degreeBound) {
		return checkCombinedSetForEmptyness(classify.f, classify.g, classify.h,
				degreeBound);
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.arithmetics.ISOSChecker#testForTautology(de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker.PolynomialClassification, de.uka.ilkd.key.java.Services)
	 */
	@Override
	public boolean testForTautology(Set<Term> ante, Set<Term> succ,
			Services services) throws RemoteException {
		return checkCombinedSetForEmptyness(classify(ante, succ, null), 15); //FIXME: degree bound is hardcoded
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#abortCalculation()
	 */
	@Override
	public void abortCalculation() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getCachedAnswerCount()
	 */
	@Override
	public long getCachedAnswerCount() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getName()
	 */
	@Override
	public String getName() {
		return "Built-in SOS";
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getQueryCount()
	 */
	@Override
	public long getQueryCount() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getTimeStatistics()
	 */
	@Override
	public String getTimeStatistics() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getTotalCalculationTime()
	 */
	@Override
	public long getTotalCalculationTime() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getTotalMemory()
	 */
	@Override
	public long getTotalMemory() throws RemoteException,
			ServerStatusProblemException, ConnectionProblemException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#isConfigured()
	 */
	@Override
	public boolean isConfigured() {
		return true;
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#resetAbortState()
	 */
	@Override
	public void resetAbortState() throws RemoteException {
		// TODO Auto-generated method stub
		
	}
}
