

/*
 * ImpliesImpl.java 1.00 Mo Jan 15 09:24:20 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.Implies;


/**
 * Implementation of {@link Implies}.
 * @version 1.00 
 * @author jdq
 */
public class ImpliesImpl extends CompoundFormulaImpl implements Implies {
	public ImpliesImpl(Formula f, Formula g) {
		addChild(f);
		addChild(g);
	}
	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "->";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return true;
	}
}

