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
package de.uka.ilkd.key.dl.arithmetics.exceptions;

import de.uka.ilkd.key.dl.arithmetics.IMathSolver;

/**
 * This exception could be thrown by a {@link IMathSolver} because there is a
 * problem communication with, e.g. the Mathematica server.
 * 
 * @author jdq
 * @since Nov 21, 2007
 * 
 */
public class ConnectionException extends SolverException {
	public ConnectionException() {
		// TODO Auto-generated constructor stub
	}

	public ConnectionException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	public ConnectionException(String message, Throwable t) {
		super(message, t);
	}

	/**
	 * 
	 */
	public ConnectionException(String message) {
		super(message);
	}
}
