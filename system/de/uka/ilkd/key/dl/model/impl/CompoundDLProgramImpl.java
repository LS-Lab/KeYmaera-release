

/*
 * CompoundDLProgramImpl.java 1.00 Mo Jan 15 09:03:04 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.CompoundDLProgram;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.reference.ExecutionContext;


/**
 * Implementation of {@link CompoundDLProgram}
 * @version 1.00 
 * @author jdq
 */
public abstract class CompoundDLProgramImpl extends DLNonTerminalProgramElementImpl implements CompoundDLProgram {
    
    /* (non-Javadoc)
     * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services, de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public String reuseSignature(Services services, ExecutionContext ec) {
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

