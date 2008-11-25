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
package de.uka.ilkd.key.dl.model.impl;

import java.io.IOException;

import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.Forall;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.reference.ExecutionContext;

/**
 * This is the implementation of {@link Forall}, i.e. it is the hybrid program
 * representation of an universal quantifier.
 * 
 * @author jdq
 * @since Nov 22, 2007
 * 
 */
public class ForallImpl extends CompoundFormulaImpl implements Forall {
	/**
	 * @param dec
	 * @param form
	 */
	public ForallImpl(VariableDeclaration dec, Formula form) {
		addChild(dec);
		addChild(form);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
	 *      de.uka.ilkd.key.java.reference.ExecutionContext)
	 */
	public String reuseSignature(Services services, ExecutionContext ec) {
		return getClass().getCanonicalName()
				+ ((DLProgramElement) getChildAt(0)).reuseSignature(services,
						ec)
				+ getSymbol()
				+ ((DLProgramElement) getChildAt(1)).reuseSignature(services,
						ec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
	 */
	/*@Override*/
	public void prettyPrint(PrettyPrinter arg0) throws IOException {
		arg0.printForall(this);
	}
}
