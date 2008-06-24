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
 * DiffSystemImpl.java 1.00 Mo Jan 15 09:44:56 CET 2007
 */

package de.uka.ilkd.key.dl.model.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.wolfram.jlink.Expr;

import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.DL2ExprConverter;
import de.uka.ilkd.key.dl.formulatools.Prog2LogicConverter;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.reference.ExecutionContext;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;

/**
 * Implementation of {@link DiffSystem}
 * 
 * @version 1.00
 * @author jdq
 * @author ap
 */
public class DiffSystemImpl extends DLNonTerminalProgramElementImpl implements
		DiffSystem {

	/**
	 * Creates a new DiffSystem with the given content
	 * 
	 * @param content
	 *            the content of the system
	 */
	public DiffSystemImpl(List<Formula> content) {
		for (Formula f : content) {
			addChild(f);
		}
	}

	/**
	 * @see de.uka.ilkd.key.dl.model.impl.DLNonTerminalProgramElementImpl#prettyPrint(de.uka.ilkd.key.java.PrettyPrinter)
	 *      prettyPrint
	 */
	public void prettyPrint(PrettyPrinter arg0) throws IOException {
		arg0.printDiffSystem(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
	 *      de.uka.ilkd.key.java.reference.ExecutionContext)
	 */
	public String reuseSignature(Services services, ExecutionContext ec) {
		StringBuilder result = new StringBuilder();
		result.append("{");
		for (ProgramElement p : this) {
			result.append(((DLProgramElement) p).reuseSignature(services, ec));
		}
		result.append("}");
		return result.toString();
	}

	/**
	 * Test whether there is a Dot in the program or not
	 * 
	 * @param el
	 *            the current root element
	 * @return true if an element is found that is instance of Dot
	 */
	public boolean isDifferentialEquation(ProgramElement el) {
		if (el instanceof Dot) {
			return true;
		} else if (el instanceof DLNonTerminalProgramElement) {
			boolean result = false;
			for (ProgramElement p : (DLNonTerminalProgramElement) el) {
				result |= isDifferentialEquation(p);
			}
			return result;
		}
		return false;
	}

	/**
	 * Get the (accumulated) invariant of this DiffSystem, i.e., the
	 * non-differential part.
	 */
	public Term getInvariant() {
		Term invariant = TermBuilder.DF.tt();
		for (ProgramElement el : this) {
			if (!isDifferentialEquation(el)) {
				if (invariant.equals(TermBuilder.DF.tt())) {
					invariant = TermBuilder.DF.and(invariant,
							Prog2LogicConverter
									.convert((DLProgramElement) el, Main
											.getInstance().mediator()
											.getServices()));
				} else {
					throw new IllegalStateException("No single invariant");
				}
			}
		}
		return invariant;
	}

	/**
	 * Get the set of differential equations occurring in this DiffSystem.
	 * 
	 * @param system
	 *            TODO
	 */
	public List<ProgramElement> getDifferentialEquations() {
		List<ProgramElement> equations = new ArrayList<ProgramElement>();
		for (ProgramElement el : this) {
			if (isDifferentialEquation(el)) {
				equations.add(el);
			}
		}
		return equations;
	}

	@Override
	public DiffSystem getDifferentialFragment() {
		List<Formula> equations = new ArrayList<Formula>();
		for (ProgramElement el : this) {
			if (isDifferentialEquation(el)) {
				equations.add((Formula) el);
			}
		}
		return new DiffSystemImpl(equations);
	}

	@Override
	public DiffSystem getInvariantFragment() {
		List<Formula> equations = new ArrayList<Formula>();
		for (ProgramElement el : this) {
			if (!isDifferentialEquation(el)) {
				equations.add((Formula) el);
			}
		}
		return new DiffSystemImpl(equations);
	}

}
