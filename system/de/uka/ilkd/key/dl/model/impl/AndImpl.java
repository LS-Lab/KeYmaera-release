/*
 * AndImpl.java 1.00 Mo Jan 15 09:24:48 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.And;
import de.uka.ilkd.key.dl.model.Formula;

/**
 * The implementation of the logic conjunction
 * 
 * @version 1.00
 * @author jdq
 */
public class AndImpl extends CompoundFormulaImpl implements And {
    /**
     * Creates the conjunction of the two formulas
     */
    public AndImpl(Formula frm, Formula frm2) {
        addChild(frm);
        addChild(frm2);
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol()
     *      getSymbol
     */
    public String getSymbol() {
        return "&";
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix()
     *      printInfix
     */
    public boolean printInfix() {
        return true;
    }
}
