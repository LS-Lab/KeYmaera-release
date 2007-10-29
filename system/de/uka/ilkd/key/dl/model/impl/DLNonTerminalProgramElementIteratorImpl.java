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
/**
 * File created 26.01.2007
 */
package de.uka.ilkd.key.dl.model.impl;

import java.util.Iterator;

import de.uka.ilkd.key.java.ProgramElement;

/**
 * This iterator can iterate over ProgramElements. It is used by the
 * {@link DLNonTerminalProgramElementImpl}.
 * 
 * @author jdq
 * @since 26.01.2007
 * 
 */
public class DLNonTerminalProgramElementIteratorImpl implements
		Iterator<ProgramElement> {

	private int i = 0;

	private DLNonTerminalProgramElementImpl impl;

	/**
	 * @param impl
	 */
	public DLNonTerminalProgramElementIteratorImpl(
			DLNonTerminalProgramElementImpl impl) {
		this.impl = impl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return impl.getChildCount() - i > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	public ProgramElement next() {
		return impl.getChildAt(i++);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException("Datastructure is immutable!");
	}

}
