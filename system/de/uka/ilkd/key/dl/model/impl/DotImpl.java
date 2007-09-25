/*
 * DotImpl.java 1.00 Mo Jan 15 09:52:04 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.reference.ExecutionContext;

import java.io.IOException;

/**
 * Implementation of {@link Dot}
 * 
 * @version 1.00
 * @author jdq
 */
public class DotImpl extends DLNonTerminalProgramElementImpl implements Dot {

    private int order;

    /**
     * Sets order to 0. As Dot represents at least one '.
     * 
     * @param var
     *                the dotted variable
     */
    public DotImpl(ProgramVariable var) {
        order = 1;
        addChild(var);
    }

    /**
     * Increments the dotcount
     */
    public void increment() {
        order++;
    }

    /**
     * @see de.uka.ilkd.key.dl.model.Dot#getOrder() getOrder
     */
    public int getOrder() {
        return order;
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
     *      prettyPrint
     */
    public void prettyPrint(PrettyPrinter arg0) throws IOException {
        arg0.printDot(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public String reuseSignature(Services services, ExecutionContext ec) {
        return ((DLProgramElement) getChildAt(0)).reuseSignature(services, ec)
                + getClass().getName() + "[" + order + "]";
    }
}
