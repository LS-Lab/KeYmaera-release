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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wolfram.jlink.Expr;
import com.wolfram.jlink.ExprFormatException;

import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Div;
import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.dl.model.GreaterEquals;
import de.uka.ilkd.key.dl.model.Less;
import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.dl.model.MinusSign;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.dl.model.Plus;
import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Named;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.op.RigidFunction.FunctionType;
import de.uka.ilkd.key.logic.sort.Sort;

/**
 * Converter for the transformation from a Mathematica Expr to a KeY Term.
 * 
 * @author jdq
 * @since 13.02.2007
 * 
 */
public class Expr2TermConverter implements ExprConstants {

    private static final String[] BLACKLIST_ARRAY = { "Reduce", "Simplify",
            "FullSimplify", "FindInstance", "Hold", "List", "Resolve",
            "DSolve", "D", "Dt", "Indeterminate" };

    private static final Set<String> BLACKLIST = new HashSet<String>(Arrays
            .asList(BLACKLIST_ARRAY));

    /**
     * @param nss
     * @param name
     * @return
     */
    private static Function lookupRigidFunction(NamespaceSet nss, Name name,
            int argNum) {
        Function num = (Function) nss.functions().lookup(name);
        Sort[] argSorts = new Sort[argNum];
        Arrays.fill(argSorts, RealLDT.getRealSort());
        if (num == null) {
            num = new RigidFunction(name, RealLDT.getRealSort(), argSorts);
        }
        return num;
    }

