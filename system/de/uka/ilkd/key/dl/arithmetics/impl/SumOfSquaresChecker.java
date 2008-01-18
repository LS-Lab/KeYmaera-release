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
import java.util.HashSet;
import java.util.Set;

import orbital.math.Polynomial;
import orbital.math.ValueFactory;
import orbital.math.Values;

import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Equality;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
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
        for (Term t : f) {
            Polynomial poly = createPoly(t.sub(0));
        }
    }

    /**
     * @param sub
     * @return
     */
    private Polynomial createPoly(Term sub) {
        if (sub.arity() == 0) {
            if (sub.op() instanceof Function) {
                try {
                    return Values.getDefault().polynomial(
                            new BigDecimal(sub.op().name().toString()));
                } catch (Exception e) {
                    return Values.getDefault().polynomial(
                            Values.getDefault().valueOf(
                                    sub.op().name().toString()));
                }
            } else if (sub.op() instanceof QuantifiableVariable
                    || sub.op() instanceof ProgramVariable) {
                return Values.getDefault()
                        .polynomial(
                                Values.getDefault().valueOf(
                                        sub.op().name().toString()));
            }
        } else {
            if(sub.op().equals(getFunction("add"))) {
                return createPoly(sub.sub(0)).add(createPoly(sub.sub(1)));
            } else if(sub.op().equals(getFunction("sub"))) {
                return createPoly(sub.sub(0)).subtract(createPoly(sub.sub(1)));
            } else if(sub.op().equals(getFunction("mul"))) {
                return createPoly(sub.sub(0)).multiply(createPoly(sub.sub(1)));
            } else if(sub.op().equals(getFunction("div"))) {
                return (Polynomial) createPoly(sub.sub(0)).multiply(createPoly(sub.sub(1)).power(Values.MINUS_ONE));
            } else if(sub.op().equals(getFunction("exp"))) {
                return (Polynomial) createPoly(sub.sub(0)).power(createPoly(sub.sub(1)));   
            }
        }
        throw new IllegalArgumentException("Dont know what to do with" + sub.op());
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
