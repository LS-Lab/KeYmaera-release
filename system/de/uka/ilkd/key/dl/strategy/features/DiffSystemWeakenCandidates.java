/***************************************************************************
 *   Copyright (C) 2008 by Jan David Quesel                                *
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
package de.uka.ilkd.key.dl.strategy.features;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import orbital.util.Setops;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.dl.strategy.termProjection.Generator;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.QuanUpdateOperator;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * @author jdq
 * @since Sep 9, 2008
 */
public class DiffSystemWeakenCandidates implements Generator<ProgramElement> {

	public static final Generator<ProgramElement> INSTANCE = new DiffSystemWeakenCandidates();

	public Iterator<ProgramElement> generate(RuleApp app, PosInOccurrence pos,
			Goal goal) {

		Term term = pos.subTerm();
		// unbox from update prefix
		while (term.op() instanceof QuanUpdateOperator) {
			term = ((QuanUpdateOperator) term.op()).target(term);
		}
		if (!(term.op() instanceof Modality && term.javaBlock() != null
				&& term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK && term
				.javaBlock().program() instanceof StatementBlock)) {
			throw new IllegalArgumentException("inapplicable to " + pos);
		}
		final DLProgram program = (DLProgram) ((StatementBlock) term
				.javaBlock().program()).getChildAt(0);

		TermFactory tf;
		try {
			tf = TermFactory.getTermFactory(TermFactoryImpl.class, goal.proof()
					.getNamespaces());

			DiffSystem one = (DiffSystem) program;
			List<Formula> forms = new LinkedList<Formula>();
			Set<ProgramVariable> dottedVars = new HashSet<ProgramVariable>();
			for (ProgramElement f : one) {
				if (one.isDifferentialEquation(f)) {
					if (f instanceof PredicateTerm) {
						PredicateTerm p = (PredicateTerm) f;
						collectDottedProgramVariables(p, dottedVars);
						if (p.getChildAt(0) instanceof Equals) {
							forms.add((Formula) f);
						} else {
							List<Expression> children = new LinkedList<Expression>();
							children.add((Expression) p.getChildAt(1));
							children.add((Expression) p.getChildAt(2));
							forms.add(tf.createPredicateTerm(tf.createEquals(),
									children));
						}
					} else {
						throw new IllegalArgumentException(
								"Rule not applicable to complex terms");
					}
				} else {
					forms.add((Formula) f);
				}
			}

			Queue<ProgramElement> candidates = new LinkedList<ProgramElement>();

			// create cross product of formulas
			Set<Formula> curForms = new LinkedHashSet<Formula>(forms);
			Set powerset = Setops.powerset(curForms);

			Set<ProgramVariable> currentDottedVars = new HashSet<ProgramVariable>();
			System.out.println("Dotted vars are: " + dottedVars);//XXX
			for (Object s : powerset) {
				Set set = (Set) s;
				if (!set.isEmpty()) {
					List<Formula> can = new LinkedList<Formula>();
					currentDottedVars.clear();
					for (Object f : set) {
						Formula form = (Formula) f;
						can.add(form);
						collectDottedProgramVariables(form, currentDottedVars);
						System.out.println("Current dotted Vars: " + currentDottedVars);//XXX
					}
					// only add the candidate if it has the same change-set.
					// (i.e. does not change new variables and does, on the
					// other hand, change every variable that was changed
					// before). The first part is necessary for soundness of the
					// rule. The second part is an heuristic.
					if (dottedVars.equals(currentDottedVars)) {
						candidates.add(tf.createDiffSystem(can));
					}

				}
			}
			return new PEIterator(candidates);
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
		return new PEIterator(new LinkedList<ProgramElement>());
	}

	public static class PEIterator implements Iterator<ProgramElement> {

		private Queue<ProgramElement> terms;

		/**
		 * 
		 */
		public PEIterator(Queue<ProgramElement> terms) {
			this.terms = terms;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.uka.ilkd.key.logic.IteratorOfTerm#hasNext()
		 */
		/*@Override*/
		public boolean hasNext() {
			return !terms.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.uka.ilkd.key.logic.IteratorOfTerm#next()
		 */
		/*@Override*/
		public ProgramElement next() {
			return terms.poll();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		/*@Override*/
		public void remove() {
			terms.poll();
		}

	}

	/**
	 * Collect all program variables which are children of a Dot.
	 */
	public static final void collectDottedProgramVariables(ProgramElement form,
			Set<ProgramVariable> vars) {
		if (form instanceof Dot) {
			ProgramVariable pv = (ProgramVariable) ((Dot) form).getChildAt(0);
			vars.add(pv);
			
		} else if (form instanceof DLNonTerminalProgramElement) {
			DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) form;
			for (ProgramElement p : dlnpe) {
				collectDottedProgramVariables(p, vars);
			}
		}
	}

}
