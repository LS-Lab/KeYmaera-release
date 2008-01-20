/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import orbital.math.Polynomial;
import orbital.math.Values;
import de.uka.ilkd.key.dl.formulatools.VariableCollector;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Equality;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.TermSymbol;

/**
 * @author jdq
 * 
 */
public class SumOfSquaresChecker {

    public static final SumOfSquaresChecker INSTANCE = new SumOfSquaresChecker();

    public void check(Set<Term> ante, Set<Term> succ) {
        final Function lt = getFunction("lt");
        final Function leq = getFunction("leq");
        final Function geq = getFunction("geq");
        final Function gt = getFunction("gt");
        final Function neq = getFunction("neq");
        Term zero = TermBuilder.DF.func(NumberCache.getNumber(
                new BigDecimal(0), RealLDT.getRealSort()));
        // handle succedent
        Set<Term> conjunction = new HashSet<Term>();
        for (Term t : succ) {
            Term sub = t.sub(0);
            Term sub2 = t.sub(1);
            Operator op = negationLookUp(t.op());
            if (!(sub.equals(zero) || sub2.equals(zero))) {
                sub = TermBuilder.DF.func(getFunction("sub"), t.sub(0), t
                        .sub(1));
                sub2 = zero;
            }
            if (t.sub(0).equals(zero) && !t.sub(1).equals(zero)) {
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
        conjunction.addAll(ante);
        // split to f, g, h
        Set<Term> f = new HashSet<Term>();
        Set<Term> g = new HashSet<Term>();
        Set<Term> h = new HashSet<Term>();
        for (Term t : conjunction) {
            if (t.op() == Equality.EQUALS) {
                h.add(t);
            } else if (t.op().equals(neq)) {
                g.add(t);
            } else if (t.op().equals(geq)) {
                f.add(t);
            } else if (t.op().equals(gt)) {
                f.add(TermBuilder.DF.func(geq, t.sub(0), t.sub(1)));
                g.add(TermBuilder.DF.func(neq, t.sub(0), t.sub(1)));
            } else if (t.op().equals(leq)) {
                f.add(TermBuilder.DF.func(geq, TermBuilder.DF.func(
                        getFunction("neg"), t.sub(0)), t.sub(1)));
                g.add(TermBuilder.DF.func(neq, TermBuilder.DF.func(
                        getFunction("neg"), t.sub(0)), t.sub(1)));
            } else if (t.op().equals(lt)) {
                f.add(TermBuilder.DF.func(geq, TermBuilder.DF.func(
                        getFunction("neg"), t.sub(0)), t.sub(1)));
            } else {
                throw new IllegalArgumentException(
                        "Dont know how to handle the predicate " + t.op());
            }
        }
        check(f, g, h);
    }

    /**
     * @param op
     * @return
     */
    private Operator negationLookUp(Operator op) {
        Function lt = getFunction("lt");
        Function leq = getFunction("leq");
        Function geq = getFunction("geq");
        Function gt = getFunction("gt");
        if (op.equals(getFunction("neq"))) {
            return Equality.EQUALS;
        } else if (op == Equality.EQUALS) {
            return getFunction("neq");
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
     * Compute a conjunction of inequaltities f of the form f >= 0, g != 0 or
     * equalities h = 0. Afterwards check if f+g^2+h = 0 is satisfiable. If this
     * holds the input is satisfiable too.
     */
    public void check(Set<Term> f, Set<Term> g, Set<Term> h) {
        Set<String> variables = new HashSet<String>();
        for (Term t : f) {
            variables.addAll(VariableCollector.getVariables(t));
        }
        for (Term t : g) {
            variables.addAll(VariableCollector.getVariables(t));
        }
        for (Term t : h) {
            variables.addAll(VariableCollector.getVariables(t));
        }
        List<String> vars = new ArrayList<String>();
        vars.addAll(variables);
        Polynomial result = null;
        for (Term t : f) {
            Polynomial poly = createPoly(t.sub(0), vars);
            if(result == null) {
                result = poly;
            } else {
                result.add(poly);
            }
            System.out.println(t);
            System.out.println(poly);
        }
        Polynomial gPoly = null;
        for (Term t : g) {
            Polynomial poly = createPoly(t.sub(0), vars);
            if(gPoly == null) {
                gPoly = poly;
            } else {
                gPoly = gPoly.multiply(poly);
            }
            System.out.println(t);
            System.out.println(poly);
        }
        if(gPoly != null) {
            result.add((Polynomial) gPoly.power(Values.getDefault().valueOf(2)));
        }
        System.out.println("GPoly: " + gPoly);//XXX
        for (Term t : h) {
            Polynomial poly = createPoly(t.sub(0), vars);
            if(result == null) {
                result = poly;
            } else {
                result.add(poly);
            }
            System.out.println(t);
            System.out.println(poly);
        }
        System.out.println("Result = " + result);//XXX
    }

    /**
     * @param sub
     * @param variables
     * @return
     */
    private Polynomial createPoly(Term sub, List<String> variables) {
        int[] size = new int[variables.size()];
        if (sub.arity() == 0) {
            if (variables.contains(sub.op().name().toString())) {
                size[variables.indexOf(sub.op().name().toString())] = 1;
                return Values.getDefault().MONOMIAL(size);
            } else {
                return Values.getDefault().MONOMIAL(
                        Values.getDefault().valueOf(
                                new BigDecimal(sub.op().name().toString())
                                        .doubleValue()), size);
            }
        } else {
            if (sub.op().equals(getFunction("add"))) {
                return createPoly(sub.sub(0), variables).add(
                        createPoly(sub.sub(1), variables));
            } else if (sub.op().equals(getFunction("sub"))) {
                return createPoly(sub.sub(0), variables).subtract(
                        createPoly(sub.sub(1), variables));
            } else if (sub.op().equals(getFunction("mul"))) {
                return createPoly(sub.sub(0), variables).multiply(
                        createPoly(sub.sub(1), variables));
            } else if (sub.op().equals(getFunction("div"))) {
                // aufmultiplizieren noetig falls der nenner komplizierter ist
                return (Polynomial) createPoly(sub.sub(0), variables).multiply(
                        createPoly(sub.sub(1), variables).power(
                                Values.MINUS_ONE));
            } else if (sub.op().equals(getFunction("exp"))) {
                try {
                    return (Polynomial) createPoly(sub.sub(0), variables)
                            .power(
                                    Values.getDefault().valueOf(
                                            new BigDecimal(sub.sub(1).op()
                                                    .name().toString())));
                } catch (Exception e) {
                    return (Polynomial) createPoly(sub.sub(0), variables)
                            .power(createPoly(sub.sub(1), variables));
                }
            } else if (sub.op().equals(getFunction("neg"))) {
                return (Polynomial) createPoly(sub.sub(0), variables).multiply(
                        Values.getDefault().MONOMIAL(                                
                                        Values.MINUS_ONE, size));
            }
        }
        throw new IllegalArgumentException("Dont know what to do with"
                + sub.op());
    }

    /**
     * @param neq
     * @return
     */
    private Function getFunction(String neq) {
        return (Function) Main.getInstance().mediator().namespaces()
                .functions().lookup(new Name(neq));
    }

}
