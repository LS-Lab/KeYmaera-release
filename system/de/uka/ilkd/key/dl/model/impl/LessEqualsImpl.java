

/*
 * LessEqualsImpl.java 1.00 Mo Jan 15 09:15:12 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.logic.Name;


/**
 * Implementation of {@link LessEquals}. This element is a singleton.
 * @version 1.00 
 * @author jdq
 */
public class LessEqualsImpl extends ComparsionImpl implements LessEquals {
	private static LessEqualsImpl instance = null;
	private LessEqualsImpl() {
	}
	public synchronized static LessEqualsImpl getInstance() {
		if ( instance == null) {
			instance = new LessEqualsImpl();
		}
		return instance;
	}

        /**
     * Returns the name of this element, to lookup in the namespaces
     */
    public Name getElementName() {
		return new Name("leq");
    }
	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "<=";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return true;
	}
}

