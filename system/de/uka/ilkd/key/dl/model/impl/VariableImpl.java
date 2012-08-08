/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
        if(obj != null && obj instanceof Variable) {
            Variable c = (Variable) obj;
            return c.getElementName().toString().equals(getElementName().toString());
        }
        return false;
	}
}
