/*
 * QuestImpl.java 1.00 Mo Jan 15 09:26:34 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.Quest;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.reference.ExecutionContext;

/**
 * Implementation of {@link Quest}.
 * 
 * @version 1.00
 * @author jdq
 */
public class QuestImpl extends DLNonTerminalProgramElementImpl implements Quest {

    /**
     * 
     * @param frm
     */
    public QuestImpl(Formula frm) {
        addChild(frm);
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol()
     *      getSymbol
     */
    public String getSymbol() {
        return "?";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    public String reuseSignature(Services services, ExecutionContext ec) {
        return "?("
                + ((DLProgramElement) getChildAt(0)).reuseSignature(services,
                        ec) + ")";
    }
}
