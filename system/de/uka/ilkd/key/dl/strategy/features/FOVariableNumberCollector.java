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
package de.uka.ilkd.key.dl.strategy.features;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;

public class FOVariableNumberCollector {

    public static int getVariableNumber(Term inputFormula) {
        return getVariables(inputFormula).size();
    }
    
    /**
     * Collects the variable symbols occurring in inputFormula, more precisely collects all
     * LogicVariable, ProgramVariable, QuantifiableVariable, MetaVariable or (non-builtin) Functions.
     */
    public static Set<Operator> getVariables(Term inputFormula) {
        final LinkedHashSet<Operator> variables = new LinkedHashSet<Operator>();
        inputFormula.execPreOrder(new Visitor() {
            /*@Override*/
            public void visit(Term visited) {
                Operator op = visited.op();
                if (op instanceof LogicVariable
                        || op instanceof de.uka.ilkd.key.logic.op.ProgramVariable
                        || op instanceof QuantifiableVariable
                        || op instanceof Function || op instanceof Metavariable) {
                    if (op instanceof Function) {
                        String s = op.name().toString();
                        if (op.arity() == 0) {
                            try {
                                new BigDecimal(s);
                                return;
                            } catch (Exception e) {
                                // s is not a number
                            }
                        } else if (op.arity() > 0 && op.arity() < 3) {
                            if (s.equalsIgnoreCase("add")
                                    || s.equalsIgnoreCase("sub")
                                    || s.equalsIgnoreCase("mul")
                                    || s.equalsIgnoreCase("div")
                                    || s.equalsIgnoreCase("neg")
                                    || s.equalsIgnoreCase("mod")
                                    || s.equalsIgnoreCase("exp")
                                    || s.equalsIgnoreCase("sin")
                                    || s.equalsIgnoreCase("cos")
                                    || s.equalsIgnoreCase("tan")
                                    || s.equalsIgnoreCase("equals")        
                                    || s.equalsIgnoreCase("lt")        
                                    || s.equalsIgnoreCase("gt")        
                                    || s.equalsIgnoreCase("leq")        
                                    || s.equalsIgnoreCase("geq")        
                                    || s.equalsIgnoreCase("neq")        
                                ) {
                                return;
                            }
                        }

                    }
                    variables.add(op);
                }

            }
        });
        return variables;
    }

}
