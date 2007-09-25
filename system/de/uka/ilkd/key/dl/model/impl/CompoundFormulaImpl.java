
/*
 * CompoundFormulaImpl.java 1.00 Mo Jan 15 09:21:58 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.CompoundFormula;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.reference.ExecutionContext;

/**
 * Implementation of {@link CompoundFormula}
 * 
 * @version 1.00
 * @author jdq
 */
public abstract class CompoundFormulaImpl extends
        DLNonTerminalProgramElementImpl implements CompoundFormula {

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public String reuseSignature(Services services, ExecutionContext ec) {
        return ((DLProgramElement) getChildAt(0)).reuseSignature(services, ec)
                + getSymbol()
                + ((DLProgramElement) getChildAt(1)).reuseSignature(services,
                        ec);
    }
}
