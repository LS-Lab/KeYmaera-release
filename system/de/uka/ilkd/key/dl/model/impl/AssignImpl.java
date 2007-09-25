/*
 * AssignImpl.java 1.00 Mo Jan 15 09:44:05 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Assign;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.reference.ExecutionContext;

/**
 * The implementation of an assignment ({@link Assign})
 * 
 * @version 1.00
 * @author jdq
 */
public class AssignImpl extends DLNonTerminalProgramElementImpl implements
        Assign {

    /**
     * Creates a new Assign statement with a given variable and a given value.
     * 
     * @param var
     *                the variable to assign
     * @param value
     *                the value to assign to the variable
     */
    public AssignImpl(ProgramVariable var, Expression value) {
        addChild(var);
        addChild(value);
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol()
     *      getSymbol
     */
    public String getSymbol() {
        return ":=";
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix()
     *      printInfix
     */
    public boolean printInfix() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public String reuseSignature(Services services, ExecutionContext ec) {
        // TODO Auto-generated method stub
        StringBuilder result = new StringBuilder();
        result.append(getSymbol() + "(");
        for (int i = 0; i < getChildCount(); i++) {
            result.append(((DLProgramElement) getChildAt(i))
                    .reuseSignature(services, ec)
                    + ", ");
        }
        result.append(")");
        return result.toString();
    }
}
