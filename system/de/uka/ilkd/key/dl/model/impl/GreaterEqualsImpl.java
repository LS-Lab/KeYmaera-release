
/*
 * GreaterEqualsImpl.java 1.00 Mo Jan 15 09:12:44 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.GreaterEquals;
import de.uka.ilkd.key.logic.Name;

/**
 * Implementation of {@link GreaterEquals}. This element is a singleton.
 * 
 * @version 1.00
 * @author jdq
 */
public class GreaterEqualsImpl extends ComparsionImpl implements GreaterEquals {
	private static GreaterEqualsImpl instance = null;

	private GreaterEqualsImpl() {
	}

	public synchronized static GreaterEqualsImpl getInstance() {
		if (instance == null) {
			instance = new GreaterEqualsImpl();
		}
		return instance;
	}

	/**
	 * Returns the name of this element, to lookup in the namespaces
	 */
	public Name getElementName() {
		return new Name("geq");
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return ">=";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return true;
	}

}
