

/*
 * OrImpl.java 1.00 Mo Jan 15 09:23:46 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.Or;


/**
 * Implementation of {@link Or}
 * @version 1.00 
 * @author jdq
 */
public class OrImpl extends CompoundFormulaImpl implements Or {
	public OrImpl(Formula frm, Formula frm2) {
		addChild(frm);
		addChild(frm2);
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "|";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return true;
	}

}

