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
 * PredicateImpl.java 1.00 Mo Jan 15 09:18:59 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import de.uka.ilkd.key.dl.model.Predicate;
import de.uka.ilkd.key.java.NameAbstractionTable;
import de.uka.ilkd.key.java.SourceElement;

/**
 * Implementation of {@link Predicate}
 * 
 * @version 1.00
 * @author jdq
 * @since Mo Jan 15 09:18:59 CET 2007
 */
public abstract class PredicateImpl extends DLTerminalProgramElementImpl
		implements Predicate {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.model.impl.DLTerminalProgramElementImpl#equalsModRenaming(de.uka.ilkd.key.java.SourceElement,
	 *      de.uka.ilkd.key.java.NameAbstractionTable)
	 */
	/*@Override*/
	public boolean equalsModRenaming(SourceElement arg0,
			NameAbstractionTable arg1) {
		return arg0 == this
				|| (getClass() == arg0.getClass() && arg1.sameAbstractName(
						this, arg0));
	}
}
