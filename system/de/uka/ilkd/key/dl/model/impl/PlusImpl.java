

/*
 * PlusImpl.java 1.00 Mo Jan 15 09:49:05 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Plus;
import de.uka.ilkd.key.logic.Name;


/**
 * Implementation of {@link Plus}. This element is a singleton.
 * @version 1.00 
 * @author jdq
 */
public class PlusImpl extends FunctionImpl implements Plus {

	private static PlusImpl instance = null;
	private PlusImpl() {
	}
	public synchronized static PlusImpl getInstance() {
		if ( instance == null) {
			instance = new PlusImpl();
		}
		return instance;
	}

        /**
     * Returns the name of this element, to lookup in the namespaces
     */
    public Name getElementName() {
		return new Name("add");
    }
	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "+";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return true;
	}
}

