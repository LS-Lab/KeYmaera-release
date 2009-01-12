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
 * File created 07.02.2007
 */
package de.uka.ilkd.key.dl.formulatools;

import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.sort.Sort;

/**
 * The ProgramVariableDeclaratorVisitor is used to insert declarations of
 * program variables into the namespaces.
 * 
 * @author jdq
 * @since 07.02.2007
 * 
 */
public class ProgramVariableDeclaratorVisitor {

	/**
	 * Recursivly add all variables reachable from the given element into the
	 * given namespace.
	 * 
	 * @param term
	 *            the root of the term
	 * @param nss
	 *            the namespace set to get the program variable namespace from.
	 */
	public static void declareVariables(Term term, final NamespaceSet nss) {

		Visitor visitor = new Visitor() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
			 */
			/* @Override */
			public void visit(Term visited) {
				if (visited.op() instanceof Modality) {
					declareVariables(((StatementBlock) visited.javaBlock()
							.program()).getChildAt(0), nss);
				}
			}
		};
		term.execPreOrder(visitor);
	}

	/**
	 * Recursivly add all variables reachable from the given element into the
	 * given namespace.
	 * 
	 * @param element
	 *            the root of the DLProgram
	 * @param nss
	 *            the namespace set to get the program variable namespace from.
	 */
	public static void declareVariables(ProgramElement element, NamespaceSet nss) {
		if (element instanceof VariableDeclaration) {
			VariableDeclaration decl = (VariableDeclaration) element;
			for (int i = 1; i < decl.getChildCount(); i++) {
				ProgramElement childAt = decl.getChildAt(i);
				if (childAt instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
					de.uka.ilkd.key.dl.model.ProgramVariable v = (de.uka.ilkd.key.dl.model.ProgramVariable) childAt;
					NamespaceSet namespaces = nss;
					// @todo assert namespaces.unique
					de.uka.ilkd.key.logic.op.ProgramVariable kv = (ProgramVariable) namespaces
							.programVariables().lookup(v.getElementName());
					if (kv == null) {
						kv = new LocationVariable(new ProgramElementName(v
								.getElementName().toString()),
								(Sort) namespaces.sorts().lookup(
										decl.getType().getElementName()));
						namespaces.programVariables().add(kv);
					}
				}
			}
		} else if (element instanceof DLNonTerminalProgramElement) {
			DLNonTerminalProgramElement ntpl = (DLNonTerminalProgramElement) element;
			for (int i = 0; i < ntpl.getChildCount(); i++) {
				declareVariables(ntpl.getChildAt(i), nss);
			}
		}

	}

}
