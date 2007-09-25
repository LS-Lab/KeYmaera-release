
/*
 * StarImpl.java 1.00 Mo Jan 15 09:07:06 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.Star;
import de.uka.ilkd.key.java.PrettyPrinter;
import java.io.IOException;

/**
 * Implementation of {@link Star}.
 * 
 * @version 1.00
 * @author jdq
 */
public class StarImpl extends CompoundDLProgramImpl implements Star {

    private Formula invariant;

    /**
     * Creates a new repetition operator
     * 
     * @param p
     *                the formula to repeat
     */
    public StarImpl(DLProgram p, Formula invariant) {
        addChild(p);
        this.invariant = invariant;
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundDLProgramImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
     *      prettyPrint
     */
    public void prettyPrint(PrettyPrinter arg0) throws IOException {
        arg0.printStar(this);
    }

    /**
     * @return the invariant
     */
    public Formula getInvariant() {
        return invariant;
    }

}
