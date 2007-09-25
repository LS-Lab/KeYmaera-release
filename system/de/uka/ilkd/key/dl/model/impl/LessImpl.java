

/*
 * LessImpl.java 1.00 Mo Jan 15 09:12:10 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Less;
import de.uka.ilkd.key.logic.Name;


/**
 * Implementation of {@link Less}. This element is a singleton.
 * @version 1.00 
 * @author jdq
 */
public class LessImpl extends ComparsionImpl implements Less {
	private static LessImpl instance = null;
	private LessImpl() {
	}
	public synchronized static LessImpl getInstance() {
		if ( instance == null) {
			instance = new LessImpl();
		}
		return instance;
	}

        /**
     * Returns the name of this element, to lookup in the namespaces
     */
    public Name getElementName() {
		return new Name("lt");
    }
	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "<";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return true;
	}
}

