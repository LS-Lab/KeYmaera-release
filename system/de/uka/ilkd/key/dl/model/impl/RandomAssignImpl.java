/**
 * File created 28.02.2007
 */
package de.uka.ilkd.key.dl.model.impl;

import java.io.IOException;

import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.RandomAssign;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.reference.ExecutionContext;

/**
 * This class is the implementation of the representation of random assignments
 * in dL. {@link RandomAssign}
 * 
 * @author jdq
 * @since 28.02.2007
 * 
 */
public class RandomAssignImpl extends DLNonTerminalProgramElementImpl implements
        RandomAssign {

    /**
     * @param variable
     */
    public RandomAssignImpl(ProgramVariable variable) {
        addChild(variable);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
     */
    @Override
    public void prettyPrint(PrettyPrinter arg0) throws IOException {
        arg0.printRandomAssign(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public String reuseSignature(Services services, ExecutionContext ec) {
        return "("
                + ((DLProgramElement) getChildAt(0)).reuseSignature(services,
                        ec) + ") := *";
    }

}
