/*
 * EqualsImpl.java 1.00 Mo Jan 15 09:16:23 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.op.Equality;

/**
 * Implementation of {@link Equals}. This element is a singleton.
 * 
 * @version 1.00
 * @author jdq
 */
public class EqualsImpl extends ComparsionImpl implements Equals {
	private static EqualsImpl instance = null;

	private EqualsImpl() {
	}

	public synchronized static EqualsImpl getInstance() {
		if (instance == null) {
			instance = new EqualsImpl();
		}
		return instance;
	}

	/**
	 * Returns the name of this element, to lookup in the namespaces
	 */
	public Name getElementName() {
		return Equality.EQUALS.name();
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "=";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return true;
	}

}
