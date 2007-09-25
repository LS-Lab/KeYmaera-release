/*
 * EqualsImpl.java 1.00 Mo Jan 15 09:16:23 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.logic.Name;

/**
 * Implementation of {@link Unequals}. This element is a singleton.
 * 
 * @version 1.00
 * @author jdq
 */
public class UnequalsImpl extends ComparsionImpl implements Unequals {
	private static UnequalsImpl instance = null;

	private UnequalsImpl() {
	}

	public synchronized static UnequalsImpl getInstance() {
		if (instance == null) {
			instance = new UnequalsImpl();
		}
		return instance;
	}

	/**
	 * Returns the name of this element, to lookup in the namespaces
	 */
	public Name getElementName() {
                return new Name("neq");
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "!=";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return true;
	}

}
