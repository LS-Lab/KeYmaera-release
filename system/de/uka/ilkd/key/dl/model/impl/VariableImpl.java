
/*
 * VariableImpl.java 1.00 Mo Jan 15 09:51:38 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.java.SourceElement;
import de.uka.ilkd.key.java.NameAbstractionTable;

/**
 * Implementation of {@link Variable}.
 * 
 * @version 1.00
 * @author jdq
 */
public abstract class VariableImpl extends DLTerminalProgramElementImpl
		implements Variable {

	private Name name;

	/**
	 * Creates a new Variable with a given name
	 * 
	 * @param name
	 *            the name of the variable
	 */
	protected VariableImpl(Name name) {
		this.name = name;
	}

	/**
	 * get the value of name
	 * 
	 * @return the value of name
	 */
	public Name getElementName() {
		return name;
	}

	/**
	 * set a new value to name
	 * 
	 * @param name
	 *            the new value to be used
	 */
	public void setVarName(Name name) {
		this.name = name;
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.DLTerminalProgramElementImpl#equalsModRenaming(de.uka.ilkd.key.java.SourceElement,
	 *      de.uka.ilkd.key.java.NameAbstractionTable) equalsModRenaming
	 */
	public boolean equalsModRenaming(SourceElement arg0,
			NameAbstractionTable arg1) {
		return arg0 == this
				|| (getClass() == arg0.getClass() && arg1.sameAbstractName(
						this, arg0));
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.DLTerminalProgramElementImpl#toString()
	 *      toString
	 */
	public String toString() {
		return name.toString();
	}
}
