
/*
 * BiimpliesImpl.java 1.00 Mo Jan 15 09:23:17 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Biimplies;
import de.uka.ilkd.key.dl.model.Formula;

/**
 * Implementation of {@link Biimplies}.
 * 
 * @version 1.00
 * @author jdq
 */
public class BiimpliesImpl extends CompoundFormulaImpl implements Biimplies {
    public BiimpliesImpl(Formula f, Formula f2) {
        addChild(f);
        addChild(f2);
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol()
     *      getSymbol
     */
    public String getSymbol() {
        return "<->";
    }

    /**
     * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix()
     *      printInfix
     */
    public boolean printInfix() {
        return true;
    }



}
