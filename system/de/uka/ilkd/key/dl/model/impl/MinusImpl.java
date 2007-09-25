

/*
 * MinusImpl.java 1.00 Mo Jan 15 09:48:12 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Minus;
import de.uka.ilkd.key.logic.Name;


/**
 * Implementation of {@link Minus}. This element is a singleton.
 * @version 1.00 
 * @author jdq
 */
public class MinusImpl extends FunctionImpl implements Minus {

	private static MinusImpl instance = null;
	private MinusImpl() {
	}
	public synchronized static MinusImpl getInstance() {
		if ( instance == null) {
			instance = new MinusImpl();
		}
		return instance;
	}

        /**
     * Returns the name of this element, to lookup in the namespaces
     */
    public Name getElementName() {
		return new Name("sub");
    }
	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "-";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return true;
	}
}

