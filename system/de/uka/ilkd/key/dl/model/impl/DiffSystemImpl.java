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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator.QuantifierType;
import de.uka.ilkd.key.dl.formulatools.Prog2LogicConverter;
import de.uka.ilkd.key.dl.model.And;
import de.uka.ilkd.key.dl.model.Biimplies;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Exists;
import de.uka.ilkd.key.dl.model.Forall;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.Implies;
import de.uka.ilkd.key.dl.model.Or;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.reference.ExecutionContext;
import de.uka.ilkd.key.logic.NamespaceSet;
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

	private static class Pair {
		QuantifierType type;
		VariableDeclaration decl;

		/**
		 * @param decl
		 * @param type
		 */
		public Pair(QuantifierType type, VariableDeclaration decl) {
			super();
			this.decl = decl;
			this.type = type;
		}

	}

	/**
	 * Creates a new DiffSystem with the given content
	 * 
	 * @param content
	 *            the content of the system
	 */
	public DiffSystemImpl(List<Formula> content) {
		for (Formula f : content) {
			// we dont like conjunctions, so we drop them
			for (Formula sub : normalize(f)) {
				addChild(sub);
			}
		}
	}

	/**
	 * @param f
	 * @return
	 */
	private List<Formula> normalize(Formula f) {
		List<Formula> result = new LinkedList<Formula>();
		if (f instanceof And) {
			And a = (And) f;
			for (int i = 0; i < a.getChildCount(); i++) {
				result.addAll(normalize((Formula) a.getChildAt(i)));
			}
		} else {
			result.add(f);
		}
		return result;
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
	 * @see
	 * de.uka.ilkd.key.java.ReuseableProgramElement#reuseSignature(de.uka.ilkd
	 * .key.java.Services, de.uka.ilkd.key.java.reference.ExecutionContext)
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
	public Term getInvariant(Services services) {
		Term invariant = TermBuilder.DF.tt();
		TermFactory tf;
		try {
			tf = TermFactory.getTermFactory(TermFactoryImpl.class, services.getNamespaces());

			for (ProgramElement el : this) {
				if (!isDifferentialEquation(el)) {
					invariant = TermBuilder.DF.and(invariant,
							Prog2LogicConverter
									.convert((DLProgramElement) el, services));
				} else {
					DLProgramElement hiddenInvariantPart = (DLProgramElement) getHiddenInvariantPart(
							el, getQuantifiedVariablesOccurringInDiffEq(el,
									new LinkedHashSet<Variable>()), tf);
					if (hiddenInvariantPart != null) {
						invariant = TermBuilder.DF.and(invariant,
								Prog2LogicConverter.convert(
										hiddenInvariantPart, services));
					}
				}
			}
			return invariant;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @param el
	 * @return
	 */
	private Set<Variable> getQuantifiedVariablesOccurringInDiffEq(
			ProgramElement el, Set<Variable> quantified) {
		Set<Variable> result = new LinkedHashSet<Variable>();

		if (el instanceof Forall || el instanceof Exists) {
			DLNonTerminalProgramElement npe = (DLNonTerminalProgramElement) el;
			VariableDeclaration decl = (VariableDeclaration) npe.getChildAt(0);
			for (int i = 1; i < decl.getChildCount(); i++) {
				quantified.add((Variable) decl.getChildAt(i));
			}
			result.addAll(getQuantifiedVariablesOccurringInDiffEq(npe
					.getChildAt(1), quantified));
		} else if (el instanceof PredicateTerm) {
			if (isDifferentialEquation(el)) {
				for (int i = 1; i < ((DLNonTerminalProgramElement) el)
						.getChildCount(); i++) {
					result.addAll(getQuantifiedVariablesOccurringInDiffEq(
							((DLNonTerminalProgramElement) el).getChildAt(i),
							quantified));
				}
			}
		} else if (el instanceof DLNonTerminalProgramElement) {
			for (int i = 0; i < ((DLNonTerminalProgramElement) el)
					.getChildCount(); i++) {
				result.addAll(getQuantifiedVariablesOccurringInDiffEq(
						((DLNonTerminalProgramElement) el).getChildAt(i),
						quantified));
			}

		} else if (el instanceof Variable) {
			if (quantified.contains(el)) {
				result.add((Variable) el);
			}
		}
		return result;
	}

	/**
	 * @param el
	 * @return
	 */
	private ProgramElement getHiddenInvariantPart(ProgramElement el,
			Set<Variable> quantified, TermFactory tf) {
		if (!isDifferentialEquation(el) && !doesContain(el, quantified)) {
			return el;
		}
		List<Pair> quants = new ArrayList<Pair>();
		while (el instanceof Forall || el instanceof Exists) {
			DLNonTerminalProgramElement npe = (DLNonTerminalProgramElement) el;
			VariableDeclaration decl = (VariableDeclaration) npe.getChildAt(0);
			List<String> vars = new ArrayList<String>();
			for (int i = 1; i < decl.getChildCount(); i++) {
				if (!quantified.contains(decl.getChildAt(i))) {
					vars.add(((Variable) decl.getChildAt(i)).getElementName()
							.toString());
				}
			}
			if (!vars.isEmpty()) {
				QuantifierType type = (el instanceof Forall) ? QuantifierType.FORALL
						: QuantifierType.EXISTS;
				quants.add(new Pair(type, tf.createVariableDeclaration(decl
						.getType(), vars, false, false)));
			}
			el = npe.getChildAt(1);
		}
		ProgramElement result = null;
		if (el instanceof And) {
			ProgramElement one = getHiddenInvariantPart(((And) el)
					.getChildAt(0), quantified, tf);
			ProgramElement two = getHiddenInvariantPart(((And) el)
					.getChildAt(1), quantified, tf);
			if (one == null) {
				result = two;
			} else if (two == null) {
				result = one;
			} else {
				result = tf.createAnd((Formula) one, (Formula) two);
			}
		} else if (el instanceof Or) {
			throw new IllegalArgumentException(
					"Dont know what the invariant part is if toplevel operator is or: "
							+ el);
		} else if (el instanceof Implies) {
			throw new IllegalArgumentException(
					"Dont know what the invariant part is if toplevel operator is implies: "
							+ el);
		} else if (el instanceof Biimplies) {
			throw new IllegalArgumentException(
					"Dont know what the invariant part is if toplevel operator is biimplies: "
							+ el);
		}
		if (result != null) {
			Collections.reverse(quants);
			for (Pair p : quants) {
				switch (p.type) {
				case FORALL:
					result = tf.createForall(p.decl, (Formula) result);
					break;
				case EXISTS:
					result = tf.createExists(p.decl, (Formula) result);
					break;
				}
			}
		}
		return result;
	}

	/**
	 * @param el
	 * @return
	 */
	private ProgramElement removeHiddenInvariantPart(ProgramElement el,
			Set<Variable> quantified, TermFactory tf) {
		if (!isDifferentialEquation(el) && !doesContain(el, quantified)) {
			return null;
		}
		List<Pair> quants = new ArrayList<Pair>();
		while (el instanceof Forall || el instanceof Exists) {
			DLNonTerminalProgramElement npe = (DLNonTerminalProgramElement) el;
			VariableDeclaration decl = (VariableDeclaration) npe.getChildAt(0);
			List<String> vars = new ArrayList<String>();
			for (int i = 1; i < decl.getChildCount(); i++) {
				if (quantified.contains(decl.getChildAt(i))) {
					vars.add(((Variable) decl.getChildAt(i)).getElementName()
							.toString());
				}
			}
			if (!vars.isEmpty()) {
				QuantifierType type = (el instanceof Forall) ? QuantifierType.FORALL
						: QuantifierType.EXISTS;
				quants.add(new Pair(type, tf.createVariableDeclaration(decl
						.getType(), vars, false, false)));
			}
			el = npe.getChildAt(1);
		}
		ProgramElement result = null;
		if (el instanceof And) {
			ProgramElement one = removeHiddenInvariantPart(((And) el)
					.getChildAt(0), quantified, tf);
			ProgramElement two = removeHiddenInvariantPart(((And) el)
					.getChildAt(1), quantified, tf);
			if (one == null) {
				result = two;
			} else if (two == null) {
				result = one;
			} else {
				result = tf.createAnd((Formula) one, (Formula) two);
			}
		} else {
			result = el;
		}
		if (result != null) {
			Collections.reverse(quants);
			for (Pair p : quants) {
				switch (p.type) {
				case FORALL:
					result = tf.createForall(p.decl, (Formula) result);
					break;
				case EXISTS:
					result = tf.createExists(p.decl, (Formula) result);
					break;
				}
			}
		}
		return result;
	}

	/**
	 * @param el
	 * @param quantified
	 * @return
	 */
	private boolean doesContain(ProgramElement el, Set<Variable> quantified) {
		if (el instanceof Variable) {
			return quantified.contains(el);
		} else if (el instanceof DLNonTerminalProgramElement) {
			for (int i = 0; i < ((DLNonTerminalProgramElement) el)
					.getChildCount(); i++) {
				if (doesContain(((DLNonTerminalProgramElement) el)
						.getChildAt(i), quantified)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the set of differential equations occurring in this DiffSystem.
	 * 
	 * @param system
	 *            TODO
	 */
	public List<ProgramElement> getDifferentialEquations(NamespaceSet nss) {
		List<ProgramElement> equations = new ArrayList<ProgramElement>();
		TermFactory tf;
		try {
			tf = TermFactory.getTermFactory(TermFactoryImpl.class, nss);
			for (ProgramElement el : this) {
				if (isDifferentialEquation(el)) {
					equations.add(removeHiddenInvariantPart(el,
							getQuantifiedVariablesOccurringInDiffEq(el,
									new LinkedHashSet<Variable>()), tf));
				}
			}
			return equations;
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/*@Override*/
	public DiffSystem getDifferentialFragment() {
		List<Formula> equations = new ArrayList<Formula>();
		for (ProgramElement el : this) {
			if (isDifferentialEquation(el)) {
				equations.add((Formula) el);
			}
		}
		return new DiffSystemImpl(equations);
	}

	/*@Override*/
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
