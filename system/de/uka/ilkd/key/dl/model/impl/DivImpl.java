
/*
 * DivImpl.java 1.00 Mo Jan 15 09:47:45 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Div;
import de.uka.ilkd.key.logic.Name;

/**
 * Implementation of {@link Div}. This operator is singleton.
 * 
 * @version 1.00
 * @author jdq
 */
public class DivImpl extends FunctionImpl implements Div {

	private static DivImpl instance = null;

	private DivImpl() {
	}

	/**
	 * @return the instance of the div operator. (Lazy loading)
	 */
	public synchronized static DivImpl getInstance() {
		if (instance == null) {
			instance = new DivImpl();
		}
		return instance;
	}

	/**
	 * Returns the name of this element, to lookup in the namespaces
	 */
	public Name getElementName() {
		return new Name("div");
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "/";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return true;
	}
}
