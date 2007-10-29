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
 * ExpImpl.java 1.00 Mo Jan 15 09:47:24 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.logic.Name;

/**
 * Implementation of {@link Exp}. This element is a singleton.
 * 
 * @version 1.00
 * @author jdq
 */
public class ExpImpl extends FunctionImpl implements Exp {

	private static ExpImpl instance = null;

	private ExpImpl() {
	}

	public synchronized static ExpImpl getInstance() {
		if (instance == null) {
			instance = new ExpImpl();
		}
		return instance;
	}

	/**
	 * Returns the name of this element, to lookup in the namespaces
	 */
	public Name getElementName() {
		return new Name("exp");
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.FunctionImpl#getSymbol() getSymbol
	 */
	public String getSymbol() {
		return "^";
	}
}
