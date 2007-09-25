

/*
 * GreaterImpl.java 1.00 Mo Jan 15 09:15:42 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Greater;
import de.uka.ilkd.key.logic.Name;


/**
 * Implementation of {@link Greater}. This element is a singleton.
 * @version 1.00 
 * @author jdq
 */
public class GreaterImpl extends ComparsionImpl implements Greater {

	private static GreaterImpl instance = null;
	private GreaterImpl() {
	}
	public synchronized static GreaterImpl getInstance() {
		if ( instance == null) {
			instance = new GreaterImpl();
		}
		return instance;
	}

    /**
     * Returns the name of this element, to lookup in the namespaces
     */
    public Name getElementName() {
		return new Name("gt");
    }
	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return ">";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return true;
	}
}

