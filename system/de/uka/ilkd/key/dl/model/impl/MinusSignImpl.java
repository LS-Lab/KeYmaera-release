

/*
 * MinusImpl.java 1.00 Mo Jan 15 09:48:12 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.MinusSign;
import de.uka.ilkd.key.logic.Name;


/**
 * Implementation of {@link MinusSign}. This element is a singleton.
 * @version 1.00 
 * @author jdq
 */
public class MinusSignImpl extends FunctionImpl implements MinusSign {

	private static MinusSignImpl instance = null;
	private MinusSignImpl() {
	}
	public synchronized static MinusSignImpl getInstance() {
		if ( instance == null) {
			instance = new MinusSignImpl();
		}
		return instance;
	}

        /**
     * Returns the name of this element, to lookup in the namespaces
     */
    public Name getElementName() {
		return new Name("neg");
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
		return false;
	}
}

