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
 * 
 */
package de.uka.ilkd.key.dl.arithmetics.impl.orbital;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import orbital.math.Arithmetic;
import orbital.math.Matrix;
import orbital.math.Values;
import orbital.math.Vector;
import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.FreeFunction;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.MinusSign;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.dl.model.Plus;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.java.ProgramElement;

/**
 * TODO jdq documentation since Aug 20, 2007
 * 
 * @author jdq
 * @since Aug 20, 2007
 * 
 */
public class DL2MatrixFormConverter {

    public static final DL2MatrixFormConverter INSTANCE = new DL2MatrixFormConverter();

    /**
     * 
     */
    private static final String CONSTANT = "[CONSTANT]";

    public static class MatrixForm {
        Matrix matrix;

        Vector b;

        Vector eta;
    }

    private static class ReturnVal {
        Arithmetic res;

        String variableName;
    }

    private DL2MatrixFormConverter() {
    }

    public MatrixForm convertToMatrixForm(List<String> variables,
            List<ProgramElement> diffEquations) {
        MatrixForm result = new MatrixForm();
        Map<String, Map<String, Arithmetic>> rows = new TreeMap<String, Map<String, Arithmetic>>();
        for (ProgramElement diffeq : diffEquations) {
            createRow(rows, diffeq, variables);
        }
        Arithmetic[] eta = new Arithmetic[rows.size()];
        Arithmetic[] b = new Arithmetic[rows.size()];
        Arithmetic[][] matrix = new Arithmetic[rows.size()][rows.size()];
        int row = 0;
        for (String str : rows.keySet()) {
            eta[row] = Values.getDefault().symbol(str);

            Map<String, Arithmetic> rule = rows.get(str);
            int col = 0;
            for (String s : rows.keySet()) {
                matrix[row][col] = rule.get(s);
                if (matrix[row][col] == null) {
                    matrix[row][col] = Values.getDefault().ZERO();
                }
                col++;
            }
            b[row] = rule.get(CONSTANT);
            if (b[row] == null) {
                b[row] = Values.getDefault().ZERO();
            }
            row++;
        }
        result.eta = Values.getDefault().valueOf(eta);
        result.matrix = Values.getDefault().valueOf(matrix);
        result.b = Values.getDefault().valueOf(b);
        return result;
    }

    /**
     * TODO jdq documentation since Aug 20, 2007
     * 
     * @param rows
     * @param diffeq
     */
    private void createRow(Map<String, Map<String, Arithmetic>> rows,
            ProgramElement diffeq, List<String> variables) {

        if (diffeq instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement de = (DLNonTerminalProgramElement) diffeq;

            if (de.getChildAt(0) instanceof Equals) {
                ProgramElement child = de.getChildAt(1);
                if (child instanceof Dot) {
                    Dot dot = (Dot) child;
                    String var = ((Variable) dot.getChildAt(0))
                            .getElementName().toString();

                    ProgramElement expression = de.getChildAt(2);
                    List<ProgramElement> addedTerms = splitPlus(expression);
                    Map<String, Arithmetic> rule = new HashMap<String, Arithmetic>();
                    for (ProgramElement t : addedTerms) {
                        List<ProgramElement> multTerms = splitMult(t);
                        ReturnVal val = new ReturnVal();
                        val.variableName = CONSTANT;
                        val.res = Values.getDefault().ONE();
                        for (ProgramElement m : multTerms) {
                            convert(variables, t, val, m);
                        }
                        rule.put(val.variableName, val.res);
                    }
                    rows.put(var, rule);
                    return;
                }
            }
        }
        throw new IllegalArgumentException(
                "Cannot convert differential equation" + diffeq);

    }

    /**
     * TODO jdq documentation since Aug 20, 2007
     * 
     * @param variables
     * @param t
     * @param val
     * @param m
     */
    private void convert(List<String> variables, ProgramElement t,
            ReturnVal val, ProgramElement m) {
        if (m instanceof Variable) {
            Variable variable = (Variable) m;
            String name = variable.getElementName().toString();
            if (variables.contains(name)) {
                if (val.variableName.equals(CONSTANT)) {
                    val.variableName = name;
                } else {
                    throw new IllegalStateException(
                            "Dont know what to do with the multiplication of two program variables");
                }
            }
        } else if (m instanceof Constant) {
            Constant c = (Constant) m;
            // try {
            // val.res = val.res.multiply(Values.getDefault()
            // .valueOf(
            // c.getValue()
            // .longValueExact()));
            // } catch (ArithmeticException e) {
            // val.res = val.res
            // .multiply(Values
            // .getDefault()
            // .valueOf(
            // c
            // .getValue()
            // .doubleValue()));
            // }
            val.res = val.res.multiply(Values.getDefault()
                    .valueOf(c.getValue()));
        } else if (m instanceof FunctionTerm) {
            FunctionTerm ft = (FunctionTerm) m;
            ProgramElement childAt = ft.getChildAt(0);
            if (childAt instanceof FreeFunction) {
                FreeFunction ff = (FreeFunction) childAt;
                if (ft.getChildCount() == 1) {
                    val.res = val.res.multiply(Values.getDefault().symbol(
                            ff.getSymbol()));
                } else {
                    throw new IllegalArgumentException(
                            "Cannot handle free function with arguments");
                }
            } else if (childAt instanceof MinusSign) {
                val.res = val.res.minus();
                convert(variables, t, val, ft.getChildAt(1));
            }
        } else {
            throw new IllegalArgumentException("Dont know how to represent a "
                    + t.getClass() + " in orbital: " + m);
        }
    }

    /**
     * TODO jdq documentation since Aug 20, 2007
     * 
     * @param expression
     * @return
     */
    private List<ProgramElement> splitPlus(ProgramElement expression) {
        List<ProgramElement> result = new ArrayList<ProgramElement>();
        if (expression instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement d = (DLNonTerminalProgramElement) expression;
            if (d.getChildAt(0) instanceof Plus) {
                result.addAll(splitPlus(d.getChildAt(1)));
                result.addAll(splitPlus(d.getChildAt(2)));
            } else {
                result.add(d);
            }
        } else {
            result.add(expression);
        }
        return result;
    }

    /**
     * TODO jdq documentation since Aug 20, 2007
     * 
     * @param expression
     * @return
     */
    private List<ProgramElement> splitMult(ProgramElement expression) {
        List<ProgramElement> result = new ArrayList<ProgramElement>();
        if (expression instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement d = (DLNonTerminalProgramElement) expression;
            if (d.getChildAt(0) instanceof Mult) {
                result.addAll(splitPlus(d.getChildAt(1)));
                result.addAll(splitPlus(d.getChildAt(2)));
            } else {
                result.add(d);
            }
        } else {
            result.add(expression);
        }
        return result;
    }

}
