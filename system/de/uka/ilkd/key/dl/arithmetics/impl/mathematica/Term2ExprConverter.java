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
/**
 * File created 13.02.2007
 */
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica;

import java.math.BigDecimal;

import com.wolfram.jlink.Expr;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Quantifier;

/**
 * Converter for the Term to Expr transformation
 * 
 * @author jdq
 * @since 13.02.2007
 * 
 */
public class Term2ExprConverter implements ExprConstants {

    /**
     * Converts the given term into the Mathematica Expr format.
     * 
     * @param form
     *                the term to convert
     * @return the equivalant Expr
     */
    public static Expr convert2Expr(Term form) {
        Expr[] args = new Expr[form.arity()];
        for (int i = 0; i < args.length; i++) {
            args[i] = convert2Expr(form.sub(i));
        }
        if (form.op() == Op.FALSE) {
            return FALSE;
        } else if (form.op() == Op.TRUE) {
            return TRUE;
        } else if (form.op().name().toString().equals("equals")) {
            return new Expr(EQUALS, args);
        } else if (form.op() instanceof Function) {
            Function f = (Function) form.op();
            if (f.name().toString().equals("gt")) {
                return new Expr(GREATER, args);
            } else if (f.name().toString().equals("geq")) {
                return new Expr(GREATER_EQUALS, args);
            } else if (f.name().toString().equals("equals")) {
                return new Expr(EQUALS, args);
            } else if (f.name().toString().equals("neq")) {
                return new Expr(UNEQUAL, args);
            } else if (f.name().toString().equals("leq")) {
                return new Expr(LESS_EQUALS, args);
            } else if (f.name().toString().equals("lt")) {
                return new Expr(LESS, args);
            } else if (f.name().toString().equals("add")) {
                return new Expr(PLUS, args);
            } else if (f.name().toString().equals("sub")) {
                return new Expr(MINUS, args);
            } else if (f.name().toString().equals("neg")) {
                return new Expr(MINUSSIGN, args);
            } else if (f.name().toString().equals("mul")) {
                return new Expr(MULT, args);
            } else if (f.name().toString().equals("div")) {
                for (Expr e : args) {
                    boolean rational = e.numberQ();
                    if (!rational) {
                        return new Expr(DIV, args);
                    }
                }
                return new Expr(RATIONAL, args);
            } else if (f.name().toString().equals("exp")) {
                return new Expr(EXP, args);
            } else {
                try {
                    BigDecimal d = new BigDecimal(form.op().name().toString());
                    try {
                        return new Expr(d.intValueExact());
                    } catch (ArithmeticException e) {
                        return new Expr(Expr.SYM_REAL,
                                new Expr[] { new Expr(d) });
                    }
                } catch (NumberFormatException e) {
                    String name = form.op().name().toString();
                    name = name.replaceAll("_", USCORE_ESCAPE);
                    Expr expr = new Expr(Expr.SYMBOL, name);
                    if (args.length == 0) {
                        return expr;
                    }
                    return new Expr(expr, args);
                }
            }
        } else if (form.op() instanceof LogicVariable
                || form.op() instanceof de.uka.ilkd.key.logic.op.ProgramVariable
                || form.op() instanceof Metavariable) {
            String name = form.op().name().toString();
            name = name.replaceAll("_", USCORE_ESCAPE);
            return new Expr(Expr.SYMBOL, name);
        } else if (form.op() instanceof Junctor) {
            if (form.op() == Junctor.AND) {
                return new Expr(AND, args);
            } else if (form.op() == Junctor.OR) {
                return new Expr(OR, args);
            } else if (form.op() == Junctor.IMP) {
                return new Expr(IMPL, args);
            } else if (form.op() == Junctor.NOT) {
                return new Expr(NOT, args);
            }
        } else if (form.op() instanceof Quantifier) {
            Expr[] newArgs = new Expr[args.length + 1];
            System.arraycopy(args, 0, newArgs, 1, args.length);
            Expr[] vars = new Expr[form.varsBoundHere(0).size()];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = new Expr(Expr.SYMBOL, form.varsBoundHere(0)
                        .getQuantifiableVariable(i).name().toString()
                        .replaceAll("_", USCORE_ESCAPE));
            }
            newArgs[0] = new Expr(LIST, vars);
            if (form.op() == Quantifier.ALL) {
                assert args.length == 1 : "'Unary' KeY quantifier \\forall x (" + args + ")";
                if(args[0].head() == FORALL) {
                    assert args[0].args().length == 2 : "'Binary' quantifier with variables and formula";
                    Expr kernel = args[0].args()[1];
                    assert args[0].args()[0].head() == LIST : "Term2ExprConverter always builds list quantifiers";
                    System.err.println(args[0]);
                    System.err.println("kernel " + args[0].args()[1]);
                    Expr[] innerVariables = args[0].args()[0].args();
                    // allVariables = (outer)vars + innerVariables 
                    Expr[] allVariables = new Expr[innerVariables.length +vars.length];
                    System.arraycopy(vars, 0, allVariables, 0, vars.length);
                    System.arraycopy(innerVariables, 0, allVariables, vars.length, innerVariables.length);
                    Expr[] mergedQuant = new Expr[args.length + 1];
                    assert mergedQuant.length == 2;
                    mergedQuant[0] = new Expr(LIST, allVariables);
                    mergedQuant[1] = kernel;
                    return new Expr(FORALL, mergedQuant);
                } else {
                    return new Expr(FORALL, newArgs);
                }
            } else if (form.op() == Quantifier.EX) {
                assert args.length == 1 : "'Unary' KeY quantifier \\exists x (" + args + ")";
                if(args[0].head() == EXISTS) {
                    assert args[0].args().length == 2 : "'Binary' quantifier with variables and formula";
                    Expr kernel = args[0].args()[1];
                    assert args[0].args()[0].head() == LIST : "Term2ExprConverter always builds list quantifiers";
                    Expr[] innerVariables = args[0].args()[0].args();
                    // allVariables = (outer)vars + innerVariables 
                    Expr[] allVariables = new Expr[innerVariables.length +vars.length];
                    System.arraycopy(vars, 0, allVariables, 0, vars.length);
                    System.arraycopy(innerVariables, 0, allVariables, vars.length, innerVariables.length);
                    Expr[] mergedQuant = new Expr[args.length + 1];
                    assert mergedQuant.length == 2;
                    mergedQuant[0] = new Expr(LIST, allVariables);
                    mergedQuant[1] = kernel;
                    return new Expr(EXISTS, mergedQuant);
                } else {
                    return new Expr(EXISTS, newArgs);
                }
            }
        }
        throw new IllegalArgumentException("Could not convert Term: " + form
                + "Operator was: " + form.op());
    }
}
