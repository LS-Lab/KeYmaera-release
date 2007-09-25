

/*
 * MultImpl.java 1.00 Mo Jan 15 09:48:41 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.logic.Name;


/**
 * Implementation of {@link Mult}. This element is a singleton.
 * @version 1.00 
 * @author jdq
 */
public class MultImpl extends FunctionImpl implements Mult {
	
	private static MultImpl instance = null;
	private MultImpl() {
	}
	public synchronized static MultImpl getInstance() {
		if ( instance == null) {
			instance = new MultImpl();
		}
		return instance;
	}

        /**
     * Returns the name of this element, to lookup in the namespaces
     */
    public Name getElementName() {
		return new Name("mul");
    }
	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "*";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return true;
	}
}

