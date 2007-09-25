
/*
 * ExpImpl.java 1.00 Mo Jan 15 09:47:24 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.logic.Name;

/**
 * Implementation of {@link Exp}. This element is a singleton.
 * 
 * @version 1.00
 * @author jdq
 */
public class ExpImpl extends FunctionImpl implements Exp {

	private static ExpImpl instance = null;

	private ExpImpl() {
	}

	public synchronized static ExpImpl getInstance() {
		if (instance == null) {
			instance = new ExpImpl();
		}
		return instance;
	}

	/**
	 * Returns the name of this element, to lookup in the namespaces
	 */
	public Name getElementName() {
		return new Name("exp");
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.FunctionImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "^";
	}
}
