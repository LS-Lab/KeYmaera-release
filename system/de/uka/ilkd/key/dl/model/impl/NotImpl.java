

/*
 * NotImpl.java 1.00 Mo Jan 15 16:12:42 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.Not;


/**
 * Implementation of {@link Not}
 * @version 1.00 
 * @author jdq
 */
public class NotImpl extends CompoundFormulaImpl implements Not {
	public NotImpl(Formula f) {
		addChild(f);
	}


	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "!";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return false;
	}

}