    /**
     * Converts the given Expr into an equivalant Term using the given
     * namespaces to determine which objects are to be used for variables and
     * functions.
     * 
     * @param expr
     *                the Expr to convert
     * @param nss
     *                the namespaces to determine which objects are to be used
     *                for variables and functions
     * @return a Term representing the given Expr.
     * @throws RemoteException
     *                 if there is an error while conversion
     */
    public static Term convert(Expr expr, NamespaceSet nss,
            Map<Name, LogicVariable> quantifiedVariables)
            throws RemoteException, UnableToConvertInputException {
        try {
            if (expr.toString().equalsIgnoreCase("$Aborted")) {
                throw new RemoteException("Calculation aborted!");
            } else if (expr.head().equals(FORALL) || expr.head().equals(EXISTS)) {
                Expr list = expr.args()[0];
                if (list.head().equals(LIST)) { // LIST
                    List<LogicVariable> vars = new ArrayList<LogicVariable>();
                    for (int i = 0; i < list.args().length; i++) {
                        Name name;
                        String asString = list.args()[i].asString();
                        if (asString.endsWith("$")) {
                            name = new Name(asString.substring(0, asString
                                    .length() - 1));
                        } else {
                            name = new Name(asString);
                        }
                        name = new Name(name.toString().replaceAll(
                                USCORE_ESCAPE, "_"));
                        LogicVariable lookup = (LogicVariable) nss.variables()
                                .lookup(name);
                        if (lookup == null) {
                            lookup = new LogicVariable(name, RealLDT
                                    .getRealSort());

                        }
                        vars.add(lookup);
                        quantifiedVariables.put(name, lookup);
                    }
                    if (expr.head().equals(EXISTS)) {
                        Term result = convert(expr.args()[1], nss,
                                quantifiedVariables);
                        for (LogicVariable var : vars) {
                            result = TermBuilder.DF.ex(var, result);
                        }
                        return result;
                    } else {
                        return TermBuilder.DF.all(vars
                                .toArray(new LogicVariable[0]), convert(expr
                                .args()[1], nss, quantifiedVariables));
                    }
                }
            }
            if (expr.head().equals(INEQUALITY)) {
                Term result = null;
                Term left = convert(expr.args()[0], nss, quantifiedVariables);
                Term mid = convert(expr.args()[2], nss, quantifiedVariables);
                Term right = convert(expr.args()[4], nss, quantifiedVariables);
                String func1 = "";
                String func2 = "";
                if (expr.args()[1].equals(LESS)) {
                    func1 = "lt";
                } else if (expr.args()[1].equals(LESS_EQUALS)) {
                    func1 = "leq";
                } else if (expr.args()[1].equals(GREATER_EQUALS)) {
                    func1 = "geq";
                } else if (expr.args()[1].equals(GREATER)) {
                    func1 = "gt";
                }
                if (expr.args()[3].equals(LESS)) {
                    func2 = "lt";
                } else if (expr.args()[3].equals(LESS_EQUALS)) {
                    func2 = "leq";
                } else if (expr.args()[3].equals(GREATER_EQUALS)) {
                    func2 = "geq";
                } else if (expr.args()[3].equals(GREATER)) {
                    func2 = "gt";
                }
                result = TermBuilder.DF.func(lookupRigidFunction(nss, new Name(
                        func1), 1), left, mid);
                result = TermBuilder.DF.and(result, TermBuilder.DF.func(
                        lookupRigidFunction(nss, new Name(func2), 1), mid,
                        right));
                return result;
            }
            Term[] ex = new Term[expr.args().length];
            for (int i = 0; i < ex.length; i++) {
                ex[i] = convert(expr.args()[i], nss, quantifiedVariables);
                if (ex[i] == null) {
                    throw new UnableToConvertInputException("Fail",
                            new UnableToConvertInputException("Converting "
                                    + expr.args()[i] + " failed!"));
                }
            }
            if (expr.rationalQ()) {
                return TermBuilder.DF.func(lookupRigidFunction(nss, new Name(
                        "div"), 2), ex[0], ex[1]);
            } else if (expr.numberQ()) {
                BigDecimal asBigDecimal = expr.asBigDecimal();
                boolean minus = false;
                if (asBigDecimal.compareTo(BigDecimal.ZERO) < 0) {
                    asBigDecimal = asBigDecimal.abs();
                    minus = true;
                }
                Name name = new Name("" + asBigDecimal);
                Function num = lookupRigidFunction(nss, name, 0);
                Term result = TermBuilder.DF.func(num);
                if (minus) {
                    result = TermBuilder.DF.func(lookupRigidFunction(nss,
                            new Name("neg"), 2), result);
                }
                return result;
            } else if (expr.head().symbolQ()) {
                if (expr.head().equals(PLUS)) {
                    Term result = ex[0];
                    for (int i = 1; i < ex.length; i++) {
                        result = TermBuilder.DF.func(RealLDT
                                .getFunctionFor(Plus.class), result, ex[i]);
                    }
                    return result;
                } else if (expr.head().equals(MINUS)) {
                    Term result = ex[0];
                    for (int i = 1; i < ex.length; i++) {
                        result = TermBuilder.DF
                                .func(
                                        RealLDT
                                                .getFunctionFor(de.uka.ilkd.key.dl.model.Minus.class),
                                        result, ex[i]);
                    }
                    return result;
                } else if (expr.head().equals(MINUSSIGN)) {
                    return TermBuilder.DF.func(RealLDT
                            .getFunctionFor(MinusSign.class), ex[0]);
                } else if (expr.head().equals(MULT)) {
                    Term result = ex[0];
                    for (int i = 1; i < ex.length; i++) {
                        result = TermBuilder.DF.func(RealLDT
                                .getFunctionFor(Mult.class), result, ex[i]);
                    }
                    return result;
                } else if (expr.head().equals(DIV)) {
                    Term result = ex[0];
                    for (int i = 1; i < ex.length; i++) {
                        result = TermBuilder.DF.func(RealLDT
                                .getFunctionFor(Div.class), result, ex[i]);
                    }
                    return result;
                } else if (expr.head().equals(EXP)) {
                    Term result = ex[0];
                    for (int i = 1; i < ex.length; i++) {
                        result = TermBuilder.DF.func(RealLDT
                                .getFunctionFor(Exp.class), result, ex[i]);
                    }
                    return result;
                } else if (expr.head().equals(INVERSE_FUNCTION)) {
                    Term result = ex[0];
                    result = TermBuilder.DF.func(lookupRigidFunction(nss,
                            new Name("InverseFunction"), 1), result, ex[1]);
                    return result;
                } else if (expr.head().equals(INTEGRATE)) {
                    Term result = ex[0];
                    result = TermBuilder.DF.func(lookupRigidFunction(nss,
                            new Name("InverseFunction"), 1), result, ex[1]);
                    return result;
                } else if (expr.head().equals(EQUALS)) {
                    Term result = ex[0];
                    result = TermBuilder.DF.equals(result, ex[1]);
                    return result;
                } else if (expr.head().equals(LESS)) {
                    Term result = ex[0];
                    result = TermBuilder.DF.func(RealLDT
                            .getFunctionFor(Less.class), result, ex[1]);
                    return result;
                } else if (expr.head().equals(GREATER)) {
                    Term result = ex[0];
                    result = TermBuilder.DF
                            .func(
                                    RealLDT
                                            .getFunctionFor(de.uka.ilkd.key.dl.model.Greater.class),
                                    result, ex[1]);
                    return result;
                } else if (expr.head().equals(GREATER_EQUALS)) {
                    Term result = ex[0];
                    result = TermBuilder.DF
                            .func(RealLDT.getFunctionFor(GreaterEquals.class),
                                    result, ex[1]);
                    return result;
                } else if (expr.head().equals(LESS_EQUALS)) {
                    Term result = ex[0];
                    result = TermBuilder.DF.func(RealLDT
                            .getFunctionFor(LessEquals.class), result, ex[1]);
                    return result;
                } else if (expr.head().equals(UNEQUAL)) {
                    Term result = ex[0];
                    result = TermBuilder.DF.func(RealLDT
                            .getFunctionFor(Unequals.class), result, ex[1]);
                    return result;
                } else if (expr.head().equals(AND)) {
                    Term result = ex[0];
                    for (int i = 1; i < ex.length; i++) {
                        result = TermBuilder.DF.and(result, ex[i]);
                    }
                    return result;
                } else if (expr.head().equals(OR)) {
                    Term result = ex[0];
                    for (int i = 1; i < ex.length; i++) {
                        result = TermBuilder.DF.or(result, ex[i]);
                    }
                    return result;
                } else if (expr.head().equals(NOT)) {
                    return TermBuilder.DF.not(ex[0]);
                } else if (expr.head().equals(IMPL)) {
                    Term result = ex[0];
                    for (int i = 1; i < ex.length; i++) {
                        result = TermBuilder.DF.imp(result, ex[i]);
                    }
                    return result;
                } else if (expr.head().equals(TRUE)) {
                    return TermBuilder.DF.tt();
                } else if (expr.head().equals(FALSE)) {
                    return TermBuilder.DF.ff();
                } else if (expr.toString().equals("E")
                        || expr.head().toString().equals("E")) {
                    Name e = new Name("E");
                    Function funcE = (Function) nss.functions().lookup(e);
                    if (funcE == null) {
                        funcE = lookupRigidFunction(nss, e, 0);
                    }
                    return TermBuilder.DF.func(funcE);
                } else if (ex.length > 0) {
                    Name name = new Name(expr.head().asString());
                    name = new Name(name.toString().replaceAll(USCORE_ESCAPE,
                            "_"));

                    Function f = (Function) nss.functions().lookup(name);
                    if (f == null) {
                        if (BLACKLIST.contains(name.toString())) {
                            throw new RemoteException(
                                    "Mathematica returned a system function "
                                            + name);
                        }
                        Sort[] argSorts = new Sort[ex.length];
                        Arrays.fill(argSorts, RealLDT.getRealSort());
                        f = new RigidFunction(name, RealLDT.getRealSort(),
                                argSorts, FunctionType.MATHFUNCTION);
                        nss.functions().add(f);
                    }
                    if (expr.args().length > 0) {
                        return TermBuilder.DF.func(f, ex);
                    }
                    return TermBuilder.DF.func(f);
                } else {
                    Name name;
                    if (expr.asString().endsWith("$")) {
                        name = new Name(expr.asString().substring(0,
                                expr.asString().length() - 1));
                    } else {
                        name = new Name(expr.toString());
                    }
                    name = new Name(name.toString().replaceAll(USCORE_ESCAPE,
                            "_"));
                    if (quantifiedVariables.containsKey(name)) {
                        return TermBuilder.DF
                                .var(quantifiedVariables.get(name));
                    }
                    Named var = nss.variables().lookup(name);
                    if (var != null) {
                        if (var instanceof LogicVariable) {
                            return TermBuilder.DF.var((LogicVariable) var);
                        } else if (var instanceof Metavariable) {
                            return TermFactory.DEFAULT
                                    .createFunctionTerm((Metavariable) var);
                        }
                    } else {
                        var = nss.functions().lookup(name);
                        if (var != null) {
                            return TermBuilder.DF.func((Function) var, ex);
                        } else {
                            var = nss.programVariables().lookup(name);
                            if (var == null) {
                                // var = new
                                // de.uka.ilkd.key.logic.op.LocationVariable(
                                // new ProgramElementName(name.toString()),
                                // getSortR(nss));
                                throw new UnableToConvertInputException(
                                        "ProgramVariable " + name
                                                + " is not declared");
                            }
                            return TermBuilder.DF
                                    .var((de.uka.ilkd.key.logic.op.ProgramVariable) var);
                        }
                    }
                }
            }
        } catch (ExprFormatException e) {
            throw new UnableToConvertInputException("Error converting Expr " + expr + " to Formula", e);
        }
        throw new UnableToConvertInputException("Dont know how to convert " + expr);
    }
}
