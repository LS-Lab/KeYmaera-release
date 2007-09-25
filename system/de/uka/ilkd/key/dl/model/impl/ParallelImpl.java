

/*
 * ChopImpl.java 1.00 Mo Jan 15 09:08:51 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.Parallel;


/**
 * Implementation of {@link Parallel}
 * @version 1.00 
 * @author jdq
 */
public class ParallelImpl extends CompoundDLProgramImpl implements Parallel {
	public ParallelImpl(DLProgram p, DLProgram q) {
		addChild(p);
		addChild(q);
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "||";
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.CompoundFormulaImpl#printInfix() printInfix
	 */
	public boolean printInfix() {
		return true;
	}

}

