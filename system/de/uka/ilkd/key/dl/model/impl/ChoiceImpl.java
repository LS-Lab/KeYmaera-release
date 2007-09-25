
/*
 * ChoiceImpl.java 1.00 Mo Jan 15 09:07:59 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Choice;
import de.uka.ilkd.key.dl.model.DLProgram;

/**
 * Implementation of {@link Choice}.
 * 
 * @version 1.00
 * @author jdq
 */
public class ChoiceImpl extends CompoundDLProgramImpl implements Choice {
    public ChoiceImpl(DLProgram p, DLProgram q) {
        addChild(p);
        addChild(q);
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol()
     *      getSymbol
     */
    public String getSymbol() {
        return "++";
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix()
     *      printInfix
     */
    public boolean printInfix() {
        return true;
    }
}
