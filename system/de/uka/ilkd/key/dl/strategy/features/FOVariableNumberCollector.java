package de.uka.ilkd.key.dl.strategy.features;

import java.math.BigDecimal;
import java.util.HashSet;

import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;

public class FOVariableNumberCollector {

    public static int getVariableNumber(Term inputFormula) {
        final HashSet<Name> variablesSeenBefore = new HashSet<Name>();
        inputFormula.execPreOrder(new Visitor() {
            @Override
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
                        } else if (op.arity() > 0 & op.arity() < 3) {
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
                    if (!variablesSeenBefore.contains(op.name())) {
                        variablesSeenBefore.add(op.name());
                    }
                }

            }
        });
        return variablesSeenBefore.size();
    }

}
