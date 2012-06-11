/***************************************************************************
 *   Copyright (C) 2007 by Jan-David Quesel                                *
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
package de.uka.ilkd.key.dl.formulatools;

import de.uka.ilkd.key.collection.DefaultImmutableSet;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.MetaVariable;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.Modality;

/**
 * This visitor is used to search for MetaVariables
 * 
 * @author jdq
 * @since Aug 28, 2007
 * 
 */
public class MetaVariableLocator {

	public static final MetaVariableLocator INSTANCE = new MetaVariableLocator();

	private class MVisitor extends Visitor {
		private ImmutableSet<Metavariable> result = DefaultImmutableSet.nil();
		private NamespaceSet nss;

		/**
		 * @param nss
		 */
		public MVisitor(NamespaceSet nss) {
			this.nss = nss;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
		 */
		/*@Override*/
		public void visit(Term visited) {
			if (visited.op() instanceof Modality) {
				result = result
						.union(findInsideModality((ProgramElement) ((StatementBlock) visited
								.javaBlock().program()).getChildAt(0), nss));
			}

		}
	}

	/**
	 * Get all MetaVarianbles in the given Term
	 * 
	 * @param dominantTerm
	 *            the term to search in
	 * @return a SetOfMetavariable with all Metavariables that occur in the
	 *         given Term
	 */
	public ImmutableSet<Metavariable> find(Term dominantTerm, NamespaceSet nss) {
		MVisitor vis = new MVisitor(nss);
		dominantTerm.execPreOrder(vis);
		return vis.result;
	}

	/**
	 * Search for Metavariables in the given programelement
	 * 
	 * @param programElement
	 *            the programelement to search in
	 * @return a SetOfMetavariables that contains all Metavariables that occur
	 *         in the program
	 */
	public static ImmutableSet<Metavariable> findInsideModality(ProgramElement programElement, NamespaceSet nss) {
		ImmutableSet<Metavariable> result = DefaultImmutableSet.nil();
		if (programElement instanceof DLNonTerminalProgramElement) {
			DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) programElement;
			for (ProgramElement p : dlnpe) {
				result = result.union(findInsideModality(p, nss));
			}
		} else if (programElement instanceof MetaVariable) {
			MetaVariable var = (MetaVariable) programElement;
			result = result.add((Metavariable)nss.variables().lookup(
							var.getElementName()));
		}
		return result;
	}

}
